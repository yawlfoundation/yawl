package org.yawlfoundation.yawl.engine.time;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.YTask;


/**
 * Author: Michael Adams
 * Creation Date: 25/05/2010
 */
public class YTimerVariable {

    private String _name;
    private YWorkItemTimer.State _state;
    private YTask _ownerTask;


    public YTimerVariable(String name, YTask ownerTask) {
        _name = name;
        _ownerTask = ownerTask;
        _state = YWorkItemTimer.State.dormant;
    }

    public YTimerVariable(YTask ownerTask) {
        this("_" + ownerTask.getName() + "_timer_", ownerTask);
    }
    

    public String getName() { return _name; }

    public void setName(String name) { _name = name; }

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
            Logger.getLogger(this.getClass()).warn("Attempt made to move timer variable state " +
            "for task '" + _ownerTask.getName() + "' from " + _state.name() + " to " + state.name());
        }
    }

    
    public void setStateActive() { setState(YWorkItemTimer.State.active); }

    public void setStateClosed() { setState(YWorkItemTimer.State.closed); }

    public void setStateExpired() { setState(YWorkItemTimer.State.expired); }


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
