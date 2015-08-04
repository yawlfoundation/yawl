package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.StringUtil;

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

        
    public String getCaseID() { return caseID; }

    public void setCaseID(String s) { caseID = s; }


    public YSpecificationID getSpecID() { return specID; }

    public void setSpecID(YSpecificationID id) { specID = id; }


    public String getCaseParams() { return caseParams; }

    public void setCaseParams(String params) { caseParams = params; }


    public String getStartedBy() { return startedBy; }

    public void setStartedBy(String s) { startedBy = s; }


    public long getStartTime() { return startTime; }

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
}
