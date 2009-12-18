/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.instance;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.SimpleDateFormat;
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
    private String caseID;
    private String id;
    private String status;
    private String resourceName;
    private String timerStatus;
    private long enabledTime;
    private long startTime;
    private long completionTime;
    private long timerExpiry;
    private SimpleDateFormat dateFormatter;
    private Map<String, ParameterInstance> parameters ;

    public WorkItemInstance() {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        parameters = new Hashtable<String, ParameterInstance>();
    }


    public WorkItemInstance(YWorkItem item) {
        this();
        workItem = item;
    }

    public WorkItemInstance(String xml) {
        this();
        fromXML(xml);
    }

    public WorkItemInstance(Element instance) {
        this();
        fromXML(instance);
    }


    public void close() {
        taskID = getTaskID();
        id = getID();
        caseID = getCaseID();
        status = getStatus();
        resourceName = getResourceName();
        timerStatus = (getTimerStatus().equals("Nil")) ? "Nil" : "Closed";
        enabledTime = getEnabledTime();
        startTime = getStartTime();
        timerExpiry = getTimerExpiry();
        if (workItem.hasCompletedStatus()) completionTime = System.currentTimeMillis();
        workItem = null;                             // deliberately drop the reference
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

    public String getCaseID() {
        if (workItem != null) return workItem.getCaseID().toString();
        return caseID;
    }


    public String getStatus() {
        if (workItem != null) return workItem.getStatus().name();
        return status;
    }

    public String getPlainStatus() {
        return getStatus().replaceFirst("status", "");
    }

    public void setStatus(String s) { status = s; }


    public String getResourceName() {
        if (workItem != null) {
            YAWLServiceReference service = workItem.getOwnerService();
            if (service != null) resourceName = service.getServiceName();
        }
        return resourceName;
    }

    public void setResourceName(String s) { resourceName = s; }


    public String getTimerStatus() {
        if (workItem != null) return workItem.getTimerStatus();
        return timerStatus;
    }

    public void setTimerExpired() {
        timerStatus = "Expired";
    }

    public void setTimerStatus(String s) { timerStatus = s; }


    public long getEnabledTime() {
        if (workItem != null) return getDateAsLong(workItem.getEnablementTime());
        return enabledTime;
    }

    public String getEnabledTimeAsDateString() {
        if (getEnabledTime() == 0) return "";
        return dateFormatter.format(getEnabledTime());
    }

    public void setEnabledTime(long time) { enabledTime = time; }


    public long getStartTime() {
        if (workItem != null) return getDateAsLong(workItem.getStartTime());
        return startTime;
    }

    public String getStartTimeAsDateString() {
        if (getStartTime() == 0) return "";
        return dateFormatter.format(getStartTime());
    }

    public void setStartTime(long time) { startTime = time; }


    public long getCompletionTime() { return completionTime; }

    public String getCompletionTimeAsDateString() {
        if (getCompletionTime() == 0) return "";
        return dateFormatter.format(getCompletionTime());
    }

    public void setCompletionTime(long time) { completionTime = time; }


    public long getTimerExpiry() {
        if (workItem != null) return workItem.getTimerExpiry();
        return 0;
    }

    public void setTimerExpiry(long expiry) { timerExpiry = expiry; }

    public String getTimerExpiryAsCountdown() {
        if (getTimerExpiry() == 0) return "";
        return formatAge(System.currentTimeMillis() - getTimerExpiry());
    }

        /**
     * formats a long time value into a string of the form 'ddd:hh:mm:ss'
     * @param age the time value (in milliseconds)
     * @return the formatted time string
     */
    public String formatAge(long age) {
        long secsPerHour = 60 * 60 ;
        long secsPerDay = 24 * secsPerHour ;
        age = age / 1000 ;                             // ignore the milliseconds

        long days = age / secsPerDay ;
        age %= secsPerDay ;
        long hours = age / secsPerHour ;
        age %= secsPerHour ;
        long mins = age / 60 ;
        age %= 60 ;                                    // seconds leftover
        return String.format("%d:%02d:%02d:%02d", days, hours, mins, age) ;
    }




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

    public void addParameterInstance(YParameter parameter, String predicate, Element data) {
        ParameterInstance param = new ParameterInstance(parameter, predicate, data);
        parameters.put(param.getName(), param);
    }


    public void addParameters(YTask task, Element data) {
        YDecomposition decomp = task.getDecompositionPrototype();
        if (decomp != null) {
            Map<String, YParameter> paramMap = decomp.getInputParameters();
            for (String name : paramMap.keySet()) {
                String predicate = task.getDataBindingForInputParam(name);
                YParameter param = paramMap.get(name);
                Element paramData = data.getChild(name);
                addParameterInstance(param, predicate, paramData);
            }
        }
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
        xml.append(StringUtil.wrap(getCaseID(), "caseid"));
        xml.append(StringUtil.wrap(getStatus(), "status"));
        xml.append(StringUtil.wrap(getResourceName(), "resource"));
        xml.append(StringUtil.wrap(getTimerStatus(), "timerStatus"));
        xml.append(StringUtil.wrap(String.valueOf(getEnabledTime()), "enabledtime"));
        xml.append(StringUtil.wrap(String.valueOf(getStartTime()), "starttime"));
        xml.append(StringUtil.wrap(String.valueOf(getCompletionTime()), "completiontime"));
        xml.append(StringUtil.wrap(String.valueOf(getTimerExpiry()), "timerexpiry"));
        xml.append("</workitemInstance>");
        return xml.toString();
    }

    public void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }

    public void fromXML(Element instance) {
        if (instance != null) {
            id = instance.getChildText("id");
            taskID = instance.getChildText("taskid");
            caseID = instance.getChildText("caseid");
            status = instance.getChildText("status");
            resourceName = instance.getChildText("resource");
            timerStatus = instance.getChildText("timerStatus");
            enabledTime = strToLong(instance.getChildText("enabledtime"));
            startTime = strToLong(instance.getChildText("starttime"));
            completionTime = strToLong(instance.getChildText("completiontime"));
            timerExpiry = strToLong(instance.getChildText("timerexpiry"));
        }
    }

    private long strToLong(String s) {
        long result = 0;
        if (s != null) {
            try {
                result = new Long(s);
            }
            catch (NumberFormatException ignore) {}
        }
        return result;
    }

}
