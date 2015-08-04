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
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc.ArcState;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.*;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock.BlockType;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Direction;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Signature;
import org.yawlfoundation.yawl.procletService.selectionProcess.ProcessEntityMID;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.state.Performatives;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import java.util.ArrayList;
import java.util.List;

public class BlockFO {
	
	private WorkItemRecord wir = null;
	private ProcletBlock block = null;
    private static Logger myLog = Logger.getLogger(BlockFO.class);
	
	public BlockFO(WorkItemRecord wir, ProcletBlock block) {
		this.wir = wir;
		this.block = block;
	}
	
	public static List<List> calculateRelations(WorkItemRecord wir) {
		List relations = new ArrayList();
		InteractionGraphs graphsInst = InteractionGraphs.getInstance();
		for (InteractionGraph graph : graphsInst.getGraphs()) {
			myLog.debug("emidGraph:" + graph.getEntityMID());
			InteractionNode node = graph.getNode(wir.getSpecURI(),
					wir.getCaseID(), wir.getTaskID());
			if (node != null) {
				// get corresponding arc(s)
				List<InteractionArc> arcs = graph.getArcsTail(wir.getSpecURI(),
						wir.getCaseID(), wir.getTaskID());
				// calculate relations (per arc)
				// 19012010
				// ignore arc if status is failed
				for (InteractionArc arc : arcs) {
					if (!arc.getArcState().equals(ArcState.FAILED)) {
						List relation = new ArrayList();
						relation.add(graph.getEntityMID());
						relation.add(arc.getEntityID());
						relation.add(arc.getHead().getClassID());
						relation.add(arc.getHead().getProcletID());
						relation.add(ProcletModels.getInstance().getOutgoingPort(arc));
						relation.add(wir.getCaseID());
						relations.add(relation);
					}
				}
			}
			else {
				// nothing to be done here...
			}
		}
		return relations;
	}
	
