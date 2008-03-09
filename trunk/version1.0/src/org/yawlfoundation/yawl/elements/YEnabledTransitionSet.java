package org.yawlfoundation.yawl.elements;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

/**
 * Author: Michael Adams
 * Creation Date: 5/03/2008
 */
public class YEnabledTransitionSet {

    private HashSet<YAtomicTask> atomicSet = new HashSet<YAtomicTask>();
    private HashSet<YCompositeTask> compositeSet = new HashSet<YCompositeTask>();
    private String _id;

    public YEnabledTransitionSet() { }

    public void add(YTask task) {
        if (task instanceof YCompositeTask)
            compositeSet.add((YCompositeTask) task);
        else if (task instanceof YAtomicTask)
            atomicSet.add((YAtomicTask) task);
    }

    public HashSet<YAtomicTask> getEnabledAtomicTasks() {
        return atomicSet;
    }

    public void setEnbledAtomicTasks(HashSet<YAtomicTask> atomicSet) {
        this.atomicSet = atomicSet;
    }

    public HashSet<YCompositeTask> getEnabledCompositeTasks() {
        return compositeSet;
    }

    public void setEnabledCompositeTasks(HashSet<YCompositeTask> compositeSet) {
        this.compositeSet = compositeSet;
    }

    public String generateID() {
        _id = UUID.randomUUID().toString() ;
        return _id;
    }

    public String getID() {
        if (_id == null) generateID();
        return _id;
    }

    public int getAtomicTaskCount() { return atomicSet.size(); }

    public int getCompositeTaskCount() { return compositeSet.size(); }

    public boolean hasCompositeTasks() { return ! compositeSet.isEmpty(); }

    public YCompositeTask getRandomCompositeTaskFromEnabledTransitionSet() {
        Object[] op = compositeSet.toArray();
        if (op.length == 0) return null ;                       // case: empty set
        if (op.length == 1) return (YCompositeTask) op[0] ;     // case: only one member

        return (YCompositeTask) op[new Random().nextInt(op.length-1)];
    }

}
