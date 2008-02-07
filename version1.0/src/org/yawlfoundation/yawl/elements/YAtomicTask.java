/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * 
 * A YAtomicTask object is the executable equivalent of the YAtomicTask
 * in the YAWL paper.   They have the same properties and behaviour.
 * @author Lachlan Aldred
 * 
 */
public class YAtomicTask extends YTask {


    public YAtomicTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }


    public void setDataMappingsForEnablement(Map map) {
        _dataMappingsForTaskEnablement.putAll(map);
    }


    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (_decompositionPrototype == null) {
            if (_multiInstAttr != null && (_multiInstAttr.getMaxInstances() > 1 ||
                    _multiInstAttr.getThreshold() > 1)) {
                messages.add(new YVerificationMessage(this, this + " cannot have multiInstances and a "
                        + " blank work description.", YVerificationMessage.ERROR_STATUS));
            }
        } else if (!(_decompositionPrototype instanceof YAWLServiceGateway)) {
            messages.add(new YVerificationMessage(this, this + " task may not decompose to " +
                    "other than a WebServiceGateway.", YVerificationMessage.ERROR_STATUS));
            messages.addAll(checkEnablementParameterMappings());
        }
        return messages;
    }

    private Collection checkEnablementParameterMappings() {
        List messages = new ArrayList();
        if (this instanceof YAtomicTask) {
            //check that there is a link to each enablementParam
            Set enablementParamNamesAtGateway =
                    ((YAWLServiceGateway) _decompositionPrototype).getEnablementParameterNames();
            Set enablementParamNamesAtTask = _dataMappingsForTaskEnablement.keySet();
            //check that task input var maps to decomp input var
            for (Iterator iterator = enablementParamNamesAtGateway.iterator(); iterator.hasNext();) {
                String paramName = (String) iterator.next();
                if (!enablementParamNamesAtTask.contains(paramName)) {
                    messages.add(new YVerificationMessage(this,
                            "The task (id= " + this.getID() + ")" +
                            " needs to be connected with the enablement parameter (" +
                            paramName + ")" + " of decomposition (" +
                            _decompositionPrototype + ").",
                            YVerificationMessage.ERROR_STATUS));
                }
            }
            for (Iterator iterator = enablementParamNamesAtTask.iterator(); iterator.hasNext();) {
                String paramNameAtTask = (String) iterator.next();

                String query = (String) _dataMappingsForTaskEnablement.get(paramNameAtTask);
                messages.addAll(checkXQuery(query, paramNameAtTask));
                if (!enablementParamNamesAtGateway.contains(paramNameAtTask)) {
                    messages.add(new YVerificationMessage(this,
                            "The task (id= " + this.getID() + ") " +
                            "cannot connect with enablement parameter (" +
                            paramNameAtTask + ") because it doesn't exist" +
                            " at its decomposition(" + _decompositionPrototype + ").",
                            YVerificationMessage.ERROR_STATUS));
                }
            }
        }
        return messages;
    }


    protected void startOne(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        this._mi_entered.removeOne(pmgr, id);
        this._mi_executing.add(pmgr, id);
    }


    public boolean isRunning() {
        return _i != null;
    }


    public synchronized void cancel(YPersistenceManager pmgr, YIdentifier caseID) throws YPersistenceException {
        super.cancel(pmgr);
         if (_decompositionPrototype != null) {
            YWorkItem workItem;
            if (_i != null) {  //work item has fired
                workItem = _workItemRepository.getWorkItem(_i.toString(), getID());
            } else { //check enabled work items
                //construct the workitem id
                String workItemID = caseID.get_idString() + ":" + getID();
                workItem = _workItemRepository.getWorkItem(workItemID);
            }
            if (null != workItem) {
                _workItemRepository.removeWorkItemFamily(workItem);
                workItem.cancel(pmgr);
                //if applicable cancel yawl service
                YAWLServiceGateway wsgw = (YAWLServiceGateway) getDecompositionPrototype();
                if (wsgw != null) {
                    YAWLServiceReference ys = wsgw.getYawlService();
                    if (ys != null) {
                        YEngine.getInstance().announceCancellationToEnvironment(ys, workItem);
                    }
                }
            }
        }
    }

    public boolean t_rollBackToFired(YPersistenceManager pmgr, YIdentifier caseID) throws YPersistenceException {
        if (_mi_executing.contains(caseID)) {
            _mi_executing.removeOne(pmgr, caseID);
            _mi_entered.add(pmgr, caseID);
            return true;
        }
        return false;
    }


    public YNet getNet() {
        return _net;
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YAtomicTask copy = (YAtomicTask) super.clone();
        return copy;
    }

    public Map getDataMappingsForEnablement() {
        return _dataMappingsForTaskEnablement;
    }

    public Element prepareEnablementData()
            throws YQueryException, YSchemaBuildingException, YDataStateException, YStateException {
        if (null == getDecompositionPrototype()) {
            return null;
        }
        Element enablementData = produceDataRootElement();
        YAWLServiceGateway serviceGateway = (YAWLServiceGateway) _decompositionPrototype;
        List enablementParams = new ArrayList(serviceGateway.getEnablementParameters().values());
        Collections.sort(enablementParams);
        for (int i = 0; i < enablementParams.size(); i++) {
            YParameter parameter = (YParameter) enablementParams.get(i);
            String paramName = parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();
            String expression = (String) _dataMappingsForTaskEnablement.get(paramName);

            Element result = performDataExtraction(expression, parameter);
            enablementData.addContent((Element) result.clone());
        }
        return enablementData;
    }
}
