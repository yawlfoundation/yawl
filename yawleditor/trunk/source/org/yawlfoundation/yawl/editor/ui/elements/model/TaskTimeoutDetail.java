package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.elements.YTimerParameters;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;

import javax.xml.datatype.Duration;
import java.util.Date;

public class TaskTimeoutDetail {

    private YVariable timerVariable;
    private YAWLAtomicTask task;

    private YTimerParameters timerParameters;

    public TaskTimeoutDetail(YAWLAtomicTask task) {
        this(task, null);
    }

    public TaskTimeoutDetail(YAWLAtomicTask task, YTimerParameters parameters) {
        this.task = task;
        timerParameters = parameters != null ? parameters : new YTimerParameters();
    }


    public YAWLAtomicTask getTask() { return task; }

    public void setTask(YAWLAtomicTask task) { this.task = task; }


    public YTimerParameters getTimerParameters() { return timerParameters; }


    public void setValue(YVariable variable) {
        timerVariable = variable;
        timerParameters.set(timerVariable.getName());
    }

    public YVariable getTimeoutVariable() { return timerVariable; }


    public void setValue(YWorkItemTimer.Trigger trigger, Duration timeoutValue) {
        timerParameters.set(trigger, timeoutValue);
    }

    public Duration getDurationValue() { return timerParameters.getDuration(); }


    public void setValue(YWorkItemTimer.Trigger trigger, Date timeoutDate) {
        timerParameters.set(trigger, timeoutDate);
    }

    public Date getTimeoutDate() { return timerParameters.getDate(); }


    public YWorkItemTimer.Trigger getTrigger() { return timerParameters.getTrigger(); }


    public String toString() {
        return timerParameters.toString();
    }

}
