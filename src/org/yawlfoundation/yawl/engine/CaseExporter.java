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

package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * @author Michael Adams
 * @date 28/9/18
 */
public class CaseExporter {

    private final YEngine _engine;

    
    public CaseExporter(YEngine engine) {
        _engine = engine;
    }


    public String exportAll() {
        return export(_engine.getRunningCaseIDs());
    }


    public String export(YIdentifier caseID) {
        return export(Collections.<YIdentifier>singletonList(caseID));
    }


    public String export(String caseCSV) {
        return export(csvToIdentifierList(caseCSV));
    }


    private String export(List<YIdentifier> caseList) {
        XNode root = new XNode("cases");
        for (YIdentifier caseID : caseList) {
            root.addChild(getCaseNode(caseID));
        }
        return root.toString();
    }


    private XNode getCaseNode(YIdentifier caseID) {
        XNode caseNode = new XNode("case");
        caseNode.addChild(getNetRunners(caseID));
        caseNode.addChild(getWorkItems(caseID));
        return caseNode;
    }


    private XNode getNetRunners(YIdentifier caseID) {
        XNode nRunners = new XNode("runners");
        for (YNetRunner runner : _engine.getRunnersForPrimaryCase(caseID)) {
            YIdentifier id = runner.getCaseID();
            XNode nRunner = nRunners.addChild("runner");
            nRunner.addChild(getParent(id));
            nRunner.addChild(getIdentifier(id));
            nRunner.addChild(getNetData(runner));
            nRunner.addChild(runner.getSpecificationID().toXNode());
            nRunner.addChild("containingtask", runner.getContainingTaskID());
            nRunner.addChild("starttime", runner.getStartTime());
            nRunner.addChild("observer", runner.get_caseObserverStr());
            nRunner.addChild("executionstatus", runner.getExecutionStatus());
            nRunner.addChild(getEnabledTasks(runner));
            nRunner.addChild(getBusyTasks(runner));
            nRunner.addChild(getTimerStates(runner.get_timerStates()));
        }
        return nRunners;
    }


    private XNode getNetData(YNetRunner runner) {
        XNode nData = new XNode("netdata");
        nData.addContent(runner.getNetData().getData());
        return nData;
    }

    private XNode getTimerStates(Map<String, String> timerStates) {
        XNode nTimers = new XNode("timerstates");
        for (String taskName : timerStates.keySet()) {
            XNode nState = nTimers.addChild("timerstate");
            nState.addChild("taskname", taskName);
            nState.addChild("state", timerStates.get(taskName));
        }
        return nTimers;
    }


    private XNode getEnabledTasks(YNetRunner runner) {
        XNode nTasks = new XNode("enabledtasks");
        runner.getEnabledTaskNames().forEach(e -> nTasks.addChild("task", e));
        return nTasks;
    }


    private XNode getBusyTasks(YNetRunner runner) {
        XNode nTasks = new XNode("busytasks");
        runner.getBusyTasks().forEach(b -> {
            XNode nTask = nTasks.addChild("task");
            nTask.addChild("name", b.getID());
            if (b.isMultiInstance()) {
                String doc = b.getMIOutputData().getDataDocString();
                XNode miNode = nTask.addChild("midata");
                miNode.addContent(doc);
                miNode.addAttribute("uid", b.getMIOutputData().getUniqueIdentifier());
            }
        });
        return nTasks;
    }


    private XNode getIdentifier(YIdentifier id) {
        XNode nID = new XNode("identifier");
        nID.addAttribute("id", id);
        nID.addChild(getLocations(id));
        XNode nChildren = nID.addChild("children");
        for (YIdentifier child : id.getChildren()) {
             nChildren.addChild(getIdentifier(child));
        }
        return nID;
    }


    private XNode getLocations(YIdentifier id) {
        XNode nLocations = new XNode("locations");
        for (String location : id.getLocationNames()) {
            nLocations.addChild("location", location);
        }
        return nLocations;
    }


