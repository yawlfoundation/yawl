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

package org.yawlfoundation.yawl.engine.time;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.exceptions.YQueryException;


/**
 * Author: Michael Adams
 * Creation Date: 25/05/2010
 */
public class YTimerVariable {

    private YWorkItemTimer.State _state;
    private YTask _ownerTask;


    public YTimerVariable(YTask ownerTask) {
        _ownerTask = ownerTask;
        _state = YWorkItemTimer.State.dormant;
    }
    

    public String getTaskName() { return _ownerTask.getName(); }

    public YTask getTask() { return _ownerTask; }


    public YWorkItemTimer.State getState() { return _state; }

    public String getStateString() { return _state.name(); }


    public void setState(YWorkItemTimer.State state) {
        setState(state, false);
    }

    public void setState(YWorkItemTimer.State state, boolean restoring) {

        // when restoring, the initial state is always 'dormant', so invalid
        // transitions should be ignored
        if (restoring || isValidTransition(state)) {
            _state = state;
        }
        else {
            Logger.getLogger(this.getClass()).debug("Attempt made to move timer variable state " +
            "for task '" + _ownerTask.getName() + "' from " + _state.name() + " to " + state.name());
        }
    }

    
    public void setStateActive() { setState(YWorkItemTimer.State.active); }

    public void setStateClosed() { setState(YWorkItemTimer.State.closed); }

    public void setStateExpired() { setState(YWorkItemTimer.State.expired); }


    // a timer predicate takes the form "timer(taskname) op 'value'"
    // op must be "=" or "!="; value must be a valid timer state
    public boolean evaluatePredicate(String predicate) throws YQueryException {
        boolean negate;
        if (predicate.contains("!=")) {
            negate = true;
        }
        else if (predicate.contains("=")) {
            negate = false;
        }
        else throw new YQueryException("Malformed timer predicate: missing " +
            "or invalid operator; predicate: " + predicate);

        int openQuote = predicate.indexOf("'");
        int closeQuote = predicate.lastIndexOf("'");
        if ((openQuote > -1) && (closeQuote > openQuote)) {
            String queryState = predicate.substring(openQuote+1, closeQuote);
            return negate ^ queryState.equals(getStateString());     // XOR op & result
        }
        else throw new YQueryException("Malformed timer predicate: missing " +
                "quote(s) around state value; predicate: " + predicate);        
    }


    // Since a workitem expires before it is fully completed, this guards
    // against a transition from expired -> closed
    private boolean isValidTransition(YWorkItemTimer.State state) {
        switch (state) {
            case dormant : return true;                         // an initialisation 
            case active  : return _state == YWorkItemTimer.State.dormant; 
            case closed  :
            case expired : return _state == YWorkItemTimer.State.active;
        }
        return false;
    }


}
