/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements.e2wfoj;

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