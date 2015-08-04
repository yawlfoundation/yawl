/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.state;

import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 19/06/2003
 * Time: 15:14:40
 * 
 */
public class YMarking {
    private List _locations;


    public YMarking(YIdentifier identifier) {
        _locations = new Vector(identifier.getLocations());
    }

    public YMarking(List locations) {
        _locations = new Vector(locations);
    }


    public YSetOfMarkings reachableInOneStep(YTask task, YTask orJoin) {
        YSetOfMarkings halfBakedSet = null;
        if (_locations.contains(task)) {
            YMarking aMarking = new YMarking(_locations);
            aMarking._locations.remove(task);
            halfBakedSet = new YSetOfMarkings();
            halfBakedSet.addMarking(aMarking);
        } else {
            halfBakedSet = doPrelimaryMarkingSetBasedOnJoinType(task);
        }
        if (halfBakedSet == null) {
            return null;
        }
        //for each marking you generate activate the cancellation set and remove the tokens
        for (Iterator iterator = halfBakedSet.getMarkings().iterator(); iterator.hasNext();) {
            YMarking halfbakedMarking = (YMarking) iterator.next();
            Set cancellationSet = task.getRemoveSet();
            halfbakedMarking._locations.removeAll(cancellationSet);
        }
        Set iterableHalfBakedSet = halfBakedSet.getMarkings();
        YSetOfMarkings finishedSet = new YSetOfMarkings();
     //   Set postset = YOrJoinUtils.getRelevantPostset(task, orJoin);
        Set postset = task.getPostsetElements();
        int splitType = task.getSplitType();
        switch (splitType) {
            case YTask._AND:
            case YTask._OR:
                {
                    for (Iterator iterator = iterableHalfBakedSet.iterator(); iterator.hasNext();) {
                        YMarking marking = (YMarking) iterator.next();
                        marking._locations.addAll(postset);
                        finishedSet.addMarking(marking);
                    }
                    break;
                }
/*            case YTask._OR : {
                Set combinationsOfPostset = doPowerSetRecursion(postset);
                for (Iterator iterator = iterableHalfBakedSet.iterator(); iterator.hasNext();) {
                    YMarking halfBakedmarking = (YMarking) iterator.next();
                    for (Iterator powerSetIter = combinationsOfPostset.iterator(); powerSetIter.hasNext();) {
                        Set set = (Set) powerSetIter.next();
                        YMarking aFinalMarking = new YMarking(halfBakedmarking.getLocations());
                        aFinalMarking._locations.addAll(set);
                        finishedSet.addMarking(aFinalMarking);
                    }
                }
                break;
            }*/
            case YTask._XOR:
                {
                    for (Iterator iterator = iterableHalfBakedSet.iterator(); iterator.hasNext();) {
                        YMarking halfBakedmarking = (YMarking) iterator.next();
                        for (Iterator postsetIter = postset.iterator(); postsetIter.hasNext();) {
                            YCondition condition = (YCondition) postsetIter.next();
                            YMarking aFinalMarking = new YMarking(halfBakedmarking.getLocations());
                            aFinalMarking._locations.add(condition);
                            finishedSet.addMarking(aFinalMarking);
                        }
                    }
                    break;
                }
        }
        return finishedSet;
    }


    protected Set doPowerSetRecursion(Set aSet) {
        Set powerset = new HashSet();
        powerset.add(aSet);
        for (Iterator iterator = aSet.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            Set smallerSet = new HashSet();
            smallerSet.addAll(aSet);
            smallerSet.remove(o);
            if (smallerSet.size() > 0) {
                powerset.addAll(doPowerSetRecursion(smallerSet));
            }
        }
        return powerset;
    }


    private YSetOfMarkings doPrelimaryMarkingSetBasedOnJoinType(YTask task) {
        Set preset = task.getPresetElements();
        YSetOfMarkings markingSet = new YSetOfMarkings();
        int joinType = task.getJoinType();
        switch (joinType) {
            case YTask._AND:
                {
                    if (!nonOrJoinEnabled(task)) {
                        return null;
                    } else {
                        YMarking returnedMarking = new YMarking(_locations);
                        for (Iterator iterator = preset.iterator(); iterator.hasNext();) {
                            YCondition condition = (YCondition) iterator.next();
                            returnedMarking._locations.remove(condition);
                        }
                        markingSet.addMarking(returnedMarking);
                    }
                    break;
                }
            case YTask._OR:
                {
                    throw new RuntimeException("This method should never be called on an OR-Join");
                }
            case YTask._XOR:
                {
                    if (!nonOrJoinEnabled(task)) {
                        return null;
                    }
                    for (Iterator iterator = preset.iterator(); iterator.hasNext();) {
                        YCondition condition = (YCondition) iterator.next();
                        if (_locations.contains(condition)) {
                            YMarking returnedMarking = new YMarking(_locations);
                            returnedMarking._locations.remove(condition);
                            markingSet.addMarking(returnedMarking);
                        }
                    }
                    break;
                }
        }
        return markingSet;
    }


