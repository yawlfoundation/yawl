package org.yawlfoundation.yawl.simulation;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.*;

class SimulatorProperties {

    private YSpecificationID specID;
    private YSimulator.SimulationType simType;
    private int caseCount;
    private long interval;
    private Map<String, Map<String, TaskResourceSettings>> tasks;   // [taskName, pid, TaskResourceSettings]
    private Map<String, ResourceLimit> resources;                  // [pid, time limit]

    private static final int DEFAULT_PROCESSING_TIME = 5000;
    private YSimulator ySimulator;


    SimulatorProperties(YSimulator ySimulator) {
        this.ySimulator = ySimulator;
        tasks = new Hashtable<String, Map<String, TaskResourceSettings>>();
        resources = new HashMap<String, ResourceLimit>();
    }


    protected YSpecificationID getSpecID() { return specID; }

    protected YSimulator.SimulationType getSimType() { return simType; }

    protected Set<String> getResources() { return resources.keySet(); }

    protected int getCaseCount() { return caseCount; }

    protected long getInterval() { return interval; }

    protected ResourceLimit getLimit(String pid) { return resources.get(pid); }


    protected int getProcessingTime(String task, String pid) {
        TaskResourceSettings resourceSettings = getResourceSettings(task, pid);
        return (resourceSettings != null) ? resourceSettings.getTiming() : DEFAULT_PROCESSING_TIME;
    }


    protected int getConcurrent(String task, String pid) {
        TaskResourceSettings resourceSettings = getResourceSettings(task, pid);
        return resourceSettings != null ? resourceSettings.getConcurrent() : 1;
    }


    protected void parse(String configFile) throws ResourceGatewayException, IOException {
        ySimulator.print("Parsing configuration...");
        String xml = StringUtil.fileToString(configFile);
        if (xml == null)
            ySimulator.fail("Failed to load config file from: " + configFile);
        XNode node = new XNodeParser().parse(xml);
        if (node == null) ySimulator.fail("Failed to parse config file");

        parseSimType(node);
        parseInterval(node);
        parseCaseCount(node);
        parseSpecID(node);
        parseServer(node);
        parseTasks(node);
        parseLimits(node);
    }


    private void parseSimType(XNode node) {
        String type = node.getAttributeValue("view");
        if (type == null)
            ySimulator.fail("Config file does not specify a simulation view");
        else if (type.equals("workitem")) simType = YSimulator.SimulationType.Workitem;
        else if (type.equals("resource")) simType = YSimulator.SimulationType.Resource;
        else if (type.equals("process")) simType = YSimulator.SimulationType.Process;
        else ySimulator.fail("Invalid simulation type in config: " + type);
    }

    // defaults to no waiting
    private void parseInterval(XNode node) {
        interval = StringUtil.strToInt(node.getAttributeValue("interval"), 0);
    }

    private void parseCaseCount(XNode node) {
        caseCount = StringUtil.strToInt(node.getAttributeValue("instances"), 0);
        if (caseCount < 1) ySimulator.fail("Invalid instances value in config");
    }

    private void parseSpecID(XNode node) {
        XNode specNode = node.getChild("specification");
        if (specNode == null) {
            ySimulator.fail("Config file does not contain a specification ID");
        } else {
            String id = specNode.getChildText("id");
            String version = specNode.getChildText("version");
            String uri = specNode.getChildText("name");
            if (id == null || version == null || uri == null) {
                ySimulator.fail("Invalid specification ID in config file");
            } else specID = new YSpecificationID(id, version, uri);
        }
    }

    private void parseServer(XNode node) {
        String server = node.getChildText("host");
        String serverUrl = "http://" + (server == null ? "localhost" : server) +
                YSimulator.DEFAULT_URL;
        ySimulator.connect(serverUrl);
    }

    private void parseTasks(XNode node) throws ResourceGatewayException, IOException {
        XNode tasksNode = node.getChild("tasks");
        for (XNode taskNode : tasksNode.getChildren()) {
            String taskID = taskNode.getAttributeValue("id");
            for (XNode resourceNode : taskNode.getChildren()) {
                String time = resourceNode.getAttributeValue("time");
                String deviation = resourceNode.getAttributeValue("deviation");
                String userID = resourceNode.getAttributeValue("userid");
                int concurrent = StringUtil.strToInt(
                        resourceNode.getAttributeValue("concurrent"), 1);
                if (userID != null) {
                    addTaskResource(taskID, ySimulator.getParticipantID(userID),
                            time, deviation, concurrent);
                } else {
                    String roleName = resourceNode.getAttributeValue("role");
                    if (roleName != null) {
                        for (String pid : ySimulator.getPIDsForRole(roleName)) {
                            addTaskResource(taskID, pid, time, deviation, concurrent);
                        }
                    }
                }
            }
        }
    }


    private void parseLimits(XNode node) throws ResourceGatewayException, IOException {
        XNode limitsNode = node.getChild("limits");
        int defLimit = StringUtil.strToInt(limitsNode.getAttributeValue("default"), -1);
        for (XNode resourceNode : limitsNode.getChildren()) {
            int limit = StringUtil.strToInt(resourceNode.getAttributeValue("limit"), -1);
            String userid = resourceNode.getAttributeValue("userid");
            if (userid != null) {
                addLimit(ySimulator.getParticipantID(userid), limit);
            } else {
                String roleName = resourceNode.getAttributeValue("role");
                if (roleName != null) {
                    for (String pid : ySimulator.getPIDsForRole(roleName)) {
                        addLimit(pid, limit);
                    }
                }
            }
        }
        setDefaultLimits(defLimit);
    }


    private void addLimit(String pid, int limit) {
        if (resources.containsKey(pid)) {
            resources.put(pid, new ResourceLimit(limit));
        }
    }


    private void setDefaultLimits(int defaultLimit) {
        for (String pid : resources.keySet()) {
            ResourceLimit limits = resources.get(pid);
            if (limits == null) {
                resources.put(pid, new ResourceLimit(defaultLimit));
            }
        }

    }


    private void addTaskResource(String taskID, String pid, String time,
                                 String deviation, int concurrent) {
        int iTime = StringUtil.strToInt(time, -1);
        if (iTime < 0) ySimulator.fail("Invalid time value for resource: " +
                    taskID + ", " + pid + ", " + iTime);
        int iDeviation = StringUtil.strToInt(deviation, 0);
        if (iDeviation < 0) ySimulator.fail("Invalid deviation value for resource: " +
                    taskID + ", " + pid + ", " + iDeviation);
        if (iDeviation > iTime) ySimulator.fail(
                "Deviation value exceeds time value for resource: " +
                    taskID + ", " + pid + ", " + iDeviation);

        TaskResourceSettings resourceSettings = new TaskResourceSettings();
        resourceSettings.setConcurrent(concurrent);
        resourceSettings.addTiming(iTime, iDeviation);

        Map<String, TaskResourceSettings> timeMap = tasks.get(taskID);
        if (timeMap == null) {
            timeMap = new Hashtable<String, TaskResourceSettings>();
            tasks.put(taskID, timeMap);
        }

        // don't overwrite individual settings with role duplicate
        if (!timeMap.containsKey(pid)) {
            timeMap.put(pid, resourceSettings);
        }

        resources.put(pid, null);
    }

    
    protected TaskResourceSettings getResourceSettings(String task, String pid) {
        Map<String, TaskResourceSettings> settingsMap = tasks.get(task);
        return (settingsMap != null) ? settingsMap.get(pid) : null;
    }

}
