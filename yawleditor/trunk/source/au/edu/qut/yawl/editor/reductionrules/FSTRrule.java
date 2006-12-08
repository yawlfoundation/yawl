

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

import au.edu.qut.yawl.editor.analyser.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: FSPY rule
 */
public class FSTRrule extends ResetReductionRule{

   /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net ResetWFNet to perform reduction
    * @element an  for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
    
    public ResetWFNet reduceElement(ResetWFNet net, RElement nextElement){
    ResetWFNet reducedNet = net;
    if (nextElement instanceof RPlace){
    RPlace place = (RPlace) nextElement;
    Set postSet = place.getPostsetElements();
    Set preSet  = place.getPresetElements();
                    
    if (preSet.size() == 1 && postSet.size() == 1)   
    {   
         RTransition t = (RTransition) preSet.toArray()[0];
         RTransition u = (RTransition) postSet.toArray()[0];
         Set preSetOfu = u.getPresetElements();
         Set postSetOfu = u.getPostsetElements();
         Set postSetOft = new HashSet(t.getPostsetElements());
         
         postSetOft.retainAll(postSetOfu);
          
         // u, p are not reset 
         // u does not have reset arcs 
         // output of u are not reset 
         // postset u is not postset t
         if (preSetOfu.size() == 1 && u.getRemoveSet().isEmpty() &&
             place.getCancelledBySet().isEmpty() && (!checkReset(postSetOfu))
             && postSetOft.isEmpty())
         {
       
         // set postflows from u to t
         Iterator postFlowIter = postSetOfu.iterator();
         while (postFlowIter.hasNext())
         { RElement next = (RElement) postFlowIter.next();
           t.setPostset(new RFlow(t,next));
         }
        
         //remove place from postset of t
        t.removePostsetFlow(new RFlow(place,t)); 
        
        //add to Mappings           
      //  t.addToYawlMappings(place.getYawlMappings());
      //  t.addToYawlMappings(u.getYawlMappings());
        
        t.addToResetMappings(place.getResetMappings());
        t.addToResetMappings(u.getResetMappings()); 
                        
        reducedNet.removeNetElement(place);
        reducedNet.removeNetElement(u);
       
        return reducedNet;
        } //if nested
      
    } // if - size 1
        
    } //endif - place
 
   return null;
} 

private boolean checkReset(Set elements)
{
	Iterator eleIter = elements.iterator();
	while (eleIter.hasNext())
	 { RElement next = (RElement) eleIter.next();
	   if (!next.getCancelledBySet().isEmpty())
	   { return true;
	   }
	 }
 return false;	 
}
}