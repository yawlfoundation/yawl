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

package org.yawlfoundation.yawl.editor.analyser;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

/**
 *  Data structure for Marking Storage;
 *
 */

public class RMarking {
    private Map _markedPlaces;
  

    public RMarking(List locations){
    	
    	//Convert to internal representation
       
    	int count;
     	for (Iterator iterator = locations.iterator(); iterator.hasNext();) {
    		RElement netElement = (RElement) iterator.next();
    		String netElementName = netElement.getID();    		
    		if (_markedPlaces.containsKey(netElementName))
    		{ Integer countString = (Integer) _markedPlaces.get(netElementName);
    		  count = countString.intValue();
    		  count ++;
    		  countString = new Integer(count);
     		  _markedPlaces.put(netElementName,countString);  		  
    		}
    	    else
    	    {   Integer tokenCount = new Integer(1);
    	    	_markedPlaces.put(netElementName,tokenCount);
    	    }
    		
        }
        		
         
    }

    public RMarking(Map markedPlaces)
    {
      _markedPlaces = new HashMap(markedPlaces);
       
    	
    }
    public List getLocations(){
       return new LinkedList(_markedPlaces.keySet());
    }

    public boolean equals(Object omarking){
    	 if (!(omarking instanceof RMarking)) {
            return false;
        }
        RMarking marking = (RMarking) omarking;
       	Set otherPlaces = marking.getMarkedPlaces().keySet();
    	Set myPlaces = _markedPlaces.keySet();
       	if (myPlaces.equals(otherPlaces))
    	{ String netElement;
    	  Integer mycount,othercount;
    	  for (Iterator iterator = myPlaces.iterator(); iterator.hasNext();){
    		netElement = (String) iterator.next();
    	    mycount = (Integer) _markedPlaces.get(netElement);
    		othercount = (Integer) marking.getMarkedPlaces().get(netElement);
    		if (mycount.intValue() != othercount.intValue())
    		  { return false;
    		  }
    	  }
    	  return true;
    	}
    	return false;
    }

    public boolean isBiggerThanOrEqual(RMarking marking){
    	
       	Set otherPlaces = marking.getMarkedPlaces().keySet();
    	Set myPlaces = _markedPlaces.keySet();
    	if (myPlaces.containsAll(otherPlaces))
    	{   Integer mycount, othercount;
    		String netElement;
    	    for (Iterator iterator = otherPlaces.iterator(); iterator.hasNext();){
   			netElement = (String) iterator.next();
   	   		mycount = (Integer) _markedPlaces.get(netElement);
    		othercount = (Integer) marking.getMarkedPlaces().get(netElement);
    		if (mycount.intValue() < othercount.intValue())
	    		{ return false; }
	    	}
	    	return true;
	    		
        }
        return false;
    }
    
    public boolean isBiggerThan(RMarking marking){
       	Set otherPlaces = marking.getMarkedPlaces().keySet();
    	Set myPlaces = _markedPlaces.keySet();
    	boolean isBigger = false;
       	
    	if (myPlaces.containsAll(otherPlaces))
    	{  Integer mycount, othercount;
    	   String netElement;
    	    for (Iterator iterator = otherPlaces.iterator(); iterator.hasNext();){
   			netElement = (String) iterator.next();
   	   		mycount = (Integer) _markedPlaces.get(netElement);
    		othercount = (Integer) marking.getMarkedPlaces().get(netElement);
	    		if (mycount.intValue() < othercount.intValue())
		    		{ return false; }
		      	else if (mycount.intValue() > othercount.intValue())
		    	    {  isBigger = true; }
	    	}
	    	
	    	//As it is possible to have equal - need to check here
	    	/*
            if (!isBigger) { 
              if (otherPlaces.containsAll(myPlaces)) {
               isEqual = true;
              }
	    	}
	    	else {
              isBigger = true;
	    	}
            */
	    	return isBigger;
        }
        return false;
    }
    
     
  public Map getMarkedPlaces()
    {
      return new HashMap(_markedPlaces);
    }
   
    /** This is used for coverable check: x' <= x 
     *
     **/   
    public boolean isLessThanOrEqual(RMarking marking){
      	Set myPlaces = _markedPlaces.keySet();
    	Set otherPlaces = marking.getMarkedPlaces().keySet();
    	//other places mark all my places
    	if (otherPlaces.containsAll(myPlaces))
    	{ Integer mycount, othercount;
    	  String netElement;
    	  for (Iterator iterator = myPlaces.iterator(); iterator.hasNext();)
    	  {
   			netElement = (String) iterator.next();
   	   		mycount = (Integer) _markedPlaces.get(netElement);
    		othercount = (Integer) marking.getMarkedPlaces().get(netElement);
    		if (mycount.intValue() > othercount.intValue())
    	    {return false;}  
    	  }
    	 return true;
    	}
    	return false;
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
