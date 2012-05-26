/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements.state;

import org.yawlfoundation.yawl.elements.*;

import java.util.*;

/**
 *
 * @author Lachlan Aldred
 * Date: 19/06/2003
 * Time: 15:14:40
 *
 */
public class YMarking {
    private List<YNetElement> _locations;


    public YMarking(YIdentifier identifier) {
        _locations = new Vector<YNetElement>(identifier.getLocations());
    }

    public YMarking(List<YNetElement> locations) {
        _locations = locations;
    }


    public YSetOfMarkings reachableInOneStep(YTask task, YTask orJoin) {
        YSetOfMarkings halfBakedSet = null;
        if (_locations.contains(task)) {
            YMarking aMarking = new YMarking(_locations);
            aMarking._locations.remove(task);
            halfBakedSet = new YSetOfMarkings();
            halfBakedSet.addMarking(aMarking);
        }
        else {
            halfBakedSet = doPrelimaryMarkingSetBasedOnJoinType(task);
        }
        if (halfBakedSet == null) {
            return null;
        }

        //for each marking you generate activate the cancellation set and remove the tokens
        for (YMarking halfbakedMarking : halfBakedSet.getMarkings()) {
            halfbakedMarking._locations.removeAll(task.getRemoveSet());
        }

        Set<YMarking> iterableHalfBakedSet = halfBakedSet.getMarkings();
        YSetOfMarkings finishedSet = new YSetOfMarkings();
        Set<YExternalNetElement> postset = task.getPostsetElements();

        switch (task.getSplitType()) {
            case YTask._AND:
            case YTask._OR: {
                for (YMarking marking : iterableHalfBakedSet) {
                    marking._locations.addAll(postset);
                    finishedSet.addMarking(marking);
                }
                break;
            }
            case YTask._XOR: {
                for (YMarking halfbakedMarking : iterableHalfBakedSet) {
                    for (YExternalNetElement element : postset) {
                        YMarking aFinalMarking = new YMarking(halfbakedMarking.getLocations());
                        aFinalMarking._locations.add((YCondition) element);
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
                return _locations.containsAll(preset);
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


    public List<YNetElement> getLocations() {
        return _locations;
    }


    public int hashCode() {
        long hashCode = 0;
        for (YNetElement element : _locations) {
            hashCode += element.hashCode();
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
        return otherMarkingsLocations.size() <= 0;
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
        return this.isBiggerThan(marking) || this.equivalentTo(marking);
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
        return otherMarkingsLocations.size() > 0;
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
        for (YNetElement element : _locations) {
            if (element instanceof YTask) {        //a busy task means not deadlocked
                return false;
            }
        }
        for (YExternalNetElement postElement : YNet.getPostset(getLocationsAsSet())) {
            YTask task = (YTask) postElement;
            if (task.getJoinType() != YTask._OR) {
                if (nonOrJoinEnabled(task)) {
                    return false;
                }
            }
            else {//must be an orJoin
                for (YExternalNetElement preElement : task.getPresetElements()) {
                    YCondition condition = (YCondition) preElement;

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
    
    
    private Set<YExternalNetElement> getLocationsAsSet() {
        Set<YExternalNetElement> set = new HashSet<YExternalNetElement>();
        for (YNetElement element : _locations) {
            set.add((YExternalNetElement) element);
        }
        return set;
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