    private XNode getParent(YIdentifier id) {
        YIdentifier parent = id.getParent();
        String parentID = parent != null ? parent.toString() : null;
        return new XNode("parent", parentID);
    }

    
    private XNode getWorkItems(YIdentifier id) {
        XNode nItems = new XNode("workitems");
        for (YWorkItem item : _engine.getWorkItemRepository().getWorkItemsForCase(id)) {
            XNode nItem = nItems.addChild("item");
            nItem.addChild("id", item.get_thisID());
            nItem.addChild(item.getSpecificationID().toXNode());
            nItem.addChild("enablement", getTime(item.getEnablementTime()));
            nItem.addChild("firing", getTime(item.getFiringTime()));
            nItem.addChild("start", getTime(item.getStartTime()));
            nItem.addChild("status", item.getStatus().toString());
            nItem.addChild("prevstatus", item.get_prevStatus());
            nItem.addChild("client", item.get_externalClient());
            nItem.addChild("allowsdynamic", item.allowsDynamicCreation());
            nItem.addChild("manualresourcing", item.requiresManualResourcing());
            nItem.addChild(getWIData(item));
            nItem.addChild("timerstarted", item.hasTimerStarted());
            nItem.addChild("timerexpiry", item.getTimerExpiry());
            nItem.addChild("deferredgroupid", item.getDeferredChoiceGroupID());
            nItem.addChild("codelet", item.getCodelet());
            nItem.addChild("parent", getWIParent(item));
        }
        return nItems;
    }


    private XNode getWIData(YWorkItem item) {
        XNode nData = new XNode("data");
        nData.addContent(item.get_dataString());
        return nData;
    }


    private void addMIDataIfRequired(XNode taskNode, YIdentifier runnerID, String taskName) {
        YNetRunner runner = _engine.getNetRunner(runnerID);
        for (YTask task : runner.getBusyTasks()) {
            if (task.getName().equals(taskName)) {
                if (task.isMultiInstance()) {
                    String doc = task.getMIOutputData().getDataDocString();
                    taskNode.addChild("midata", JDOMUtil.encodeEscapes(doc));
                }
            }
        }
    }


    private String getWIParent(YWorkItem item) {
        YWorkItem parent = item.getParent();
        return parent != null ? parent.get_thisID() : null;
    }

    private long getTime(Date date) {
        return date != null ? date.getTime() : 0;
    }


    private List<YIdentifier> csvToIdentifierList(String csv) {
        if (csv == null) return Collections.emptyList();
        List<YIdentifier> caseList = new ArrayList<>();
        String[] caseArray = csv.split("\\s*,\\s*");
        for (String caseStr : caseArray) {
            if (StringUtil.isNullOrEmpty(caseStr)) continue;
            if (caseStr.contains("-")) {
                caseList.addAll(getCasesInRange(caseStr));
            }
            else {
                addCaseToList(caseList, caseStr);
            }
        }
        return caseList;
    }


    private List<YIdentifier> getCasesInRange(String caseStr) {
        List<YIdentifier> caseList = new ArrayList<>();
        String[] caseArray = caseStr.split("\\s*-\\s*");
        int a = StringUtil.strToInt(caseArray[0], Integer.MAX_VALUE);
        int b = StringUtil.strToInt(caseArray[1], Integer.MAX_VALUE);
        if (a == Integer.MAX_VALUE || b == Integer.MAX_VALUE) {
            return caseList;
        }
        for (int caseNbr = Math.min(a, b); caseNbr <= Math.max(a, b); caseNbr++) {
            addCaseToList(caseList, String.valueOf(caseNbr));
         }
        return caseList;
    }


    private void addCaseToList(List<YIdentifier> caseList, String caseStr) {
        YIdentifier caseID = _engine.getCaseID(caseStr);
        if (caseID  != null) {
            caseList.add(caseID);
        }
    }

}
