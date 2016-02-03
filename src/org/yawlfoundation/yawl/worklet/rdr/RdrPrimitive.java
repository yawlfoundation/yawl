package org.yawlfoundation.yawl.worklet.rdr;


import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;

public class RdrPrimitive implements Comparable<RdrPrimitive> {

    private int index;
    private ExletAction action;
    private ExletTarget target;
    private String worklet;

    protected RdrPrimitive() { }  // for hibernate


    public RdrPrimitive(int i, String a, String t) {
        setIndex(i);
        setAction(a);
        setTarget(t);
    }

    public RdrPrimitive(int i, ExletAction a, ExletTarget t) {
        index = i; action = a; target = t;
    }


    public int getIndex() { return index; }

    public String getAction() { return action.toString(); }

    public String getTarget() {
        return worklet != null ? worklet : target.toString();
    }

    public String getWorkletTarget() { return worklet; }


    public ExletAction getExletAction() { return action; }

    public ExletTarget getExletTarget() { return target; }


    public void setIndex(int index) { this.index = index; }

    public void setAction(String action) {
        this.action = ExletAction.fromString(action);
    }

    public void setTarget(String target) {
        this.target = ExletTarget.fromString(target);
        if (this.target == ExletTarget.Invalid && this.action.isWorkletAction()) {
            worklet = target;
        }
    }

    public void setExletAction(ExletAction action) { this.action = action; }

    public void setExletTarget(ExletTarget target) { this.target = target; }


    public boolean isValid() {
        return getExletAction() != ExletAction.Invalid &&
                (getExletTarget() != ExletTarget.Invalid || getWorkletTarget() != null);
    }


    public int compareTo(RdrPrimitive other) {return index - other.index; }


    public boolean equals(Object o) {
        if (o instanceof RdrPrimitive) {
            RdrPrimitive other = (RdrPrimitive) o;
            return getIndex() == other.getIndex() &&
                    getAction().equals(other.getAction()) &&
                    getTarget().equals((other.getTarget()));
        }
        return false;
    }


    public int hashCode() {
        return 31 * getIndex() * (getAction().hashCode() + getTarget().hashCode());
    }


    public String toString() {
        return getAction() + " " + getTarget();
    }

}
