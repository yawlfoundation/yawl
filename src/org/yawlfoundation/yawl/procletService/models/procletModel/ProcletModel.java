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

package org.yawlfoundation.yawl.procletService.models.procletModel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock.BlockType;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Direction;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Signature;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.StoredBlockRel;
import org.yawlfoundation.yawl.procletService.persistence.StoredProcletBlock;
import org.yawlfoundation.yawl.procletService.persistence.StoredProcletPort;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ProcletModel extends DirectedSparseGraph {

	private String classID = "";
	
	public ProcletModel(String classID) {
		super();
		this.classID = classID;
	}
	
	public String getClassID () {
		return this.classID;
	}
	
	public void setClassID (String classID) {
		this.classID = classID;
	}
	
	public void addBlock (ProcletBlock block) {
		if (!this.getVertices().contains(block)) { 
			this.addVertex(block);
		}
	}
	
	public void deleteBlock (ProcletBlock block) {
		this.removeVertex(block);
		// brels
		List<BlockRel> brelsRem = this.getBRels();
		for (BlockRel brel : this.getBRels()) {
			if (brel.getIBlock().equals(block) ||
					brel.getOBlock().equals(block)) {
				brelsRem.add(brel);
			}
		}
		for (BlockRel brel : brelsRem) {
			this.removeEdge(brel);
		}
		// ports
//		List<BlockPortEdge> bportsRem = new ArrayList<BlockPortEdge>();
//		List<BlockPortEdge> bports = this.getBlockPortEdges();
//		for (BlockPortEdge bpe : bports) {
//			if (bpe.getBlock().equals(block)) {
//				bportsRem.add(bpe);
//			}
//		}
//		for (BlockPortEdge bpe : bportsRem) {
//			this.removeEdge(bpe);
//		}
		// search for the isolated ports
		List<ProcletPort> portsRem = new ArrayList<ProcletPort>();
		Collection ports = this.getVertices();
		Iterator it = ports.iterator();
		while (it.hasNext()) {
			Object item = it.next();
			// check out whether it is port or a ip
			if (item instanceof ProcletPort) {
				ProcletPort port = (ProcletPort) item;
				boolean found = false;
				// check out whether it is in an edge
				Collection edges = this.getEdges();
				Iterator it2 = edges.iterator();
				while (it2.hasNext()) {
					Object item2 = it2.next();
					if (item2 instanceof BlockPortEdge) {
						BlockPortEdge bpe = (BlockPortEdge) item2;
						if (bpe.getPort().equals(port)) {
							// found
							found = true;
							break;
						}
					}
				}
				if (!found) {
					// port is isolated, remove it
					portsRem.add(port);
				}
			}
		}
		for (ProcletPort port : portsRem) {
			this.removeVertex(port);
		}
	}
	
	public void deletePort(ProcletPort port) {
		this.removeVertex(port);
		List<BlockPortEdge> bportsRem = new ArrayList<BlockPortEdge>();
		List<BlockPortEdge> bports = this.getBlockPortEdges();
		for (BlockPortEdge bpe : bports) {
			if (bpe.getPort().equals(port)) {
				bportsRem.add(bpe);
			}
		}
		for (BlockPortEdge bpe : bportsRem) {
			this.removeEdge(bpe);
		}
	}
	
	public void deleteBRel(BlockRel brel) {
		this.removeEdge(brel);
	}
	
	public void deleteBlockPortEdge(BlockPortEdge bpe) {
		this.removeEdge(bpe);
	}
	
	public void addProcletPort(ProcletPort port,ProcletBlock block) {
		if (!this.getVertices().contains(port)) {
			// add port
			this.addVertex(port);
			// add block if needed
			if (!this.getVertices().contains(block)) {
				this.addVertex(block);
			}
			BlockPortEdge bpe = new BlockPortEdge(block,port);
			// add vertex
			if (port.getDirection().equals(Direction.OUT)) {
				this.addEdge(bpe,block,port,EdgeType.DIRECTED);
			}
			else {
				this.addEdge(bpe,port,block,EdgeType.DIRECTED);
			}
		}
	}
	
	public List<ProcletPort> getPorts () {
		List<ProcletPort> ports = new ArrayList<ProcletPort> ();
		List<BlockPortEdge> bpes = this.getBlockPortEdges();
		for (BlockPortEdge bpe : bpes) {
			ProcletPort port = bpe.getPort();
			if (!ports.contains(port)) {
				ports.add(port);
			}
		}
		return ports;
	}
	
	public void addBRel (ProcletBlock iBlock, ProcletBlock oBlock) {
		if (this.getVertices().contains(iBlock) && 
				this.getVertices().contains(oBlock)) {
			BlockRel brel = new BlockRel(iBlock,oBlock);
			this.addEdge(brel, iBlock, oBlock, EdgeType.DIRECTED);
		}
	}
	
	public List<ProcletBlock> getBlocks() {
		Collection col = this.getVertices();
		List<ProcletBlock> blocks = new ArrayList<ProcletBlock> ();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ProcletBlock) {
				blocks.add((ProcletBlock)obj);
			}
		}
		return blocks;
	}
	
	public ProcletBlock getBlock(String blockID) {
		List<ProcletBlock> blocks = this.getBlocks();
		for (ProcletBlock block : blocks) {
			if (block.getBlockID().equals(blockID)) {
				return block;
			}
		}
		return null;
	}
	
	public List<ProcletPort> getPortsBlock (ProcletBlock block) {
		List<ProcletPort> ports = new ArrayList<ProcletPort> ();
		Collection edges = this.getEdges();
		Iterator it = edges.iterator();
		while (it.hasNext()) {
			Object edge = it.next();
			if (edge instanceof BlockPortEdge && 
					((BlockPortEdge)edge).getBlock().equals(block)) {
				ports.add(((BlockPortEdge)edge).getPort());
			}
		}
		return ports;
	}
	
	public List<BlockPortEdge> getBlockPortEdges () {
		List<BlockPortEdge> bpe = new ArrayList<BlockPortEdge>();
		Collection edges = this.getEdges();
		Iterator it = edges.iterator();
		while (it.hasNext()) {
			Object edge = it.next();
			if (edge instanceof BlockPortEdge) {
				bpe.add((BlockPortEdge)edge);
			}
		}
		return bpe;
	}
	
	public List<BlockRel> getBRels () {
		List<BlockRel> bRels = new ArrayList<BlockRel> ();
		Collection col = this.getEdges();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object edge = it.next();
			if (edge instanceof BlockRel) {
				bRels.add((BlockRel)edge);
			}
		}
		return bRels;
	}
	
	public void deleteAllVertices (){
		Collection vertices = this.getVertices();
		List<Object> verticesRem = new ArrayList<Object> ();
		for (Object ver : vertices) {
			verticesRem.add(ver);
		}
		for (Object obj : verticesRem) {
			this.removeVertex(obj);
		}
	}
	
	public void deleteAllEdges (){
		Collection edges = this.getVertices();
		List<Object> edgesRem = new ArrayList<Object> ();
		for (Object edge : edges) {
			edgesRem.add(edge);
		}
		for (Object obj : edgesRem) {
			this.removeEdge(obj);
		}
	}
	
	public boolean buildFromDB() {
//		this.getVertices().clear();
//		this.getEdges().clear();
        List items = DBConnection.getObjectsForClassWhere("StoredProcletBlock",
                "classID='" + classID + "'");
        for (Object o : items) {
            StoredProcletBlock block = (StoredProcletBlock) o;
            addBlock(block.newProcletBlock());
        }        

		// get ports
        items = DBConnection.getObjectsForClassWhere("StoredProcletPort",
                "classID='" + classID + "'");
        for (Object o : items) {
            StoredProcletPort port = (StoredProcletPort) o;
            addProcletPort(port.newProcletPort(), getBlock(port.getBlockID()));
        }

	    // brels
        items = DBConnection.getObjectsForClassWhere("StoredBlockRel",
                "classID='" + classID + "'");
        for (Object o : items) {
            StoredBlockRel bRel= (StoredBlockRel) o;
            addBRel(getBlock(bRel.getInput()), getBlock(bRel.getOutput()));
        }
		return true;
	}
	
	public void persistProcletModel () {
		this.deleteProcletModelFromDB();
        for (ProcletBlock block : getBlocks()) {
            DBConnection.insert(new StoredProcletBlock(classID, block.getBlockID(),
                    block.getBlockType().toString(), block.isCreate(), block.getTimeOut()));
        }

        for (BlockPortEdge bpe : getBlockPortEdges()) {
            DBConnection.insert(new StoredProcletPort(classID,
            	    bpe.getBlock().getBlockID(), bpe.getPort().getPortID(),
            		bpe.getPort().getDirection().toString(),
            		bpe.getPort().getCardinality().toString(),
            		bpe.getPort().getMultiplicity().toString()));
        }

        for (BlockRel brel : getBRels()) {
            DBConnection.insert(new StoredBlockRel(classID, brel.getIBlock().getBlockID(),
            				brel.getOBlock().getBlockID()));
        }
	}
	
	
	public void deleteProcletModelFromDB () {
        String template = "delete from %s as s where s.classID='" + classID + "'";
        DBConnection.execUpdate(String.format(template, "StoredProcletBlock"));
        DBConnection.execUpdate(String.format(template, "StoredProcletPort"));
        DBConnection.execUpdate(String.format(template, "StoredBlockRel"));
	}
	
	public String toString() {
		return this.classID;
	}
	
	public static void main(String [] args) {
		ProcletModel pmodel = new ProcletModel("visit");
		pmodel.buildFromDB();
		pmodel.persistProcletModel();
		ProcletBlock block1 = new ProcletBlock("b1",BlockType.PI,false,5);
		ProcletBlock block2 = new ProcletBlock("b2",BlockType.PI,false,5);
		pmodel.addBlock(block1);
		pmodel.addBlock(block2);
		ProcletPort port = new ProcletPort("p1",Direction.OUT,Signature.ONE,Signature.ONE);
		ProcletPort port2 = new ProcletPort("p2",Direction.OUT,Signature.ONE,Signature.ONE);
		pmodel.addProcletPort(port, block1);
		pmodel.addProcletPort(port2, block1);
		pmodel.getBlocks();
		pmodel.getPortsBlock(block1);
		// visualization
		Layout layout = new CircleLayout(pmodel);
		layout.setSize(new Dimension(300,300));
		BasicVisualizationServer vv = new BasicVisualizationServer(layout);
		vv.setPreferredSize(new Dimension(350,350));
		
		JFrame frame = new JFrame("simple");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}


}
