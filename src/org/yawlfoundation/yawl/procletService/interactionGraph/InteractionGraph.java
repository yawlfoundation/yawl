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

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc.ArcState;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.Item;
import org.yawlfoundation.yawl.procletService.persistence.StoredInteractionArc;
import org.yawlfoundation.yawl.procletService.persistence.StoredItem;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;
import org.yawlfoundation.yawl.procletService.util.EntitySID;

import java.util.ArrayList;
import java.util.List;

// colset InteractionGraph = product EntityMID * InteractionNodes * InteractionArcs;

public class InteractionGraph extends DirectedSparseGraph{
	
	private EntityMID emid = null;
	
	public InteractionGraph (EntityMID emid) {
		this.emid = emid;
	}
	
	public InteractionGraph (String emid) {
		this.emid = new EntityMID(emid);
	}
	
	public EntityMID getEntityMID () {
		return this.emid;
	}
	
	public void setEntityMID(EntityMID emid) {
		this.emid = emid;
	}
	
	public void addNode (InteractionNode node) {
		if (!this.getVertices().contains(node)) { 
			this.addVertex(node);
		}
	}
	
	public void deleteNode (InteractionNode node) {
		this.removeVertex(node);
		// remove also edges having this node
		List<InteractionArc> removeArcs = new ArrayList<InteractionArc> ();
		List<InteractionArc> arcs = this.getArcs();
		for (InteractionArc arc : arcs) {
			if (arc.getHead().equals(node) || arc.getTail().equals(node)) {
				removeArcs.add(arc);
			}
		}
		for (InteractionArc arc : removeArcs) {
			this.removeEdge(arc);
		}
	}
	
	public List<InteractionNode> getNodes() {
		List<InteractionNode> nodes = new ArrayList<InteractionNode> ();
		for (Object node : this.getVertices()) {
			if (node instanceof InteractionNode) {
				nodes.add((InteractionNode)node);
			}
		}
		return nodes;
	}
	
	public InteractionNode getNode(String classID, String procletID, String blockID) {
		for (InteractionNode node : this.getNodes()) {
			if (node.getClassID().equals(classID) && 
					node.getProcletID().equals(procletID) && 
					node.getBlockID().equals(blockID)) {
				return node;
			}
		}
		return null;
	}
	
	public boolean nodeExists(String classID, String procletID, String blockID) {
		InteractionNode node = this.getNode(classID, procletID, blockID);
		if (node == null) {
			return false;
		}
		return true;
	}
	
	public InteractionArc getArc(String tailClassID, String tailProcletID, String tailBlockID, String headClassID, String headProcletID, String headBlockID) {
		for (InteractionArc arc : this.getArcs()) {
			if (arc.getTail().getClassID().equals(tailClassID) && 
					arc.getTail().getProcletID().equals(tailProcletID) && 
					arc.getTail().getBlockID().equals(tailBlockID) && 
					arc.getHead().getClassID().equals(headClassID) && 
					arc.getHead().getProcletID().equals(headProcletID) && 
					arc.getHead().getBlockID().equals(headBlockID)) {
				return arc;
			}
		}
		return null;
	}
	
	public boolean arcExists(String tailClassID, String tailProcletID, String tailBlockID, String headClassID, String headProcletID, String headBlockID) {
		InteractionArc arc = this.getArc(tailClassID,tailProcletID,tailBlockID,headClassID,headProcletID,headBlockID);
		if (arc == null) {
			return false;
		}
		return true;
	}
	
	public void addArc (InteractionArc arc) {
		List<InteractionNode> nodes = this.getNodes();
		if (nodes.contains(arc.getHead()) && nodes.contains(arc.getTail())) {
			this.addEdge(arc, arc.getTail(), arc.getHead(), EdgeType.DIRECTED);
		}
	}
	
	public void deleteArc (InteractionArc arc) {
		this.removeEdge(arc);
	}
	
	public List<InteractionArc> getArcs () {
		List<InteractionArc> arcs = new ArrayList<InteractionArc> ();
		for (Object obj : this.getEdges()) {
			if (obj instanceof InteractionArc) {
				arcs.add((InteractionArc) obj);
			}
		}
		return arcs;
	}
	
