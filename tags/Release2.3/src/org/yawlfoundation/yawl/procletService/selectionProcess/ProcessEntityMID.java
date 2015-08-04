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

package org.yawlfoundation.yawl.procletService.selectionProcess;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.procletService.ProcletService;
import org.yawlfoundation.yawl.procletService.SingleInstanceClass;
import org.yawlfoundation.yawl.procletService.blockType.BlockFO;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc.ArcState;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.*;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock.BlockType;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Direction;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Signature;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.StoredDecisions;
import org.yawlfoundation.yawl.procletService.persistence.StoredOptions;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import java.util.ArrayList;
import java.util.List;

public class ProcessEntityMID {
	
	private WorkItemRecord wir = null;
	private ProcletBlock block = null;
	private EntityMID emid = null;
	
	private String uniqueID = "";
	
	private static Logger myLog = Logger.getLogger(ProcessEntityMID.class);
	
	public ProcessEntityMID(WorkItemRecord wir, ProcletBlock block, EntityMID emid, String uniqueID) {
		this.wir = wir;
		this.block = block;
		this.emid = emid;
		this.uniqueID = uniqueID;
	}
	
	public String getUID() {
		return this.uniqueID;
	}
	
	public void initialGraphs(boolean exception) {
		// set wir task id temporarily to exception if exception is true
		String oldValue = "";
		if (exception) {
			oldValue = wir.getTaskID();
			wir.setTaskID("exception");
		}
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		igraphs.createTempGraphs();
		boolean exists = false;
		InteractionGraph graph = igraphs.getTempGraph(emid);
		if (graph != null) {
			for (InteractionNode node : graph.getNodes()) {
				if (node.getClassID().equals(wir.getSpecURI()) && 
						node.getProcletID().equals(wir.getCaseID()) && 
						node.getBlockID().equals(wir.getTaskID())) {
					// node exists 
					exists = true;
					break;
				}
			}
			if (!exists) {
				InteractionNode newNode = new InteractionNode(wir.getSpecURI(),
						wir.getCaseID(),wir.getTaskID());
				graph.addNode(newNode);
			}
		}
		else {
			InteractionGraph newGraph = new InteractionGraph(new EntityMID(emid.getValue() + "TEMP"));
			InteractionNode newNode = new InteractionNode(wir.getSpecURI(),
					wir.getCaseID(),wir.getTaskID());
			newGraph.addNode(newNode);
			igraphs.addGraph(newGraph);
		}
		// in case of exception, set the task value of the wir back
		if (exception) {
			wir.setTaskID(oldValue);
		}
	}
	
	public boolean doChecks() {
		myLog.debug("DO CHECKS");
		return testDuplicates() && checkOneOrQuestGraphsOut() &&
                checkOneOrQuestGraphsIn() && danglingArcs();
	}
	
