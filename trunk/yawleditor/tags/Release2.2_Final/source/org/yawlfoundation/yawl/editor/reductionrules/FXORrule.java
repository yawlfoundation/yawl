

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

import java.util.Iterator;
import java.util.Set;

/**
 * Reduction rule for YAWL net: FXOR rule
 */
public class FXORrule extends YAWLReductionRule{

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
        if (preSet.size() == 1 && postSet.size() == 1)
         
        {   YTask t = (YTask) preSet.toArray()[0];
            YTask u = (YTask) postSet.toArray()[0];
            
            if (t.getSplitType() == YTask._XOR && u.getJoinType() == YTask._XOR &&
             t.getRemoveSet().isEmpty() && u.getRemoveSet().isEmpty() &&
             t.getCancelledBySet().isEmpty() && u.getCancelledBySet().isEmpty())
            {
             
             Set postSetOft = t.getPostsetElements();
             Set preSetOfu  = u.getPresetElements();
             //N>1 and \pre{p} = t and \post{p} = u and p not cancel
            if (preSetOfu.equals(postSetOft) && checkEqualConditions(preSetOfu))
            { 
              Iterator conditionsIter = preSetOfu.iterator();
              while (conditionsIter.hasNext()) {
                 YExternalNetElement c = (YExternalNetElement) conditionsIter.next();
                 //remove conditions
                 reducedNet.removeNetElement(c);
                      	                   
	         } //end while
             Set postSetOfu = u.getPostsetElements();       
             // set postflows from u to t
             Iterator postFlowIter = postSetOfu.iterator();
             while (postFlowIter.hasNext())
             { YExternalNetElement next = (YExternalNetElement) postFlowIter.next();
               t.addPostset(new YFlow(t,next));
               
             }
             t.setSplitType(u.getSplitType());
             reducedNet.removeNetElement(u);
             return reducedNet;
             	
          	}//endif preset equals postset
          }//endif - splittype	
        } // if - size 1
    
	} //endif - condition
     
   return null;
} 


}