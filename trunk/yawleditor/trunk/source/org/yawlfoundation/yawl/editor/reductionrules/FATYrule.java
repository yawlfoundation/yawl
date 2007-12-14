

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
 
package org.yawlfoundation.yawl.editor.reductionrules;

import org.yawlfoundation.yawl.elements.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: FATY rule
 */
public class FATYrule extends YAWLReductionRule{

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
        Set postSet = task.getPostsetElements();
        Set preSet  = task.getPresetElements(); 
                     
        //check if task is xor-split and xor-join  
        if (preSet.size() > 1 && postSet.size()>1 &&
            task.getSplitType() == YTask._XOR && 
            task.getJoinType() == YTask._XOR )
          { 
            // potential candidate exits so now try and find 
            // one or more other tasks
            Map netElements = net.getNetElements();
            Iterator netElesIter = netElements.values().iterator();
			while (netElesIter.hasNext()) {
       			 YExternalNetElement element = (YExternalNetElement) netElesIter.next();
        		 if (element instanceof YTask) {
                       Set postSet2 = element.getPostsetElements();
                       Set preSet2  = element.getPresetElements(); 
                       YTask elementTask = (YTask) element;
                     if (postSet.equals(postSet2) && preSet.equals(preSet2) && 
                      task.getRemoveSet().isEmpty() &&
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