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

package org.yawlfoundation.yawl.procletService.models.procletModel;

import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;

import java.util.ArrayList;
import java.util.List;

public class ProcletModels {
	
	private String procletModelTN = "procletmodel";
	private static ProcletModels pModels = null;
	private static List<ProcletModel> pModelsList = new ArrayList<ProcletModel>();
	
	private ProcletModels () {
		
	}
	
	public void addProcletModel (ProcletModel pmodel) {
		if (!this.pModelsList.contains(pmodel)) {
			this.pModelsList.add(pmodel);
		}
	}
	
	public ProcletModel getProcletClass(String classID) {
		for (ProcletModel pmodel : this.pModelsList) {
			if (pmodel.getClassID().equals(classID)) {
				return pmodel;
				
			}
		}
		return null;
	}
	
	public List<ProcletModel> getProcletClasses () {
		return this.pModelsList;
	}
	
	public void deleteProcletModel (ProcletModel pmodel) {
		if (this.pModelsList.contains(pmodel)) {
			this.pModelsList.remove(pmodel);
			// also remove it from the DB!
			pmodel.deleteProcletModelFromDB();
		}
	}
	
	public List<ProcletPort> getPorts() {
		List<ProcletPort> portsTot = new ArrayList<ProcletPort>();
		for (ProcletModel pmodel : this.pModelsList) {
			List<ProcletPort> ports = pmodel.getPorts();
			portsTot.addAll(ports);
		}
		return portsTot;
	}
	
	public ProcletBlock getBlockForInteractionNode (InteractionNode node) {
		for (ProcletModel pmodel : this.getProcletClasses()) {
			if (pmodel.getClassID().equals(node.getClassID())) {
				for (ProcletBlock block : pmodel.getBlocks()) {
					if (block.getBlockID().equals(node.getBlockID())) {
						return block;
					}
				}
			}
		}
		return null;
	}
	
	public ProcletBlock getProcletBlock(String classID, String blockID) {
		for (ProcletModel pmodel : this.getProcletClasses()) {
			if (pmodel.getClassID().equals(classID)) {
				for (ProcletBlock block : pmodel.getBlocks()) {
					if (block.getBlockID().equals(blockID)) {
						return block;
					}
				}
			}
		}
		return null;
	}
	
	public List<ProcletPort> getPortsBlock (InteractionNode node) {
		for (ProcletModel pmodel : this.getProcletClasses()) {
			if (pmodel.getClassID().equals(node.getClassID())) {
				for (ProcletBlock block : pmodel.getBlocks()) {
					if (block.getBlockID().equals(node.getBlockID())) {
						return pmodel.getPortsBlock(block);
					}
				}
			}
		}
		return null;
	}
	
	public ProcletPort getConnectedPortIn(List<ProcletPort> oPorts, List<ProcletPort> iPorts) {
		PortConnections conns = PortConnections.getInstance();
		List<PortConnection> pconns = conns.getPortConnections();
		// step1, 
		List<PortConnection> intermediate = new ArrayList<PortConnection>();
		for (ProcletPort port : oPorts) {
			for (PortConnection pconn : pconns) {
				if (port.getPortID().equals(pconn.getIPort().getPortID())) {
					intermediate.add(pconn);
				}
			}
		}
		// step2 
		PortConnection pconnNeeded = null;
		for (ProcletPort port : iPorts) {
			for (PortConnection pconn : intermediate) {
				if (port.getPortID().equals(pconn.getOPort().getPortID())) {
					pconnNeeded = pconn;
				}
			}
		}
		// step3
		for (ProcletPort port : oPorts) {
			if (port.getPortID().equals(pconnNeeded.getIPort().getPortID())) {
				return port;
			}
		}
		return null;
	}
	
	public ProcletPort getConnectedPortOut(List<ProcletPort> iPorts, List<ProcletPort> oPorts) {
		PortConnections conns = PortConnections.getInstance();
		List<PortConnection> pconns = conns.getPortConnections();
		// step1, 
		List<PortConnection> intermediate = new ArrayList<PortConnection>();
		for (ProcletPort port : iPorts) {
			for (PortConnection pconn : pconns) {
				if (port.getPortID().equals(pconn.getOPort().getPortID())) {
					intermediate.add(pconn);
				}
			}
		}
		// step2 
		PortConnection pconnNeeded = null;
		for (ProcletPort port : oPorts) {
			for (PortConnection pconn : intermediate) {
				if (port.getPortID().equals(pconn.getIPort().getPortID())) {
					pconnNeeded = pconn;
				}
			}
		}
		// step3
		for (ProcletPort port : iPorts) {
			if (port.getPortID().equals(pconnNeeded.getOPort().getPortID())) {
				return port;
			}
		}
		return null;
	}
	
	
	public ProcletPort getOutgoingPort (InteractionArc arc) {
		InteractionNode tail = arc.getTail();
		InteractionNode head = arc.getHead();
		// get ports block
		List<ProcletPort> tailPorts = getPortsBlock (tail);
		List<ProcletPort> headPorts = getPortsBlock (head);
		ProcletPort port = this.getConnectedPortIn(tailPorts, headPorts);
		return port;
	}
	
	public ProcletPort getIncomingPort (InteractionArc arc) {
		InteractionNode tail = arc.getTail();
		InteractionNode head = arc.getHead();
		// get ports block
		List<ProcletPort> tailPorts = getPortsBlock (tail);
		List<ProcletPort> headPorts = getPortsBlock (head);
		ProcletPort port = this.getConnectedPortOut(headPorts, tailPorts);
		return port;
	}
	
	public static ProcletModels getInstance() {
		if (pModels == null) {
			pModels = new ProcletModels();
			try {
				pModels.buildPModelsFromDB();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pModels;
	}
	
	public void buildPModelsFromDB () {
        List items = DBConnection.execQuery("select distinct s.classID from StoredProcletBlock as s");
        for (Object o : items) {
            ProcletModel pmodel = new ProcletModel((String) o);
        	pmodel.buildFromDB();
        	pModelsList.add(pmodel);
        }
	}
	
	public void deletePModelsFromDB () {
		for (ProcletModel pmodel : this.pModelsList) {
			pmodel.deleteProcletModelFromDB();
		}
		//this.pModelsList.clear();
	}
	
	public void persistProcletModels () {
		this.deletePModelsFromDB();
		for (ProcletModel pmodel : this.pModelsList) {
			pmodel.persistProcletModel();
		}
	}
	
	public static void main(String [ ] args) {
		ProcletModels pmodels = ProcletModels.getInstance();
		List<ProcletModel> plist = pmodels.getProcletClasses();
		System.out.println();
	}

}
