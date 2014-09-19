package org.yawlfoundation.yawl.worklet.rdr;

/**
 * @author Michael Adams
 * @date 19/09/2014
 */
public class RdrPair {

    private RdrNode lastTrue;
    private RdrNode lastEvaluated;

    public RdrPair(RdrNode lTrue, RdrNode lEvaluated) {
        lastTrue = lTrue;
        lastEvaluated = lEvaluated;
    }


    public boolean isPairEqual() {
        return lastTrue != null && lastEvaluated != null && lastTrue == lastEvaluated;
    }


    public RdrNode getParentForNewNode() {
        return isPairEqual() ? getLastTrueNode() : getLastEvaluatedNode();
    }


    public RdrNode getLastTrueNode() { return lastTrue; }


    public RdrNode getLastEvaluatedNode() { return lastEvaluated; }


    public String toString() {
        String trueString = lastTrue != null ? lastTrue.toXML() : "";
        String evalString = lastEvaluated != null ? lastEvaluated.toXML() : "";
        return trueString + ":::" + evalString;
    }
}
