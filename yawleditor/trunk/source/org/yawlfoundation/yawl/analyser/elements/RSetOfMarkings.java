/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.analyser.elements;

import java.util.HashSet;
import java.util.Set;

/**
 *  Data structure for Storage of RMarkings.
 *
 **/

public class RSetOfMarkings {

    private Set<RMarking> _markings = new HashSet<RMarking>();


    public void addMarking(RMarking marking) {
        if (! contains(marking)) _markings.add(marking);
    }

    public Set<RMarking> getMarkings() {
        return _markings;
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
        for (RMarking marking : newmarkings.getMarkings()) {
            addMarking(marking);
        }
    }

    public boolean equals(RSetOfMarkings markings) {
        if (markings == null) return false;
        Set<RMarking> markingsToCompare = markings.getMarkings();
        return _markings.size() == markingsToCompare.size() &&
                containsAll(markingsToCompare) && (markings.containsAll(_markings));
    }

    public boolean contains(RMarking m) {
        if (m != null) {
            for (RMarking contained : _markings) {
                if (contained.equals(m)) return true;
            }
        }
        return false;
    }

    public boolean containsAll(Set<RMarking> markingsToCompare) {
        if (markingsToCompare == null) return false;
        for (RMarking marking : markingsToCompare) {
            if (! contains(marking)) return false;
        }
        return true;
    }


    public boolean containsBiggerEqual(RMarking m) {
        for (RMarking marking : _markings) {
            if (marking.isBiggerThanOrEqual(m)) return true;
        }
        return false;
    }

}