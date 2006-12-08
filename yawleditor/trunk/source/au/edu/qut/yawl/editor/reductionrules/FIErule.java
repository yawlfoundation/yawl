

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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class FIErule extends YAWLReductionRule{

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
        //ORjoin 
        if (task.getJoinType() == YTask._OR) 
        {   //all inputs to ORjoin - preSet
            List preSet = new LinkedList(task.getPresetElements());
            //from two or more places
            if (preSet.size() >=  2)
           { //all input tasks to these places
             Set preSetTasks = YNet.getPreset(preSet);      
             Iterator preEls = preSetTasks.iterator();
             while (preEls.hasNext()) {
              YTask t = (YTask) preEls.next();
              List postSetOft = t.getPostsetElements();
              
              preSet.retainAll(postSetOft);
              
              //task has more than one input to ORjoin 
             if (preSet.size()>1 && checkEqualConditions(preSet))
              { //remove common places except 1
              	 Iterator commonInputsIter = preSet.iterator();
                 YExternalNetElement firstCommonPlace = (YExternalNetElement) commonInputsIter.next();
                 while (commonInputsIter.hasNext())
                 { YExternalNetElement commonPlace = (YExternalNetElement) commonInputsIter.next();
                   reducedNet.removeNetElement(commonPlace);
                   task.addToYawlMappings(commonPlace);
                   task.addToYawlMappings(commonPlace.getYawlMappings());
                   isReducible = true;
                 }	
              }
             if (isReducible)
            { 
            	return reducedNet;
            }                    
           }//while  
        } //endif N>=2 
       }  //endif - OR-join
	} //endif - task
   return null;
} 


}