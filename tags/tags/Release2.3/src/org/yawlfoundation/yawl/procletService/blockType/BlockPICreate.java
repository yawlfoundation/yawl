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
import org.yawlfoundation.yawl.procletService.ProcletService;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.procletService.state.Performatives;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockPICreate {
	
	private static BlockPICreate singleInst = null;
	private Logger myLog = Logger.getLogger(BlockPICreate.class);
	
	private BlockPICreate () {
	}
	
	public static BlockPICreate getInstance() {
		if (singleInst == null) {
			singleInst = new BlockPICreate();
		}
		return singleInst;
	}
	
	public void checkForCreationProclets () {
		synchronized(this) {
			myLog.debug("CHECK FOR CREATION PROCLETS");
			List<InteractionArc> cprs = this.evalCreateProclets();
			myLog.debug("cprs:" + cprs);
			if (!cprs.isEmpty()) {
				this.createProclets(cprs);
			}
		}
	}
		
	private List<InteractionArc> evalCreateProclets () {
		myLog.debug("EVALCREATEPROCLETS");
		// get all arcs for which the procletid of the head is <= 0
		Performatives perfsInst = Performatives.getInstance();
		ProcletModels pmodels = ProcletModels.getInstance();
		List<InteractionArc> relevantArcs = new ArrayList<InteractionArc>();
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		List<InteractionGraph> graphs = igraphs.getGraphs();
		for (InteractionGraph graph : graphs) {
			for (InteractionArc arc : graph.getArcs()) {
				if (Integer.parseInt(arc.getHead().getProcletID()) <= 0 && 
						pmodels.getBlockForInteractionNode(arc.getHead()).isCreate()) {
					myLog.debug("add Arc:" + arc + "," + graph.getEntityMID());
					relevantArcs.add(arc);
				}
			}
		}
		myLog.debug("relevantArcs:" + relevantArcs);
		// check for which ones we have performatives
		List<InteractionArc> createArcs = new ArrayList<InteractionArc>();
		for (InteractionArc relArc : relevantArcs) {
			EntityID eid = relArc.getEntityID();
			for (Performative perf : perfsInst.getPerformatives()) {
				myLog.debug("perf:" + perf);
				for (EntityID eidPerf : perf.getEntityIDs()) {
					if (eidPerf.toString().equals(eid.toString())) {
						// 20022010
						// do not add arc twice
						boolean exists = false;
						for (InteractionArc arcCheck : createArcs) {
							if (arcCheck.getEntityID().toString().equals(relArc.getEntityID().toString())) {
								exists = true;
							}
						}
						if (!exists) { 
							createArcs.add(relArc);
						}
						break;
					}
				}
			}
		}
		myLog.debug("createArcs:" + createArcs);
		return createArcs;
	}
	
	private void createProclets(List<InteractionArc> enabledArcs) {
		myLog.debug("create proclets");
		// delete perfs
		List<Performative> relPerfs = new ArrayList<Performative> ();
		List<EntityID> eids = new ArrayList<EntityID> ();
		Performatives perfsInst = Performatives.getInstance();
		myLog.debug("perfs CreateProclets:" + perfsInst.getPerformatives());
		for (InteractionArc arc : enabledArcs) {
			EntityID eid = arc.getEntityID();
			eids.add(eid);
			for (Performative perf : perfsInst.getPerformatives()) {
				for (EntityID eidPerf : perf.getEntityIDs()) {
					if (eidPerf.toString().equals(eid.toString())) {
						relPerfs.add(perf);
						break;
					}
				}
			}
		}
		myLog.debug("relPerfs:" + relPerfs);
		// update the graph
		InteractionGraphs igraphs = InteractionGraphs.getInstance();
		myLog.debug("updateGraphPerfIn:" + eids);
		igraphs.updateGraphPerfIn(eids);
		// start cases
		// match data, start case
		myLog.debug("perfsCheck:" + perfsInst.getPerformatives());
		for (InteractionArc arc : enabledArcs) {
			myLog.debug("enabledArcs:" + enabledArcs);
			myLog.debug("launch case:" + arc);
			String oldProcletID = arc.getHead().getProcletID();
			String newProcletID = "";
			String classID = arc.getHead().getClassID();
			// get the data, get a corresponding perf
			Performative corPerf =  null;
			for (Performative perf : relPerfs) {
				// 20022010
				List<EntityID> eidsPerf = perf.getEntityIDs();
				for (EntityID eidCheck : eidsPerf) {
					if (arc.getEntityID().toString().equals(eidCheck.toString())) {
						corPerf = perf;
						break;
					}
				}
				if (corPerf != null) {
					break;
				}
			}
			myLog.debug("considering perf:" + corPerf);
			String data = corPerf.getContent();
			myLog.debug("data:" + data);
			// get only that part of the data that is relevant!!!
			// 20022010
			// remove what is not needed
			data = this.removeUnneededData(data, eids);
			myLog.debug("data:" + data);
			// ask YAWL
			myLog.debug("YAWL launchCase:" + classID);
			newProcletID = ProcletService.getInstance().launchCase(classID, data);
			myLog.debug("YAWL launchCase:done" + classID);
			// update graph for this one
			igraphs.updateGraphCaseID(arc.getEntityID(), newProcletID, oldProcletID);
			// 19022010
			igraphs.updateGraphPI(classID,newProcletID,arc.getHead().getBlockID());
			igraphs.persistGraphs();
			//20022010
			// delete the perf 
			myLog.debug("perfsCheck2:" + perfsInst.getPerformatives());
			perfsInst.deletePerformativeContent(corPerf);
			myLog.debug("perfsCheck3:" + perfsInst.getPerformatives());
		}
		
	}
	
	private String removeUnneededData (String s, List<EntityID> eids) {
		myLog.debug("REMOVEUNNEEDEDDATA");
		List<Element> eltsRemove = new ArrayList<Element>();
		Element dataList = JDOMUtil.stringToElement(s);
		dataList = (Element)dataList.clone();
		myLog.debug("dataList:" + JDOMUtil.elementToString(dataList));
		//Element eidData = dataList.getChild("entity");
		Element eidData = dataList;
		if (eidData != null) {
			myLog.debug("have entities");
			List<Element> children = eidData.getChildren("entity");
			for (Element child : children) {
				myLog.debug("have entity");
				Element emid = child.getChild("entity_id");
				String value = emid.getValue().trim();
				myLog.debug("value:" + value);
				// check if this one occurs in eids
				boolean match = false;
				for (EntityID eid : eids) {
					if (eid.getEmid().getValue().equals(value)) {
						// match
						myLog.debug("found match");
						match = true;
						break;
					}
				}
				if (!match) {
					eltsRemove.add(child);
				}
			}
		}
		myLog.debug("eltsRemove:" + eltsRemove.toString());
		Element eidData2 = dataList;
		if (eidData != null) {
			myLog.debug("have entities");
			for (Element eltSave : eltsRemove) {
				eidData2.removeContent(eltSave);
			}
		}
		String outputStr = JDOMUtil.elementToString(eidData2);
		myLog.debug("outputStr:" + outputStr);
		return outputStr;
	}
	
	   public static void main(String [ ] args) {
		   	BlockPICreate bpic = BlockPICreate.getInstance();
		   	String s = "<entities>" +
		    "<entity>" +
		    "<entity_id>ronny</entity_id>" +
		    "<name_value_pair>" + 
		      "<name>visit</name>" +
		      "<value>visit2</value>" + 
		    "</name_value_pair>" +
		  "</entity>" +
		  "<entity>" +
		    "<entity_id>ronny2</entity_id>" +
		    "<name_value_pair>" + 
		      "<name>visit</name>" +
		      "<value>visit2</value>" + 
		    "</name_value_pair>" +
		  "</entity>" +
		"</entities>";
		   	List<EntityID> eids = new ArrayList<EntityID>();
		   	//eids.add(new EntityID("ronny","1"));
		   	String data = bpic.removeUnneededData(s, eids);
		   	bpic.checkForCreationProclets();
	    	System.out.println();
	    }
	
}
