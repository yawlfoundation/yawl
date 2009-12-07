/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;


/**
 * 
 * A YAtomicTask object is the executable equivalent of the Atomic Task
 * in the YAWL paper.   They have the same properties and behaviour.
 *
 * @author Lachlan Aldred
 * @author Michael Adams (v2.0 updates)
 */
public class YAtomicTask extends YTask {

    private static Logger logger = Logger.getLogger(YAtomicTask.class);

    public YAtomicTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }


    public void setDataMappingsForEnablement(Map<String, String> map) {
        _dataMappingsForTaskEnablement.putAll(map);
    }


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


    protected void startOne(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        this._mi_entered.removeOne(pmgr, id);
        this._mi_executing.add(pmgr, id);
    }


    public boolean isRunning() {
        return _i != null;
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

        // if applicable cancel yawl service
        YAWLServiceGateway wsgw = (YAWLServiceGateway) getDecompositionPrototype();
        if (wsgw != null) {
            YAWLServiceReference ys = wsgw.getYawlService();
            if (ys == null) ys = YEngine.getInstance().getDefaultWorklist();

            try {
                Announcements<CancelWorkItemAnnouncement> announcements =
                                         new Announcements<CancelWorkItemAnnouncement>();
                announcements.addAnnouncement(new CancelWorkItemAnnouncement(ys, workItem));
                YEngine.getInstance().announceCancellationToEnvironment(announcements);
            }
            catch (YStateException e) {
                logger.error("Failed to announce cancellation of workitem '" +
                              workItem.getIDString() + "': ",e);
            }
        }
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        cancelBusyWorkItem(pmgr);
        super.cancel(pmgr);
    }


    public synchronized void cancel(YPersistenceManager pmgr, YIdentifier caseID)
            throws YPersistenceException {
        if (! cancelBusyWorkItem(pmgr)) {
            String workItemID = caseID.get_idString() + ":" + getID();
            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (null != workItem) cancelWorkItem(pmgr, workItem) ;
        }
        super.cancel(pmgr);
    }


    public boolean t_rollBackToFired(YPersistenceManager pmgr, YIdentifier caseID)
            throws YPersistenceException {
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
        return super.clone();
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
}