	public List<InteractionArc> getArcsTail(String classID, String procletID, String blockID) {
		List<InteractionArc> arcs = new ArrayList<InteractionArc> ();
		for (Object obj : this.getEdges()) {
			if (obj instanceof InteractionArc) {
				InteractionArc arc = (InteractionArc) obj;
				if (arc.getTail().getClassID().equals(classID) && 
						arc.getTail().getProcletID().equals(procletID) && 
						arc.getTail().getBlockID().equals(blockID)) {
					arcs.add(arc);
				}
			}
		}
		return arcs;
	}
	
	public void deleteAllNodes () {
		List<InteractionNode> nodes = this.getNodes();
		for (InteractionNode node : nodes) {
			this.deleteNode(node);
		}
	}
	
	public void deleteAllArcs () {
		List<InteractionArc> arcs = this.getArcs();
		for (InteractionArc arc : arcs) {
			this.deleteArc(arc);
		}
	}
	
	public boolean buildFromDB() {
		this.deleteAllNodes();
		this.deleteAllArcs();
        List items = DBConnection.getStoredItems(Item.InteractionGraph);
        for (Object o : items) {
            StoredItem item = (StoredItem) o;
            if (item.getEmid().equals(emid.getValue())) {
                addNode(item.newInteractionNode());
            }
        }

        items = DBConnection.getObjectsForClassWhere("StoredInteractionArc", 
                "emid='" + emid.getValue() + "'");
        for (Object o : items) {
            StoredInteractionArc item = (StoredInteractionArc) o;
            InteractionNode tail = getNode(item.getTailClassID(), item.getTailProcletID(),
                    item.getTailBlockID());
            InteractionNode head = getNode(item.getHeadClassID(), item.getHeadProcletID(),
                    item.getHeadBlockID());
            InteractionArc.ArcState astate = InteractionArc.getArcStateFromString(
                    item.getArcState());
       		EntityID eid = new EntityID(emid, new EntitySID(item.getEsid()));
       		InteractionArc arc = new InteractionArc(tail, head, eid, astate);
       		this.addArc(arc);            
        }    
		return true;
	}
	
	public void deleteGraphFromDB () {
        List items = DBConnection.getStoredItems(Item.InteractionGraph);
        for (Object o : items) {
            if (((StoredItem) o).getEmid().equals(emid.getValue())) {
                DBConnection.delete(o);
            }
        }
        
        items = DBConnection.getObjectsForClass("StoredInteractionArc");
        for (Object o : items) {
            if (((StoredInteractionArc) o).getEmid().equals(emid.getValue())) {
                DBConnection.delete(o);
            }
        }
	}
	
	public void persistProcletModel () {
		this.deleteGraphFromDB();
     	for (InteractionNode node : getNodes()) {
            DBConnection.insert(new StoredItem(node.getClassID(), node.getProcletID(),
                    node.getBlockID(), emid.getValue(), Item.InteractionGraph));
        }

		for (InteractionArc arc : getArcs()) {
            DBConnection.insert(new StoredInteractionArc(emid.getValue(),
				arc.getTail().getClassID(),	arc.getTail().getProcletID(),
				arc.getTail().getBlockID(),	arc.getHead().getClassID(),
				arc.getHead().getProcletID(), arc.getHead().getBlockID(),
				arc.getEntityID().getEsid().getValue(),
				arc.getArcState().toString()));
		}
	}
	
	public static void main(String [] args) {
		EntityMID emid = new EntityMID("test");
		EntitySID esid1 = new EntitySID("");
		InteractionGraph graph = new InteractionGraph(emid);
		graph.buildFromDB();
		InteractionNode node1 = new InteractionNode("c1","p1","b1");
		InteractionNode node2 = new InteractionNode("c1","p1","b2");
		graph.addNode(node1);
		graph.addNode(node2);
		EntityID eid = new EntityID(emid,esid1);
		graph.addArc(new InteractionArc(node1,node2,eid,ArcState.UNPRODUCED));
		graph.deleteNode(node1);
		graph.persistProcletModel();
	}
	
}
