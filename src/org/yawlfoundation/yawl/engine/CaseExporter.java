package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Date;
import java.util.Map;
import java.util.Set;

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
        XNode root = new XNode("cases");
        for (YIdentifier caseID : _engine.getRunningCaseIDs()) {
            root.addChild(getCaseNode(caseID));
        }
        return root.toString();
    }


    public String export(YIdentifier caseID) {
        XNode root = new XNode("cases");
        root.addChild(getCaseNode(caseID));
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
            nRunner.addChild(getMarkedTasks("enabled", runner.getEnabledTaskNames()));
            nRunner.addChild(getMarkedTasks("busy", runner.getBusyTaskNames()));
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


    private XNode getMarkedTasks(String tag, Set<String> taskNames) {
        XNode nTasks = new XNode(tag + "tasks");
        for (String taskName : taskNames) {
            nTasks.addChild("task", taskName);
        }
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
        nData.addContent(item.getDataString());
        return nData;
    }


    private String getWIParent(YWorkItem item) {
        YWorkItem parent = item.getParent();
        return parent != null ? parent.get_thisID() : null;
    }

    private long getTime(Date date) {
        return date != null ? date.getTime() : 0;
    }

}
