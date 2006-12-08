

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
 * Reduction rule for RWF-net: FESR rule
 */
public class FESRrule extends ResetReductionRule{

     /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @net ResetWFNet to perform reduction
    * @p2 an  for consideration.
    * returns a reduced ResetWFNet or null if a given net cannot be reduced.
    */
    
    public ResetWFNet reduceElement(ResetWFNet net, RElement nextElement){
      ResetWFNet reducedNet = net;
      boolean isReducible = false;
      if (nextElement instanceof RPlace){
                RPlace p = (RPlace) nextElement;
                Set postSet = p.getPostsetElements();
                Set preSet  = p.getPresetElements(); 
               
		                                     
                //check if more than one && preset and postset transitions have exactly one
                if (preSet.size() > 1 && postSet.size() >1 &&
                   checkTransitionsOnePrePost(preSet) &&
                   checkTransitionsOnePrePost(postSet))
                  
                  { 
                     // potential candidate exits so now try and find 
                    // one or more other ps
                    Map netElements = net.getNetElements();
                    Iterator netElesIter = netElements.values().iterator();
    				while (netElesIter.hasNext()) {
	           			 RElement p2 = (RElement) netElesIter.next();
	            		 if (p2 instanceof RPlace) {
	                           Set postSet2 = p2.getPostsetElements();
	                           Set preSet2  = p2.getPresetElements(); 
	                           
		                     
		                      // found another place
		                     if (preSet2.size() > 1 && postSet2.size() >1 &&
                  				checkTransitionsOnePrePost(preSet2) &&
                   				checkTransitionsOnePrePost(postSet2) && 
                   				p2.getCancelledBySet().equals(p.getCancelledBySet()) &&
                   				checkForEquivalence(preSet,preSet2) && 
                   				checkForEquivalence(postSet,postSet2))             		                     {  
		                         isReducible = true;
		                         reducedNet.removeNetElement(p2);
		                         removeFromNet(reducedNet,preSet2);
		                         removeFromNet(reducedNet,postSet2);
                             }
		                     
	                     }//endif - p          
                   
                    }//endwhile - netElements
                    if (isReducible)
                    { return reducedNet;
                    }
                                      
                } // if - size > 1
            
        } //endif - p
   
     
   return null;
}
 
private boolean checkTransitionsOnePrePost(Set elements)
{
 Iterator elesIter = elements.iterator();
 while (elesIter.hasNext()) {
	 RElement element = (RElement) elesIter.next();
	 Set preSet = element.getPresetElements();
	 Set postSet = element.getPostsetElements();
	 if (!(preSet.size() == 1 && postSet.size() ==1))
	 { return false;	
	 }
}
return true;
}

private boolean checkForEquivalence(Set transitions1, Set transitions2)
{   
    if (transitions1.size() == transitions2.size())
    {
    
    Set preSet = ResetWFNet.getPreset(transitions1);
    Set preSet2 = ResetWFNet.getPreset(transitions2);
    Set postSet = ResetWFNet.getPostset(transitions1);
    Set postSet2 = ResetWFNet.getPostset(transitions2);
    
    if (preSet.equals(preSet2) && postSet.equals(postSet2))
    { //now consider individual transition and 
      //compare removeset
      Iterator t1Iter = transitions1.iterator();
      while(t1Iter.hasNext()){
      	 RTransition t1 = (RTransition) t1Iter.next();
      	
      	 if (!hasEquivalentTransition(t1,transitions2))
      	 { 
      	   return false; 
      	 }
       } //end while	 
     }
    else
    { return false;
    }
   }
   else
   { return false;
   }
 return true;
	
}


private boolean hasEquivalentTransition(RTransition t,Set transitions)
{ 
   Set removeSet = t.getRemoveSet();
   Set preSetOft = t.getPresetElements();
   Set postSetOft = t.getPostsetElements();
   
   Iterator tIter = transitions.iterator();
   while(tIter.hasNext()){
      RTransition t2 = (RTransition) tIter.next();
      	Set removeSet2 = t2.getRemoveSet();
        Set preSetOft2 = t2.getPresetElements();
        Set postSetOft2 = t2.getPostsetElements();
        
      	if (preSetOft.equals(preSetOft2) && postSetOft.equals(postSetOft2) &&
      	 removeSet.equals(removeSet2) )
      	 { 
      	   return true; 
      	 }
    }	 	
   return false;	
}


private void removeFromNet(ResetWFNet net, Set elements)
{
 Iterator elesIter = elements.iterator();
 while (elesIter.hasNext()) {
	 RElement element = (RElement) elesIter.next();
	 net.removeNetElement(element);
 }	
}

}