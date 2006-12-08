

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
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * Reduction rule for YAWL net: FSPY rule
 */
public class FPPRrule extends ResetReductionRule{

     /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net ResetWFNet to perform reduction
    * @element an  for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
    
    public ResetWFNet reduceElement(ResetWFNet net, RElement nextElement){
      ResetWFNet reducedNet = net;
      boolean isReducible = false;
      if (nextElement instanceof RPlace){
                RPlace place = (RPlace) nextElement;
                Set postSet = place.getPostsetElements();
                Set preSet  = place.getPresetElements(); 
                             
                //check if more than one 
                if (preSet.size() > 1 && postSet.size()>1)
                  
                  { 
                    // potential candidate exits so now try and find 
                    // one or more other places
                    Map netElements = net.getNetElements();
                    Iterator netElesIter = netElements.values().iterator();
    				while (netElesIter.hasNext()) {
	           			 RElement element = (RElement) netElesIter.next();
	            		 if (element instanceof RPlace) {
	                           Set postSet2 = element.getPostsetElements();
	                           Set preSet2  = element.getPresetElements(); 
	                           
		                      //two places with same presets and postsets
		                      //in same cancellation regions
		                     if (postSet.equals(postSet2) && preSet.equals(preSet2) && !element.equals(place) 
		                     && element.getCancelledBySet().equals(place.getCancelledBySet()))
		                     {   isReducible = true;
		                         reducedNet.removeNetElement(element);
		                         place.addToResetMappings(element.getResetMappings());   
		                     }
	                     }//endif - place          
                   
                    }//endwhile - netElements
                    if (isReducible)
                    { return reducedNet;
                    }
                                      
                } // if - size > 1
            
        } //endif - place
   
     
   return null;
} 

}