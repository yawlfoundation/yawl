

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

import org.yawlfoundation.yawl.editor.analyser.*;
import org.yawlfoundation.yawl.editor.analyser.RElement;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Reduction rule for RWF-net: FSPR rule
 */
public class FSPRrule extends ResetReductionRule{

   /**
    * Innermost method for a reduction rule.
    * @net ResetWFNet to perform reduction
    * @element one element for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    * Must be implemented by individual reduction rule
    */
    public ResetWFNet reduceElement(ResetWFNet net,RElement nextElement)
     {
     ResetWFNet reducedNet = net;
     if (nextElement instanceof RPlace){
        RPlace place = (RPlace) nextElement;
        Set postSet = place.getPostsetElements();
        Set postSetPlaces = ResetWFNet.getPostset(postSet);
        Set preSet = place.getPresetElements();
           //\post{p} = 1 and \post{t} = q (just one) and p is not input place
        if (postSet.size() == 1 && postSetPlaces.size() == 1 && !preSet.isEmpty())
         
        {    RTransition t = (RTransition) postSet.toArray()[0];
             Set preSetOft = t.getPresetElements();
             //pre{t} = p (just one) 
             if (preSetOft.size() == 1)
             {
             
             RPlace q = (RPlace) postSetPlaces.toArray()[0];
             Set preSetOfq = new HashSet(q.getPresetElements());
             Set preSetOfp  = new HashSet(place.getPresetElements());
             
             preSetOfp.retainAll(preSetOfq);
             
             //q is not output place
             //check for direct places \pre{p} \cap \pre{q} = empty               
             //R-1(p) = R-1(q)
             //rem(t) = emptyset
             if (!(q.equals(net.getOutputPlace())) && preSetOfp.isEmpty() &&
             t.getRemoveSet().isEmpty() && 
             place.getCancelledBySet().equals(q.getCancelledBySet())) 
             {
                     
             // set postflows from q to p
             Set postSetOfq = new HashSet(q.getPostsetElements());
             Iterator postFlowIter = postSetOfq.iterator();
             while (postFlowIter.hasNext())
             { RElement next = (RElement) postFlowIter.next();
               place.setPostset(new RFlow(place,next));
             }
            
             // set preflows from q to p - t
             preSetOfq.remove(t); 
             Iterator preFlowIter = preSetOfq.iterator();
             while (preFlowIter.hasNext())
             { RElement next = (RElement) preFlowIter.next();
               place.setPreset(new RFlow(next,place));
             }
             
             //remove t from postset of place 
            place.removePostsetFlow(new RFlow(place,t)); 
            
            //added for mappings
      //      place.addToYawlMappings(q.getYawlMappings());
      //      place.addToYawlMappings(t.getYawlMappings());
           
            place.addToResetMappings(q.getResetMappings());    
            place.addToResetMappings(t.getResetMappings());
            	           
            reducedNet.removeNetElement(q);
            reducedNet.removeNetElement(t);
           
            return reducedNet;
            } //if nested
          }
        } // if - size 1
    
	} //endif - place
   return null;
} 
}