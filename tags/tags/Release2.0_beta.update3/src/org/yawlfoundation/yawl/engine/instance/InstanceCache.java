package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.engine.YWorkItem;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class InstanceCache extends Hashtable<String, CaseInstance> {


    public void addCase(CaseInstance instance) {
        this.put(instance.getCaseID(), instance);
    }

    public void addCase(String caseID, String specName, String specVersion,
                        String caseParams, String startedBy) {
        this.put(caseID,
                new CaseInstance(caseID, specName, specVersion, caseParams, startedBy));
    }

    public CaseInstance getCase(String caseID) {
        return this.get(caseID);
    }

    public CaseInstance removeCase(String caseID) {
        return this.remove(caseID);
    }

    public Collection<CaseInstance> getCases() {
        return this.values();
    }


    public void addWorkItem(YWorkItem item) {
        CaseInstance instance = getCase(getRootCaseID(item));
        if (instance != null) {
            instance.addWorkItemInstance(item);
        }
        
    }

    public void closeWorkItem(YWorkItem item) {
        CaseInstance instance = getCase(getRootCaseID(item));
        if (instance != null) {
            WorkItemInstance workitem = instance.getWorkItemInstance(item.getIDString());
            if (workitem != null) workitem.close();
        }

    }

    public Collection<WorkItemInstance> getWorkitems(String caseID) {
        CaseInstance instance = getCase(caseID);
        if (instance != null) {
            return instance.getWorkItems();
        }
        return null;
    }


    public Collection<ParameterInstance> getParameters(String caseID, String itemID) {
        WorkItemInstance workitem = getWorkItem(caseID, itemID);
        if (workitem != null) {
            return workitem.getParameters();
        }
        return null;
    }


    public String marshalCases() {
        StringBuilder result = new StringBuilder("<caseInstances>");
        for (CaseInstance instance : this.values()) {
            result.append(instance.toXML());
        }
        result.append("</caseInstances>");
        return result.toString();
    }


    public String marshalWorkItems(String caseID) {
        CaseInstance instance = getCase(caseID);
        if (instance != null) {
            return instance.marshalWorkitems();
        }
        return "<workitems/>";
    }


    public String marshalParameters(String caseID, String itemID) {
        WorkItemInstance workitem = getWorkItem(caseID, itemID);
        if (workitem != null) {
            return workitem.marshalParameters();
        }
        return "<parameters/>";
    }


    public WorkItemInstance getWorkItem(String caseID, String itemID) {
        CaseInstance instance = getCase(caseID);
        return (instance != null) ? instance.getWorkItemInstance(itemID) : null;
    }


    private String getRootCaseID(YWorkItem item) {
        String caseID = item.getCaseID().toString();
        if ((caseID != null) && (caseID.indexOf(".") > 0)) {
            caseID = caseID.split("\\.")[0] ;
        }
        return caseID ;

    }

}

