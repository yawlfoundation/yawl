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

package org.yawlfoundation.yawl.procletService.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnection;
import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnections;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.persistence.StoredPerformative;
import org.yawlfoundation.yawl.procletService.util.EntityID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;



public class Performatives {
	
	private static Performatives perfsSingle = null;
	private List<Performative> perfs = new ArrayList<Performative> ();
    
    private static Logger myLog = LogManager.getLogger(Performatives.class);
	
	private Performatives () {
		
	}
	
	public static Performatives getInstance() {
		if (perfsSingle == null) {
			perfsSingle = new Performatives();
			perfsSingle.buildFromDB();
		}
		return perfsSingle;
	}
	
	public void addPerformative (Performative perf) {
		if (!perfs.contains(perf)) {
			this.perfs.add(perf);
			this.persistPerformatives();
		}
		// persist this
		this.persistPerformatives();
	}
	
	public List<Performative> getPerformatives() {
		return this.perfs;
	}
	
	public void deletePerformative (Performative perf) {
		perfs.remove(perf);
		this.persistPerformatives();
	}
	
	public void deletePerformativeContent (Performative perf) {
		Performative perfRem = null;
		for (Performative perfLoop : perfs) {
			if (perf.equalContent(perfLoop)) {
				perfRem = perfLoop;
				break;
			}
		}
		if (perfRem != null) {
			this.deletePerformative(perfRem);
		}
	}
	
	public void deleteAllPerformatives () {
		perfs = new ArrayList<Performative>();
	}
	
	public static List<Performative> createPerformatives (List<List<List>> relationExtsList, List dataRels, WorkItemRecord wir) {
		myLog.debug("CREATEPERFORMATIVES");
		List<Performative> perfs = new ArrayList<Performative> ();
		for (List<List> rl : relationExtsList) {
			List relationExt = rl.get(0);
			String sender = (String) relationExt.get(5);
			//String receiver = (String) relationExt.get(3);
			// determine receivers
			// 20022010
			List<String> receivers = new ArrayList<String>();
			for (List relationExt1 : rl) {
				String receiver = (String) relationExt1.get(3);
				// 09032010
				// do not add if already in
				if (!receivers.contains(receiver)) {
					receivers.add(receiver);
				}
			}
			ProcletPort port = (ProcletPort) relationExt.get(4);
			PortConnection pconn = PortConnections.getInstance().getPortConnectionIPort(port.getPortID());
			List<EntityID> eids =  new ArrayList();
			for (List relation : rl) {
				EntityID eid = (EntityID) relation.get(1);
				eids.add(eid);
			}
			// create performative
			// calculate content
			// assume unique emid for eids
			String content = calculateContent(eids,wir);
			Performative perf = new Performative(pconn.getChannel(),sender,receivers,"",content,
					"",ProcletPort.Direction.OUT,eids);
			myLog.debug("perf:" + perf);
			myLog.debug("perfs:" + perfs);
			// 20022010
			// if perf has multiple receivers, duplicate
			if (receivers.size()>1) {
				for (int i=0; i<receivers.size()-1;i++) {
					Performative perfNew = new Performative(perf.getChannel(),perf.getSender(),perf.getReceivers(),
							perf.getAction(),perf.getContent(),perf.getScope(),perf.getDirection(),
							perf.getEntityIDs());
					perfs.add(perfNew);
				}
			}
			perfs.add(perf);
		}
		myLog.debug("perfs:" + perfs);
		return perfs;
	}
	
	public static List<EntityID> parseEntityIDsStr(String eidsStr) {
		List<EntityID> eids = new ArrayList<EntityID> ();
		if (!eidsStr.equals("")) {
			String[] split = eidsStr.split(",");
			for (int i=0; i<split.length;i=i+2) {
				String first = split[i];
				String second = split[i+1];
				EntityID eid = new EntityID(first,second);
				eids.add(eid);
			}
		}
		return eids;
	}
	
