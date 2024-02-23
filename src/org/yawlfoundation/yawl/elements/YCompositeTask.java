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

package org.yawlfoundation.yawl.elements;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.util.ArrayList;
import java.util.List;

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
     * @param pmgr a valid persistence manager instance
     * @param id the task identifier
     * @throws YDataStateException
     */
    protected synchronized void startOne(YPersistenceManager pmgr, YIdentifier id)
            throws YDataStateException, YPersistenceException, YQueryException, YStateException {

        // set token locations
        _mi_executing.add(pmgr, id);
        _mi_entered.removeOne(pmgr, id);

        // create a net runner for this task's contained subnet
        YNetRunner netRunner = new YNetRunner(pmgr, (YNet) _decompositionPrototype,
                this, id, getData(id));
        getNetRunnerRepository().add(netRunner);
        logTaskStart(netRunner);
        netRunner.continueIfPossible(pmgr);
        netRunner.start(pmgr);
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        List<YNetRunner> cancelledRunners = new ArrayList<YNetRunner>();
        YIdentifier thisI = _i;
        if (_i != null) {
            for (YIdentifier identifier : _mi_active.getIdentifiers()) {
                YNetRunner netRunner = getNetRunnerRepository().get(identifier);
                if (netRunner != null) {
                    netRunner.cancel(pmgr);
                    for (YWorkItem item : getWorkItemRepository().cancelNet(identifier)) {
                        item.cancel(pmgr);
                        YEventLogger.getInstance().logWorkItemEvent(item,
                                YWorkItemStatus.statusDeleted, null);
                    }
                    cancelledRunners.add(netRunner);
                }
            }
        }
        super.cancel(pmgr);

        for (YNetRunner runner : cancelledRunners) {
            runner.removeFromPersistence(pmgr);
        }

        if (thisI != null) {
            YNetRunner parentRunner = getNetRunnerRepository().get(thisI);
            if (parentRunner != null) {
                parentRunner.removeActiveTask(pmgr, this);
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
        YSpecificationID specID =
                _decompositionPrototype.getSpecification().getSpecificationID();
        YLogPredicate logPredicate = _decompositionPrototype.getLogPredicate();
        YLogDataItemList logData = null;
        if (logPredicate != null) {
            String predicate = logPredicate.getParsedStartPredicate(_decompositionPrototype);
            if (predicate != null) {
                logData = new YLogDataItemList(new YLogDataItem("Predicate",
                             "OnNetStart", predicate, "string"));
            }
        }
        YEventLogger.getInstance().logSubNetCreated(specID, netRunner,
                                                    this.getID(), logData);
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
