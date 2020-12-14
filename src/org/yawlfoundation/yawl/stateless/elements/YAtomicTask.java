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


import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.util.YVerificationHandler;

/**
 * A YAtomicTask object is the executable equivalent of the Atomic Task
 * in the YAWL language. They have the same properties and behaviour.
 *
 * @author Lachlan Aldred
 * @author Michael Adams (v2.0 and later)
 * @since 0.1
 */
public class YAtomicTask extends YTask {

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
     * Changes the inner state of an atomic task from entered to executing.
     * @param parentRunner
     * @param id the identifier to move from inner marking 'entered' to inner marking
     */
    protected void startOne(YNetRunner parentRunner, YIdentifier id)  {
        this._mi_entered.removeOne(id);
        this._mi_executing.add(id);
        setNetRunner(parentRunner);
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
     */
    public synchronized void cancel()  {
        cancelBusyWorkItem();
        super.cancel();
    }


    /**
     * Cancels the task. If the task is not currently executing, first check that the
     * workitem is still referenced by the engine before attempting the cancellation.
     * @param caseID the case identifier of this atomic task.
     */
    public synchronized void cancel(YIdentifier caseID) {
        if (! cancelBusyWorkItem()) {
            String workItemID = caseID.get_idString() + ":" + getID();
            YWorkItem workItem = getNetRunner().getWorkItemRepository().get(workItemID);
            if (null != workItem) cancelWorkItem(workItem) ;
        }
        super.cancel();
    }




    private boolean cancelBusyWorkItem() {

        // nothing to do if not fired or has no decomposition
        if ((_i == null) || (_decompositionPrototype == null)) return false;

        YWorkItem workItem = getNetRunner().getWorkItemRepository().get(_i.toString(), getID());
        if (null != workItem) cancelWorkItem(workItem) ;
        return true;
    }


    private void cancelWorkItem(YWorkItem workItem) {
        getNetRunner().getWorkItemRepository().removeWorkItemFamily(workItem);
        workItem.cancel();
    }


    /**
     * Rolls back a task's inner state from 'executing' to 'entered'.
     * <p/>
     * The roll back will only occur if the task's inner 'executing' marking contains
     * the identifier specified.
     * @param caseID the case identifier of this atomic task.
     * @return true if the rollback occurs, false if the rollback request could not
     * be actioned because the task's inner 'executing' marking does not contain 
     * the identifier specified.
     */
    public boolean t_rollBackToFired(YIdentifier caseID){
        if (_mi_executing.contains(caseID)) {
            _mi_executing.removeOne(caseID);
            _mi_entered.add(caseID);
            return true;
        }
        return false;
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


    /***** VERIFICATION *******************************************************/

    /**
     * Verifies this atomic task definition against YAWL semantics.
     * @return a List of error and/or warning messages. An empty list is returned if
     * the atomic task verifies successfully.
     */
    public void verify(YVerificationHandler handler) {
        super.verify(handler);
        if (_decompositionPrototype == null) {
            if (_multiInstAttr != null && (_multiInstAttr.getMaxInstances() > 1 ||
                    _multiInstAttr.getThreshold() > 1)) {
                handler.error(this, this +
                        " cannot have multiple instances and a blank work description.");
            }
        }
        else if (!(_decompositionPrototype instanceof YAWLServiceGateway)) {
            handler.error(this, this +
                    " task may not decompose to other than a WebServiceGateway.");
        }
    }
    
}
