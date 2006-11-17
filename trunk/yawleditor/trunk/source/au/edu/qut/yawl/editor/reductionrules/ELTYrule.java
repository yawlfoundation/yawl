

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

import au.edu.qut.yawl.editor.analyser.CollectionUtils;
import au.edu.qut.yawl.elements.*;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class ELTYrule extends YAWLReductionRule{

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
               Set preSet = CollectionUtils.getSetFromList(task.getPresetElements());
               Set postSet = CollectionUtils.getSetFromList(task.getPostsetElements());
               // t has only one input and output
               // t cannot reset and t is not part of cancellation region
               if (preSet.equals(postSet) && preSet.size() == 1 && 
                  task.getRemoveSet().isEmpty() && task.getCancelledBySet().isEmpty())   
                  {  
                   YCondition condition = (YCondition) preSet.toArray()[0];
                   //p is not in the cancellation region
                   if (condition.getCancelledBySet().isEmpty())
                   {
                   	reducedNet.removeNetElement(task); 
                   	
                   	condition.addToYawlMappings(task);
                   	condition.addToYawlMappings(task.getYawlMappings());
                    
                    setLabel(condition);
                       
                    return reducedNet;
                   }
                   
               } // if - size 1
            
             } //endif - task
    
    return null;
} 
}