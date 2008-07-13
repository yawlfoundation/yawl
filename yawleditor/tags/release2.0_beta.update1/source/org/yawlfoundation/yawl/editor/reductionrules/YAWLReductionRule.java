
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
public abstract class YAWLReductionRule 
{
	 /**
    * Main method for calling reduction rule, apply a reduction rule recursively 
    * to a given net until it cannot be reduced further.
    * @net YNet to perform reduction
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
	public YNet reduce(YNet net){
		
	YNet reducedNet = reduceANet(net);
     if (reducedNet == null)
     { return null;
     }
    else
     {  
        while(reducedNet != null)
        { YNet tempNet = reduceANet(reducedNet); 
          if (tempNet == null)
           { return reducedNet;
           }
          else
           { reducedNet = tempNet;
             //System.out.println("tempNet"+tempNet.getNetElements().size());
             //System.out.println("reducedNet"+reducedNet.getNetElements().size());
           }
        }
     }
     return null;
	
	};
	
   /**
    * Inner method for a reduction rule.
    * Go through all elements in a YNet 
    * @net YNet to perform reduction
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
    private YNet reduceANet(YNet net){
      YNet reducedNet = null;
      Map netElements = net.getNetElements();
      Iterator netElesIter = netElements.values().iterator();
      while (netElesIter.hasNext()) {
        YExternalNetElement nextElement = (YExternalNetElement) netElesIter.next();
        
       // setLabel(nextElement);
        
        reducedNet = reduceElement(net,nextElement);
        if (reducedNet != null)
        { return reducedNet;
        }
      }
     return reducedNet;	
    }

/* private YNet reduceANet(YNet net){
      YNet reducedNet = null;
      YInputCondition i = net.getInputCondition();
      Set postElements = new HashSet(i.getPostsetElements());
      reducedNet = reduceElements(net,postElements);
      do{
      	 
         postElements = YNet.getPostset(postElements);
         
         if (postElements.size() > 0)
         {   reducedNet = reduceElements(net,postElements);
             if (reducedNet != null)
             { return reducedNet;
             }
          }
          else
          {  return null;
          }
      }
      while(reducedNet == null);
    
     return reducedNet;	
    }
  */
  
   /**
    * Innermost method for a reduction rule.
    * @net YNet to perform reduction
    * @element one element for consideration.
    * returns a reduced YNet or null if a given net cannot be reduced.
    * Must be implemented by individual reduction rule
    */
    public abstract YNet reduceElement(YNet net,YExternalNetElement element);
     
     
/**
 * Returns true if every condition in the set has one input and 
 * one output and are not part of cancellation regions.
 * 
 */
	public boolean checkEqualConditions(Set conditions)
{
	Iterator conditionsIter = conditions.iterator();
    while (conditionsIter.hasNext()) {
	YCondition c = (YCondition) conditionsIter.next();
	if (!(c.getPresetElements().size() == 1 &&
	     c.getPostsetElements().size() == 1 &&
	     c.getCancelledBySet().isEmpty()))
	     {
	     return false;	
	     }
    }
    return true;
}
	
	/**
	 * This method add labels for YConditions without a name 
	 * by setting the name to be the same as the ID.
	 *
	 */
	public void setLabel(YExternalNetElement e)
	{   
	    String name = e.getName();
	    
/*	    if (name == null)
	    {
	    	System.out.println("setLabel"+e.toString());
	    }
*/		
		if (e instanceof YCondition && (name=="" || name == null))
		 {
		 	e.setName(e.getID());
		 }
	}
	
	}
	