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

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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


    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (_decompositionPrototype == null) {
            messages.add(new YVerificationMessage(this, this + " composite task must contain a net.", YVerificationMessage.ERROR_STATUS));
        }
        if (!(_decompositionPrototype instanceof YNet)) {
            messages.add(new YVerificationMessage(this, this + " composite task may not decompose to other than a net.", YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YCompositeTask copy = (YCompositeTask) super.clone();
        return copy;
    }


    /**
     * @param pmgr
     * @param id
     * @throws YDataStateException
     */
    protected synchronized void startOne(YPersistenceManager pmgr, YIdentifier id)
            throws YDataStateException, YPersistenceException,
                   YQueryException, YStateException {
        _mi_executing.add(pmgr, id);
        _mi_entered.removeOne(pmgr, id);

        YNetRunner netRunner = new YNetRunner(pmgr,
                (YNet) _decompositionPrototype,
                this,
                id,
                getData(id));
        _netRunnerRepository.add(netRunner);

        // log sub-case start event
        YSpecificationID specID =
                _decompositionPrototype.getSpecification().getSpecificationID();

        YLogPredicate logPredicate = _decompositionPrototype.getLogPredicate();
        YLogDataItemList logData = null;
        if (logPredicate != null) {
            String predicate = logPredicate.getParsedStartPredicate(_decompositionPrototype);
            if (predicate != null) {
                logData = new YLogDataItemList(new YLogDataItem("Predicate",
                             "OnStart", predicate, "string"));
            }
        }

        YEventLogger.getInstance().logSubNetCreated(pmgr, specID, netRunner,
                                                    this.getID(), logData);
        netRunner.continueIfPossible(pmgr);
        netRunner.start(pmgr);
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        List<YNetRunner> cancelledRunners = new ArrayList<YNetRunner>();
        YIdentifier thisI = _i;
        if (_i != null) {
            List<YIdentifier> activeChildIdentifiers = _mi_active.getIdentifiers();
            for (YIdentifier identifier : activeChildIdentifiers) {
                YNetRunner netRunner = _netRunnerRepository.get(identifier);
                if (netRunner != null) {
                    netRunner.cancel(pmgr);
                    for (YWorkItem item : _workItemRepository.cancelNet(identifier)) {
                        item.cancel(pmgr);
                        YEventLogger.getInstance().logWorkItemEvent(pmgr, item,
                                YWorkItemStatus.statusDeleted, null);
                        YEngine.getInstance().getAnnouncer().announceCancelledWorkItem(item);

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
            YNetRunner parentRunner = _netRunnerRepository.get(thisI);
            if (parentRunner != null) {
                parentRunner.removeActiveTask(pmgr, this);
            }
        }
    }
}
