package org.yawlfoundation.yawl.worklet.rdr;


import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;

public class RdrPrimitive implements Comparable<RdrPrimitive> {
    private int index;
    private String action;
    private String target;

    protected RdrPrimitive() { }  // for hibernate

    public RdrPrimitive(int i, String a, String t) {
        index = i; action = a; target = t;
    }

    public RdrPrimitive(int i, ExletAction a, ExletTarget t) {
        this(i, a.toString(), t.toString());
    }


    public int getIndex() { return index; }

    public String getAction() { return action; }

    public String getTarget() { return target; }


    public void setIndex(int index) { this.index = index; }

    public void setAction(String action) { this.action = action; }

    public void setTarget(String target) { this.target = target; }


    public int compareTo(RdrPrimitive other) {return index - other.index; }
}
