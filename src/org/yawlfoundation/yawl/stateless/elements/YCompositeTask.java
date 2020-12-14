/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.stateless.elements;

import org.jdom2.Element;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.data.YParameter;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;
import org.yawlfoundation.yawl.stateless.listener.event.YLogEvent;
import org.yawlfoundation.yawl.stateless.listener.predicate.YLogPredicate;
import org.yawlfoundation.yawl.util.YVerificationHandler;

/**
 * 
 * A YCompositeTask object is the executable equivalent of the YCompositeTask
 * in the YAWL paper.   It has the same properties and behaviour.
 * @author Lachlan Aldred
 * 
 */
public final class YCompositeTask extends YTask {


    public YCompositeTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        return super.clone();
    }


    /****** EXECUTION ***************************************************/
    
    /**
     * Starts this composite task.
     *
     * @param parentRunner
     * @param id the task identifier
     * @throws YDataStateException
     */
    protected synchronized void startOne(YNetRunner parentRunner, YIdentifier id)
            throws YDataStateException, YQueryException, YStateException {

        // set token locations
        _mi_executing.add(id);
        _mi_entered.removeOne(id);

        // create a net runner for this task's contained subnet
        YNetRunner netRunner = new YNetRunner((YNet) _decompositionPrototype,
                this, id, getData());
        netRunner.setAnnouncer(parentRunner.getAnnouncer());
        setNetRunner(parentRunner);
        parentRunner.addChildRunner(netRunner);
        logTaskStart(netRunner);
        netRunner.continueIfPossible();
        netRunner.start();
    }


    public synchronized void cancel() {
//        List<YNetRunner> cancelledRunners = new ArrayList<YNetRunner>();
        YIdentifier thisI = _i;
        if (_i != null) {
             for (YIdentifier identifier : _mi_active.getIdentifiers()) {
                YNetRunner netRunner = getNetRunner().getCaseRunner(identifier);
                if (netRunner != null) {
                    netRunner.cancel();
                    for (YWorkItem item : netRunner.getWorkItemRepository().cancelNet(identifier)) {
                        item.cancel();
                        netRunner.getAnnouncer().announceLogEvent(
                                new YLogEvent(YEventType.ITEM_CANCEL, item, null));
                    }
//                    cancelledRunners.add(netRunner);
                }
            }
        }
        super.cancel();

        if (thisI != null) {
            if (getNetRunner() != null) {
                getNetRunner().removeActiveTask(this);
            }
        }
    }


    // overridden to allow passthrough of non-mandatory empty elements to subnet
    protected Element performDataExtraction(String expression, YParameter inputParam)
            throws YDataStateException, YQueryException {

        Element result = evaluateTreeQuery(expression, _net.getInternalDataDocument());

        // if the param id of empty complex type flag type, don't return the query result
        // as input data if the flag is not currently set
        if (inputParam.isEmptyTyped()) {
            if (!isPopulatedEmptyTypeFlag(expression)) return null;
        }

        // AJH: Allow option to inhibit schema validation for outbound data.
        if (_net.getSpecification().getSchemaVersion().isSchemaValidating()
                && (!skipOutboundSchemaChecks())) {
            performSchemaValidationOverExtractionResult(expression, inputParam, result);
        }
        return result;
    }


    // write the sub-net start event to the process log
    private void logTaskStart(YNetRunner netRunner) {
        YLogPredicate logPredicate = _decompositionPrototype.getLogPredicate();
        YLogDataItemList logData = null;
        if (logPredicate != null) {
            String predicate = logPredicate.getParsedStartPredicate(_decompositionPrototype);
            if (predicate != null) {
                logData = new YLogDataItemList(new YLogDataItem("Predicate",
                             "OnStart", predicate, "string"));
            }
        }
        getNetRunner().getAnnouncer().announceLogEvent(
                new YLogEvent(YEventType.NET_STARTED, netRunner, logData));
    }


    /****** VERIFICATION ***************************************************/

    public void verify(YVerificationHandler handler) {
        super.verify(handler);   // check parent first
        if (_decompositionPrototype == null) {
            handler.error(this, this + " composite task must contain a net.");
        }
        if (!(_decompositionPrototype instanceof YNet)) {
            handler.error(this,
                    this + " composite task may not decompose to other than a net.");
        }
    }

}
