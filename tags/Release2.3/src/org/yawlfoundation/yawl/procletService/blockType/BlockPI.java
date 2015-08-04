/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.blockType;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.procletService.SingleInstanceClass;
import org.yawlfoundation.yawl.procletService.connect.Trigger;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc.ArcState;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.Item;
import org.yawlfoundation.yawl.procletService.persistence.StoredItem;
import org.yawlfoundation.yawl.procletService.selectionProcess.ProcessEntityMID;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.state.Performatives;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;
import org.yawlfoundation.yawl.procletService.util.ThreadNotify;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockPI {

	private WorkItemRecord wir = null;
	private ProcletBlock block = null;
	
	private static Logger myLog = Logger.getLogger(BlockPI.class);
	
	public BlockPI(WorkItemRecord wir, ProcletBlock block) {
		this.wir = wir;
		this.block = block;
	}
	
	public void processWIR() {
		myLog.debug("PROCESSWIR");
		List result = this.evalWI();
		myLog.debug("result of evalWI:" + result);
		if ( ((List) result.get(0)).size() == 0) {
			myLog.debug("everything available!");
			// everything available
			myLog.debug("calculate data passing");
			List<EntityID> receivedEids = (List<EntityID>) result.get(1);
			calculateDataPassing(receivedEids, wir);
			myLog.debug("processSuccessfullPI");
			processSuccessfulPI();
		}
		else {
			myLog.debug("not everything available!");
			// not everything available
			// generate waiting period
			SingleInstanceClass sic = SingleInstanceClass.getInstance();
			// wait, only go out if timeout or condition satisfied\
			ThreadNotify oldNotify = null;
			ThreadNotify notif = null;
			long timeleft = 0;
			boolean first = true;
			while (true) {
				try {
					long time = 0;
					if (timeleft  == 0) {
						time = block.getTimeOut();
						//time = 500;
					}
					else {
						time = timeleft;
					}
					//oldNotify = notif;
					myLog.debug("time:" + time);
					SingleInstanceClass.InternalRunner ir = null;
					if (first) {
						notif = new ThreadNotify();
						myLog.debug("registerAndWait1");
						ir = sic.registerAndWait(notif, time);
					}
					else {
						notif = new ThreadNotify();
						myLog.debug("registerAndWaitDuringNotify");
						ir = sic.registerAndWaitDuringNotify(notif, time);
						sic.done(oldNotify);
						//sic.unregister(oldNotify);
					}
					myLog.debug("notif start and join1");
					notif.start();
					notif.join();
					myLog.debug("notif start and join done1");
					oldNotify = notif;
					first = false;
					//
					boolean irDied = !ir.isAlive();
					// re-evaluate result
					result = this.evalWI();
					myLog.debug("re-evaluate result:" + result);
					if (((List) result.get(0)).size() == 0) {
						myLog.debug("everything done");
						sic.done(oldNotify);
						sic.unregister(oldNotify);
						break;
					}
					else if (irDied) { // timeout happened
						myLog.debug("timeout occurred");
						sic.done(oldNotify);
						sic.unregister(oldNotify);
						break;
					}
					else {
						// no timeout, but also not all perfs there
						// get time left for timeout
						myLog.debug("no timeout and not all perfs there");
						timeleft = ir.leftOver();
						if (timeleft <= 0) {
							break;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} // while true
			// woken up again
			// either ok, or user decision needed
			result = this.evalWI();
			myLog.debug("result evaluate:" + result);
			if ( ((List) result.get(0)).size() == 0) {
				myLog.debug("everything available");
				// everything available
				myLog.debug("calculateDataPassing");
				calculateDataPassing((List) result.get(1), wir);
				myLog.debug("processSuccessfulPI");
				processSuccessfulPI();
			}
			else {
				while (true) {
					myLog.debug("ask user for decision");
					// ask user for decision
					List to = timeOutAction();
					publishException(to);
					// receive answer
					boolean userDecision = true;
					while (true) {
						try {
							System.out.println("user decision");
							Thread.sleep(500);
							if (BlockPI.isExceptionCaseSelectedUser(wir.getSpecURI(), wir.getCaseID(), wir.getTaskID())) {
								myLog.debug("user decision:EXCEPTION");
								userDecision = true;
								break;
							}
							else if (BlockPI.isExceptionCaseSelectedUser(wir.getSpecURI(),wir.getCaseID(),wir.getTaskID()+"TIME")) {
								myLog.debug("user decision:TIME OUT");
								userDecision = false;
								break;
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (userDecision) {
						myLog.debug("doNotWaitlongerAction()");
						// create the temp graphs
						//InteractionGraphs.getInstance().createTempGraphs();
						doNotWaitlongerAction();
						myLog.debug("handleException: " + wir.getSpecURI() + "," + wir.getCaseID() + "," + 
								wir.getTaskID() + "," + to.get(0));
						// run an exception
						handleException(wir.getSpecURI(),wir.getCaseID(),wir.getTaskID(),
								(List<EntityMID>) to.get(0)); 
						break;
					}
					else {
						System.out.println("user wait longer");
						myLog.debug("user wait longer");
						BlockPI.deleteExceptionCaseSelected(wir.getSpecURI(),wir.getCaseID(),wir.getTaskID()+"TIME");
						// wait again
						// wait for timeout
						// could be that everything is already there..
						// so re-evaluate
						result = this.evalWI();
						if (((List) result.get(0)).size() != 0) {
								while (true) {
									try {
										long time = 0;
										if (timeleft  == 0) {
											time = block.getTimeOut();
											//time = 500;
										}
										else {
											time = timeleft;
										}
										//oldNotify = notif;
										SingleInstanceClass.InternalRunner ir = null;
										if (first) {
											notif = new ThreadNotify();
											myLog.debug("registerAndWait2");
											ir = sic.registerAndWait(notif, time);
										}
										else {
											notif = new ThreadNotify();
											//System.out.println("register and wait");
											myLog.debug("registerAndWaitDuringNotify2");
											ir = sic.registerAndWaitDuringNotify(notif, time);
											//System.out.println("unregister2");
											sic.done(oldNotify);
											//System.out.println("unregister done2");
											//sic.unregister(oldNotify);
										}
										myLog.debug("notif start and join2");
										notif.start();
										//System.out.println("waiting for the join " + notif);
										notif.join();
										//System.out.println("waiting done! " + notif);
										myLog.debug("notif start and join done2");
										oldNotify = notif;
										first = false;
										//
										boolean irDied = !ir.isAlive();
										// re-evaluate result
										result = this.evalWI();
										myLog.debug("evaluate result2:" + result);
										if (((List) result.get(0)).size() == 0) {
											myLog.debug("everything available2");
											//System.out.println("unregister1");
											sic.done(oldNotify);
											//System.out.println("unregister done1");
											//sic.unregister(oldNotify);
											break;
										}
										else if (irDied) { // timeout happened
											myLog.debug("timeout occurred2");
											//System.out.println("unregister");
											sic.done(oldNotify);
											//sic.unregister(oldNotify);
											//System.out.println("unregister done");
											break;
										}
										else {
											// no timeout, but also not all perfs there
											// get time left for timeout
											myLog.debug("no timeout and not all perfs there2");
											timeleft = ir.leftOver();
											if (timeleft <= 0) {
												break;
											}
										}
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								} // while true
						}
						// if everything received -> jump out
						if ( ((List) result.get(0)).size() == 0) {
							myLog.debug("calculateDataPassing2");
							// everything available
							calculateDataPassing((List) result.get(1), wir);
							myLog.debug("processSuccessfulPI");
							processSuccessfulPI();
							break;
						} // else ask user again
						
						// comment out!
//						sic.done(notif);
//						sic.unregister(notif);
//						// not everything there
//						// new notif
//						ThreadNotify newNotif = new ThreadNotify();
//						notif = newNotif;
//						sic.registerAndWaitDuringNotify(newNotif, block.getTimeOut());
//						try {
//							newNotif.start();
//							newNotif.join();
//							// woken up again
//							result = this.evalWI();
//							if ( ((List) result.get(0)).size() == 0) {
//								// everything available
//								this.calculateDataPassing();
//								processSuccessfulPI();
//								break;
//							} // else ask user again
//						}
//						catch (Exception e) {
//							e.printStackTrace();
//						}
						// end else
					}
				} // end while true
			}
		}
	}
	
	private void handleException(String classID, String procletID, String blockID, List<EntityMID> emids) {
		boolean ignore = false;
		boolean firstPass = false;
		if (!emids.isEmpty() && !ignore) {
			// first connect 
			Trigger trigger = new Trigger();
			trigger.initiate();
			while (true) {
//				trigger.send("something");
				String selectedEmidStr = "";
				if (!firstPass) {
					this.deleteAvailableEmidsBlockExceptionToUser(classID, procletID, blockID);
					this.pushAvailableEmidsBlockExceptionToUser(classID, procletID, blockID,emids);
				}
				trigger.send("BLAAT2");
				// time check for selection
				selectedEmidStr = trigger.receive();
				// take selected one
				// enter phase of editing graph
				if (!selectedEmidStr.equals("EXIT")) {
					EntityMID emidSel = null;
					for (EntityMID emid : emids) {
						if (emid.getValue().equals(selectedEmidStr)) {
							emidSel = emid;
						}
					}
					String id = BlockCP.getUniqueID();
					WorkItemRecord wir = new WorkItemRecord(procletID,blockID,classID,"","");
					ProcletModels pmodelsInst = ProcletModels.getInstance();
					ProcletModel pmodel = pmodelsInst.getProcletClass(classID);
					ProcletBlock block = pmodel.getBlock(blockID);
					ProcessEntityMID pemid = new ProcessEntityMID(wir, block,
							emidSel, id);
					// process user request
					pemid.initialGraphs(true);
					InteractionGraphs igraphs2 = InteractionGraphs.getInstance();
					igraphs2.persistGraphs();
					while (true) {
						List<List<List>> result = pemid.generateNextOptions(true);
						List<List> ncrBlocks = pemid
								.determineOptionsNonCrBlocks(result.get(1));
						List<List<List>> options = new ArrayList<List<List>>();
						options.add(result.get(0));
						options.add(ncrBlocks);
						// send this to user/ together with an update of the graph
						ProcessEntityMID.deleteOptionsFromDB();
						ProcessEntityMID.sendOptionsToDB(options);
						// inform user that options are send
						trigger.send("something");
						// get answer back
						// which is either commit
						// or finish selection
						String userDecision = trigger.receive();
						//boolean userDecisionChecks = false;
						// transfer to user
						// if ok then continue
						// if notok then start all over again
						if (userDecision.equals("commit")) {
							// commit button
							boolean checks = pemid.doChecks();
							if (checks) {
								// checks ok
								//removeSuccessfull emid
								this.deleteEmidBlockExceptionToUser(selectedEmidStr);
								// delete emid also from list
								EntityMID emidToRemove = null;
								for (EntityMID emid : emids) {
									if (emid.getValue().equals(selectedEmidStr)) {
										emidToRemove = emid;
									}	
								}
								emids.remove(emidToRemove);
								firstPass = true;
								// commit graphs
								InteractionGraphs igraphs = InteractionGraphs.getInstance();
								// 22022010
								// remove CP nodes from the interaction graph of the entity which have
								// no incoming or outgoing arcs
								for (InteractionGraph graph : igraphs.getGraphs()) {
									// "TEMP"
									if (graph.getEntityMID().getValue().equals(emidToRemove.getValue() + "TEMP")) {
										// find if there are outgoing CP arcs with no outgoing or incoming arcs
										List<InteractionNode> nodesRemove = new ArrayList<InteractionNode>();
										for (InteractionNode node : graph.getNodes()) {
											boolean found = false;
											for (InteractionArc arc : graph.getArcs()) {
												if ((arc.getTail().getClassID().equals(node.getClassID()) && 
														arc.getTail().getProcletID().equals(node.getProcletID()) && 
														arc.getTail().getBlockID().equals(node.getBlockID())) || 
														(arc.getHead().getClassID().equals(node.getClassID()) && 
														arc.getHead().getProcletID().equals(node.getProcletID()) && 
														arc.getHead().getBlockID().equals(node.getBlockID()))) {
													// found
													found = true;
													break;
												}
											}
											if (!found) {
												// remove node
												nodesRemove.add(node);
											}
										}
										// remove the nodes for the graph
										for (InteractionNode node : nodesRemove) {
											graph.deleteNode(node);
										}
									}
								}
								// done removing nodes
								igraphs.commitTempGraphEmid(emidToRemove.getValue());
								// delete everything from DB
								ProcessEntityMID.deleteOptionsFromDB();
								ProcessEntityMID.deleteDecisionsFromDB();
								trigger.send("ok");
								break;
							} else {
								// user continues or graphs are wrong
								// put old graphs back
								InteractionGraphs igraphs = InteractionGraphs
										.getInstance();
								igraphs.deleteTempGraphs();
								igraphs.deleteTempGraphsFromDB();
								//pemid.initialGraphs();
								igraphs.persistGraphs();
								// delete everything from DB
								ProcessEntityMID.deleteOptionsFromDB();
								ProcessEntityMID.deleteDecisionsFromDB();
								trigger.send("nok");
								break;
							}
						} else if (userDecision.equals("finishSelection")) {
							// finish button hitted
							List decisions = ProcessEntityMID.getDecisionsFromDB();
							try {
								pemid.extendGraph(decisions);
								// commit temp graph
								InteractionGraphs igraphs = InteractionGraphs.getInstance();
								igraphs.persistGraphs();
								ProcessEntityMID.deleteDecisionsFromDB();
								trigger.send("something");
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							// and continue building
						}
						else {
							// nothing to be done
						}
					}
					// get new id
					String newID = pemid.getUID();
					// update db
					BlockCP.updateUniqueID(newID);
					pemid.commitGraphs();
//					pemid.sendPerformatives(true);
//					SingleInstanceClass sic = SingleInstanceClass.getInstance();
//					sic.notifyPerformativeListeners();
					// emid processing
				} else {
					// "EXIT" received -> done
					// delete everything from DB
					InteractionGraphs.getInstance().commitTempGraphs();
					ProcessEntityMID.sendPerformatives(true,wir);
					ProcessEntityMID.deleteOptionsFromDB();
					ProcessEntityMID.deleteDecisionsFromDB();
					trigger.close();
					BlockPI.deleteExceptionCaseSelected(classID, procletID, blockID);
					BlockPI.deleteAllAvailableEmidsBlockExceptionToUser();
					break;
				}
			} // while true
		} // end emids
	}
	
	private List<EntityID> getRequiredEntityIDsForFiring () {
		String classID = wir.getSpecURI();
		String procletID = wir.getCaseID();
		String blockID = wir.getTaskID();
		List<EntityID> eids = new ArrayList<EntityID> ();
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				if (arc.getHead().getClassID().equals(classID) &&
						arc.getHead().getProcletID().equals(procletID) &&
						arc.getHead().getBlockID().equals(blockID) && 
						!arc.getArcState().equals(ArcState.FAILED)) {
					// 25012010
					eids.add(arc.getEntityID());
				}
			}
		}
		return eids;
	}
	
	private List<Performative> getRelevantPerformatives () {
		Performatives perfsInst = Performatives.getInstance();
		List<Performative> perfs = perfsInst.getPerformatives();
		// get all relevant arcs 
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionArc> allArcs = igraphs.getAllArcs();
		List<InteractionArc> relevantArcs = new ArrayList<InteractionArc>();
		for (InteractionArc arc : allArcs) {
			if (arc.getHead().getProcletID().equals(wir.getCaseID()) && 
					arc.getHead().getBlockID().equals(wir.getTaskID())) {
					relevantArcs.add(arc);
			}
		}
		// search per performative
		List<Performative> perfFinal = new ArrayList<Performative> ();
		for (Performative perf : perfs) {
			List<EntityID> eidsPerf = perf.getEntityIDs();
			for (InteractionArc arc : relevantArcs) {
				EntityID eid = arc.getEntityID();
				// search whether eid is in eidsPerf
				boolean found = false;
				for (EntityID eidSearch : eidsPerf) {
					if (eidSearch.toString().equals(eid.toString())) {
						// found
						found = true;
						break;
					}
				}
				if (found) {
					// 20022010
					// check if not already in
					boolean alreadyIn = false;
					for (Performative perfCheck :  perfFinal) {
						if (perfCheck.equalContent(perf)) {
							alreadyIn = true;
						}
					}
					if (!alreadyIn) {
						perfFinal.add(perf);
					}
					break;
				}
			}
		}
		return perfFinal;
	}
	
	private List<EntityID> calculateUnreceivedEids (List<Performative> perfs, List<EntityID> eids) {
		List<EntityID> eidsToRemove = new ArrayList<EntityID> ();
		for (Performative perf : perfs) {
			eidsToRemove.addAll(perf.getEntityIDs());
		}
		List<EntityID> eidLeft = new ArrayList<EntityID> ();
		for (EntityID eid : eids) {
			boolean toBeRemoved = false;
			for (EntityID eid2 : eidsToRemove) {
				if (eid2.toString().equals(eid.toString())) {
					// needs to be removed
					toBeRemoved = true;
					break;
				}
			}
			if (!toBeRemoved) {
				eidLeft.add(eid);
			}
		}
		return eidLeft;
	}
	
	public List<List> evalWI() {
		List<List> answer = new ArrayList ();
		List<EntityID> eidsFiring = this.getRequiredEntityIDsForFiring();
		List<Performative> relPerfs = this.getRelevantPerformatives();
		List<EntityID> unreceivedEids = this.calculateUnreceivedEids(relPerfs, eidsFiring);
		// determine receivedEids
		List<EntityID> receivedEids = new ArrayList<EntityID> ();
		for (EntityID eid : eidsFiring) {
			boolean needed = true;
			for (EntityID eid2 : unreceivedEids) {
				if (eid.toString().equals(eid2.toString())) {
					// don't need to keep
					needed = false;
					break;
				}
			}
			if (needed) {
				receivedEids.add(eid);
			}
		}
		answer.add(unreceivedEids);
		answer.add(receivedEids);
		return answer;
	}
	
	public static void calculateDataPassing (List<EntityID> eids, WorkItemRecord wir) {
		myLog.debug("CALCULATEDATAPASSING");
		// get datalist
		Element dl = wir.getDataList();
		Element eidData = dl.getChild("entities");
		// get relevant perfs
		List<Performative> relPerfs = new ArrayList<Performative> ();
		for (EntityID eid : eids) {
			for (Performative perf : Performatives.getInstance().getPerformatives()) {
				for (EntityID eidPerf : perf.getEntityIDs()) {
					if (eidPerf.toString().equals(eid.toString())) {
						// 20022010
						// check if perf not already in
						boolean alreadyIn = false;
						for (Performative perfCheck : relPerfs) {
							if (perfCheck.equalContent(perf)) {
								alreadyIn = true;
							}
						}
						if (!alreadyIn) {
							relPerfs.add(perf);
						}
						break;
					}
				}
			}
		}
		myLog.debug("relPerfs:" + relPerfs);
		if (eidData != null) {
			for (Performative perf : relPerfs) {
				// go through content of performative
				String content = perf.getContent();
				myLog.debug("eids:" + perf.getEntityIDs().toString());
				myLog.debug("content:" + content);
				Element eltContent = JDOMUtil.stringToElement(content);
				List<Element> entitiesElts = eltContent.getChildren("entity");
				for (Element entityElt : entitiesElts) {
					// get the name
					Element emid = entityElt.getChild("entity_id");
					String value = emid.getValue().trim();
					myLog.debug("value:" + value);
					// check if the data of the wir we also have an emid with the same name
					// also check if entityElt is RELEVANT!!! so is it contained in eids
					// 20022010
					boolean found = false;
					for (EntityID eid : eids) {
						if (value.equals(eid.getEmid().getValue())) {
							found = true;
						}
					}
					if (eidData != null && found) {
						boolean match = false;
						List<Element> children = eidData.getChildren("entity");
						for (Element child : children) {
							myLog.debug("have entity");
							Element emidData = child.getChild("entity_id");
							String valueData = emidData.getValue().trim();
							myLog.debug("entity_id:" + valueData);
							if (valueData.equals(value)) {
								// match
								match = true;
								// add the data
								List<Element> nvsPerf = entityElt.getChildren("name_value_pair");
								for (Element nvPerf : nvsPerf) {
									String nameNvPerf = nvPerf.getChild("name").getValue().trim();
									String valueNvPerf = nvPerf.getChild("value").getValue().trim();
									// find out whether I have an nv in the wir with the same name
									List<Element> nvsWir = child.getChildren("name_value_pair");
									boolean match2 = false;
									for (Element nvWir : nvsWir) {
										String nameNvWir = nvWir.getChild("name").getValue().trim();
										String valueNvWir = nvWir.getChild("value").getValue().trim();
										if (nameNvWir.equals(nameNvPerf)) {
											// have a match
											match2 = true;
											nvWir.getChild("value").setText(valueNvPerf);
											break;
										}
									}
									if (!match2) {
										// no nv pair with such a name exists
										myLog.debug("no nv pair with such a name exists!");
										// add one
										Element newElt = new Element("name_value_pair");
										Element nameElt = new Element("name");
										nameElt.setText(nameNvPerf);
										Element valueElt = new Element("value");
										valueElt.setText(valueNvPerf);
										newElt.addContent(nameElt);
										newElt.addContent(valueElt);
										myLog.debug("newElt:" + JDOMUtil.elementToString(newElt));
										child.addContent(newElt);
									}
								}
								break;
							}
						}
						if (!match) {
							// no emid with same name in the data of the wir
							myLog.debug("no emid with the same name in data of wir!");
							// eid
							Element newEntityElt = new Element("entity");
							String eid = entityElt.getChild("entity_id").getValue();
							Element eidElt = new Element("entity_id");
							eidElt.setText(eid);
							newEntityElt.addContent(eidElt);
							// nv pairs
							List<Element> elts = entityElt.getChildren("name_value_pair");
							for (Element elt : elts) {
								String name = elt.getChild("name").getValue();
								String valueElt = elt.getChild("value").getValue();
								Element nvElt = new Element("name_value_pair");
								Element newNameElt = new Element("name");
								newNameElt.setText(name);
								Element newValueElt = new Element("value");
								newValueElt.setText(valueElt);
								nvElt.addContent(newNameElt);
								nvElt.addContent(newValueElt);
								newEntityElt.addContent(nvElt);
							}
							myLog.debug("newEntityElt:" + JDOMUtil.elementToString(newEntityElt));
							eidData.addContent(newEntityElt);
						}
					}
				}
			}
		}
		// done
		myLog.debug("dl:" + JDOMUtil.elementToString(dl));
		wir.setDataList(dl);
		myLog.debug("data wir:" + wir.getDataListString());
	}
	
	public void processSuccessfulPI () {
		myLog.debug("PROCESSSUCESSFULPI");
		List result = this.evalWI();
		List<EntityID> receivedEids = (List<EntityID>) result.get(1);
		myLog.debug("receivedEids:" + receivedEids);
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		igraphs.updateGraphPerfIn(receivedEids);
		// 19022010
		igraphs.updateGraphPI(wir.getSpecURI(),wir.getCaseID(),wir.getTaskID());
		Performatives perfsInst = Performatives.getInstance();
		myLog.debug("perfsInst:" + perfsInst.getPerformatives());
		// delete perfs
		List<Performative> remPerfs = new ArrayList<Performative> ();
		for (EntityID eid : receivedEids) {
			for (Performative perf : perfsInst.getPerformatives()) {
				for (EntityID eidPerf : perf.getEntityIDs()) {
					if (eidPerf.toString().equals(eid.toString())) {
						// ONLY ADD IF WE DO NOT ALREADY HAVE A PERF WITH EXACTLY THE SAME EIDS!
						// 20022010
						boolean existsCheck = false;
						for (Performative perfCheck : remPerfs) {
							if (perfCheck.equalContent(perf)) {
								existsCheck = true;
							}
						}
						if (!existsCheck) {
							remPerfs.add(perf);
						}
						break;
					}
				}
			}
		}
		myLog.debug("remPerfs:" + remPerfs);
		for (Performative perf : remPerfs) {
			perfsInst.deletePerformative(perf);
		}
		myLog.debug("perfsInst2:" + perfsInst.getPerformatives());
	}
	
	private List determineFailingEmidsWIR(List<EntityID> eids) {
		List<EntityMID> emids = new ArrayList<EntityMID>();
		for (EntityID eid : eids) {
			// 10032010
			// do not add twice
			boolean exists = false;
			for (EntityMID emidCheck : emids) {
				if (emidCheck.getValue().equals(eid.getEmid().getValue())) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				emids.add(eid.getEmid());
			}
			
		}
		List returnList = new ArrayList();
		returnList.add(emids);
		returnList.add(wir);
		returnList.add(block);
		return returnList;
	}
	
	public List timeOutAction () {
		List<EntityID> unrecEids = this.evalWI().get(0);
		return this.determineFailingEmidsWIR(unrecEids);
	}
	
	public void doNotWaitlongerAction () {
		// calculateDataPassing
		List result = this.evalWI();
		List<EntityID> unreceivedEids = (List<EntityID>) result.get(0);
		List<EntityID> receivedEids = (List<EntityID>) result.get(1);
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		igraphs.updateGraphPerfIn(receivedEids);
		igraphs.updateGraphPerfInFailed(unreceivedEids);
		// remove perfs
		// delete perfs
		Performatives perfsInst = Performatives.getInstance();
		List<Performative> remPerfs = new ArrayList<Performative> ();
		for (EntityID eid : receivedEids) {
			for (Performative perf : perfsInst.getPerformatives()) {
				for (EntityID eidPerf : perf.getEntityIDs()) {
					if (eidPerf.toString().equals(eid.toString())) {
						boolean existsCheck = false;
						for (Performative perfCheck : remPerfs) {
							if (perfCheck.equalContent(perf)) {
								existsCheck = true;
							}
						}
						if (!existsCheck) {
							remPerfs.add(perf);
						}
						break;
					}
				}
			}
		}
		for (Performative perf : remPerfs) {
			perfsInst.deletePerformative(perf);
		}
		// take exception block
//		ProcletModels pmodelsInst = ProcletModels.getInstance();
//		ProcletModel pmodel = pmodelsInst.getProcletClass(wir.getSpecificationID());
//		ProcletBlock blockExc = pmodel.getBlock("exception");
		// persist graphs
		igraphs.persistGraphs();
	}
	
	public static void publishException (List ec) {
		List<EntityMID> emids = (List<EntityMID>) ec.get(0);
		String emidsStr = "";
		for (EntityMID emid : emids) {
			emidsStr += emid.getValue() + ",";
		}
		emidsStr = emidsStr.substring(0, emidsStr.length()-1);
        DBConnection.insert(new StoredItem((WorkItemRecord) ec.get(1), emidsStr,
                Item.ExceptionCaseBlock));
	}

    public static List getExceptions () {
        List resultFin = new ArrayList();
        List items = DBConnection.getStoredItems(Item.ExceptionCaseBlock);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            String emidsStr = item.getEmid();

            // split the string
            List<EntityMID> emids = new ArrayList<EntityMID>();
            String[] split = emidsStr.split(",");
            for (int i=0; i<split.length; i++) {
                String t = split[i];
                emids.add(new EntityMID(t));
            }
            List result = new ArrayList();
            result.add(emids);
            result.add(item.getClassID());
            result.add(item.getProcletID());
            result.add(item.getBlockID());
            resultFin.add(result);
        }

        return resultFin;
    }
	
	public static void deleteException (String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID,
                Item.ExceptionCaseBlock);
        if (item != null) DBConnection.delete(item);
	}
	
	public static void deleteExceptionCaseSelected(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID,
                Item.ExceptionCaseSelectionBlock);
        if (item != null) DBConnection.delete(item);
	}
	
	public static void publishExceptionCase(String classID, String procletID, String blockID) {
        StoredItem item = new StoredItem(classID, procletID, blockID,
                Item.ExceptionCaseSelectionBlock);
        item.setSelected(true);
        DBConnection.insert(item);
	}
	
	public static boolean isExceptionCaseSelectedUser(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getSelectedStoredItem(classID, procletID, blockID,
                Item.ExceptionCaseSelectionBlock);
        return (item != null);
    }

	private void deleteAvailableEmidsBlockExceptionToUser(String classID, String procletID, String blockID) {
        List items = DBConnection.getStoredItems(classID, procletID, blockID,
                Item.EmidExceptionCaseSelectionBlock);
        for (Object o : items) {
            DBConnection.delete(o);
        }
	}
	
	public static void deleteAllAvailableEmidsBlockExceptionToUser() {
		DBConnection.deleteAll(Item.EmidExceptionCaseSelectionBlock);
	}

    private void pushAvailableEmidsBlockExceptionToUser(String classID, String procletID,
                                                        String blockID, List<EntityMID> emids) {
        for (EntityMID emid : emids) {
            DBConnection.insert(new StoredItem(classID, procletID, blockID, emid.getValue(),
                    Item.EmidExceptionCaseSelectionBlock));
        }
    }
	
	private void deleteEmidBlockExceptionToUser(String emidStr) {
        String query = "delete from StoredItem as s where s.emid='" + emidStr +
                "' and s.itemType=" + Item.EmidExceptionCaseSelectionBlock.ordinal();
        DBConnection.execUpdate(query);
	}
	
	public static List<EntityMID> getAvailableEmidsBlockExceptionToUser() {
        List<EntityMID> emidList = new ArrayList<EntityMID>();
        List items = DBConnection.getStoredItems(Item.EmidExceptionCaseSelectionBlock);
        for (Object o : items) {
             emidList.add(((StoredItem) o).newEntityMID());
        }
		return emidList;
	}
	
	public static void setEmidSelectedBlockException(EntityMID emid) {
        List items = DBConnection.getStoredItems(Item.EmidExceptionCaseSelectionBlock);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            if (item.getEmid().equals(emid.getValue())) {
                item.setSelected(true);
                DBConnection.update(item);
            }
        }
	}
	
	public static List<InteractionNode> getExceptionBlockSelected () {
        List<InteractionNode> nodes = new ArrayList<InteractionNode>();
        List items = DBConnection.getStoredItems(Item.ExceptionCaseSelectionBlock);
	    for (Object o : items) {
            StoredItem item = (StoredItem) o;
            if (item.isSelected()) {
                nodes.add(item.newInteractionNode());
            }
        }
		return nodes;
	}
	
	public static void setExceptionBlockSelected(String classID, String procletID, String blockID) {
        DBConnection.setStoredItemsSelected(classID, procletID, blockID,
                Item.ExceptionCaseSelectionBlock);
	}
}