	private boolean testDuplicates () {
		myLog.debug("TESTDUPLICATES");
		boolean dupl = false;
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getTempGraphs()) {
			// nodes
			for (InteractionNode node : graph.getNodes()) {
				for (InteractionNode node2 : graph.getNodes()) {
					if (node.getClassID().equals(node2.getClassID()) && 
							node.getProcletID().equals(node2.getProcletID()) && 
							node.getBlockID().equals(node2.getBlockID()) &&
							node != node2) {
						myLog.debug("node:" + node + ",node:" + node);
						dupl = true;
						break;
					}
				}
			}
			// arcs
			for (InteractionArc arc : graph.getArcs()) {
				for (InteractionArc arc2 : graph.getArcs()) {
					if (arc.getTail() == arc2.getTail() &&
							arc.getHead() == arc2.getHead() &&
							arc != arc2) {
						myLog.debug("arc:" + arc + ",arc2:" + arc2);
						dupl = true;
						break;
					}
				}
			}
		}
		if (dupl) {
			myLog.debug("return:false");
			return false;
		}
		else {
			myLog.debug("return:true");
			return true;
		}
	}
	
	private boolean danglingArcs () {
		myLog.debug("DANGLINGARCS");
		boolean err = false;
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getTempGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				InteractionNode head = null;
				InteractionNode tail = null;
				myLog.debug("considering:" + arc);
				for (InteractionNode node : graph.getNodes()) {
					if (arc.getHead().getClassID().equals(node.getClassID()) && 
							arc.getHead().getProcletID().equals(node.getProcletID()) &&
							arc.getHead().getBlockID().equals(node.getBlockID())) {
						head = node;						
					}
					if (arc.getTail().getClassID().equals(node.getClassID()) && 
							arc.getTail().getProcletID().equals(node.getProcletID()) &&
							arc.getTail().getBlockID().equals(node.getBlockID())) {
						tail = node;						
					}
				}
				if (head == null || tail == null) {
					// something went wrong
					err = true;
					break;
				}
			}
		}
		if (err) {
			myLog.debug("return:false");
			return false;
		}
		else {
			myLog.debug("return:true");
			return true;
		}
	}
	
	private boolean checkOneOrQuestGraphsOut() {
		myLog.debug("CHECKONEORQUESTGRAPHSOUT");
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionGraph> tempGraphs = igraphs.getTempGraphs();
		List<InteractionArc> allArcs = new ArrayList<InteractionArc>();
		// 09032010
		// ignore arc which state FAILED
		for (InteractionGraph graph : tempGraphs) {
			for (InteractionArc arc : graph.getArcs()) {
				if (!arc.getArcState().equals(ArcState.FAILED)) {
					allArcs.add(arc);
				}
			}
		} 
		ProcletModels pmodelsInst = ProcletModels.getInstance();
		List<List> combis = new ArrayList<List> ();
		for (InteractionArc arc : allArcs) {
			// if arc in same proclet then ignore
			if (!arc.getTail().getProcletID().equals(arc.getHead().getProcletID())) {
				List item = new ArrayList();
				// ProcletPort
				item.add(pmodelsInst.getOutgoingPort(arc));
				// tail procletid
				item.add(arc.getTail().getProcletID());
				// head procletid
				item.add(arc.getHead().getProcletID());
				combis.add(item);
			}
		}
		// check for duplicates
		boolean err = false;
		for (List combi : combis) {
			for (List combi2 : combis) {
				if ( ((ProcletPort)combi.get(0)).getPortID().equals(((ProcletPort) combi2.get(0)).getPortID()) &&
						combi.get(1).equals(combi2.get(1)) && 
						!combi.get(2).equals(combi2.get(2))) {
					ProcletPort port = (ProcletPort) combi.get(0);
					if (port.getCardinality().equals(Signature.ONE) || 
							port.getCardinality().equals(Signature.QUEST)) {
						myLog.debug("combi1:" + combi);
						myLog.debug("combi2:" + combi2);
						err = true;
					}
				}
			}
		}
		if (!err) {
			myLog.debug("return:true");
			return true;
		}
		else {
			myLog.debug("return:false");
			return false;
		}
	}
	
	private boolean checkOneOrQuestGraphsIn() {
		myLog.debug("CHECKONEORQUESTGRAPHSIN");
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionArc> allArcs = new ArrayList<InteractionArc>();
		List<InteractionGraph> tempGraphs = igraphs.getTempGraphs();
		// 09032010
		// ignore arc which state FAILED
		for (InteractionGraph graph : tempGraphs) {
			for (InteractionArc arc : graph.getArcs()) {
				if (!arc.getArcState().equals(ArcState.FAILED)) {
					allArcs.add(arc);
				}
			}
		} 
		ProcletModels pmodelsInst = ProcletModels.getInstance();
		List<List> combis = new ArrayList<List> ();
		for (InteractionArc arc : allArcs) {
			if (!arc.getHead().getProcletID().equals(arc.getTail().getProcletID())) {
				List item = new ArrayList();
				// ProcletPort
				item.add(pmodelsInst.getIncomingPort(arc));
				// tail procletid
				item.add(arc.getHead().getProcletID());
				// head procletid
				item.add(arc.getTail().getProcletID());
				combis.add(item);
			}
		}
		// check for duplicates
		boolean err = false;
		for (List combi : combis) {
			for (List combi2 : combis) {
				if ( ((ProcletPort)combi.get(0)).getPortID().equals(((ProcletPort) combi2.get(0)).getPortID()) &&
						combi.get(1).equals(combi2.get(1)) && 
						!combi.get(2).equals(combi2.get(2))) {
					ProcletPort port = (ProcletPort) combi.get(0);
					if (port.getMultiplicity().equals(Signature.ONE) || 
							port.getMultiplicity().equals(Signature.QUEST)) {
						myLog.debug("combi1:" + combi);
						myLog.debug("combi2:" + combi2);
						err = true;
					}
				}
			}
		}
		if (!err) {
			myLog.debug("return:true");
			return true;
		}
		else {
			myLog.debug("return:false");
			return false;
		}
	}
	
	public void commitGraphs () {
		// put temp graphs for old ones
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionGraph> tempGraphs = igraphs.getTempGraphs();
		for (InteractionGraph tempGraph : tempGraphs) {
			EntityMID emid = tempGraph.getEntityMID();
			String newValue = emid.getValue().replace("TEMP", "");
			// find graph with that value
			InteractionGraph graph = igraphs.getGraph(new EntityMID(newValue));
			igraphs.removeGraph(graph);
			emid.setEmid(newValue);
		}
		int maxInt = this.maxSubIDInteractionGraphs();
		int counter = maxInt+1;
		List<InteractionArc> allArcs = igraphs.getAllArcs();
		// put new unique id to arcs
		for (InteractionArc arc : allArcs) {
			// 19022010
			if (arc.getArcState().equals(ArcState.UNPRODUCED) || arc.getArcState().equals(ArcState.EXECUTED_NONE)) {
				arc.getEntityID().getEsid().setEsid(Integer.toString(counter));
				// remove the temp from the emid
				arc.getEntityID().getEmid().setEmid(arc.getEntityID().getEmid().getValue().replace("TEMP", ""));
				counter++;
			}
		}
		
	}
	
	public static void sendPerformatives(boolean exception, WorkItemRecord wir) {
		String oldValue = "";
		if (exception) {
			oldValue = wir.getTaskID();
			wir.setTaskID("exception");
		}
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<List> relationExts = BlockFO.calculateRelations(wir);
		myLog.debug("relationExts:" + relationExts);
		igraphs.updateGraphPerfOut(relationExts);
		// send performatives
		List<Performative> perfs =BlockFO.calcPerformativesOut(wir);
		myLog.debug("perfs:" + perfs);
		if (exception) {
			wir.setTaskID(oldValue);
		}
		SingleInstanceClass sic = SingleInstanceClass.getInstance();
		myLog.debug("notifyPerformativeListeners");
		sic.notifyPerformativeListeners(perfs);
	}
	
	private int maxSubIDInteractionGraphs () {
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		int maxInt = 0;
		for (InteractionGraph graph : igraphs.getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				int esid = 0;
				if (!arc.getEntityID().getEsid().getValue().equals("")) {
					esid = Integer.parseInt(arc.getEntityID().getEsid().getValue());
				}
				if (esid > maxInt) {
					maxInt = esid;
				}
			}
		}
		return maxInt;
	}
	
	public List<List<List>> generateNextOptions (boolean exception) {
		myLog.debug("GENERATE NEXT OPTIONS");
		myLog.debug("wir:" + wir.toString());
		List<List> resultsCR = new ArrayList<List>();
		List<List> resultsNCR = new ArrayList<List>();
		ProcletModels pmodels = ProcletModels.getInstance();
		PortConnections pconns = PortConnections.getInstance();
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		InteractionGraph graph = igraphs.getTempGraph(emid);
		// limit options
		List<InteractionNode> nodesRel = new ArrayList<InteractionNode>();
		List<String> actCases = ProcletService.getInstance().getRunningCaseIDs();

		myLog.debug("actCases:" + actCases);
		myLog.debug("nodes:" + graph.getNodes());
		for (InteractionNode node : graph.getNodes()) {
			myLog.debug("considering node:" + node);
			for (String actCase : actCases) {
				if (node.getProcletID().equals(actCase) && !node.getBlockID().equals("exception")) {
					// relevant node
					nodesRel.add(node);
					break;
				}
			}
			if (Integer.parseInt(node.getProcletID())<0) {
				// also relevant
				nodesRel.add(node);
			}
			if (exception) {
				if (node.getBlockID().equals("exception") && node.getProcletID().equals(wir.getCaseID())) {
					// exception node for current wir
					myLog.debug("exception node relevant");
					nodesRel.add(node);
				}
			}
		}
		myLog.debug("nodesRel:" + nodesRel);
		// first for outgoing ports
		for (InteractionNode node : nodesRel) {
			myLog.debug("node:" + node);
			ProcletBlock block = pmodels.getBlockForInteractionNode(node);
			// 19012010
			// if block does not exists then ignore!
			if (block != null) {
				// for each outgoing port
				List<ProcletPort> ports = pmodels.getPortsBlock(node);
				for (ProcletPort port : ports) {
					if (port.getDirection().equals(Direction.OUT)) {
						// outgoing port, get pconn
						for (PortConnection pconn : pconns.getPortConnections()) {
							if (pconn.getIPort().getPortID().equals(port.getPortID())) {
								// have pconn
								ProcletPort otherPort = pconn.getOPort();
								// get corresponding block, search in all procletmodels
								List<ProcletModel> models = pmodels.getProcletClasses();
								for (ProcletModel pmodel : models) {
									for (ProcletBlock blockOther : pmodel.getBlocks()) {
										for (ProcletPort portOtherProclet : pmodel.getPortsBlock(blockOther)) {
											if (portOtherProclet.getDirection().equals(Direction.IN) && 
													portOtherProclet.getPortID().equals(otherPort.getPortID())) {
												// have the port and block and class
												List result = new ArrayList();
												result.add(pmodel.getClassID());
												result.add(blockOther.getBlockID());
												result.add(port.getCardinality());
												result.add(node);
												if (blockOther.isCreate()) {
													resultsCR.add(result);
												}
												else {
													resultsNCR.add(result);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			else {
				// block is null, generate a warning
				myLog.debug("there does not exist a block for node:" + node);
				System.out.println("There does not exist a block for node:" + node);
			}
		}
		myLog.debug("resultsCR:" + resultsCR);
		// calculate in proclet
		for (InteractionNode node : nodesRel) {
			ProcletBlock block = pmodels.getBlockForInteractionNode(node);
			if (block != null) {
				if (block.getBlockType().equals(BlockType.PI)) {
					// brels, get corresponding model
					ProcletModel pmodel = pmodels.getProcletClass(node.getClassID());
					for (BlockRel blockRel : pmodel.getBRels()) {
						if (blockRel.getIBlock().getBlockID().equals(block.getBlockID())) {
							// found the brel
							List result = new ArrayList();
							result.add(pmodel.getClassID());
							result.add(blockRel.getOBlock().getBlockID());
							result.add(Signature.ZERO);
							result.add(node);
							// only add if not already in
							// 18012010
							boolean check = false;
							for (List res : resultsCR) {
								if (((String)res.get(0)).equals(pmodel.getClassID()) && 
										((String)res.get(1)).equals(blockRel.getOBlock().getBlockID()) && 
										((Signature)res.get(2)).equals(Signature.ZERO) && 
										((InteractionNode)res.get(3)).toString().equals(node.toString())) {
									check = true;
									break;
								}
							}
							if (!check) {
								resultsCR.add(result);
							}
						}
					}
				}
			}
			else {
				// block is null
				myLog.debug("there does not exist a block for node:" + node);
				System.out.println("There does not exist a block for node:" + node);
			}
		}
		List<List<List>> finalResult = new ArrayList();
		finalResult.add(resultsCR);
		myLog.debug("resultsCR:" + resultsCR);
		finalResult.add(resultsNCR);
		return finalResult;
	}
	
	public List<List> determineOptionsNonCrBlocks(List<List> options) {
		myLog.debug("determineOptionsNonCrBlocks");
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<List> results = new ArrayList<List>();

		List<String> pidsList = ProcletService.getInstance().getSpecURIsForRunningCases();
		List<String> cidsList = ProcletService.getInstance().getRunningCaseIDs();
		myLog.debug("options:" + options);
		for (List option : options) {
			for (int i=0; i<pidsList.size();i++) {
				String pid = pidsList.get(i);
				String cid = cidsList.get(i);
				if ( ((String) option.get(0)).equals(pid) && !cid.contains(".") ) {
					List result = new ArrayList();
					result.add(option.get(0));
					result.add(cid);
					result.add(option.get(1));
					result.add(option.get(2));
					result.add(option.get(3));
					results.add(result);
				}
			}
			// getProcletIDsFromGraph
			for (InteractionGraph graph : igraphs.getGraphs()) {
				if (graph.getEntityMID().getValue().equals(emid.getValue()+"TEMP")) {
					for (InteractionNode node : graph.getNodes()) {
						if (node.getClassID().equals( ((String) option.get(0)))) {
							if (cidsList.contains(node.getProcletID()) || 
									Integer.parseInt(node.getProcletID()) < 0) {
								// additional check if case still exists
								List result = new ArrayList();
								result.add(option.get(0));
								result.add(node.getProcletID());
								result.add(option.get(1));
								result.add(option.get(2));
								result.add(option.get(3));
								// check if not already in
								boolean exists = false;
								for (List resultCheck : results) {
									if ( ((String) resultCheck.get(0)).equals( (String) result.get(0)) && 
											((String) resultCheck.get(1)).equals( (String) result.get(1)) && 
											((String) resultCheck.get(2)).equals( (String) result.get(2)) && 
											((Signature) resultCheck.get(3)).equals( (Signature) result.get(3)) && 
											resultCheck.get(4) ==  result.get(4)) {
										exists = true;
									}
								}
								if (!exists) {
									results.add(result);
								}
							}
						}
					}
				}
			}
		}
		return results;
	}
	
	public static void sendOptionsToDB(List<List<List>> options) {
		List<List> cr = options.get(0);
		List<List> ncr = options.get(1);
        for (List option : cr) {
            Signature signature = (Signature) option.get(2);
      		InteractionNode node = (InteractionNode) option.get(3);
            DBConnection.insert(new StoredOptions((String) option.get(0), (String) option.get(1),
                    signature.toString(), node.getClassID(), node.getProcletID(),
            		node.getBlockID()));
        }
        for (List option : ncr) {
        	Signature signature = (Signature) option.get(3);
        	InteractionNode node = (InteractionNode) option.get(4);
            DBConnection.insert(new StoredOptions((String) option.get(0),
                    (String) option.get(1), (String) option.get(2), signature.toString(),
        			node.getClassID(), node.getProcletID(),	node.getBlockID()));
        }
	}
	
	public static List<List<List>> getOptionsFromDB() {
		List<List> cr = new ArrayList<List>();
		List<List> intern = new ArrayList<List>();
		List<List> fragment = new ArrayList<List>();
        List items = DBConnection.getObjectsForClass("StoredOptions");
        for (Object o : items) {
            StoredOptions item = (StoredOptions) o;
            if (item.isCr()) {
                if (Signature.valueOf(item.getSign()).equals(Signature.ZERO)) {
                    intern.add(item.getInternList());
                }
                else {
                    cr.add(item.getCrList());
                }
            }
            else {
                fragment.add(item.getFragmentList());
            }
        }

		List<List<List>> result = new ArrayList();
		result.add(cr);
		result.add(intern);
		result.add(fragment);
		return result;
	}
	
	public static void deleteOptionsFromDB() {
		DBConnection.deleteAll("StoredOptions");
	}
	
	public static void sendDecisionsToDB(List<List> decisions) {
        for (List decision : decisions) {
            DBConnection.insert(new StoredDecisions(decision));
        }
	}
	
	public static List<List> getDecisionsFromDB() {
		List<List> results = new ArrayList<List>();
        List items = DBConnection.getObjectsForClass("StoredDecisions");
        for (Object o : items) {
            results.add(((StoredDecisions) o).getDecisionsAsList());
        }
		return results;
	}
	
	public static void deleteDecisionsFromDB() {
		DBConnection.deleteAll("StoredDecisions");
	}
		
	
	public void extendGraph (List<List> choices) throws Exception {
		myLog.debug("EXTENDGRAPH");
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		InteractionGraph graph = igraphs.getTempGraph(emid);
		for (List choice : choices) {
			int dec = (Integer) choice.get(6);
			if (dec == 0) {
				 // CR
				String destClassID = (String) choice.get(0);
				String destProcletID = "";
				String destBlockID = (String) choice.get(1);
				int number = Integer.parseInt( (String) choice.get(2));
				String sourceClassID = (String) choice.get(3);
				String sourceProcletID = (String) choice.get(4);
				String sourceBlockID = (String) choice.get(5);
				myLog.debug("dec == 0");
				myLog.debug("destClassID:" + destClassID);
				myLog.debug("destProcletID:" + destProcletID);
				myLog.debug("destBlockID:" + destBlockID);
				myLog.debug("number:" + number);
				myLog.debug("sourceClassID:" + sourceClassID);
				myLog.debug("sourceProcletID:" + sourceProcletID);
				myLog.debug("sourceBlockID:" + sourceBlockID);
				//
				for (int i=number;i>0;i--) {
					boolean sd = false;
					ProcletModels pmodelsInst = ProcletModels.getInstance();
					ProcletBlock destBlock = pmodelsInst.getProcletBlock(destClassID, destBlockID);
					if (destBlock.getBlockType().equals(BlockType.FO)) {
						// same procletid
						sd = true;
						destProcletID = sourceProcletID;
					}
					else {
						destProcletID = this.uniqueID;
						// raise uniqueID
						int destProcletIDid = Integer.parseInt(this.uniqueID);
						destProcletIDid--;
						this.uniqueID = Integer.toString(destProcletIDid);
					}
					// node exists
					boolean nodeEx = graph.nodeExists(destClassID, destProcletID, destBlockID);
					boolean arcEx = graph.arcExists(sourceClassID,sourceProcletID,sourceBlockID,destClassID,destProcletID,destBlockID);
					// add node if needed
					if (!nodeEx) {
						InteractionNode newNode = new InteractionNode(destClassID, destProcletID, destBlockID);
						graph.addNode(newNode);
					}
					// add arc if needed
					if (!arcEx) {
						InteractionNode tail = graph.getNode(sourceClassID,sourceProcletID,sourceBlockID);
						InteractionNode head = graph.getNode(destClassID, destProcletID, destBlockID);
						ArcState as = null;
						if (sd) {
							as = ArcState.EXECUTED_NONE;
						}
						else {
							as = ArcState.UNPRODUCED;
						}
						// 09032010
						// get rid of temp
						EntityID eid = new EntityID(graph.getEntityMID().getValue().replace("TEMP", ""),"");
						InteractionArc newArc = new InteractionArc(tail,head,eid,as);
						graph.addArc(newArc);
					}
					// 19022010'
					if (arcEx && sd) {
						// arcExists and in same proclet instance
						// check if state may be overwritten
						InteractionArc arc = graph.getArc(sourceClassID, sourceProcletID, sourceBlockID, destClassID, destProcletID, destBlockID); 
						if (arc != null && arc.getArcState().equals(ArcState.EXECUTED_BOTH)) {
							arc.setArcState(ArcState.EXECUTED_NONE);
						}
					}
				}
			}
			else if (dec == 1) {
				myLog.debug("dec == 1");
				// NCR
				String destClassID = (String) choice.get(0);
				String destProcletID = (String) choice.get(1);
				String destBlockID = (String) choice.get(2);
				String sourceClassID = (String) choice.get(3);
				String sourceProcletID = (String) choice.get(4);
				String sourceBlockID = (String) choice.get(5);
				myLog.debug("destClassID:" + destClassID);
				myLog.debug("destProcletID:" + destProcletID);
				myLog.debug("destBlockID:" + destBlockID);
				myLog.debug("sourceClassID:" + sourceClassID);
				myLog.debug("sourceProcletID:" + sourceProcletID);
				myLog.debug("sourceBlockID:" + sourceBlockID);
				// node exists
				boolean nodeEx = graph.nodeExists(destClassID, destProcletID, destBlockID);
				boolean arcEx = graph.arcExists(sourceClassID,sourceProcletID,sourceBlockID,destClassID,destProcletID,destBlockID);
				boolean arcReplAllowed = true;
				if (arcEx && graph.getArc(sourceClassID,sourceProcletID,sourceBlockID,destClassID,destProcletID,destBlockID).equals(ArcState.SENT)) {
					arcReplAllowed = false;
				}
				// add node if needed
				if (!nodeEx) {
					InteractionNode newNode = new InteractionNode(destClassID, destProcletID, destBlockID);
					graph.addNode(newNode);
				}
				// add arc if needed
				if (!arcEx) {
					InteractionNode tail = graph.getNode(sourceClassID,sourceProcletID,sourceBlockID);
					InteractionNode head = graph.getNode(destClassID, destProcletID, destBlockID);
					EntityID eid = new EntityID(graph.getEntityMID().getValue(),"");
					InteractionArc newArc = null;
					if (tail.getProcletID().equals(head.getProcletID())) {
						// internal interaction
						newArc =  new InteractionArc(tail,head,eid,ArcState.EXECUTED_NONE);
					}
					else {
						newArc =  new InteractionArc(tail,head,eid,ArcState.UNPRODUCED);
					}
					graph.addArc(newArc);
				}
				else {
					// arc existing
					if (arcReplAllowed) {
						InteractionArc arc = graph.getArc(sourceClassID,sourceProcletID,sourceBlockID,destClassID,destProcletID,destBlockID);
						arc.setArcState(ArcState.UNPRODUCED);
					}
				}
			}
			else {
				throw new Exception("too many fields for an option");					
			}
		}
	}
}
