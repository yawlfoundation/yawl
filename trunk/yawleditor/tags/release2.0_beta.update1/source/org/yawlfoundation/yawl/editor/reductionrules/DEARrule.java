

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
 * Reduction rule for RWF-net: DEAR rule
 */
public class DEARrule extends ResetReductionRule{

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
             
        RPlace s = (RPlace) nextElement;
        Set postSet = s.getPostsetElements();
        Set preSet = s.getPresetElements();
              
           //\post{s} = t && s cannot be reset
           //s cannot be input place - pre empty
        if (!preSet.isEmpty() && postSet.size() == 1 && s.getCancelledBySet().isEmpty())
         
        {    RTransition t = (RTransition) postSet.toArray()[0];
             Set preSetOft  = new HashSet(t.getPresetElements());
             
             //\pre{t} = s && t does not have reset arcs
             if (preSetOft.size() == 1 && t.getRemoveSet().isEmpty())
             {
             	
             Set preSetOfs = new HashSet(s.getPresetElements());
             Set postpreSetOfs = ResetWFNet.getPostset(preSetOfs);
             Set postSetOft = new HashSet(t.getPostsetElements()); 
             
             postpreSetOfs.retainAll(postSetOft);
             //no direct connection pre s x post t is empty
             //all output of t are not reset places
             if (postpreSetOfs.isEmpty() && !isResetplaces(postSetOft) && 
             isNotOutput(postSetOft) && isNotInput(preSetOfs))
             {                              
             // connections from pre s to post t
             Set preSetOfs2 = new HashSet(s.getPresetElements());
             Iterator preFlowIter = preSetOfs2.iterator();
             { while (preFlowIter.hasNext())
	             { RElement prenext = (RElement) preFlowIter.next();
	               //added for mappings
	             //  prenext.addToYawlMappings(s.getYawlMappings());
                 //  prenext.addToYawlMappings(t.getYawlMappings());
                     prenext.addToResetMappings(s.getResetMappings());
                     prenext.addToResetMappings(t.getResetMappings());
                     
                     
	               Iterator postFlowIter = postSetOft.iterator();
	               while (postFlowIter.hasNext())
		             { RElement postnext = (RElement) postFlowIter.next();
		               prenext.setPostset(new RFlow(prenext,postnext));
		               //added for mappings
		             //  postnext.addToYawlMappings(s.getYawlMappings());
                     //  postnext.addToYawlMappings(t.getYawlMappings());
                         postnext.addToResetMappings(s.getResetMappings());
                         postnext.addToResetMappings(t.getResetMappings());
		             }
	               
	             }
             	
             }
            //remove s and t 
            reducedNet.removeNetElement(s);
            reducedNet.removeNetElement(t);
            
            System.out.println("st"+s.getID()+t.getID());
              //added for mappings
            
                     
            return reducedNet;
            } //if empty
          
        } // if - size 1 t
      } // endif - size 1 s 
	} //endif - s
   return null;
}

private boolean isResetplaces(Set places)
{
  for (Iterator i = places.iterator(); i.hasNext();)
  { RPlace p = (RPlace) i.next();
  	if (!p.getCancelledBySet().isEmpty())
  	{ return true;
  	}
  }
  return false;
} 

private boolean isNotOutput(Set places)
{
  for (Iterator i = places.iterator(); i.hasNext();)
  { RPlace p = (RPlace) i.next();
  	if (p.getPostsetElements().size() == 0)
  	{ return false;
  	}
  }
  return true;
}

private boolean isNotInput(Set places)
{
  for (Iterator i = places.iterator(); i.hasNext();)
  { RPlace p = (RPlace) i.next();
  	if (p.getPresetElements().size() == 0)
  	{ return false;
  	}
  }
  return true;
}
}