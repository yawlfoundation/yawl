

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

import au.edu.qut.yawl.elements.*;
import java.util.Iterator;
import java.util.List;
/**
 * Reduction rule for YAWL net: FPTY rule
 */
public class FPTYrule extends YAWLReductionRule{

    /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net YNet to perform reduction
    * @element an  for consideration.
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
    
    public YNet reduceElement(YNet net, YExternalNetElement nextElement){
     YNet reducedNet = net;
     boolean isReducible = false;
	if (nextElement instanceof YTask){
	        YTask task = (YTask) nextElement;
	        List postSet = task.getPostsetElements();
	        List preSet  = task.getPresetElements(); 
	                     
	        //check if task is and-split and and-join  
	        if (preSet.size() > 1 && postSet.size() > 1 &&
	            task.getSplitType() == YTask._AND && 
	            task.getJoinType() == YTask._AND && 
	            task.getRemoveSet().isEmpty())
	          { 
	            // potential candidate exits so now try and find 
	            // one or more other tasks
	            List netElements = net.getNetElements();
	            Iterator netElesIter = netElements.iterator();
				while (netElesIter.hasNext()) {
	       			 YExternalNetElement element = (YExternalNetElement) netElesIter.next();
	        		 if (element instanceof YTask) {
	                       List postSet2 = element.getPostsetElements();
	                       List preSet2  = element.getPresetElements(); 
	                       YTask elementTask = (YTask) element;
	                       
	                       //two tasks are identical
	                     if (postSet.equals(postSet2) && preSet.equals(preSet2) && elementTask.getRemoveSet().isEmpty() &&
	                     	task.getSplitType() == elementTask.getSplitType() && 
	                        task.getJoinType() == elementTask.getJoinType() &&
	                        task.getCancelledBySet().isEmpty() && element.getCancelledBySet().isEmpty() &&
	                        elementTask.getRemoveSet().isEmpty() && !element.equals(task))
	                     {   isReducible = true;
	                         reducedNet.removeNetElement(element); 
	                          task.addToYawlMappings(element);
       						  task.addToYawlMappings(element.getYawlMappings());
         
	                     }
	                 }//endif - condition          
	           
	            }//endwhile - netElements
	            if (isReducible)
	            { return reducedNet;
	            }
	                              
	        } // if - size > 1
	    
	} //endif - condition
       
   return null;
} 

}