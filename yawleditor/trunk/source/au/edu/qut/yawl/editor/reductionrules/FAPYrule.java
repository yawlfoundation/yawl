

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

import org.yawlfoundation.yawl.elements.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: FAPY rule
 */
public class FAPYrule extends YAWLReductionRule{

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
      if (nextElement instanceof YCondition){
            YCondition condition = (YCondition) nextElement;
            Set postSet = condition.getPostsetElements();
            Set preSet  = condition.getPresetElements(); 
                         
            //check if all pre and post tasks are xor-splits and xor-joins  
            if (preSet.size() > 1 && postSet.size() >1 &&
               checkTaskSplitJoinType(preSet,true,YTask._XOR) && 
               checkTaskSplitJoinType(postSet,false,YTask._XOR))
              { 
                // potential candidate exits so now try and find 
                // one or more other conditions
                Map netElements = net.getNetElements();
                Iterator netElesIter = netElements.values().iterator();
				while (netElesIter.hasNext()) {
           			 YExternalNetElement element = (YExternalNetElement) netElesIter.next();
            		 if (element instanceof YCondition) {
                           Set postSet2 = element.getPostsetElements();
                           Set preSet2  = element.getPresetElements(); 
                           
	                      //To do: cancellation 
	                     if (postSet.equals(postSet2) && preSet.equals(preSet2) && !element.equals(condition)
	                           && element.getCancelledBySet().equals(condition.getCancelledBySet()))
	                     {   isReducible = true;
	                         reducedNet.removeNetElement(element); 
	                         condition.addToYawlMappings(element);
                 			 condition.addToYawlMappings(element.getYawlMappings());
                 			 setLabel(condition);  
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

private boolean checkTaskSplitJoinType(Set elements,boolean checkSplit,int type)
{	Iterator elementsIter = elements.iterator();
	while (elementsIter.hasNext()){
	   YExternalNetElement next = (YExternalNetElement) elementsIter.next();
       if (next instanceof YTask){
	       
	       YTask task = (YTask) next;
	       if (checkSplit)
		   {
		       if (task.getSplitType() != type)
		       { return false;
		       }	
           }
           else
		   {
			   if (task.getJoinType() != type)
		       { return false;
		       }
			}                      
      	}
    } 
	return true;
}
}