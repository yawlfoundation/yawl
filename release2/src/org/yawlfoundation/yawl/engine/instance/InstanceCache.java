/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.instance;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Manages a dataset of all 'live' case instances, including their workitems (live and
 * completed) and the data parameters of those workitems.
 *
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class InstanceCache extends Hashtable<String, CaseInstance> {


    // CASE CACHE //

    // Case records are added when a case starts, and removed when the case completes
    // or is cancelled.

    public void addCase(CaseInstance instance) {
        this.put(instance.getCaseID(), instance);
    }


    public void addCase(String caseID, YSpecificationID specID,
                        String caseParams, YLogDataItemList logData, long startTime) {
        if (caseID != null) {
            this.put(caseID, new CaseInstance(caseID, specID, caseParams, logData, startTime));
        }
    }


    public CaseInstance getCase(String caseID) {
        return this.get(caseID);
    }


    public Collection<CaseInstance> getCases() {
        return this.values();
    }


    public CaseInstance removeCase(String caseID) {
        return this.remove(caseID);
    }

    /**************************************************************************/

    // WORKITEM CACHE //

    // Workitems are added when they are enabled, and 'closed' when the workitem
    // reaches a finished status - at which point a copy of their descriptors are
    // stored. There is no need for a remove method as they are discarded when their
    // parent case is removed.

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


    public void setTimerExpired(YWorkItem item) {
        CaseInstance instance = getCase(getRootCaseID(item));
        if (instance != null) {
            WorkItemInstance workitem = instance.getWorkItemInstance(item.getIDString());
            workitem.setTimerExpired();
        }
    }


    public Collection<WorkItemInstance> getWorkitems(String caseID) {
        CaseInstance instance = getCase(caseID);
        if (instance != null) {
            return instance.getWorkItems();
        }
        return null;
    }


    public WorkItemInstance getWorkItemInstance(String caseID, String itemID) {
        CaseInstance instance = getCase(caseID);
        return (instance != null) ? instance.getWorkItemInstance(itemID) : null;
    }


    /*************************************************************************/

    // PARAMETER CACHE //

    // Parameters are added when their parent workitem is enabled. Like workitems, there
    // is no remove method - they are discarded when the case completes or cancels.

//    public void addParameter(YIdentifier identifier, YParameter parameter,
//                             String predicate, Element data) {
//
//        // workitem will always have at least 2 conditions
//        YInternalCondition condition = (YInternalCondition) identifier.getLocations().get(0);
//        String itemID = identifier.get_idString() + ":" + condition._myTask.getID();
//        WorkItemInstance workitem = getWorkItemInstance(identifier.get_idString(),
//                                                        itemID);
//        if (workitem != null) {
//            workitem.addParameterInstance(parameter, predicate, data);
//        }
//    }

    
    public void addParameters(YWorkItem workitem, YTask task, Element data) {
        String caseID;
        YWorkItem parent = workitem.getParent();
        if (parent != null)
            caseID = parent.getCaseID().toString();
        else
            caseID = workitem.getCaseID().toString();

        WorkItemInstance instance = getWorkItemInstance(caseID, workitem.getIDString());
        if (instance != null) {
            instance.addParameters(task, data);
        }
    }


    public Collection<ParameterInstance> getParameters(String caseID, String itemID) {
        WorkItemInstance workitem = getWorkItemInstance(caseID, itemID);
        if (workitem != null) {
            return workitem.getParameters();
        }
        return null;
    }


    /****************************************************************************/

    // MARSHALING METHODS //

    // These methods each return an xml'd string summarising the caches contents
    // at the appropriate level of granularity

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
        WorkItemInstance workitem = getWorkItemInstance(caseID, itemID);
        if (workitem != null) {
            return workitem.marshalParameters();
        }
        return "<parameters/>";
    }


    /*****************************************************************************/

    // PRIVATE METHODS //

    private String getRootCaseID(YWorkItem item) {
        String caseID = item.getCaseID().toString();
        if ((caseID != null) && (caseID.indexOf(".") > 0)) {
            caseID = caseID.split("\\.")[0] ;
        }
        return caseID ;
    }

}

