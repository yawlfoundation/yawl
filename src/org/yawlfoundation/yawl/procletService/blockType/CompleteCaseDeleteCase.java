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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import java.util.ArrayList;
import java.util.List;

public class CompleteCaseDeleteCase {

	private String procletID = "";
	
	private Logger myLog = LogManager.getLogger(CompleteCaseDeleteCase.class);
	
	public CompleteCaseDeleteCase (String procletID) {
		this.procletID = procletID;
	}
	
	public String getClassIDfromGraphs () {
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getGraphs()) {
			for (InteractionNode node : graph.getNodes()) {
				if (node.getProcletID().equals(this.procletID)) {
					return node.getClassID();
				}
			}
		}
		return "";
	}
	
	public void removeFromGraphs (String procletID) {
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getGraphs()) {
			List<InteractionNode> remNodes = new ArrayList<InteractionNode> ();
			for (InteractionNode node : graph.getNodes()) {
				if (node.getProcletID().startsWith(procletID)) {
					remNodes.add(node);
				}
			}
			// remove from graph
			for (InteractionNode node : remNodes) {
				graph.deleteNode(node);
			}
		}
	}
	
	// first calculateFailingArcs before REMOVAL!
	public List<InteractionArc> calcFailingArcs () {
		myLog.debug("CALCFAILINGARCS");
		List<InteractionArc> failingArcs = new ArrayList<InteractionArc> ();
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		for (InteractionGraph graph : igraphs.getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				myLog.debug("arc:" + arc);
				if (arc.getHead().getProcletID().equals(this.procletID)  
						&& (arc.getArcState().equals(ArcState.UNPRODUCED) ||
								arc.getArcState().equals(ArcState.SENT))) {
					failingArcs.add(arc);
				}
				if (arc.getTail().getProcletID().equals(this.procletID) &&
						arc.getArcState().equals(ArcState.UNPRODUCED)) {
					failingArcs.add(arc);
				}
				if (arc.getTail().getProcletID().equals(this.procletID) 
						&& (arc.getArcState().equals(ArcState.EXECUTED_NONE) ||
								arc.getArcState().equals(ArcState.EXECUTED_SOURCE))
						&& arc.getHead().getProcletID().equals(this.procletID)) {
					failingArcs.add(arc);
				}
			}
		}
		return failingArcs;
	}
	
	public List<EntityMID> emidsFromArcsNoDupl(List<InteractionArc> arcs) {
		List<EntityMID> emids = new ArrayList<EntityMID>();
		for (InteractionArc arc : arcs) {
			EntityMID emid = arc.getEntityID().getEmid();
			boolean exists = false;
			for (EntityMID check : emids) {
				if (check.getValue().equals(emid.getValue())) {
					// already exists
					exists = true;
				}
			}
			if (!exists) {
				emids.add(emid);
			}
		}
		return emids;
	}
	
	public List<Object> exceptionCase (List<EntityMID> emids, String classID) {
		List<Object> returnList = new ArrayList<Object>();

		// get exception block
		ProcletModels pmodels = ProcletModels.getInstance();
		ProcletModel pmodel = pmodels.getProcletClass(classID);
		ProcletBlock blockExc = pmodel.getBlock("exception");
		WorkItemRecord wir = new WorkItemRecord(procletID,"exception",classID,"","");

		returnList.add(emids);
		returnList.add(wir);
		returnList.add(blockExc);
		return returnList;
	}
	
	public void removalCaseCompletionCase () {
		// check first if deletion is allowed
		while (SingleInstanceClass.getInstance().isCaseBlocked(procletID)) {
			try {
				Thread.sleep(500);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		SingleInstanceClass.getInstance().blockCase(procletID);
		String classID = this.getClassIDfromGraphs();
		List<InteractionArc> failingArcs = this.calcFailingArcs();

			// set state of failing arcs to FAILED
		for (InteractionArc arc : failingArcs) {
			arc.setArcState(ArcState.FAILED);
		}
		InteractionGraphs.getInstance().persistGraphs();

		myLog.debug("failing arcs:" + failingArcs);
		if (failingArcs.size() > 0) {
			myLog.debug("work needs to be done");
			List<EntityMID> emids = this.emidsFromArcsNoDupl(failingArcs);
			myLog.debug("emids:" + emids);
			List exceptionCase = this.exceptionCase(emids, classID);
			myLog.debug("exception case:" + exceptionCase);
			publishException(exceptionCase);

			// take on from here
			myLog.debug("handleException:" + classID + "," + this.procletID + "," + emids);
			handleException(classID,this.procletID,"exception",emids); 

			// persist the graphs
			InteractionGraphs igraphsInst = InteractionGraphs.getInstance();
			igraphsInst.persistGraphs();
		}
		else {
			// nothing to be done
			myLog.debug("nothing to be done");

		    // only remove nodes from the graph
			InteractionGraphs.getInstance().persistGraphs();
			myLog.debug("no affected emids because of completion case / removal case");
		}

		// remove the temp graphs again
		InteractionGraphs.getNewInstance().deleteTempGraphs();
		InteractionGraphs.getNewInstance().deleteTempGraphsFromDB();
		SingleInstanceClass.getInstance().unblockCase(procletID);
	}
	
	private void handleException(String classID, String procletID, String blockID, List<EntityMID> emids) {
		WorkItemRecord wir = null;
		boolean ignore = false;
		while (true) {
			try {
				Thread.sleep(500);
				if (isExceptionCaseSelectedUser(classID, procletID, blockID)) {
					break;
				}
				else if (isExceptionCaseSelectedUser("none", "none", blockID)) {
					ignore = true;
					break;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean firstPass = false;
		if (!emids.isEmpty() && !ignore) {

			// first connect
			Trigger trigger = new Trigger();
			trigger.initiate();
			while (true) {
				String selectedEmidStr = "";
				if (!firstPass) {
					deleteAvailableEmidsCaseExceptionToUser(classID, procletID, blockID);
					pushAvailableEmidsCaseExceptionToUser(classID, procletID, blockID, emids);
				}
				trigger.send("BLAAT3");

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
					wir = new WorkItemRecord(procletID,blockID,classID,"","");
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

						// get answer back which is either commit or finish selection
						String userDecision = trigger.receive();

						// transfer to user if ok then continue else start all over again
						if (userDecision.equals("commit")) {

							// commit button
							boolean checks = pemid.doChecks();
							if (checks) {
								// checks ok
								deleteEmidCaseExceptionToUser(selectedEmidStr);

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

								// remove CP nodes from the interaction graph of the entity which have
								// no incoming or outgoing arcs
								for (InteractionGraph graph : igraphs.getGraphs()) {
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
							}
                            else {
								// user continues or graphs are wrong
								// put old graphs back
								InteractionGraphs igraphs = InteractionGraphs
										.getInstance();
								igraphs.deleteTempGraphs();
								igraphs.deleteTempGraphsFromDB();
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
				}
                else {
					myLog.debug("EXIT received");
					// "EXIT" received -> done - delete everything from DB
					InteractionGraphs.getInstance().commitTempGraphs();
					ProcessEntityMID.sendPerformatives(true,wir);
					ProcessEntityMID.deleteOptionsFromDB();
					ProcessEntityMID.deleteDecisionsFromDB();
					trigger.close();
					CompleteCaseDeleteCase.deleteExceptionCaseSelected(classID, procletID, blockID);
					CompleteCaseDeleteCase.deleteAllEmidsCaseExceptionToUser();
					break;
				}
			} // while true
		} // end emids
		if (ignore) {
			CompleteCaseDeleteCase.deleteExceptionCaseSelected("none", "none", "exception");
		}
	}
	
	public static void publishException (List ec) {
		List<EntityMID> emids = (List<EntityMID>) ec.get(0);
		String emidsStr = "";
		for (EntityMID emid : emids) {
			emidsStr = emidsStr + emid.getValue() + ",";
		}
		emidsStr = emidsStr.substring(0, emidsStr.length()-1);
        DBConnection.insert(new StoredItem((WorkItemRecord) ec.get(1), emidsStr,
            Item.ExceptionCase));
	}
	
	public static void deleteException (String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID,
                Item.ExceptionCase);
        if (item != null) DBConnection.delete(item);
	}

    public static List getExceptions () {
        List resultFin = new ArrayList();
        List items = DBConnection.getStoredItems(Item.ExceptionCase);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            String emidsStr = item.getEmid();

            // split the string
            List<EntityMID> emids = new ArrayList<EntityMID>();
            String[] split = emidsStr.split(",");
            for (String t : split) {
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
	
	public static List<InteractionNode> getExceptionCasesSelected () {
		List<InteractionNode> nodes = new ArrayList<InteractionNode>();
        List items = DBConnection.getStoredItems(Item.ExceptionCaseSelection);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            if (item.isSelected()) {
                nodes.add(item.newInteractionNode());
            }
        }
		return nodes;
	}
	
	public static void publishExceptionCase(String classID, String procletID, String blockID) {
        StoredItem item = new StoredItem(classID, procletID, blockID,
                Item.ExceptionCaseSelection);
        item.setSelected(true);
        DBConnection.insert(item);
	}
	
	public static boolean isExceptionCaseSelectedUser(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getSelectedStoredItem(classID, procletID, blockID,
                Item.ExceptionCaseSelection);
        return (item != null);
	}
	
	public static void setExceptionCaseSelected(String classID, String procletID, String blockID) {
		DBConnection.setStoredItemSelected(classID, procletID, blockID,
                Item.ExceptionCaseSelection);
	}
	
	public static InteractionNode getExceptionCaseSelected() {
        List items = DBConnection.getStoredItems(Item.ExceptionCaseSelection);
        for (Object o : items) {
            if (((StoredItem) o).isSelected()) {
                return ((StoredItem) o).newInteractionNode();
            }
        }
		return null;
	}
	
	public static void deleteExceptionCaseSelected(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID, 
                Item.ExceptionCaseSelection);
        if (item != null) DBConnection.delete(item);
	}
	
	private void pushAvailableEmidsCaseExceptionToUser(String classID, String procletID,
                                                       String blockID, List<EntityMID> emids) {
	    for (EntityMID emid : emids) {
            DBConnection.insert(new StoredItem(classID, procletID, blockID, emid.getValue(),
                    Item.EmidExceptionCaseSelection));
        }
	}
	
	private void deleteEmidCaseExceptionToUser(String emidStr) {
        String query = "delete from StoredItem as s where s.emid='" + emidStr +
                "' and s.itemType=" + Item.EmidExceptionCaseSelection.ordinal();
        DBConnection.execUpdate(query);
	}
	
	public static void deleteAllEmidsCaseExceptionToUser() {
		DBConnection.deleteAll(Item.EmidExceptionCaseSelection);
	}
	
	private void deleteAvailableEmidsCaseExceptionToUser(String classID, String procletID, String blockID) {
        StoredItem item = DBConnection.getStoredItem(classID, procletID, blockID,
                Item.EmidExceptionCaseSelection);
        if (item != null) DBConnection.delete(item);
	}
	
	public static List<EntityMID> getAvailableEmidsCaseExceptionToUser() {
		List<EntityMID> emidList = new ArrayList<EntityMID>();
        List items = DBConnection.getStoredItems(Item.EmidExceptionCaseSelection);
        for (Object o : items) {
            emidList.add(((StoredItem) o).newEntityMID());
        }
		return emidList;
	}
	
	public static void setEmidSelectedCaseException(EntityMID emid) {
        List items = DBConnection.getStoredItems(Item.EmidExceptionCaseSelection);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            if (item.getEmid().equals(emid.getValue())) {
                item.setSelected(true);
                DBConnection.update(item);
            }
        }
	}

	
	public static void main(String [] args) { 
//		String test = "1";
//		String[] split = test.split(",");
//		for (int i=0; i<split.length; i++) {
//			String t = split[i];
//		}
		CompleteCaseDeleteCase.deleteException("visit","p2","exception");
		List exc = CompleteCaseDeleteCase.getExceptions();
		System.out.println();
	}
	
}