    /**
     * Checks to see if this marking enables the <task> passed as parameter.
     * This method should never be used for an or-join.
     * @param task
     * @return true iff this marking enables the task.
     */
    public boolean nonOrJoinEnabled(YTask task) {
        if (_locations.contains(task)) {
            return true;
        }
        Set preset = task.getPresetElements();
        int joinType = task.getJoinType();
        switch (joinType) {
            case YTask._AND:
                {
                    if (!_locations.containsAll(preset)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            case YTask._OR:
                {
                    throw new RuntimeException("This method should never be called on an OR-Join");
                }
            case YTask._XOR:
                {
                    for (Iterator iterator = preset.iterator(); iterator.hasNext();) {
                        YCondition condition = (YCondition) iterator.next();
                        if (_locations.contains(condition)) {
                            return true;
                        }
                    }
                    return false;
                }
        }
        return false;
    }


    public List getLocations() {
        return _locations;
    }


    public int hashCode() {
        long hashCode = 0;
        for (Iterator iterator = _locations.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            hashCode += o.hashCode();
        }
        return (int) (hashCode % Integer.MAX_VALUE);
    }


    public boolean equals(Object marking) {
        if (!(marking instanceof YMarking)) {
            return false;
        }
        List otherMarkingsLocations = new Vector(((YMarking) marking).getLocations());
        List myLocations = new Vector(_locations);
        for (Iterator iterator = myLocations.iterator(); iterator.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            if (otherMarkingsLocations.contains(netElement)) {
                otherMarkingsLocations.remove(netElement);
            } else {
                return false;
            }
        }
        if (otherMarkingsLocations.size() > 0) {
            return false;
        }
        return true;
    }


    public boolean strictlyGreaterThanOrEqualWithSupports(YMarking marking) {
        List otherMarkingsLocations = new Vector(marking.getLocations());
        List myLocations = new Vector(_locations);
        if (!(myLocations.containsAll(otherMarkingsLocations)
                && otherMarkingsLocations.containsAll(myLocations))) {
            return false;
        }
        for (Iterator iterator = otherMarkingsLocations.iterator(); iterator.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            if (myLocations.contains(netElement)) {
                myLocations.remove(netElement);
            } else {
                return false;
            }
        }
        return true;
    }
    
     //moe - ResetAnalyser
  public boolean isBiggerThanOrEqual(YMarking marking) {
        List otherMarkingsLocations = new Vector(marking.getLocations());
        List myLocations = new Vector(_locations);
        if (this.isBiggerThan(marking) || this.equivalentTo(marking))
        { return true;
        }
        return false;
    }
    
     //moe - ResetAnalyser
  public boolean isBiggerThan(YMarking marking) {
        List otherMarkingsLocations = new Vector(marking.getLocations());
        List myLocations = new Vector(_locations);

        //This test is for c1+c2+c3 bigger than c1+c2
        if ((myLocations.containsAll(otherMarkingsLocations)
                && !otherMarkingsLocations.containsAll(myLocations))) {
            return true;
        }
        
        //This test is for c1+2c2 bigger than c1+c2
        else if (myLocations.containsAll(otherMarkingsLocations)
                && otherMarkingsLocations.containsAll(myLocations)
        	    && myLocations.size() > otherMarkingsLocations.size())
        {
        	return true;
        }
        return false;
    }

    public boolean strictlyLessThanWithSupports(YMarking marking) {
        List otherMarkingsLocations = new Vector(marking.getLocations());
        List myLocations = new Vector(_locations);
        if (!(myLocations.containsAll(otherMarkingsLocations)
                && otherMarkingsLocations.containsAll(myLocations))) {
            return false;
        }
        for (Iterator iterator = myLocations.iterator(); iterator.hasNext();) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            if (otherMarkingsLocations.contains(netElement)) {
                otherMarkingsLocations.remove(netElement);
            } else {
                return false;
            }
        }
        if (otherMarkingsLocations.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isBiggerEnablingMarkingThan(YMarking marking, YTask orJoin) {
        Set preset = orJoin.getPresetElements();
        Set thisMarkingsOccupiedPresetElements = new HashSet();
        Set otherMarkingsOccupiedPresetElements = new HashSet();
        for (Iterator presetIter = preset.iterator(); presetIter.hasNext();) {
            YCondition condition = (YCondition) presetIter.next();
            if (this._locations.contains(condition)) {
                thisMarkingsOccupiedPresetElements.add(condition);
            }
            if (marking._locations.contains(condition)) {
                otherMarkingsOccupiedPresetElements.add(condition);
            }
        }
        return thisMarkingsOccupiedPresetElements.containsAll(
                otherMarkingsOccupiedPresetElements)
                &&
                !otherMarkingsOccupiedPresetElements.containsAll(
                        thisMarkingsOccupiedPresetElements);
    }


    public boolean deadLock(YTask orJoin) {
        for (Iterator locationIter = _locations.iterator(); locationIter.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) locationIter.next();
            if (element instanceof YTask) {//a busy task means not deadlocked
                return false;
            }
        }
        Set postset = YNet.getPostset(new HashSet(_locations));
        for (Iterator taskIter = postset.iterator(); taskIter.hasNext();) {
            YTask task = (YTask) taskIter.next();
            if (task.getJoinType() != task._OR) {
                if (nonOrJoinEnabled(task)) {
                    return false;
                }
            } else {//must be an orJoin
                Set orJoinPreset = task.getPresetElements();
                for (Iterator pjPresetIter = orJoinPreset.iterator(); pjPresetIter.hasNext();) {
                    YCondition condition = (YCondition) pjPresetIter.next();
                    //if we find an orJoin that contains an identifier then the marking
                    //is definitely not deadlocked
                    if (_locations.contains(condition) && task != orJoin) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public String toString() {
        return _locations.toString();
    }

    public boolean equivalentTo(YMarking marking) {
        Vector otherMarkingsLocations = new Vector(marking.getLocations());

        // short-circuit test if sizes differ
        if (otherMarkingsLocations.size() != _locations.size()) return false;

        // ok, same size so sort and compare for equality
        Vector thisMarkingsLocations = new Vector(_locations);        // don't sort orig.
        Collections.sort(otherMarkingsLocations);
        Collections.sort(thisMarkingsLocations);

        // vectors are equal if each element pair is equal 
        return thisMarkingsLocations.equals(otherMarkingsLocations);
    }
}