	public static String calculateContent(List<EntityID> eids, WorkItemRecord wir) {
		myLog.debug("CALCULATECONTENT");
		myLog.debug("eids:" + eids);
		Element dataList = wir.getDataList();
		if (dataList != null) {
			dataList = dataList.clone();
			myLog.debug("dataList:" + JDOMUtil.elementToString(dataList));
			Element eidData = dataList.getChild("entities");
			List<Element> eltsRemove = new ArrayList<Element>();
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
			// remove what is not needed
			myLog.debug("eltsRemove:" + eltsRemove.toString());
			Element eidData2 = dataList.getChild("entities");
			if (eidData != null) {
				myLog.debug("have entities");
				for (Element eltSave : eltsRemove) {
					eidData2.removeContent(eltSave);
				}
			}
	//		// create output
	//		Element output = new Element("entities");
	//		for (Element elt : eltsSave) {
	//			output.addContent(elt);
	//		}
			String outputStr = JDOMUtil.elementToString(eidData2);
			myLog.debug("outputStr:" + outputStr);
			return outputStr;
		}
		else {
			// get from the graphs which entities it concerns
			List<EntityMID> emids = new ArrayList<EntityMID> ();
			myLog.debug("wir:" + wir);
			List<InteractionGraph> graphs = InteractionGraphs.getInstance().getGraphs();
			for (InteractionGraph graph : graphs) {
				if (!graph.getEntityMID().getValue().contains("TEMP")) {
					for (InteractionArc arc : graph.getArcs()) {
						myLog.debug("arc:" + arc);
						// check the tail
						if (arc.getTail().getClassID().equals(wir.getSpecURI()) && 
								arc.getTail().getProcletID().equals(wir.getCaseID()) && 
								arc.getTail().getBlockID().equals(wir.getTaskID())) {
							myLog.debug("in loop");
							EntityMID emid = arc.getEntityID().getEmid();
							// check if not already in emids
							boolean check = false;
							for (EntityMID emidC : emids) {
								if (emidC.getValue().equals(emid.getValue())) {
									check = true;
								}
							}
							if (!check) {
								emids.add(emid);
								myLog.debug("emid added:" + emid);
							}
						}
					}
				}
			}
			// have the relevant emids
			Element newEntsElt = new Element("entities");
			for (EntityMID emid : emids) {
				Element newEntElt =  new Element("entity");
				Element newEidElt = new Element("entity_id");
				newEidElt.setText(emid.getValue());
				newEntElt.addContent(newEidElt);
				newEntsElt.addContent(newEntElt);
			}
			String outputStr = JDOMUtil.elementToString(newEntsElt);
			myLog.debug("outputStr:" + outputStr);
			return outputStr;
		}
	}
	
	public static String parseEntityIDs (List<EntityID> eids) {
		String returnString = "";
		for (EntityID eid : eids) {
			returnString = returnString + eid + ",";
		}
		if (returnString.length()>0) {
			returnString = returnString.substring(0, returnString.length()-1);
		}
		return returnString;
	}
	
	public boolean buildFromDB() {
		this.perfs.clear();
        List items = DBConnection.getObjectsForClass("StoredPerformative");
        for (Object o : items) {
            addPerformative(((StoredPerformative) o).newPerformative());
        }
		return true;
	}
	
	public void deletePerfsFromDB () {
        DBConnection.deleteAll("StoredPerformative");
	}
	
	public void persistPerformatives () {
		this.deletePerfsFromDB();
		for (Performative perf : perfs) {
			String receiversStr = "";
			for (String receiverStr : perf.getReceivers()) {
				receiversStr = receiversStr + receiverStr + ",";
			}
			// remove last comma
			receiversStr = receiversStr.substring(0, receiversStr.length()-1);

            DBConnection.insert(new StoredPerformative(perf.getTime(),
				perf.getChannel(), perf.getSender(), receiversStr,
				perf.getAction(), perf.getContent(),
				perf.getScope(), perf.getDirection().name(),
				Performatives.parseEntityIDs(perf.getEntityIDs())));
		}
	}
	
	public static void main(String [] args) {
		Performatives perfs = Performatives.getInstance();
		List<EntityID> eids1 = new ArrayList<EntityID> ();
		eids1.add(new EntityID("1","1"));
		eids1.add(new EntityID("1","2"));
		List<String> receivers = new ArrayList<String>();
		receivers.add("first");
		receivers.add("second");
		Performative perf1 = new Performative(
				"ch1","s1",receivers,"a1","c1","sc1",ProcletPort.Direction.IN,eids1);
		List<EntityID> eids = new ArrayList<EntityID> ();
		eids.add(new EntityID("1","1"));
		eids.add(new EntityID("1","2"));
		List<String> receivers2 = new ArrayList<String>();
		receivers2.add("third");
		receivers2.add("fourth");
		Performative perf2 = new Performative(
				"ch1","s1",receivers2,"a1","c1","sc1",ProcletPort.Direction.IN,eids);
		boolean test = perf1.equalContent(perf2);
		perfs.addPerformative(perf1);
		perfs.addPerformative(perf2);
		perfs.deletePerfsFromDB();
		perfs.persistPerformatives();
		perfs.deleteAllPerformatives();
		perfs.buildFromDB();
		System.out.println("done");
	}
	
}
