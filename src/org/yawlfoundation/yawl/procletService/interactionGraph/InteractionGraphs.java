/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.procletService.interactionGraph;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc.ArcState;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock.BlockType;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.Item;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InteractionGraphs {
	
	private String interactionGraphTN = "interactiongraph";
	private String interactionGraphTNfieldEmid = "emid";
	
	private String interactionArcTN = "interactionarc";
	
	private static InteractionGraphs igraphs = null;
	private List<InteractionGraph> igraphsList = new ArrayList<InteractionGraph> ();
	
	private Logger myLog = LogManager.getLogger(InteractionGraphs.class);
	
	private InteractionGraphs () {
		
	}
	
	public static InteractionGraphs getInstance() {
		if (igraphs == null) {
			igraphs = new InteractionGraphs ();
			igraphs.buildFromDB();
		}
		return igraphs;
	}
	
	public static InteractionGraphs getNewInstance() {
		igraphs = new InteractionGraphs ();
		igraphs.buildFromDB();
		return igraphs;
	}
	
	public void reset() {
		igraphs = new InteractionGraphs();
	}
	
	public void addGraph(InteractionGraph graph) {
		if (!igraphsList.contains(graph)) {
			igraphsList.add(graph);			
		}
	}
	
	public void removeGraph (InteractionGraph graph) {
		this.igraphsList.remove(graph);
	}
	
	public List<InteractionGraph> getGraphs() {
		return this.igraphsList;
	}
	
	public List<InteractionGraph> getTempGraphs() {
		List<InteractionGraph> graphs = new ArrayList<InteractionGraph>();
		for (InteractionGraph graph : this.getGraphs()) {
			if (graph.getEntityMID().getValue().contains("TEMP")) {
				graphs.add(graph);
			}
		}
		return graphs;
	}
	
	public InteractionGraph getGraph(EntityMID emid) {
		for (InteractionGraph graph : this.getGraphs()) {
			if (graph.getEntityMID().getValue().equals(emid.getValue())) {
				return graph;
			}
		}
		return null;
	}
	
	public InteractionGraph getTempGraph(EntityMID emid) {
		for (InteractionGraph graph : this.getGraphs()) {
			if (graph.getEntityMID().getValue().equals(emid.getValue() + "TEMP")) {
				return graph;
			}
		}
		return null;
	}
	
	public void createTempGraphs() {
		List<InteractionGraph> tempList = new ArrayList<InteractionGraph>();
		for (InteractionGraph graph : this.getGraphs()) {
			tempList.add(graph);
		}
		for (InteractionGraph graph : tempList) {
			this.createTempGraph(graph.getEntityMID());
		}
	}
	
	public void deleteTempGraphs() {
		List<InteractionGraph> remGraphs = new ArrayList<InteractionGraph>();
		for (InteractionGraph graph : this.getGraphs()) { 
			if (graph.getEntityMID().getValue().contains("TEMP")) {
				remGraphs.add(graph);
			}
		}
		for (InteractionGraph graph : remGraphs) {
			this.removeGraph(graph);
		}
	}
	
	public void createTempGraph(EntityMID emid) {
		myLog.debug("CREATE TEMP GRAPH");
		InteractionGraph graph = this.getGraph(emid);
		InteractionGraph tempGraph = new InteractionGraph(new EntityMID(graph.getEntityMID().getValue() + 
				"TEMP"));
		if (graph != null) {
			// create a temporary graph for this one
			// nodes
			for (InteractionNode node : graph.getNodes()) {
				InteractionNode newNode = new InteractionNode(node.getClassID(),
						node.getProcletID(),node.getBlockID()); 
				tempGraph.addNode(newNode);
				myLog.debug("add node:" + node.getClassID() + "," + node.getProcletID() + "," + node.getBlockID());
			}
			// arcs
			for (InteractionArc arc : graph.getArcs()) {
				myLog.debug("considering arc:" + arc);
				InteractionNode tailNode = null;
				InteractionNode headNode = null;
				for (InteractionNode node : tempGraph.getNodes()) {
					if (node.getClassID().equals(arc.getTail().getClassID()) && 
							node.getProcletID().equals(arc.getTail().getProcletID()) && 
							node.getBlockID().equals(arc.getTail().getBlockID())) {
						tailNode = node;
						myLog.debug("tail has been set");
					}
					if (node.getClassID().equals(arc.getHead().getClassID()) && 
							node.getProcletID().equals(arc.getHead().getProcletID()) && 
							node.getBlockID().equals(arc.getHead().getBlockID())) {
						headNode = node;
						myLog.debug("head has been set");
					}
				}
				InteractionArc newArc = new InteractionArc(tailNode,headNode,
						new EntityID(arc.getEntityID().getEmid().getValue(),
								arc.getEntityID().getEsid().getValue()),
						arc.getArcStateFromString(arc.getArcState().toString()));
				tempGraph.addArc(newArc);
				myLog.debug("arc has been added to tempgraph:" + newArc);
			}
			this.addGraph(tempGraph);
		}
	}
	
	public List<InteractionArc> getAllArcs () {
		List<InteractionArc> allArcs = new ArrayList<InteractionArc> ();
		for (InteractionGraph graph : this.getGraphs()) {
			allArcs.addAll(graph.getArcs());
		}
		return allArcs;
	}
	
	// 19022010
	public void updateGraphPI(String classID, String procletID, String blockID) {
		myLog.debug("UPDATEGRAPHPI:" + classID + "," + procletID + "," + blockID);
		for (InteractionGraph graph : this.getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				myLog.debug("considering arc:" + arc);
				InteractionNode head = arc.getHead();
				InteractionNode tail = arc.getTail();
				if (head.getClassID().equals(tail.getClassID()) && 
						head.getProcletID().equals(tail.getProcletID()) && 
						tail.getClassID().equals(classID) && 
						tail.getProcletID().equals(procletID) && 
						tail.getBlockID().equals(blockID)) {
					myLog.debug("first check");
					// check the types for them
					ProcletBlock blockHead = ProcletModels.getInstance().getBlockForInteractionNode(head);
					ProcletBlock blockTail = ProcletModels.getInstance().getBlockForInteractionNode(tail);
					if (blockHead != null && blockTail != null && 
							blockTail.getBlockType().equals(BlockType.PI) && 
							blockHead.getBlockType().equals(BlockType.FO)) {
						myLog.debug("second chek");
						// found an internal interaction arc
						// change the arc state
						arc.setArcState(ArcState.EXECUTED_SOURCE);
					}
				}
			}
		}
	}
	
	// 19022010
	public void updateGraphFO(String classID, String procletID, String blockID) {
		myLog.debug("UPDATEGRAPHFO:" + classID + "," + procletID + "," + blockID);
		for (InteractionGraph graph : this.getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				myLog.debug("considering arc:" + arc);
				InteractionNode head = arc.getHead();
				InteractionNode tail = arc.getTail();
				if (head.getClassID().equals(tail.getClassID()) && 
						head.getProcletID().equals(tail.getProcletID()) && 
						head.getClassID().equals(classID) && 
						head.getProcletID().equals(procletID) && 
						head.getBlockID().equals(blockID)) {
					myLog.debug("first check");
					// check the types for them
					ProcletBlock blockHead = ProcletModels.getInstance().getBlockForInteractionNode(head);
					ProcletBlock blockTail = ProcletModels.getInstance().getBlockForInteractionNode(tail);
					if (blockHead != null && blockTail != null && 
							blockTail.getBlockType().equals(BlockType.PI) && 
							blockHead.getBlockType().equals(BlockType.FO)) {
						myLog.debug("second check");
						// found an internal interaction arc
						// change the arc state
						if (arc.getArcState().equals(ArcState.EXECUTED_SOURCE)) {
							myLog.debug("third check");
							arc.setArcState(ArcState.EXECUTED_BOTH);
						}
					}
				}
			}
		}
	}
	
	public void updateGraphPerfOut(List<List> relationExts) {
		for (List relationExt : relationExts) {
			EntityMID emid = (EntityMID) relationExt.get(0);
			for (InteractionGraph graph : this.getGraphs()) {
				if (graph.getEntityMID().getValue().equals(emid.getValue())) {
					// update arc
					EntityID eidRel = (EntityID) relationExt.get(1);
					for (InteractionArc arc : graph.getArcs()) {
						if (arc.getEntityID().toString().equals(eidRel.toString())) {
							arc.setArcState(ArcState.SENT);
						}
					}
				}
			}
		}
	}
	
	public void updateGraphPerfIn(List<EntityID> eids) {
		for (EntityID eid : eids) {
			for (InteractionGraph graph : this.getGraphs()) {
				if (graph.getEntityMID().getValue().equals(eid.getEmid().getValue()) ||
						graph.getEntityMID().getValue().equals(eid.getEmid().getValue() + "TEMP")) {
					// update arc
					for (InteractionArc arc : graph.getArcs()) {
						if (arc.getEntityID().toString().equals(eid.toString())) {
							arc.setArcState(ArcState.CONSUMED);
						}
					}
				}
			}
		}
	}
	
	public void updateGraphPerfInFailed(List<EntityID> eids) {
		for (EntityID eid : eids) {
			for (InteractionGraph graph : this.getGraphs()) {
				List<InteractionArc> removeArcs = new ArrayList<InteractionArc>();
				if (graph.getEntityMID().getValue().equals(eid.getEmid().getValue()) || 
						graph.getEntityMID().getValue().equals(eid.getEmid().getValue() + "TEMP")) {
					// arc
					for (InteractionArc arc : graph.getArcs()) {
						if (arc.getEntityID().toString().equals(eid.toString())) {
							//removeArcs.add(arc);
							// set the arcState to Failed
							arc.setArcState(ArcState.FAILED);
						}
					}
				}
				// remove arcs
				for (InteractionArc arc : removeArcs) {
					graph.deleteArc(arc);
				}
			}
		}
		
		
	}
	
	public void updateGraphCaseID(EntityID eid, String newVal, String oldVal) {
		myLog.debug("UPDATEGRAPHCASEID:" + eid + "," + newVal + "," + oldVal);
		// remove temp from eid
		String eidStr = eid.getEmid().getValue();
		eidStr = eidStr.replace("TEMP", "");
		eid.getEmid().setEmid(eidStr);
		myLog.debug("eid now:" + eid);
		for (InteractionGraph graph : this.getGraphs()) {
			myLog.debug("graph:" + graph.getEntityMID());
			if (graph.getEntityMID().getValue().equals(eid.getEmid().getValue())) {
				myLog.debug("in loop");
				// update nodes
				for (InteractionNode node : graph.getNodes()) {
					if (node.getProcletID().equals(oldVal)) {
						node.setProcletID(newVal);
					}
				}
				// update arcs
				for (InteractionArc arc : graph.getArcs()) {
					myLog.debug("considering arc:" + arc);
					if (arc.getHead().getProcletID().equals(newVal)) {
						// arc already changes because node is already changed
						//arc.getHead().setProcletID(newVal);
						myLog.debug("change head");
						// also change arcstate to consumed
						if (arc.getArcState().equals(ArcState.SENT)) {
							myLog.debug("change arc state");
							arc.setArcState(ArcState.CONSUMED);
						}
					}
					if (arc.getTail().getProcletID().equals(oldVal)) {
						arc.getTail().setProcletID(newVal);
					}
				}
			}
		}
	}
	
	public void buildFromDB () {
        List items = DBConnection.execQuery("select distinct s.emid from StoredItem as s " +
                            "where s.itemType=" + Item.InteractionGraph.ordinal());
        for (Object o : items) {
            InteractionGraph graph = new InteractionGraph((String) o);
        	graph.buildFromDB();
        	addGraph(graph);
        }
    }

	public void commitTempGraphs () {
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionGraph> graphsTemp = igraphs.getTempGraphs();
		List<InteractionGraph> graphsToRemove = new ArrayList<InteractionGraph>();
		for (InteractionGraph tempGraph : graphsTemp) {
			EntityMID emidTemp = tempGraph.getEntityMID();
			EntityMID emitWithoutTemp = new EntityMID(emidTemp.getValue().replace("TEMP",""));
			InteractionGraph graphOld = getGraph(emitWithoutTemp);
			graphsToRemove.add(graphOld);
			// rename
			tempGraph.setEntityMID(new EntityMID(emidTemp.getValue().replace("TEMP", "")));
		}
		// remove the removeGraphs
		for (InteractionGraph remGraph : graphsToRemove) {
			removeGraph(remGraph);
		}
		// persist
		this.deleteGraphsFromDB();
		this.deleteTempGraphs();
		this.persistGraphs();
	}
	
	public void commitTempGraphEmid (String emid) {
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionGraph> graphsTemp = igraphs.getTempGraphs();
		List<InteractionGraph> graphsToRemove = new ArrayList<InteractionGraph>();
		for (InteractionGraph tempGraph : graphsTemp) {
			EntityMID emidTemp = tempGraph.getEntityMID();
			EntityMID emidWithoutTemp = new EntityMID(emidTemp.getValue().replace("TEMP",""));
			if (emidWithoutTemp.getValue().equals(emid)) {
				InteractionGraph graphOld = getGraph(emidWithoutTemp);
				graphsToRemove.add(graphOld);
				// rename
				tempGraph.setEntityMID(new EntityMID(emidTemp.getValue().replace("TEMP", "")));
			}
		}
		// remove the removeGraphs
		for (InteractionGraph remGraph : graphsToRemove) {
			removeGraph(remGraph);
		}
		// persist
		this.deleteGraphsFromDB();
		//this.deleteTempGraphs();
		this.persistGraphs();
	}
	
	public synchronized void persistGraphs () {
		List<InteractionGraph> graphs = this.getGraphs();
		for (InteractionGraph graph : graphs) {
			graph.persistProcletModel();
		}
	}
	
	public void deleteGraphsFromDB () {
        DBConnection.deleteAll(Item.InteractionGraph);
        DBConnection.deleteAll("StoredInteractionArc");
	}

	public void deleteTempGraphsFromDB () {
        DBConnection.execUpdate("delete from StoredItem as s where s.emid like '%TEMP' " +
                                "and s.itemType=" + Item.InteractionGraph.ordinal());
        DBConnection.execUpdate("delete from StoredInteractionArc as s where s.emid like '%TEMP'");
	}
	
	
	public static void main(String [] args) {
		JOptionPane.showMessageDialog(null,
			    "Block already exists!",
			    "Error",
			    JOptionPane.ERROR_MESSAGE);
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		igraphs.buildFromDB();
		InteractionGraph graph = igraphs.getGraph(new EntityMID("test"));
		Layout<Integer,String> layout = new CircleLayout(graph);
		layout.setSize(new Dimension(300,300));
		BasicVisualizationServer<Integer,String> vv = new BasicVisualizationServer<Integer,String>(layout);
		vv.setPreferredSize(new Dimension(350,350));
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		JFrame frame = new JFrame("simple");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		System.out.println("done");
	}
}
