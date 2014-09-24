package org.yawlfoundation.yawl.worklet.rdr;


class RdrPrimitive implements Comparable<RdrPrimitive> {
    private int index;
    private String action;
    private String target;

    protected RdrPrimitive() { }  // for hibernate

    protected RdrPrimitive(int i, String a, String t) {
        index = i; action = a; target = t;
    }


    public int getIndex() { return index; }

    public String getAction() { return action; }

    public String getTarget() { return target; }


    public int compareTo(RdrPrimitive other) {return index - other.index; }
}
