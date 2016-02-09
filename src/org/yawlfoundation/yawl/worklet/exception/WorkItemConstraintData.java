package org.yawlfoundation.yawl.worklet.exception;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/** class/structure used to store and retrieve event data items */
class WorkItemConstraintData {

    private WorkItemRecord _wir ;
    private String _data ;
    private boolean _preCheck ;

    public WorkItemConstraintData(WorkItemRecord wir, String data, boolean preCheck) {
        _wir = wir ;
        _data = data ;
        _preCheck = preCheck ;
    }

    public WorkItemRecord getWIR() { return _wir; }

    public String getData() { return _data; }

    public boolean getPreCheck() { return _preCheck; }

}
