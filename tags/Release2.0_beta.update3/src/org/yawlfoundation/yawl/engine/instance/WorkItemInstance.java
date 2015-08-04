package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class WorkItemInstance {

    private YWorkItem workItem;
    private String taskID;
    private String id;
    private String status;
    private String resource;
    private String timerStatus;
    private long enabledTime;
    private long startTime;
    private long completionTime;
    private long timerExpiry;
    private Map<String, ParameterInstance> parameters ;

    public WorkItemInstance() {
        parameters = new Hashtable<String, ParameterInstance>();
    }


    public WorkItemInstance(YWorkItem item) {
        this();
        workItem = item;
    }


    public void close() {
        taskID = getTaskID();
        id = getID();
        status = getStatus();
        resource = getResource();
        timerStatus = null;         // todo
        enabledTime = getEnabledTime();
        startTime = getStartTime();
        timerExpiry = getTimerExpiry();
        if (workItem.hasCompletedStatus()) completionTime = System.currentTimeMillis();
        workItem = null;
    }

    public String getTaskID() {
        if (workItem != null) return workItem.getTaskID();
        return taskID;
    }

    public void setTaskID(String s) { taskID = s; }


    public String getID() {
        if (workItem != null) return workItem.getIDString();
        return id;
    }

    public void setID(String s) { id = s; }


    public String getStatus() {
        if (workItem != null) return workItem.getStatus().name();
        return status;
    }

    public void setStatus(String s) { status = s; }


    public String getResource() {
        if (workItem != null) return workItem.getUserWhoIsExecutingThisItem();
        return resource;
    }

    public void setResource(String s) { resource = s; }


    public String getTimerStatus() {
 //       if (workItem != null) return workItem.getTimerStatus();
        return timerStatus;
    }

    public void setTimerStatus(String s) { timerStatus = s; }


    public long getEnabledTime() {
        if (workItem != null) return getDateAsLong(workItem.getEnablementTime());
        return enabledTime;
    }

    public void setEnabledTime(long time) { enabledTime = time; }


    public long getStartTime() {
        if (workItem != null) return getDateAsLong(workItem.getStartTime());
        return startTime;
    }

    public void setStartTime(long time) { startTime = time; }


    public long getCompletionTime() { return completionTime; }

    public void setCompletionTime(long time) { completionTime = time; }


    public long getTimerExpiry() {
        if (workItem != null) return workItem.getTimerExpiry();
        return timerExpiry;
    }

    public void setTimerExpiry(long expiry) { timerExpiry = expiry; }


    public Map<String, ParameterInstance> getParameterMap() {
        return parameters;
    }

    public void setParameterMap(Map<String, ParameterInstance> params) {
        parameters = params;
    }

    public Collection<ParameterInstance> getParameters() {
        return parameters.values();
    }

    public void addParameterInstance(ParameterInstance param) {
        parameters.put(param.getName(), param);
    }

    public ParameterInstance getParameterInstance(String name) {
        return parameters.get(name);
    }


    private long getDateAsLong(Date date) {
        if (date != null) return date.getTime();
        return 0;
    }


    public String marshalParameters() {
        StringBuilder result = new StringBuilder("<parameterInstances>");
        for (ParameterInstance param : parameters.values()) {
            result.append(param.toXML());
        }
        result.append("</parameterInstances>");
        return result.toString();
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<workitemInstance>");
        xml.append(StringUtil.wrap(getID(), "id"));
        xml.append(StringUtil.wrap(getTaskID(), "taskid"));
        xml.append(StringUtil.wrap(getStatus(), "status"));
        xml.append(StringUtil.wrap(getResource(), "resource"));
        xml.append(StringUtil.wrap(getTimerStatus(), "timerStatus"));
        xml.append(StringUtil.wrap(String.valueOf(getEnabledTime()), "enabledtime"));
        xml.append(StringUtil.wrap(String.valueOf(getStartTime()), "starttime"));
        xml.append(StringUtil.wrap(String.valueOf(getCompletionTime()), "completiontime"));
        xml.append(StringUtil.wrap(String.valueOf(getTimerExpiry()), "timerexpiry"));
        xml.append("</workitemInstance>");
        return xml.toString();
    }
     
}
