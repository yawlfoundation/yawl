

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
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class FORrule extends YAWLReductionRule{

    /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net YNet to perform reduction
    * @element an  for consideration.
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
    
    public YNet reduceElement(YNet net, YExternalNetElement nextElement){
    	
      YNet reducedNet = net;
      if (nextElement instanceof YTask){
               YTask task = (YTask) nextElement;
               //join is OR and no cancellation
                if (task.getJoinType() == YTask._OR && task.getRemoveSet().isEmpty() && task.getCancelledBySet().isEmpty()) 
                {
                    Set preSet = task.getPresetElements();
                    Set preSetTasks = YNet.getPreset(preSet);
                    
                    //join paths come from one task
                    if (preSetTasks.size() == 1)   
                    {                        
                        YTask t = (YTask) preSetTasks.toArray()[0];
                        Set postSetOft = t.getPostsetElements();
                         // \post{t} = \pre{u} and t cannot cancel && all are cancelled by the same task
                         // Not sure - should be no cancel as with XOR and AND combinations
                        if (t.getRemoveSet().isEmpty() && t.getCancelledBySet().isEmpty() && postSetOft.equals(preSet) && checkEqualConditions(preSet)) 
                        {
                                      
                         // set postflows
                         Set postFlowElements = task.getPostsetElements();
                         Iterator postFlowIter = postFlowElements.iterator();
                         while (postFlowIter.hasNext())
                         { YExternalNetElement next = (YExternalNetElement) postFlowIter.next();
                           YFlow postflow = new YFlow(t,next);
                           t.setPostset(postflow);
                         }
                        
                         // modify the reducedNet
                        t.setSplitType(task.getSplitType());
                        reducedNet.removeNetElement(task);
                        t.addToYawlMappings(task);
                        t.addToYawlMappings(task.getYawlMappings());
                        
                        Iterator conditionsIter = preSet.iterator();
                        while (conditionsIter.hasNext())
                         { YExternalNetElement next = (YExternalNetElement) conditionsIter.next();
                           reducedNet.removeNetElement(next);
                           
                           t.addToYawlMappings(next);
                           t.addToYawlMappings(next.getYawlMappings());
                         }
                         
                    return reducedNet;
                    } //endif
                } // if - size 1
             } //endif - OR-join
        } //endif - task
    
   return null;
} 

///***
// * Returns true if every condition in the set has one input and 
// * one output and are cancelled by the same task as t and u.
// * 
// */
//private boolean checkEqualConditionswithReset(Set conditions)
//{   YExternalNetElement c;
//    YTask pret;
//    YTask postt;
//    Set preSet, postSet;
//    
//    Iterator conditionsIter = conditions.iterator();
//    while (conditionsIter.hasNext()) {
//		 c = (YExternalNetElement) conditionsIter.next();
//		 preSet = c.getPresetElements();
//		 postSet = c.getPostsetElements();
//		 if (preSet.size() == 1 && postSet.size() == 1)
//	     {  pret = (YTask)preSet.toArray()[0];
//	        postt = (YTask)postSet.toArray()[0];
//	        if (!(pret.getCancelledBySet().equals(c.getCancelledBySet()) &&
//	              postt.getCancelledBySet().equals(c.getCancelledBySet())))
//	        {
//	        	return false;
//	        }	
//	     }
//    }
//    return true;
//}


}