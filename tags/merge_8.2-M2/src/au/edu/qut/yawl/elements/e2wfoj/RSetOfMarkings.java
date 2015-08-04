/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.e2wfoj;

import java.util.HashSet;
import java.util.Set;

/**
 *  Data structure for Storage of RMarkings.
 *
 **/
public class RSetOfMarkings {
    private Set _markings = new HashSet();


    public void addMarking(RMarking marking) {
        _markings.add(marking);
    }

    public Set getMarkings() {
        return new HashSet(_markings);
    }

    public int size() {
        return _markings.size();
    }

    public void removeAll() {
        _markings.clear();
    }

    public void removeMarking(RMarking marking) {
        _markings.remove(marking);
    }

    public void addAll(RSetOfMarkings newmarkings) {
        _markings.addAll(newmarkings.getMarkings());

    }

    public boolean equals(RSetOfMarkings markings) {
        Set markingsToCompare = markings.getMarkings();
        if (_markings.size() != markingsToCompare.size()) {
            return false;
        } else {
            if (_markings.containsAll(markingsToCompare) &&
                    (markingsToCompare.containsAll(_markings))) {
                return true;
            }
        }
        return false;
    }

}