package org.yawlfoundation.yawl.stateless.monitor;

import org.jdom2.Element;
import org.yawlfoundation.yawl.stateless.YStatelessEngine;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.YTimerParameters;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 30/6/2022
 */
public class YCaseExporter {

    public YCaseExporter() {  }


    public String marshal(YNetRunner runner) {
        Element eCase = new Element("case");
        eCase.setAttribute("id", runner.getCaseID().toString());
        eCase.addContent(stringToClonedElement(marshalSpecification(runner)));
        eCase.addContent(stringToClonedElement(marshalRunner(runner).toString()));
        return JDOMUtil.elementToString(eCase);
    }


    private Element stringToClonedElement(String xml) {
        return JDOMUtil.stringToElement(xml).clone();
    }


    private String marshalSpecification(YNetRunner runner) {
        return YMarshal.marshal(runner.getNet().getSpecification());
    }


    private XNode marshalRunner(YNetRunner topRunner) {
        XNode nRunners = new XNode("runners");
        for (YNetRunner runner : topRunner.getAllRunnersForCase()) {
            YIdentifier id = runner.getCaseID();
            XNode nRunner = nRunners.addChild("runner");
            nRunner.addChild(marshalParent(id));
            nRunner.addChild(marshalIdentifier(id));
            nRunner.addChild(marshalNetData(runner));
            nRunner.addChild("containingtask", runner.getContainingTaskID());
            nRunner.addChild("starttime", runner.getStartTime());
            nRunner.addChild("executionstatus", runner.getExecutionStatus());
            nRunner.addChild(marshalMarkedTasks("enabled", runner.getEnabledTasks()));
            nRunner.addChild(marshalMarkedTasks("busy", runner.getBusyTasks()));
            nRunner.addChild(marshalTimerStates(runner.get_timerStates()));

            nRunner.addChild(marshalWorkItems(runner));
        }
        return nRunners;
    }


    private XNode marshalWorkItems(YNetRunner runner) {
        XNode nItems = new XNode("workitems");
        for (YWorkItem item : runner.getWorkItemRepository().getWorkItems()) {
            XNode nItem = nItems.addChild("item");
            nItem.addChild("id", item.get_thisID());
            nItem.addChild("taskid", item.getTaskID());
            nItem.addChild("enablement", getTime(item.getEnablementTime()));
            nItem.addChild("firing", getTime(item.getFiringTime()));
            nItem.addChild("start", getTime(item.getStartTime()));
            nItem.addChild("status", item.getStatus().toString());
            nItem.addChild("prevstatus", item.get_prevStatus());
            nItem.addChild("allowsdynamic", item.allowsDynamicCreation());
            nItem.addChild("manualresourcing", item.requiresManualResourcing());
            nItem.addChild(marshalWIData(item));
            nItem.addChild("timerstarted", item.hasTimerStarted());
            nItem.addChild("timerexpiry", item.getTimerExpiry());
            nItem.addChild("deferredgroupid", item.getDeferredChoiceGroupID());
            nItem.addChild("codelet", item.getCodelet());
            nItem.addChild("parent", getWIParent(item));

            YTimerParameters timerParameters = item.getTimerParameters();
            if (timerParameters != null) {
                nItem.addChild("timerparameters", timerParameters.toXML());
            }
        }
        return nItems;
    }


    private XNode marshalParent(YIdentifier id) {
        YIdentifier parent = id.getParent();
        String parentID = parent != null ? parent.toString() : null;
        return new XNode("parent", parentID);
    }


    private XNode marshalIdentifier(YIdentifier id) {
        XNode nID = new XNode("identifier");
        nID.addAttribute("id", id);
        nID.addChild(marshalLocations(id));
        XNode nChildren = nID.addChild("children");
        for (YIdentifier child : id.getChildren()) {
            nChildren.addChild(marshalIdentifier(child));
        }
        return nID;
    }


    private XNode marshalLocations(YIdentifier id) {
        XNode nLocations = new XNode("locations");
        for (String location : id.getLocationNames()) {
            nLocations.addChild("location", location);
        }
        return nLocations;
    }


    private XNode marshalNetData(YNetRunner runner) {
        XNode nData = new XNode("netdata");
        nData.addContent(JDOMUtil.encodeEscapes(runner.getNetData().getData()));
        return nData;
    }


    private XNode marshalMarkedTasks(String tag, Set<YTask> tasks) {
        XNode nTasks = new XNode(tag + "tasks");
        for (YTask task : tasks) {
            nTasks.addChild("task", task.getID());
        }
        return nTasks;
    }


    private XNode marshalTimerStates(Map<String, String> timerStates) {
        XNode nTimers = new XNode("timerstates");
        for (String taskName : timerStates.keySet()) {
            XNode nState = nTimers.addChild("timerstate");
            nState.addChild("taskname", taskName);
            nState.addChild("state", timerStates.get(taskName));
        }
        return nTimers;
    }


    private XNode marshalWIData(YWorkItem item) {
        XNode nData = new XNode("data");
        nData.addContent(JDOMUtil.encodeEscapes(item.getDataString()));
        return nData;
    }


    private long getTime(Date date) {
        return date != null ? date.getTime() : 0;
    }


    private String getWIParent(YWorkItem item) {
        YWorkItem parent = item.getParent();
        return parent != null ? parent.get_thisID() : null;
    }


    // test
    public static void main(String[] args) {
        YStatelessEngine engine = new YStatelessEngine();
        String specFile = "/Users/adamsmj/Documents/temp/simpleTimerSpec.yawl" ;
        String specXML = StringUtil.fileToString(specFile);
        try {

            // we have to first transform the XML to a YSpecification object
            YSpecification spec = engine.unmarshalSpecification(specXML);

            YNetRunner runner = engine.launchCase(spec);
            YCaseExporter exporter = new YCaseExporter();

            long startTime = System.nanoTime();
            String xml = exporter.marshal(runner);
            long endTime = System.nanoTime();
            System.out.println("Duration (msecs): " + ((endTime - startTime) / 1000000));
        //    System.out.println(xml);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
