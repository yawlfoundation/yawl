/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YNetRunner;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.Iterator;
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
    protected synchronized void startOne(YPersistenceManager pmgr, YIdentifier id) throws YDataStateException, YSchemaBuildingException, YPersistenceException, YQueryException, YStateException {
        _mi_executing.add(pmgr, id);
        _mi_entered.removeOne(pmgr, id);
        //todo Creating anotehr YNetRunner thread

        YNetRunner netRunner = new YNetRunner(pmgr,
                (YNet) _decompositionPrototype,
                this,
                id,
                getData(id));

        /*
          INSERTED FOR PERSISTANCE
         */
        //todo AJH Do we actually need this call here ????
//        YPersistance.getInstance().storeData(netRunner);

        netRunner.continueIfPossible(pmgr);
        netRunner.start(pmgr);
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        if (_i != null) {
            List activeChildIdentifiers = _mi_active.getIdentifiers();
            Iterator iter = activeChildIdentifiers.iterator();
            while (iter.hasNext()) {
                YIdentifier identifier = (YIdentifier) iter.next();
                YNetRunner netRunner = _workItemRepository.getNetRunner(identifier);
                if (netRunner != null) {
                    netRunner.cancel(pmgr);
                }
            }
        }
        super.cancel(pmgr);
    }
}