	public void checkSourceNodeExecuted(String classID, String procletID, String blockID) {
		for (InteractionGraph graph : InteractionGraphs.getInstance().getGraphs()) {
			for (InteractionArc arc : graph.getArcs()) {
				InteractionNode head = arc.getHead();
				InteractionNode tail = arc.getTail();
				if (head.getClassID().equals(tail.getClassID()) && 
						head.getProcletID().equals(tail.getProcletID()) && 
						head.getClassID().equals(classID) && 
						head.getProcletID().equals(procletID) && 
						head.getBlockID().equals(blockID)) {
					// check the types for them
					ProcletBlock blockHead = ProcletModels.getInstance().getBlockForInteractionNode(head);
					ProcletBlock blockTail = ProcletModels.getInstance().getBlockForInteractionNode(head);
					if (blockHead != null && blockTail != null && 
							blockTail.getBlockType().equals(ProcletBlock.BlockType.PI) &&
							blockHead.getBlockType().equals(ProcletBlock.BlockType.FO)) {
						// found an internal interaction arc
						// check the arc state
						while (true) {
							if (arc.getArcState().equals(ArcState.EXECUTED_SOURCE)) {
								break;
							}
							else {
								// wait till arc state is executed_source
								try {
									Thread.sleep(1000);
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static List<Performative> calcPerformativesOut(WorkItemRecord wir) {
		// MULTICAST!!!

		myLog.debug("CALCPERFORMATIVESOUT");
		List<List<List>> relationsExtList = new ArrayList<List<List>>();
		List<List> relationsExt  = calculateRelations(wir);
		myLog.debug("relationsExt:" + relationsExt);
		// PER PORT!!
		if (relationsExt.size()>0) {
			while (true) {
				List<List> perPort = new ArrayList();
				// 20022010
				//String procletID = (String) relationsExt.get(0).get(3);
				ProcletPort port = (ProcletPort) relationsExt.get(0).get(4);
				for (List relationExt : relationsExt) {
					// 20022010
					if (//procletID.equals((String)relationExt.get(3)) && 
							port.getPortID().equals(((ProcletPort) relationExt.get(4)).getPortID())) {
						perPort.add(relationExt);
					}
				}
				relationsExtList.add(perPort);
				// remove from original list 
				for (Object per : perPort) {
					relationsExt.remove(per);
				}
				if (relationsExt.size() == 0) {
					break;
				}
			}
		}
		myLog.debug("relationsExtList:" + relationsExtList);
		List<Performative> perfs = Performatives.createPerformatives(relationsExtList, new ArrayList(), wir);
		return perfs;
	}
	
	public static void main(String [] args) {
//		SingleInstanceClass sic = SingleInstanceClass.getInstance();
//		SingleInstanceClass sic2 = SingleInstanceClass.getInstance();
//		ThreadNotify thr1 = new ThreadNotify();
//		ThreadNotify thr2 = new ThreadNotify();
//		thr1.start();
//		thr2.start();
//		thr1.notification(true);
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		InteractionGraph graph1 = new InteractionGraph("1");
		InteractionNode node1 = new InteractionNode("c1","p1","A");
		InteractionNode node2 = new InteractionNode("mdoMeeting","15","register");
		InteractionNode node3 = new InteractionNode("mdoMeeting","16","register");
		InteractionArc arc1 = new InteractionArc(node1,node2,new EntityID("1","1"),ArcState.UNPRODUCED);
		InteractionArc arc2 = new InteractionArc(node1,node3,new EntityID("1","2"),ArcState.UNPRODUCED);
		graph1.addNode(node1);
		graph1.addNode(node2);
		graph1.addNode(node3);
		graph1.addArc(arc1);
		graph1.addArc(arc2);
		igraphs.addGraph(graph1);
		InteractionGraph graph2 = new InteractionGraph("2");
		InteractionNode node11 = new InteractionNode("c1","p1","A");
		InteractionNode node12 = new InteractionNode("c2","p3","A2");
		InteractionNode node13 = new InteractionNode("c5","p5","F");
		InteractionArc arc11 = new InteractionArc(node11,node12,new EntityID("2","1"),ArcState.UNPRODUCED);
		graph2.addNode(node11);
		graph2.addNode(node12);
		graph2.addNode(node13);
		graph2.addArc(arc11);
		igraphs.addGraph(graph2);
		WorkItemRecord wir = new WorkItemRecord("15","register","mdoMeeting","","");
		ProcletModels pmodels = ProcletModels.getInstance();
		// c1
		ProcletModel pmodel1 = new ProcletModel("c1");
		ProcletBlock block1 = new ProcletBlock("A",BlockType.FO,false,5);
		ProcletPort port1 = new ProcletPort("A1out",Direction.OUT,Signature.ONE,Signature.ONE);
		ProcletPort port2 = new ProcletPort("A2out",Direction.OUT,Signature.ONE,Signature.ONE);
		ProcletPort port3 = new ProcletPort("A3out", Direction.OUT,Signature.ONE,Signature.ONE);
		pmodel1.addBlock(block1);
		pmodel1.addProcletPort(port1, block1);
		pmodel1.addProcletPort(port2, block1);
		pmodel1.addProcletPort(port3, block1);
		pmodels.addProcletModel(pmodel1);
		// c2
		ProcletModel pmodel2 = new ProcletModel("c2");
		ProcletBlock block2 = new ProcletBlock("A2",BlockType.PI,true,5);
		ProcletBlock block3 = new ProcletBlock("B",BlockType.PI,false,5);
		ProcletPort port11 = new ProcletPort("A1in",Direction.IN,Signature.ONE,Signature.ONE);
		ProcletPort port12 = new ProcletPort("A4in",Direction.IN,Signature.ONE,Signature.ONE);
		ProcletPort port13 = new ProcletPort("A2in",Direction.IN,Signature.ONE,Signature.ONE);
		ProcletPort port14 = new ProcletPort("B2in",Direction.IN,Signature.ONE,Signature.ONE);
		pmodel2.addBlock(block2);
		pmodel2.addBlock(block3);
		pmodel2.addProcletPort(port11, block2);
		pmodel2.addProcletPort(port12, block2);
		pmodel2.addProcletPort(port13, block3);
		pmodel2.addProcletPort(port14, block3);
		//
		ProcletModel pmodel3 = new ProcletModel("5");
		ProcletBlock block4 = new ProcletBlock("F",BlockType.PI,false,5);
		pmodel3.addBlock(block4);
		//
		pmodels.addProcletModel(pmodel1);
		pmodels.addProcletModel(pmodel2);
		pmodels.addProcletModel(pmodel3);
		//
        ProcletModel pmodel = pmodels.getProcletClass("c2");
		ProcletBlock block = pmodel.getBlock("A2");
		
		PortConnections pconns = PortConnections.getInstance();
		PortConnection pconn1 = new PortConnection(port1,port11,"HIS");
		PortConnection pconn2 = new PortConnection(port2,port13,"HIS");
		pconns.addPortConnection(pconn1);
		pconns.addPortConnection(pconn2);
		
		
//		WorkItemRecord wir2 = new WorkItemRecord("p2","A2","c2","","");
//		ProcletService serv = new ProcletService(wir2);
		//serv.handleEnabledWorkItemEvent(wir2);
		//serv.start();
		//SingleInstanceClass sic = SingleInstanceClass.getInstance();
		//sic.notifyPerformativeListeners();
		ProcessEntityMID pemid = new ProcessEntityMID(wir,block,new EntityMID("1"),"0");
		List option1 = new ArrayList();
		option1.add("c2");
		option1.add("p2");
		option1.add("A2");
		option1.add("c1");
		option1.add("p1");
		option1.add("A");
		option1.add(1);
		List options = new ArrayList();
		options.add(option1);
		// "c2","p2","A2"
		try {
			//pemid.extendGraph(options);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
//		List<List<List>> result = pemid.generateNextOptions();
//		pemid.determineOptionsNonCrBlocks(result.get(1));
////		boolean result = pemid.doChecks();
//		pemid.initialGraphs();
//		pemid.commitGraphs();
//		pemid.sendPerformatives();
		ProcletModels pmodelsInst = ProcletModels.getInstance();
		ProcletModel pmodel4 = pmodelsInst.getProcletClass("mdoMeeting");
//		final BlockPI bpi = new BlockPI(wir,pmodel4.getBlock("register"),null);
//		bpi.processWIR();
//		Performatives perfsInst = Performatives.getInstance();
//		List<EntityID> eidTest = new ArrayList<EntityID>();
//		eidTest.add(new EntityID("1","1"));
//		Performative perf = new Performative("chann","s","r","a","c","s",Direction.OUT,eidTest);
//		perfsInst.addPerformative(perf);
//		bpi.evalWI();
//		Thread thr = new Thread () {
//			public void run () {
//				bpi.processWIR();
//			}
//		};
//		thr.start();
//		Performatives perfsInst = Performatives.getInstance();
//		List<EntityID> eidTest = new ArrayList<EntityID>();
//		eidTest.add(new EntityID("1","2"));
//		Performative perf = new Performative("chann","s","r","a","c","s",Direction.OUT,eidTest);
//		perfsInst.addPerformative(perf);
//		SingleInstanceClass sic = SingleInstanceClass.getInstance();
//		sic.notifyPerformativeListeners();
//		//
//		List<EntityID> eidTest2 = new ArrayList<EntityID>();
//		eidTest2.add(new EntityID("1","1"));
//		Performative perf2 = new Performative("chann","s","r","a","c","s",Direction.OUT,eidTest2);
//		perfsInst.addPerformative(perf2);
//		SingleInstanceClass sic2 = SingleInstanceClass.getInstance();
//		sic2.notifyPerformativeListeners();
		
//		bpi.doNotWaitlongerAction();
		InteractionGraphs igraphsInst = InteractionGraphs.getInstance();
		InteractionGraph graph = new InteractionGraph("1");
		InteractionNode node21 = new InteractionNode("visit","1","meet");		
		InteractionNode node22 = new InteractionNode("visit","3","createCondition");
		InteractionNode node23 = new InteractionNode("lab","2","createCondition");
		InteractionNode node24 = new InteractionNode("lab","2","send");
		InteractionNode node25 = new InteractionNode("visit","3","create");
		graph.addNode(node21);
		graph.addNode(node22);
		graph.addNode(node23);
		graph.addNode(node24);
		graph.addNode(node25);
		InteractionArc arc21 = new InteractionArc(node21,node22,new EntityID("1","1"),ArcState.CONSUMED);
		InteractionArc arc22 = new InteractionArc(node21,node23,new EntityID("1","2"),ArcState.CONSUMED);
		InteractionArc arc23 = new InteractionArc(node23,node24,new EntityID("1","3"),ArcState.EXECUTED_NONE);
		InteractionArc arc24 = new InteractionArc(node24,node25,new EntityID("1","1"),ArcState.UNPRODUCED);
		graph.addArc(arc21);
		graph.addArc(arc22);
		graph.addArc(arc23);
		graph.addArc(arc24);
		igraphsInst.addGraph(graph);
		CompleteCaseDeleteCase ccdc = new CompleteCaseDeleteCase("3");
		ccdc.removalCaseCompletionCase();
//		BlockPICreate bpc = new BlockPICreate();
//		Performatives perfsInst = Performatives.getInstance();
//		List<EntityID> eidTest = new ArrayList<EntityID>();
//		eidTest.add(new EntityID("1","1"));
//		Performative perf = new Performative("chann","s","r","a","c","s",Direction.OUT,eidTest);
//		perfsInst.addPerformative(perf);
//		List<InteractionArc> arcs = bpc.evalCreateProclets();
//		bpc.createProclets(arcs);
//		BlockFO bfo = new BlockFO(wir,block);
//		List<List> relations = bfo.calculateRelations();
//		igraphs.updateGraphPerfOut(relations);
//		bfo.calcPerformativesOut();
		System.out.println("done");
	}

}

