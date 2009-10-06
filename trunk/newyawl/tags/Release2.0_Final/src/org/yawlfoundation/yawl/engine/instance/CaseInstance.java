/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.instance;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class CaseInstance {

    private String caseID;
    private YSpecificationID specID;
    private String caseParams;
    private String startedBy;
    private long startTime;
    private Map<String, WorkItemInstance> workitems ;


    public CaseInstance() {
        workitems = new Hashtable<String, WorkItemInstance>();
    }

    public CaseInstance(String caseID, String specName, String specVersion,
                        String caseParams, String startedBy) {
        this();
        this.caseID = caseID;
        this.specID = new YSpecificationID(specName, specVersion);
        this.caseParams = caseParams;
        this.startedBy = startedBy;
        this.startTime = System.currentTimeMillis();
    }

    public CaseInstance(String xml) {
        this();
        fromXML(xml);
    }

    public CaseInstance(Element instance) {
        this();
        fromXML(instance);
    }


    public String getCaseID() { return caseID; }

    public void setCaseID(String s) { caseID = s; }


    public YSpecificationID getSpecID() { return specID; }

    public void setSpecID(YSpecificationID id) { specID = id; }

    public String getSpecName() { return specID.getSpecName(); }

    public String getSpecVersion() { return specID.getVersionAsString(); }


    public String getCaseParams() { return caseParams; }

    public void setCaseParams(String params) { caseParams = params; }


    public String getStartedBy() { return startedBy; }

    public void setStartedBy(String s) { startedBy = s; }


    public long getStartTime() { return startTime; }

    public String getStartTimeAsDateString() {
        String result = null;
        if (startTime > 0) {
            result = new SimpleDateFormat("yyyy-MM-dd H:mm:ss").format(startTime);
        }
        return result;
    }

    public String getAgeAsDateString() {
        String result = null;
        if (startTime > 0) {
            result = formatAge(System.currentTimeMillis() - startTime);
        }
        return result;
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


    public void setStartTime(long time) { startTime = time; }


    public Map<String, WorkItemInstance> getWorkitemMap() {
        return workitems;
    }

    public void setWorkitemMap(Map<String, WorkItemInstance> items) {
        workitems = items;
    }

    public Collection<WorkItemInstance> getWorkItems() {
        return workitems.values();
    }

    public void addWorkItemInstance(WorkItemInstance item) {
        workitems.put(item.getID(), item);
    }

    public void addWorkItemInstance(YWorkItem workitem) {
        addWorkItemInstance(new WorkItemInstance(workitem));
    }

    public WorkItemInstance getWorkItemInstance(String id) {
        return workitems.get(id);
    }


    public String marshalWorkitems() {
        StringBuilder result = new StringBuilder("<workitemInstances>");
        for (WorkItemInstance item : workitems.values()) {
            result.append(item.toXML());
        }
        result.append("</workitemInstances>");
        return result.toString();
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<caseInstance>");
        xml.append(StringUtil.wrap(caseID, "caseid"));
        xml.append(StringUtil.wrap(specID.getSpecName(), "specname"));
        xml.append(StringUtil.wrap(specID.getVersionAsString(), "specversion"));
        xml.append(StringUtil.wrap(startedBy, "startedby"));
        xml.append(StringUtil.wrap(String.valueOf(startTime), "starttime"));
        xml.append("</caseInstance>");
        return xml.toString();
    }

    public void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }

    public void fromXML(Element instance) {
        if (instance != null) {
            caseID = instance.getChildText("caseid");
            startedBy = instance.getChildText("startedby");
            String startStr = instance.getChildText("starttime");
            if (startStr != null) startTime = new Long(startStr);
            String specName = instance.getChildText("specname");
            String version = instance.getChildText("specversion");
            specID = new YSpecificationID(specName, version);
        }
    }
}
