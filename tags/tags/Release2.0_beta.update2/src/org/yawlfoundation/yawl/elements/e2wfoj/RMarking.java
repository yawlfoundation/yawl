/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.e2wfoj;

import java.util.*;

/**
 *  Data structure for Marking Storage;
 *
 */

public class RMarking {
    private Map _markedPlaces = new HashMap();


    public RMarking(List locations) {

        //Convert to internal representation
        for (Iterator iterator = locations.iterator(); iterator.hasNext();) {
            RElement netElement = (RElement) iterator.next();
            String netElementName = netElement.getID();
            Integer tokenCount = new Integer(1);
            if (_markedPlaces.containsKey(netElementName)) {
                Integer countString = (Integer) _markedPlaces.get(netElementName);
                int count = countString.intValue();
                count++;
                tokenCount = new Integer(count);

            }
            _markedPlaces.put(netElementName, tokenCount);

        }


    }

    public RMarking(Map markedPlaces) {
        _markedPlaces = new HashMap(markedPlaces);


    }

    public List getLocations() {
        return new Vector(_markedPlaces.keySet());
    }

    public boolean equals(Object omarking) {
        if (!(omarking instanceof RMarking)) {
            return false;
        }
        RMarking marking = (RMarking) omarking;
        Map otherMarking = new HashMap(marking.getMarkedPlaces());
        Set otherPlaces = otherMarking.keySet();
        Set myPlaces = _markedPlaces.keySet();

        if (myPlaces.equals(otherPlaces)) {
            for (Iterator iterator = myPlaces.iterator(); iterator.hasNext();) {
                String netElement = (String) iterator.next();
                Integer mycount = (Integer) _markedPlaces.get(netElement);
                Integer othercount = (Integer) otherMarking.get(netElement);
                if (mycount.intValue() != othercount.intValue()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }


    }

    public boolean isBiggerThanOrEqual(RMarking marking) {

        Map otherMarking = marking.getMarkedPlaces();
        Set otherPlaces = otherMarking.keySet();
        Set myPlaces = _markedPlaces.keySet();

        Integer mycount, othercount;
        if (myPlaces.containsAll(otherPlaces)) {
            for (Iterator iterator = otherPlaces.iterator(); iterator.hasNext();) {
                String netElement = (String) iterator.next();
                mycount = (Integer) _markedPlaces.get(netElement);
                othercount = (Integer) otherMarking.get(netElement);
                if (mycount.intValue() < othercount.intValue()) {
                    return false;
                }
            }
            return true;

        }
        return false;
    }

    public boolean isBiggerThan(RMarking marking) {

        Map otherMarking = marking.getMarkedPlaces();
        Set otherPlaces = otherMarking.keySet();
        Set myPlaces = _markedPlaces.keySet();
        boolean isBigger = false;
        boolean isEqual = false;
        Integer mycount, othercount;
        if (myPlaces.containsAll(otherPlaces)) {
            for (Iterator iterator = otherPlaces.iterator(); iterator.hasNext();) {
                String netElement = (String) iterator.next();
                mycount = (Integer) _markedPlaces.get(netElement);
                othercount = (Integer) otherMarking.get(netElement);
                if (mycount.intValue() < othercount.intValue()) {
                    return false;
                } else if (mycount.intValue() > othercount.intValue()) {
                    isBigger = true;
                }
            }

            //As it is possible to have equal - need to check here
            if (!isBigger) {
                if (otherPlaces.containsAll(myPlaces))
                    isEqual = true;
            } else {
                isBigger = true;
            }
            return isBigger;

        }
        return false;
    }


    public Map getMarkedPlaces() {
        return _markedPlaces;
    }

    /** This is used for coverable check: x' <= x
     *
     **/
    public boolean isLessThanOrEqual(RMarking marking) {
        Map otherMarking = marking.getMarkedPlaces();
        Set myPlaces = _markedPlaces.keySet();
        Set otherPlaces = otherMarking.keySet();
        //other places mark all my places
        if (otherPlaces.containsAll(myPlaces)) {
            for (Iterator iterator = myPlaces.iterator(); iterator.hasNext();) {
                String netElement = (String) iterator.next();
                Integer mycount = (Integer) _markedPlaces.get(netElement);
                Integer othercount = (Integer) otherMarking.get(netElement);
                if (mycount.intValue() > othercount.intValue()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }

    }
    /*
    //For debugging
    public void debugMarking(String msg){

    String printM = msg + ":";
    Set mPlaces = _markedPlaces.entrySet();
	for (Iterator i= mPlaces.iterator();i.hasNext();)
	{  Map.Entry e = (Map.Entry) i.next();
	    printM += e.getKey()+"("+ e.getValue() + ")\t";

	}
    System.out.println(printM);
    }
    */

}
