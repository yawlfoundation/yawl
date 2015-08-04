/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Lachlan Aldred
 * Date: 19/06/2003
 * Time: 15:22:56
 * 
 */
public class YSetOfMarkings {
    private Set _markings = new HashSet();

 
   
   //moe - ResetAnalyser 
    public void addMarking(YMarking marking){
   	   if (!contains(marking)){
         _markings.add(marking);
       }	
    }
    
   //moe - ResetAnalyser    
   public void addAll(YSetOfMarkings newmarkings){
     Set markingsToAdd = newmarkings.getMarkings();
     for (Iterator i = markingsToAdd.iterator(); i.hasNext();)
     {  YMarking marking = (YMarking) i.next();
        addMarking(marking);
     }
     
 //_markings.addAll(newmarkings.getMarkings());   	
    }

    //changed by moe - ResetAnalyser
    public boolean contains(YMarking marking) {
        
     for (Iterator i = _markings.iterator(); i.hasNext();)
     {
       YMarking currentM = (YMarking) i.next();	
       if (currentM.equivalentTo(marking))
       { return true;
       }
     }  
    	
     return false;
    }

    //added by moe - ResetAnalyser
     public boolean equals(YSetOfMarkings markings) {
    Set markingsToCompare = markings.getMarkings();
    if (_markings.size() != markingsToCompare.size()) {
      return false;
    }
    if (containsAll(markingsToCompare)
        && (markings.containsAll(_markings))) {
      return true;
    }
    return false;
  }
  
    public boolean containsAll(Set markingsToCompare)
    { YMarking M;
     for (Iterator i = markingsToCompare.iterator(); i.hasNext();)
     {	  M = (YMarking) i.next();
		  if (!this.contains(M))
	       { return false;
	       }
     }
     return true;	
    }
    
   //moe - ResetAnalyser
   public void removeAll(){
    	_markings.clear();
    }
    
    public Set getMarkings() {
        return _markings;
    }

    public int size() {
        return _markings.size();
    }

    public YMarking removeAMarking() {
        if (_markings.size() > 0) {
            YMarking marking = (YMarking) _markings.iterator().next();
            _markings.remove(marking);
            return marking;
        }
        return null;
    }

    public boolean containsEquivalentMarkingTo(YSetOfMarkings possibleFutureMarkingSet) {
        Set possibleMarkings = possibleFutureMarkingSet.getMarkings();
        for (Iterator iterator = possibleMarkings.iterator(); iterator.hasNext();) {
            YMarking marking = (YMarking) iterator.next();
            for (Iterator thisSetOfMarkings = _markings.iterator(); thisSetOfMarkings.hasNext();) {
                YMarking thisSetsMarking = (YMarking) thisSetOfMarkings.next();
                if (marking.equivalentTo(thisSetsMarking)) {
                    return true;
                }
            }
        }
        return false;
    }
     //moe - ResetAnalyser 
    public boolean containsBiggerEqual(YMarking m)
    {
     for (Iterator i = _markings.iterator(); i.hasNext();)
     {
       YMarking currentM = (YMarking) i.next();	
       if (currentM.isBiggerThanOrEqual(m))
       { return true;
       }
     }  
    return false;
   }
}