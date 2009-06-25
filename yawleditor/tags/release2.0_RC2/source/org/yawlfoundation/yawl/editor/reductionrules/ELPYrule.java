

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
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: ELPY
 */
public class ELPYrule extends YAWLReductionRule{

    /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net YNet to perform reduction
    * @element an  for consideration.
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
    
    public YNet reduceElement(YNet net, YExternalNetElement nextElement){
      YNet reducedNet = net;    
      if (nextElement instanceof YCondition){
               YCondition condition = (YCondition) nextElement;
               Set preSet = condition.getPresetElements();
               Set postSet = condition.getPostsetElements();
               // x has only one input and output - t
               // t cannot reset and t is not part of cancellation region
               if (preSet.equals(postSet) && preSet.size() == 1 )   
                  {  
                   YTask task = (YTask) preSet.toArray()[0];
                   //t does not reset
                   if (task.getRemoveSet().isEmpty())
                   {
                   //Get all pre and post conditions of t
                   Set prepostConditions = task.getPresetElements();
                   prepostConditions.addAll(task.getPostsetElements());
                   Set cancelledBySet = condition.getCancelledBySet();
                   //has the same reset arcs or empty
                   if (hasSameCancelledBySet(prepostConditions,cancelledBySet))
                   	{
                	   reducedNet.removeNetElement(condition); 
                          	
                   	task.addToYawlMappings(condition);
                   	task.addToYawlMappings(condition.getYawlMappings());
                    
                    setLabel(task);
                       
                    return reducedNet;
                   }
                   
               } 
            
              }
      }
    return null;
} 
     private boolean hasSameCancelledBySet(Set conditions,Set cancelledBySet)
     { 	 Iterator conditionsIter = conditions.iterator();
	     while (conditionsIter.hasNext()) 
	     {
		  YExternalNetElement element = (YExternalNetElement) conditionsIter.next();
           if (!element.getCancelledBySet().equals(cancelledBySet))
          {
      	   return false;
          }
	     }
          return true;
      
     }
}