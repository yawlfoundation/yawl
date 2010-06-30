/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;

/**
 * A YAtomicTask object is the executable equivalent of the Atomic Task
 * in the YAWL language. They have the same properties and behaviour.
 *
 * @author Lachlan Aldred
 * @author Michael Adams (v2.0 and later)
 */
public class YAtomicTask extends YTask {

    private static Logger logger = Logger.getLogger(YAtomicTask.class);

    /**
     * Constructs a new atomic task.
     * @param id the task identifier.
     * @param joinType the task's join type.
     * @param splitType the task's split type.
     * @param container the task's containing net.
     * @see YTask
     */
    public YAtomicTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }


    /**
     * Sets the enablement data mappings for an atomic task to those specified.
     * @param map a map of [variable name, query] pairs.
     * @deprecated Since 2.0, enablement mappings have no function.
     */
    public void setDataMappingsForEnablement(Map<String, String> map) {
        _dataMappingsForTaskEnablement.putAll(map);
    }


    /**
     * Changes the inner state of an atomic task from entered to executing.
     * @param pmgr an instantiated persistence manager object.
     * @param id the identifier to move from inner marking 'entered' to inner marking
     * 'executing'.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    protected void startOne(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        this._mi_entered.removeOne(pmgr, id);
        this._mi_executing.add(pmgr, id);
    }


    /**
     * Checks that a task is currently executing.
     * @return true if the task has 'executing' state.
     */
    public boolean isRunning() {
        return _i != null;
    }


    /**
     * Cancels a task.
     * @param pmgr an instantiated persistence manager object.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        cancelBusyWorkItem(pmgr);
        super.cancel(pmgr);
    }


    /**
     * Cancels the task. If the task is not currently executing, first check that the
     * workitem is still referenced by the engine before attempting the cancellation.
     * @param pmgr an instantiated persistence manager object.
     * @param caseID the case identifier of this atomic task.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public synchronized void cancel(YPersistenceManager pmgr, YIdentifier caseID)
            throws YPersistenceException {
        if (! cancelBusyWorkItem(pmgr)) {
            String workItemID = caseID.get_idString() + ":" + getID();
            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (null != workItem) cancelWorkItem(pmgr, workItem) ;
        }
        super.cancel(pmgr);
    }


    /**
     * Rolls back a task's inner state from 'executing' to 'entered'.
     * <p/>
     * The roll back will only occur if the task's inner 'executing' marking contains
     * the identifier specified.
     * @param pmgr an instantiated persistence manager object.
     * @param caseID the case identifier of this atomic task.
     * @return true if the rollback occurs, false if the rollback request could not
     * be actioned because the task's inner 'executing' marking does not contain 
     * the identifier specified.
     * @throws YPersistenceException if there's a problem persisting the change.
     */
    public boolean t_rollBackToFired(YPersistenceManager pmgr, YIdentifier caseID)
            throws YPersistenceException {
        if (_mi_executing.contains(caseID)) {
            _mi_executing.removeOne(pmgr, caseID);
            _mi_entered.add(pmgr, caseID);
            return true;
        }
        return false;
    }


    /**
     * Gets the net that contains this atomic task.
     * @return the containing net.
     */
    public YNet getNet() {
        return _net;
    }


    /**
     * Clones this atomic task.
     * @return a (Object) clone of the task.
     * @throws CloneNotSupportedException if there's a problem cloning the task.
     */
    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        return super.clone();
    }


    /**
     * Gets the map of enablement mappings for the task.
     * @return the map of enablement mappings.
     * @deprecated Since 2.0, enablement mappings have no function.
     */
    public Map getDataMappingsForEnablement() {
        return _dataMappingsForTaskEnablement;
    }


    /**
     * Builds the enablement data set for the task.
     * @return the enablement data set; that is, a set of data variables and
     * corresponding values that were populated after evaluating their mapping
     * expressions.
     * @throws YQueryException if thre's a problem with a query evaluation.
     * @throws YSchemaBuildingException if there's a problem populating the data set.
     * @throws YDataStateException if there's a problem with the evaluated data.
     * @throws YStateException if there's a problem setting the task state.
     * @deprecated Since 2.0, enablement mappings have no function.
     */
    public Element prepareEnablementData()
            throws YQueryException, YSchemaBuildingException, YDataStateException,
            YStateException {
        if (null == getDecompositionPrototype()) {
            return null;
        }
        Element enablementData = produceDataRootElement();
        YAWLServiceGateway serviceGateway = (YAWLServiceGateway) _decompositionPrototype;
        List<YParameter> enablementParams =
                new ArrayList<YParameter>(serviceGateway.getEnablementParameters().values());
        Collections.sort(enablementParams);
        for (YParameter parameter : enablementParams) {
            String paramName = parameter.getPreferredName();
            String expression = _dataMappingsForTaskEnablement.get(paramName);
            Element result = performDataExtraction(expression, parameter);
            enablementData.addContent((Element) result.clone());
        }
        return enablementData;
    }


    /**
     * Verifies this atomic task definition against YAWL semantics.
     * @return a List of error and/or warning messages. An empty list is returned if
     * the atomic task verifies successfully.
     */
    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (_decompositionPrototype == null) {
            if (_multiInstAttr != null && (_multiInstAttr.getMaxInstances() > 1 ||
                    _multiInstAttr.getThreshold() > 1)) {
                messages.add(new YVerificationMessage(this, this + " cannot have multiInstances and a "
                        + " blank work description.", YVerificationMessage.ERROR_STATUS));
            }
        }
        else if (!(_decompositionPrototype instanceof YAWLServiceGateway)) {
            messages.add(new YVerificationMessage(this, this + " task may not decompose to " +
                    "other than a WebServiceGateway.", YVerificationMessage.ERROR_STATUS));
            messages.addAll(checkEnablementParameterMappings());
        }
        return messages;
    }


    private synchronized boolean cancelBusyWorkItem(YPersistenceManager pmgr)
                                                         throws YPersistenceException {

        // nothing to do if not fired or has no decomposition
        if ((_i == null) || (_decompositionPrototype == null)) return false;

        YWorkItem workItem = _workItemRepository.getWorkItem(_i.toString(), getID());
        if (null != workItem) cancelWorkItem(pmgr, workItem) ;
        return true;
    }


    private synchronized void cancelWorkItem(YPersistenceManager pmgr,
                                             YWorkItem workItem)
            throws YPersistenceException {
        _workItemRepository.removeWorkItemFamily(workItem);
        workItem.cancel(pmgr);
        YEngine.getInstance().getAnnouncer().announceCancelledWorkItem(workItem);
    }


    /**
     * Verify this atomic task's enablement mappings.
     * @return a List of error and/or warning messages. An empty list is returned if
     * the atomic task's enablement mappings verify successfully.
     * @deprecated Since 2.0, enablement mappings have no function.
     */
    private List<YVerificationMessage> checkEnablementParameterMappings() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();

        //check that there is a link to each enablementParam
        Set<String> enablementParamNamesAtGateway =
                ((YAWLServiceGateway) _decompositionPrototype).getEnablementParameterNames();
        Set<String> enablementParamNamesAtTask = _dataMappingsForTaskEnablement.keySet();

        //check that task input var maps to decomp input var
        for (String paramName : enablementParamNamesAtGateway) {
            if (! enablementParamNamesAtTask.contains(paramName)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() +
                                ") needs to be connected with the enablement parameter (" +
                                paramName + ") of decomposition (" +
                                _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        for (String paramNameAtTask : enablementParamNamesAtTask) {
            String query = _dataMappingsForTaskEnablement.get(paramNameAtTask);
            messages.addAll(checkXQuery(query, paramNameAtTask));
            if (! enablementParamNamesAtGateway.contains(paramNameAtTask)) {
                messages.add(new YVerificationMessage(this,
                        "The task (id= " + this.getID() +
                                ") cannot connect with enablement parameter (" +
                                paramNameAtTask + ") because it doesn't exist" +
                                " at its decomposition (" + _decompositionPrototype + ").",
                        YVerificationMessage.ERROR_STATUS));
            }
        }

        return messages;
    }

}
