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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Data structure for Marking Storage;
 *
 */

public class RMarking {

    private enum Operator { equals, greaterThan, lessThanOrEquals, greaterThanOrEquals }

    private Map<String, Integer> _markedPlaces;
  

    public RMarking(List<RElement> locations) {
    	
    	//Convert to internal representation
     	for (RElement netElement : locations) {
    		String netElementName = netElement.getID();
    		if (_markedPlaces.containsKey(netElementName)) {
     	 	    _markedPlaces.put(netElementName, _markedPlaces.get(netElementName) + 1);
    		}
    	    else {
    	    	_markedPlaces.put(netElementName, 1);
    	    }
        }
    }

    public RMarking(Map<String, Integer> markedPlaces) {
      _markedPlaces = markedPlaces;
    }

    public Map<String, Integer> getMarkedPlaces() {
        return _markedPlaces;
    }

    public Set<String> getLocations() {
       return _markedPlaces.keySet();
    }

    public boolean equals(Object oMarking) {
        if (! (oMarking instanceof RMarking)) return false;
        RMarking marking = (RMarking) oMarking;
        Set<String> myPlaces = getLocations();
        Map<String, Integer> otherPlaces = marking.getMarkedPlaces();
        if (myPlaces.equals(otherPlaces.keySet())) {
            for (String netElement : myPlaces) {
                if (! _markedPlaces.get(netElement).equals(otherPlaces.get(netElement))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }



    public boolean isBiggerThanOrEqual(RMarking marking) {
        Map<String, Integer> otherPlaces = marking.getMarkedPlaces();
        if (getLocations().containsAll(otherPlaces.keySet())) {
            for (String netElement : otherPlaces.keySet()) {
                 if (_markedPlaces.get(netElement) < otherPlaces.get(netElement)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public boolean isBiggerThan(RMarking marking) {
        Map<String, Integer> otherPlaces = marking.getMarkedPlaces();
        boolean isBigger = false;
        if (getLocations().containsAll(otherPlaces.keySet())) {
            for (String netElement : otherPlaces.keySet()) {
                int myCount = _markedPlaces.get(netElement);
                int otherCount = otherPlaces.get(netElement);
                if (myCount < otherCount) {
                    return false;
                }
                else if (myCount > otherCount) {
                    isBigger = true;
                }
            }
            return isBigger;
        }
        return false;
    }
    
     
    /**
     * This is used for coverable check: x' <= x
     */
    public boolean isLessThanOrEqual(RMarking marking) {
        Set<String> myPlaces = getLocations();
        Map<String, Integer> otherPlaces = marking.getMarkedPlaces();
        if (otherPlaces.keySet().containsAll(myPlaces)) {
            for (String netElement : myPlaces) {
                if (_markedPlaces.get(netElement) > otherPlaces.get(netElement)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
