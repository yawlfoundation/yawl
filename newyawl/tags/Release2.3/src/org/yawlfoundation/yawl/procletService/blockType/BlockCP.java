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
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.Item;
import org.yawlfoundation.yawl.procletService.persistence.StoredItem;
import org.yawlfoundation.yawl.procletService.persistence.UniqueID;
import org.yawlfoundation.yawl.procletService.selectionProcess.ProcessEntityMID;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.state.Performatives;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;
import org.yawlfoundation.yawl.procletService.util.EntitySID;

import java.util.ArrayList;
import java.util.List;

public class BlockCP {

	private WorkItemRecord wir = null;
	private ProcletBlock block = null;

	private static String uniqueidTN = "UniqueID";

	private static Logger myLog = Logger.getLogger(BlockCP.class);

	public BlockCP(WorkItemRecord wir, ProcletBlock block) {
		this.wir = wir;
		this.block = block;
	}

	private List<EntityMID> calculateEmids() {
		List<EntityMID> emids = new ArrayList<EntityMID>();
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getGraphs()) {
			for (InteractionNode node : graph.getNodes()) {
				if (node.getClassID().equals(wir.getSpecURI())
						&& node.getProcletID().equals(wir.getCaseID())) {
					// check if not already in
					if (!emids.contains(graph.getEntityMID()) && 
							!graph.getEntityMID().getValue().contains("TEMP")) {
						emids.add(graph.getEntityMID());
					}
					break;
				}
			}
		}
		return emids;
	}

	public static String getUniqueID() {
        List list = DBConnection.getObjectsForClass(uniqueidTN);
        return (! list.isEmpty()) ? ((UniqueID) list.get(0)).getUniqueID() : "";
	}

	public static void updateUniqueID(String id) {
        DBConnection.deleteAll(uniqueidTN);
        DBConnection.insert(new UniqueID(id));
	}

	public void processWIR() {
		myLog.debug("PROCESSWIR");
		List<ProcessEntityMID> pemids = new ArrayList<ProcessEntityMID>();
		// get the data from the wir
		Element dataList = wir.getDataList();
		List<EntityMID> emids = getEntityMIDsFromData(dataList);
		ProcessEntityMID.deleteDecisionsFromDB();
		ProcessEntityMID.deleteOptionsFromDB();
		deleteWorkitemSelected(wir.getSpecURI(), wir.getCaseID(), wir.getTaskID());
		// publish workitem 
		publishWorkItem(wir.getSpecURI(),wir.getCaseID(),wir.getTaskID());
		// wait for selection user
		while (true) {
			try {
				Thread.sleep(500);
				if (this.isWorkItemSelectedUser()) {
					myLog.debug("workitem selected");
					break;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		myLog.debug("emidsBefore:" + emids);
		// add emids, check for no duplicates
		List<EntityMID> nextEmids = this.calculateEmids();
		for (EntityMID emid : nextEmids) {
			boolean found = false;
			for (EntityMID emidC : emids) {
				if (emidC.getValue().equals(emid.getValue())) {
					found = true;
					break;
				}
			}
			if (!found) {
				emids.add(emid);
			}
		}
		myLog.debug("emids:" + emids);
		boolean firstPass = false;
		if (!emids.isEmpty()) {
			myLog.debug("emids not empty");
			// push selectable emids to user
			// first connect 
			Trigger trigger = new Trigger();
			myLog.debug("trigger initiate");
			trigger.initiate();
			myLog.debug("trigger initiate done");
			while (true) {
				String selectedEmidStr = "";
				if (!firstPass) {
					this.deleteAvailableEmidsToUser();
					try {
						Thread.sleep(500);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					pushAvailableEmidsToUser(emids);
				}
				trigger.send("BLAAT");
				// time check for selection
				selectedEmidStr = trigger.receive();
				myLog.debug("selectedEmidStr:" + selectedEmidStr);
				// take selected one
				// enter phase of editing graph
				if (!selectedEmidStr.equals("EXIT")) {
					EntityMID emidSel = null;
					for (EntityMID emid : emids) {
						if (emid.getValue().equals(selectedEmidStr)) {
							emidSel = emid;
						}
					}
					String id = getUniqueID();
					myLog.debug("unique id:" + id);
					ProcessEntityMID pemid = new ProcessEntityMID(wir, block,
							emidSel, id);
					// process user request
					myLog.debug("initialGraphs");
					pemid.initialGraphs(false);
					InteractionGraphs igraphs2 = InteractionGraphs.getInstance();
					igraphs2.persistGraphs();
					while (true) {
						List<List<List>> result = pemid.generateNextOptions(false);
						myLog.debug("next options:" + result);
						List<List> ncrBlocks = pemid
								.determineOptionsNonCrBlocks(result.get(1));
						myLog.debug("ncrBlocks:" + ncrBlocks);
						List<List<List>> options = new ArrayList<List<List>>();
						options.add(result.get(0));
						options.add(ncrBlocks);
						myLog.debug("options:" + options);
						// send this to user/ together with an update of the graph
						ProcessEntityMID.deleteOptionsFromDB();
						ProcessEntityMID.sendOptionsToDB(options);
						// inform user that options are send
						myLog.debug("trigger send something");
						trigger.send("something");
						// get answer back
						// which is either commit
						// or finish selection
						String userDecision = trigger.receive();
						myLog.debug("userDecision:" + userDecision);
						//boolean userDecisionChecks = false;
						// transfer to user
						// if ok then continue
						// if notok then start all over again
						if (userDecision.equals("commit")) {
							// commit button
							myLog.debug("userDecision:COMMIT");
							boolean checks = pemid.doChecks();
							myLog.debug("result checks:" + checks);
							if (checks) {
								// checks ok
								//removeSuccessfull emid
								BlockCP.deleteEmidToUser(selectedEmidStr);
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
								myLog.debug("removing isolated nodes");
								myLog.debug("emidToRemove:" + emidToRemove.getValue());
								InteractionGraphs igraphs = InteractionGraphs.getInstance();
								// 22022010
								// remove CP nodes from the interaction graph of the entity which have
								// no incoming or outgoing arcs
								for (InteractionGraph graph : igraphs.getGraphs()) {
									// "TEMP"
									myLog.debug("considering graph:" + graph.getEntityMID());
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
										myLog.debug("nodesRemove:" + nodesRemove);
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
							myLog.debug("userDecision:FINISHSELECTION");
							// finish button hitted
							List decisions = ProcessEntityMID.getDecisionsFromDB();
							myLog.debug("decisions from DB:" + decisions);
							try {
								myLog.debug("extend graph");
								pemid.extendGraph(decisions);
								// commit temp graph
								InteractionGraphs igraphs = InteractionGraphs.getInstance();
								igraphs.persistGraphs();
								ProcessEntityMID.deleteDecisionsFromDB();
								trigger.send("something");
								myLog.debug("trigger send something");
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
					myLog.debug("update unique id:" + newID);
					this.updateUniqueID(newID);
					myLog.debug("commitGraphs");
					pemid.commitGraphs();
					InteractionGraphs igraphs = InteractionGraphs.getInstance();
					igraphs.persistGraphs();
					// save the pemid
//					pemids.add(pemid);
//					pemid.sendPerformatives(false);
					//SingleInstanceClass sic = SingleInstanceClass.getInstance();
					//myLog.debug("notifyPerformativeListeners");
					//sic.notifyPerformativeListeners();
					// emid processing
				} else {
					// "EXIT" received -> done
					// delete everything from DB
					myLog.debug("EXIT RECEIVED");
					// send the performatives
					myLog.debug("sendPerformatives");
					InteractionGraphs.getInstance().commitTempGraphs();
					ProcessEntityMID.sendPerformatives(false, wir);
					ProcessEntityMID.deleteOptionsFromDB();
					ProcessEntityMID.deleteDecisionsFromDB();
					trigger.close();
					BlockCP.deleteWorkitemSelected(wir.getSpecURI(), wir.getCaseID(), wir.getTaskID());
					BlockCP.deleteAllEmidsToUser();
					break;
				}
			} // while true
		} // end emids
	}

	private void pushAvailableEmidsToUser(List<EntityMID> emids) {
	    for (EntityMID emid : emids) {
            DBConnection.insert(new StoredItem(wir.getSpecURI(), wir.getCaseID(),
                    wir.getTaskID(), emid.getValue(), Item.EmidSelection));
        }
	}
	
	public static void deleteEmidToUser(String emidStr) {
		String query = "delete from StoredItem as s where s.emid = '" + emidStr +
                "' and s.itemType=" + Item.EmidSelection.ordinal();
        DBConnection.execUpdate(query);
	}
	
	public static void deleteAllEmidsToUser() {
		DBConnection.deleteAll(Item.EmidSelection);
	}
	
	public static List<EntityMID> getAvailableEmidsToUser() {
		List<EntityMID> emidList = new ArrayList<EntityMID>();
		List items = DBConnection.getStoredItems(Item.EmidSelection);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            emidList.add(item.newEntityMID());
        }
		myLog.debug("emidList:" + emidList);
		return emidList;
	}

	public static List<InteractionNode> getInteractionNodesUser () {
		List<InteractionNode> nodes = new ArrayList<InteractionNode>();
        List items = DBConnection.getStoredItems(Item.WorkItemSelection);
        for (Object item : items) {
            nodes.add(((StoredItem) item).newInteractionNode());
        }
        return nodes;
	}
	
	private boolean isWorkItemSelectedUser() {
        StoredItem item = DBConnection.getSelectedStoredItem(wir.getSpecURI(), wir.getCaseID(),
                wir.getTaskID(), Item.WorkItemSelection);
        return (item != null);
	}
	
	public static void publishWorkItem(String classID, String procletID, String blockID) {
        DBConnection.insert(new StoredItem(classID, procletID, blockID,
                Item.WorkItemSelection));
	}
	
	public static void setWorkitemSelected(String classID, String procletID, String blockID) {
        DBConnection.setStoredItemSelected(classID, procletID, blockID,
                Item.WorkItemSelection);
	}
	
	public static InteractionNode getWorkitemSelected() {
        List items = DBConnection.getStoredItems(Item.WorkItemSelection);
        for (Object item : items) {
            if (((StoredItem) item).isSelected()) {
                return ((StoredItem) item).newInteractionNode();
            }
        }
        return null;
	}
	
	public static void deleteWorkitemSelected(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID,
                Item.WorkItemSelection);
        if (item != null) DBConnection.delete(item);
	}

	public String getSelectedEmidFromUser() {
        StoredItem item = DBConnection.getSelectedStoredItem(wir.getSpecURI(),
                wir.getCaseID(), wir.getTaskID(), Item.EmidSelection);
        return (item != null) ? item.getEmid() : "NONE";
	}
	
	public static void setEmidSelected(EntityMID emid) {
        List items = DBConnection.getObjectsForClassWhere("StoredItem", "emid='" + emid + "'");
        for (Object o : items) {
            DBConnection.setStoredItemSelected((StoredItem) o);
        }
	}

	private void deleteAvailableEmidsToUser() {
		DBConnection.deleteAll(Item.EmidSelection);
	}
	
	public static List<EntityMID> getEntityMIDsFromData (Element dataList) {
		List<EntityMID> returnList = new ArrayList<EntityMID>();
		Element eidData = dataList.getChild("entities");
		if (eidData != null) {
			List<Element> children = eidData.getChildren("entity");
			for (Element child : children) {
				Element emid = child.getChild("entity_id");
				String value = emid.getValue().trim();
				returnList.add(new EntityMID(value));
			}
		}
		return returnList;
	}

	public static void main(String[] args) {
		InteractionArc arc = new InteractionArc(null,null, new EntityID(new EntityMID("1"),new EntitySID("1")),
				InteractionArc.ArcState.CONSUMED);
		arc.getEntityID().getEsid().setEsid("5");
		Performatives perfsInst = Performatives.getInstance();
		List<EntityID> eidTest = new ArrayList<EntityID>();
		eidTest.add(new EntityID("1","1"));
		List<String> receivers = new ArrayList<String>();
		receivers.add("r1");
		Performative perf = new Performative("chann","s",receivers,"a","c","s", ProcletPort.Direction.OUT,eidTest);
		perfsInst.addPerformative(perf);
		SingleInstanceClass sic = SingleInstanceClass.getInstance();
		sic.notifyPerformativeListeners(null);
		//
//		WorkItemRecord wir = new WorkItemRecord("1","meet","visit","","");
//		ProcletBlock block = new ProcletBlock("visit",BlockType.CP,false,5);
//		BlockCP bcp = new BlockCP(wir,block);
//		bcp.processWIR();
//		String uid = bcp.getUniqueID();
//		bcp.updateUniqueID("6");
	}
}
