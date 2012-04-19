package org.yawlfoundation.yawl.editor.analyser;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.*;

public class OrjoinInCycleUtils {

	  /**
     * checks whether two or more OR-joins are in a cycle, resulting in a potential 
     * vicious circle.
     * returns two values Object[0] is a boolean value and Object[1] contains a message string.
     */
    public Object[] checkORjoinsInCycle(YNet net)
    {   String msg = "";
        boolean containCycles = false;
        //First, get all the OR-joins in the net
        List tasks = net.getNetTasks();
        Set ORJoins = new HashSet();
        Map ojPresets = new HashMap();
        for (Iterator t = tasks.iterator(); t.hasNext();)
        { 	 YTask task = (YTask) t.next();
             if (task.getJoinType() == YTask._OR)
       	 {           	    
         		 ORJoins.add(task);
            }
        }
        if (ORJoins.size() > 1)
        {
     	  //Get all predecessors for each OR-join
       for (Iterator i= ORJoins.iterator(); i.hasNext();)
 	   	 {  YTask oj = (YTask) i.next();
 	        Set preSet = oj.getPresetElements();    
 	        Set predecessors = new HashSet();
 	        predecessors.addAll(preSet);
 	        Set pred2 = new HashSet();
 	        do
 	        { pred2.clear();
 	          pred2.addAll(predecessors);
 	          predecessors.addAll(YNet.getPreset(pred2));
 	        }	
 	        while(!pred2.containsAll(predecessors));
 	        //keep all OR-joins in presets
 	        predecessors.retainAll(ORJoins);
 	       //if the OR-join in question is in its own preset, remove it.
 	        predecessors.remove(oj);
 	        ojPresets.put(oj, predecessors);
 	    }
 	   	 
 	   	 //Check predecessors for each OR-join
 		for( Iterator i1= ojPresets.keySet().iterator(); i1.hasNext();)
 		 { YTask oj1 = (YTask) i1.next();
 		 Set preSet = (Set) ojPresets.get(oj1);
 	   	 if (preSet != null)
 	   	 {
 	   		 for (Iterator i2= preSet.iterator(); i2.hasNext();)
 	   		 { YTask oj2 = (YTask) i2.next();
 		     Set jpreSet = (Set) ojPresets.get(oj2);
 		     if (jpreSet !=null && jpreSet.contains(oj1))
 		     {   containCycles = true;
 		         //remove oj1 so that there is no duplicate error msgs.
 		         jpreSet.remove(oj1);
 		         ojPresets.put(oj2, jpreSet);
 		         String ms = "OR-joins " + oj1.getID() + " and " + oj2.getID() +" are on a cycle.";
 		         msg += formatXMLMessage(ms,false); 
 		     }
 	   		 
 	   		 }
 	   	 }
 		 }
        }
      	 if (!containCycles)
 	   	 {
 	   		  msg += formatXMLMessage("No OR-joins are in a cycle.",true);  
 	   	 }
 	   	 
          	
        return new Object[]{containCycles,msg};
    }
        
    /**
	 * used for formatting xml messages.
	 * Message could be a warning or observation. 
	 */ 
     private String formatXMLMessage(String msg,boolean isObservation)
	 { 
	  
	   String xmlHeader;
	   String xmlFooter;
	  if (isObservation)	
	  {
	      xmlHeader="<observation>";
		  xmlFooter="</observation>";
		  
	  }
	  else
	  {
		  xmlHeader="<warning>";
		  xmlFooter="</warning>";
	  } 	 
	  	return xmlHeader + msg + xmlFooter;
	 	
	 }
	      
}
