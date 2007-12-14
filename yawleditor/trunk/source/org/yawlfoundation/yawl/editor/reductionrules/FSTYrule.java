

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
 * Reduction rule for YAWL net: FSPY rule
 */
public class FSTYrule extends YAWLReductionRule{

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
    Set postSet = condition.getPostsetElements();
    Set preSet  = condition.getPresetElements();
    
    //\pre{p} = 1, \post{p} = 1                
    if (preSet.size() == 1 && postSet.size() == 1)   
    {   
         YTask t = (YTask) preSet.toArray()[0];
         YTask u = (YTask) postSet.toArray()[0];
         Set preSetOfu = u.getPresetElements();
         Set postSetOfu = u.getPostsetElements();
         // t,u and p are not reset 
         // u does not have reset arcs 
         
         Set postSetOft = new HashSet(t.getPostsetElements());
         postSetOft.retainAll(postSetOfu);
          
         if (t.getSplitType()==YTask._AND && u.getSplitType() == YTask._AND 
          && preSetOfu.size() == 1 && u.getRemoveSet().isEmpty() &&
          t.getCancelledBySet().isEmpty() && u.getCancelledBySet().isEmpty() &&
          condition.getCancelledBySet().isEmpty() &&
          (!checkReset(postSetOfu)) && postSetOft.isEmpty())
         {
         
         // set postflows from u to t
         
         Iterator postFlowIter = postSetOfu.iterator();
         while (postFlowIter.hasNext())
         { YExternalNetElement next = (YExternalNetElement) postFlowIter.next();
           t.setPostset(new YFlow(t,next));
         }
        
         //remove condition from postset of t
        t.removePostsetFlow(new YFlow(condition,t)); 
                   
        reducedNet.removeNetElement(condition);
        reducedNet.removeNetElement(u);
       
       t.addToYawlMappings(condition);
       t.addToYawlMappings(condition.getYawlMappings());
       t.addToYawlMappings(u);
       t.addToYawlMappings(u.getYawlMappings());
       
       
        return reducedNet;
        } //if nested
      
    } // if - size 1
        
    } //endif - condition
 
   return null;
} 
private boolean checkReset(Set elements)
{
	Iterator eleIter = elements.iterator();
	while (eleIter.hasNext())
	 { YExternalNetElement next = (YExternalNetElement) eleIter.next();
	   if (!next.getCancelledBySet().isEmpty())
	   { return true;
	   }
	 }
 return false;	 
}
}