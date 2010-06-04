/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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
     * @throws YSchemaBuildingException
     */
    protected synchronized void startOne(YPersistenceManager pmgr, YIdentifier id)
            throws YDataStateException, YSchemaBuildingException, YPersistenceException,
                   YQueryException, YStateException {
        _mi_executing.add(pmgr, id);
        _mi_entered.removeOne(pmgr, id);

        YNetRunner netRunner = new YNetRunner(pmgr,
                (YNet) _decompositionPrototype,
                this,
                id,
                getData(id));

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

        YEventLogger.getInstance().logSubNetCreated(specID, netRunner, 
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
                YNetRunner netRunner = _workItemRepository.getNetRunner(identifier);
                if (netRunner != null) {
                    for (YWorkItem item : netRunner.cancel(pmgr)) {
                        item.cancel(pmgr);
                        YEventLogger.getInstance().logWorkItemEvent(item,
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
            YNetRunner parentRunner = _workItemRepository.getNetRunner(thisI);
            if (parentRunner != null) {
                parentRunner.removeActiveTask(pmgr, this);
            }
        }
    }
}
