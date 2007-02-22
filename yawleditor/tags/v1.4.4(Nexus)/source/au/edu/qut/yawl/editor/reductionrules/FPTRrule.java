

/*
 * 
 * @author Moe Thandar Wynn
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
 
package au.edu.qut.yawl.editor.reductionrules;

import au.edu.qut.yawl.editor.analyser.*;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: FPTY rule
 */
public class FPTRrule extends ResetReductionRule{

    /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net ResetWFNet to perform reduction
    * @element an  for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
    
    public ResetWFNet reduceElement(ResetWFNet net, RElement nextElement){
     ResetWFNet reducedNet = net;
     boolean isReducible = false;
	if (nextElement instanceof RTransition){
	        RTransition task = (RTransition) nextElement;
	        Set postSet = task.getPostsetElements();
	        Set preSet  = task.getPresetElements(); 
	                     
	       
	     //   if (preSet.size() > 1 && postSet.size() > 1 &&
	          if( task.getRemoveSet().isEmpty())
	          { 
	            // potential candidate exits so now try and find 
	            // one or more other tasks
	            Map netElements = net.getNetElements();
	            Iterator netElesIter = netElements.values().iterator();
				while (netElesIter.hasNext()) {
	       			 RElement element = (RElement) netElesIter.next();
	        		 if (element instanceof RTransition) {
	                       Set postSet2 = element.getPostsetElements();
	                       Set preSet2  = element.getPresetElements(); 
	                       RTransition elementTask = (RTransition) element;
	                       
	                       //two tasks are identical
	                     if (postSet.equals(postSet2) && preSet.equals(preSet2) && elementTask.getRemoveSet().isEmpty() &&
	                         task.getCancelledBySet().isEmpty() && element.getCancelledBySet().isEmpty() &&
	                        elementTask.getRemoveSet().isEmpty() && !element.equals(task))
	                     {   isReducible = true;
	                         reducedNet.removeNetElement(element);
	                         //add to mappings
	                         task.addToYawlMappings(element.getYawlMappings());
	                        
	                     }//endif - condition          
	           		}
	            }//endwhile - netElements
	            if (isReducible)
	            { return reducedNet;
	            }
	                              
	        } // if - size > 1
	    
	} //endif - condition
       
   return null;
} 

}