package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskTimeoutDetail {

    public static final int TRIGGER_ON_ENABLEMENT = 0;
    public static final int TRIGGER_ON_STARTING = 1;

    private DataVariable timerVariable;
    private String timerValue;
    private Date timerDate;
    private int trigger;
    private YAWLAtomicTask task;

    public TaskTimeoutDetail(YAWLAtomicTask task) {
        this.task = task;
    }


    public YAWLAtomicTask getTask() { return task; }

    public void setTask(YAWLAtomicTask task) { this.task = task; }


    public void setValue(DataVariable variable) {
        nullAll();
        timerVariable = variable;
    }

    public DataVariable getTimeoutVariable() { return timerVariable; }


    public void setValue(String timeoutValue) {
        nullAll();
        timerValue = timeoutValue;
    }

    public String getTimeoutValue() { return timerValue; }


    public void setValue(Date timeoutDate) {
        nullAll();
        timerDate = timeoutDate;
    }

    private void nullAll() {
        timerVariable = null;
        timerDate = null;
        timerValue = null;
    }

    public Date getTimeoutDate() { return timerDate; }


    public void setTrigger(int trigger) { this.trigger = trigger; }

    public int getTrigger() { return trigger; }


    public String toString() {
        String s = trigger == TRIGGER_ON_STARTING ? "Start: " : "Offer: ";
        if (timerValue != null) s += timerValue;
        else if (timerVariable != null) s += timerVariable;
        else {
            s += new SimpleDateFormat().format(timerDate);
        }
        return s;
    }

}
