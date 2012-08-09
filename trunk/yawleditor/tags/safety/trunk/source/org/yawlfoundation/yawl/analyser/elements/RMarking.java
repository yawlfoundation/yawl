/*
 * Created on 16/02/2006
 * YAWLEditor v1.4 
 *
 * @author Moe Thandar Wyn
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.analyser.elements;

import java.util.*;

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
        return oMarking instanceof RMarking && compare((RMarking) oMarking, Operator.equals);
    }

    public boolean isBiggerThanOrEqual(RMarking marking) {
        return compare(marking, Operator.greaterThanOrEquals);
    }
    
    public boolean isBiggerThan(RMarking marking) {
        return compare(marking, Operator.greaterThan);
    }
    
     
    /**
     * This is used for coverable check: x' <= x
     */
    public boolean isLessThanOrEqual(RMarking marking) {
        return compare(marking, Operator.lessThanOrEquals);
    }


    private boolean compare(RMarking marking, Operator operator) {
        Map<String, Integer> otherPlaces = marking.getMarkedPlaces();
        if (getLocations().containsAll(otherPlaces.keySet())) {
            for (String netElement : otherPlaces.keySet()) {
                switch (operator) {
                    case equals :
                        if (! _markedPlaces.get(netElement).equals(otherPlaces.get(netElement))) {
                            return false;
                        }
                        break;
                    case greaterThan :
                        if (_markedPlaces.get(netElement) <= otherPlaces.get(netElement)) {
              		        return false;
              		    }
                        break;
                    case lessThanOrEquals :
                        if (_markedPlaces.get(netElement) > otherPlaces.get(netElement)) {
                            return false;
                        }
                        break;
                    case greaterThanOrEquals :
                        if (_markedPlaces.get(netElement) < otherPlaces.get(netElement)) {
              		        return false;
              		    }
                        break;
                   }
            }
            return true;   // all passed
        }
        return false;
    }

}
