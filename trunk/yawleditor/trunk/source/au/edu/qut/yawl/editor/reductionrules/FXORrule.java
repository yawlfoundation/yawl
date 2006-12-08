

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

import java.util.List;
import java.util.Iterator;

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
        List postSet = condition.getPostsetElements();
        List preSet  = condition.getPresetElements();
        if (preSet.size() == 1 && postSet.size() == 1)
         
        {   YTask t = (YTask) preSet.toArray()[0];
            YTask u = (YTask) postSet.toArray()[0];
            
            if (t.getSplitType() == YTask._XOR && u.getJoinType() == YTask._XOR &&
             t.getRemoveSet().isEmpty() && u.getRemoveSet().isEmpty() &&
             t.getCancelledBySet().isEmpty() && u.getCancelledBySet().isEmpty())
            {
             
             List postSetOft = t.getPostsetElements();
             List preSetOfu  = u.getPresetElements();
             //N>1 and \pre{p} = t and \post{p} = u and p not cancel
            if (preSetOfu.equals(postSetOft) && checkEqualConditions(preSetOfu))
            { 
              Iterator conditionsIter = preSetOfu.iterator();
              while (conditionsIter.hasNext()) {
                 YExternalNetElement c = (YExternalNetElement) conditionsIter.next();
                 //remove conditions
                 reducedNet.removeNetElement(c);
                      	                   
	         } //end while
             List postSetOfu = u.getPostsetElements();       
             // set postflows from u to t
             Iterator postFlowIter = postSetOfu.iterator();
             while (postFlowIter.hasNext())
             { YExternalNetElement next = (YExternalNetElement) postFlowIter.next();
               t.setPostset(new YFlow(t,next));
               
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