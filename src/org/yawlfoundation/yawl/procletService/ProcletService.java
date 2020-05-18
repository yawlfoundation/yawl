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

package org.yawlfoundation.yawl.procletService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.procletService.blockType.BlockCP;
import org.yawlfoundation.yawl.procletService.blockType.BlockFO;
import org.yawlfoundation.yawl.procletService.blockType.BlockPI;
import org.yawlfoundation.yawl.procletService.blockType.CompleteCaseDeleteCase;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.state.Performative;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcletService extends InterfaceBWebsideController  {

    private static ProcletService INSTANCE = null;
	private Logger myLog = LogManager.getLogger(ProcletService.class);

    // holds a session handle to the engine
    private String _handle = null;

	private ProcletService () {
		super ();
        DBConnection.init(null);
		myLog.debug("Proclet service constructor!");
	}

    public static ProcletService getInstance() {
        if (INSTANCE == null) INSTANCE = new ProcletService();
        return INSTANCE;
    }

    public void destroy() {
        DBConnection.close();
    }


    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        myLog.debug("HANDLE ENABLED WORKITEM EVENT");
        myLog.debug("spec id: " + wir.getSpecURI());
        myLog.debug("case id: " + wir.getCaseID());
        myLog.debug("task id: " + wir.getTaskID());

        // lock the case
        while (SingleInstanceClass.getInstance().isCaseBlocked(changeCaseID(wir.getCaseID()))) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        myLog.debug("blockcase: " + changeCaseID(wir.getCaseID()));
        SingleInstanceClass.getInstance().blockCase(changeCaseID(wir.getCaseID()));

        try {
            connect();

            // check if case still existing
            boolean exists = successful(
                    _interfaceBClient.getCaseData(wir.getCaseID(), _handle));
            myLog.debug("exists: " + exists);
            if (exists) {

                // checkout ... process ... checkin
                wir = checkOut(wir.getID(), _handle);
                myLog.debug("data: " + JDOMUtil.elementToString(wir.getDataList()));

                // take block and wir
                String classID = wir.getSpecURI();
                String caseID = wir.getCaseID();
                String procletID = changeCaseID(caseID);
                String taskID = wir.getTaskID();
                String blockID = changeTaskID(taskID);
                wir.setCaseID(procletID);
                wir.setTaskID(blockID);
                ProcletModels pmodels = ProcletModels.getInstance();
                ProcletModel pmodel = pmodels.getProcletClass(classID);
                if (pmodel != null) {
                    ProcletBlock block = pmodel.getBlock(blockID);
                    if (block != null) {
                        if (block.getBlockType().equals(ProcletBlock.BlockType.CP)) {
                            myLog.debug("block type is CP");
                            InteractionGraphs.getNewInstance();
                            BlockCP bcp = new BlockCP(wir,block);
                            bcp.processWIR();
                        }
                        else if (block.getBlockType().equals(ProcletBlock.BlockType.FO)) {

                            // reset graphs
                            myLog.debug("block type is FO");
                            InteractionGraphs.getNewInstance();
                            BlockFO bfo = new BlockFO(wir,block);
                            bfo.checkSourceNodeExecuted(classID, procletID, blockID);
                            List<List> relations = BlockFO.calculateRelations(wir);
                            myLog.debug("relations: " + relations);
                            InteractionGraphs.getInstance().updateGraphPerfOut(relations);
                            InteractionGraphs.getInstance().updateGraphFO(classID, procletID, blockID);
                            InteractionGraphs.getInstance().persistGraphs();
                            List<Performative> perfs = BlockFO.calcPerformativesOut(wir);
                            myLog.debug("performatives: " + perfs);

                            SingleInstanceClass sic = SingleInstanceClass.getInstance();
                            myLog.debug("notify performativeListeners");
                            sic.notifyPerformativeListeners(perfs);
                        }
                        else if (block.getBlockType().equals(ProcletBlock.BlockType.PI)) {
                            myLog.debug("block type is PI");
                            InteractionGraphs.getNewInstance();
                            BlockPI bpi = new BlockPI(wir,block);
                            bpi.processWIR();
                            // persist the graphs
                            InteractionGraphs.getInstance().persistGraphs();
                        }
                    }
                    else myLog.debug("No proclet block found in class '" + classID + "" +
                            "' for work item: " + wir.getID());
                }
                else myLog.debug("No proclet class found for work item: " + wir.getID());

	            // change taskid back again
	        	wir.setCaseID(caseID);
	            wir.setTaskID(taskID);
	            checkInWorkItem(wir.getID(), wir.getDataList(), wir.getDataList(), null, _handle);
                myLog.debug("PROCLETSERVICE: checking in workitem done");
                myLog.debug("spec id: " + wir.getSpecURI());
                myLog.debug("case id: " + wir.getCaseID());
                myLog.debug("task id: " + wir.getTaskID());
	        }
        }
	    catch (Exception ioe) {
	        ioe.printStackTrace();
	    }
    	SingleInstanceClass.getInstance().unblockCase(changeCaseID(wir.getCaseID()));
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord wir) {  
    	myLog.debug("HANDLE CANCELLED WORKITEM EVENT");
    	myLog.debug("spec id: " + wir.getSpecURI());
    	myLog.debug("case id: " + wir.getCaseID());
    	myLog.debug("task id: " + wir.getTaskID());
        myLog.debug("HANDLE CANCELLED WORKITEM EVENT done");
    }
    
    public void handleCancelledCaseEvent(String caseID) { 
    	myLog.debug("HANDLE CANCELLED CASE EVENT");
        completeCase(caseID);
        myLog.debug("DONE HANDLE CANCELLED CASE EVENT " + caseID);
    }
    
    public void handleCompleteCaseEvent(String caseID, String casedata) { 
        myLog.debug("HANDLE COMPLETED CASE EVENT");
        completeCase(caseID);
		myLog.debug("DONE HANDLE COMPLETED CASE EVENT " + caseID);
    }


    public String launchCase(String classID, String data) {
        String newID = null;
        try {
            connect();
            
            //first check whether this model is already loaded into the engine
            YSpecificationID yidSelected = null;
            List<SpecificationData> specList = getSpecificationPrototypesList(_handle);
            for (SpecificationData specData : specList) {
                if (specData.getID().getUri().equals(classID)) {
                    yidSelected = specData.getID();
                }
            }
            if (yidSelected != null) {
                String dataReturn = StringUtil.wrap(data, yidSelected.getUri());
                myLog.debug("dataReturn is " + dataReturn);
                newID = _interfaceBClient.launchCase(
                        new YSpecificationID(yidSelected.getIdentifier(),
                        yidSelected.getVersion(), yidSelected.getUri()),
                        dataReturn, null, _handle);
                myLog.debug("start case for " + yidSelected.getUri() + ", yawlResponse is " + newID);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return newID;
    }
    
    private void completeCase(String caseID) {
        myLog.debug("case id: " + caseID);
    	InteractionGraphs.getNewInstance();
    	CompleteCaseDeleteCase ccdc = new CompleteCaseDeleteCase(caseID);
		ccdc.removalCaseCompletionCase();
    }
    
    
    private boolean connected() {
        return _handle != null;
    }
    
    private Element getOutputData(String taskName, String data) {
        Element output = new Element(taskName);
        Element result = new Element("result");
        result.setText(data);
        output.addContent(result);
        return output;
    }
    
    public static String changeTaskID (String taskID) {
    	int last = taskID.lastIndexOf("_");
    	String newID = taskID;
    	if (last >= 0) {
    		newID = taskID.substring(0,last);
    	}
    	return newID;
    }
    
    public static String changeCaseID (String caseID){
    	int index = caseID.indexOf(".");
    	String newStr = caseID;
    	if (index > 0) {
    		newStr = caseID.substring(0,index);
    	}
    	return newStr;
    }

    public List<String> getSpecURIsForRunningCases() {
        List<String> uris = new ArrayList<String>();

        if (connect()) {
            XNode cases = getAllRunningCases();
            if (cases != null) {
                for (XNode node : cases.getChildren()) {
                    uris.add(node.getAttributeValue("uri"));
                }
            }
        }
        return uris;
    }

    public List<String> getRunningCaseIDs() {
        List<String> caseIDs = new ArrayList<String>();

        if (connect()) {
            XNode cases = getAllRunningCases();
            if (cases != null) {
                for (XNode node : cases.getChildren()) {
                    caseIDs.add(node.getChildText("caseID"));
                }
            }
        }
        return caseIDs;
    }

    
    private XNode getAllRunningCases() {
        try {
            if (connect()) {
                String caseStr = _interfaceBClient.getAllRunningCases(_handle);
                if (successful(caseStr)) {
                    return new XNodeParser().parse(StringUtil.unwrap(caseStr));
                }
            }
        }
        catch (IOException ioe) {
            myLog.error("Could not get Running Case list: ", ioe);
        }
        return null;
    }
    
    
    private boolean connect() {
        try { 
            // connect only if not already connected
            if (! checkConnection(_handle)) {
                _handle = connect(engineLogonName, engineLogonPassword);
            }
        }
        catch (IOException ioe) {
            myLog.error("Service could not establish connection to YAWL Engine");
        }
        return successful(_handle);
    }

    public static void main(String [ ] args) {
    	ProcletService serv = new ProcletService();
    	WorkItemRecord wir = new WorkItemRecord("103","meet","visit", "");
    	serv.handleEnabledWorkItemEvent(wir);
    	String newOne = ProcletService.changeCaseID("15.1");
    	System.out.println(newOne);
    }
}

