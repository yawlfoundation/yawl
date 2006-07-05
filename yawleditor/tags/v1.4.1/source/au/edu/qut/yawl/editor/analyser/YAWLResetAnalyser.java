/*
 * Created on 16/02/2006
 * YAWLEditor v1.4 
 *
 * @author Moe Thandar Wyn
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

package au.edu.qut.yawl.editor.analyser;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.exceptions.*;

import java.io.IOException;
import java.util.*;
import org.jdom.JDOMException;

public class YAWLResetAnalyser{

  public static final String RESET_NET_ANALYSIS_PREFERENCE = "resetNetAnalysisCheck";
  public static final String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";
  public static final String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";
  public static final String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";
  public static final String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";
  public static final String SHOW_OBSERVATIONS_PREFERENCE = "resetShowObservationsCheck";
	
      /**
     * This method is used for the analysis.
     * @fileURL - xml file format of the YAWL model
     * @options - four options (weak soundness w, soundness s, cancellation set c, orjoins o) 
     * returns a xml formatted string with warnings and observations.
     */
     public String analyse(String fileURL,String options) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException {
    	
      	
       List specifications = YMarshal.unmarshalSpecifications(fileURL);
       ListIterator listIt = specifications.listIterator();
       String msg = "";
       
       // TODO: Check whether reset net analysis is needed at all.
        
       while (listIt.hasNext()){
       
       YSpecification specs = (YSpecification) listIt.next();
  
	   //Check if there is decomposition
	   Set decompositions = new HashSet(specs.getDecompositions());
	   if (decompositions.size()>0)
	   { 
	        for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
	            YDecomposition decomposition = (YDecomposition) iterator.next();
		        if (decomposition instanceof YNet) {
		        
		        YNet decomRootNet = (YNet) decomposition;        
	            ResetWFNet decomResetNet = new ResetWFNet(decomRootNet);
		        YAWLReachabilityUtils utils = new YAWLReachabilityUtils(decomRootNet);
		        if (options.indexOf("w") >= 0)
		        { msg += decomResetNet.checkWeakSoundness();
		        }
		       
		   	    
		   	    //checking rechability set for bounded nets
		   	    if (options.indexOf("s") >= 0)
		        {  try{
		           	if (decomResetNet.containsORjoins())
		           	{ 
		           	  
		           	  msg += utils.checkSoundness();
		           	}
		           	else
		           	{  
		           	 msg += decomResetNet.checkSoundness();
		           	}
		           	
		           }
		           catch (Exception e)
		           {
		            msg += formatXMLMessage(e.toString(),false);
		        
		          
		           }
		           catch (OutOfMemoryError e)
		           {
		           	msg += formatXMLMessage(e.toString(),false);
		           }
		   	   }
		   	   
		   	   if (options.indexOf("c") >= 0)
		   	   {
		   	      msg += decomResetNet.checkCancellationSets();  
		   	   }
		   	   
		   	   //do unnecessary orjoin checks
		   	  if (options.indexOf("o") >= 0)
		   	  {  
		   	     try{
		           	if (decomResetNet.containsORjoins())
		           	{ 
		           	  
		           	  msg += utils.checkUnnecessaryORJoins();
		           	}
		           	else
		           	{  
		           	 msg += formatXMLMessage("There are no OR-joins in the net "+decomRootNet.getID()+".",true);
		           	}
		           	
		           }
		           catch (Exception e)
		           {
		            msg += formatXMLMessage(e.toString(),false);
		       
		           }
		           catch (OutOfMemoryError e)
		           {
		           	msg += formatXMLMessage(e.toString(),false);
		           }
		   	  } 
		   	    		
		 	
		  /**
		     //Alternative proper completion check 
                YNet transformedNet = transformNetforProperCompletionCheck(decomRootNet);
		        ResetWFNet transformedResetNet = new ResetWFNet(transformedNet);
		   		msg += transformedResetNet.checkProperCompletion2();
		   		
		   		msg += decomResetNet.checkCancellationSets();
			   
		  */       
		        
	         } //endif
	   		} //end for
	   }//end if
	 	    
     }//end while
	return formatXMLMessageForEditor(msg);  
      
    }
    
    
    
    public String formatXMLMessageForEditor(String msg)
	 {String xmlHeader="<wofyawl><net><behavior>";
	  String xmlFooter="</behavior></net></wofyawl>";
	  
	  return xmlHeader + msg + xmlFooter;
	 	
	 }
	 
	/**
	 * used for formatting xml messages.
	 * Message could be a warning or observation. 
	 */ 
    private String formatXMLMessage(String msg,boolean isObservation)
	 { 
	  
	   String xmlHeader = "";
	   String xmlFooter = "";
	  if (isObservation)	
	  {
	      xmlHeader="<observation>";
		  xmlFooter="</observation>";
		  
	  }
	  else
	  {
		  xmlHeader="<warning>";
		  xmlFooter="</warning>";
	  } 	 
	  	return xmlHeader + msg + xmlFooter;
	 	
	 }
	 
	/* 
	private Set generateCombination(Set netElements, int size) {

      Set subSets = new HashSet();
      Object[] elements = netElements.toArray();
      int[] indices;
      CombinationGenerator x = new CombinationGenerator(elements.length, size);
      while (x.hasMore()) {
        Set combsubSet = new HashSet();
        indices = x.getNext();
        for (int i = 0; i < indices.length; i++) {
          combsubSet.add(elements[indices[i]]);
        }
        subSets.add(combsubSet);
      }
      return subSets;
    }*/ 
	 
	  /**
    * This transformation involves adding 2 tasks and 2 conditions
    * to E2WFNet to check proper completion. This enables us to
    * ask just one coverable question.
    * limitation is we don't know where the dead token is .
    */
   
/*   private YNet transformNetforProperCompletionCheck(YNet net) {
   		
   	YNet yNet = (YNet) net.clone();
    YOutputCondition output = yNet.getOutputCondition();
       
   	//Create two join tasks - t_XOR and t_AND - default split is AND
   	YAtomicTask t_xor = new YAtomicTask("t_xor",YTask._XOR,YTask._AND,yNet);
   	YAtomicTask t_and = new YAtomicTask("t_and",YTask._AND,YTask._AND,yNet);
   	
   	//Add flow relations for preset of t_XOR
   	 YExternalNetElement XORtask = t_xor;
   	 YExternalNetElement ANDtask = t_and;
   	 
   	 yNet.addNetElement(XORtask);
   	 yNet.addNetElement(ANDtask);
   	 
   	 //Connect all conditions except input and output to t_xor
   	 Map netElements = yNet.getNetElements();
     for (Iterator i= netElements.values().iterator(); i.hasNext();)
   	 {  YExternalNetElement nextElement = (YExternalNetElement) i.next();
   	 
   	 //only interested in conditions
	   	 if(nextElement instanceof YCondition)
	     { if (!(nextElement instanceof YInputCondition)||!(nextElement instanceof YOutputCondition))
	       { 
	         YFlow preflow = new YFlow(nextElement,XORtask);
	         XORtask.setPreset(preflow);
	       }
	          
	     }
     } 
        
   	//Create two conditions c_bt and c_output
   	
   	YCondition cbt= new YCondition("c_bt","c_bt",yNet);
   	YCondition coutput= new YCondition("c_output","c_output",yNet);
   	
    YExternalNetElement c_bt = cbt;
   	YExternalNetElement c_output = coutput;
   	
   	yNet.addNetElement(c_bt);
   	yNet.addNetElement(c_output);
   	
   	//Add flow relations post for t_XOR to c_bt
   	YFlow cbtPreflow = new YFlow(XORtask,c_bt);
   	XORtask.setPostset(cbtPreflow);
  
   	
   	//Add flow relations preset from c_bt to t_AND
   	YFlow cbtPostflow = new YFlow(c_bt,ANDtask);
  	ANDtask.setPreset(cbtPostflow);
   	
   	//Add flow relations from c_out to t_AND
   	YFlow cOutflow = new YFlow(c_output,ANDtask);
   	ANDtask.setPreset(cOutflow);
   	
   	//Change flow relations for preset of output condition
    //Need to replace output condition with c_output
    if(output != null)
    {  Set outPresetElements = output.getPresetElements();
      for(Iterator i= outPresetElements.iterator(); i.hasNext();)
       { YExternalNetElement ele = (YExternalNetElement) i.next();
       
       	 YFlow f = ele.getPostsetFlow(output);
       	 ele.removePostsetFlow(f);
       	 f = new YFlow(ele,c_output);
       	 c_output.setPreset(f);
       	// ele.setPostset(f);
       
       
       }
     
    }
   	    	
   	// Create a new output condition
   	YOutputCondition newOutput = new YOutputCondition(output.getID(),output.getID(),yNet);
   	//Add flow relations postset from t_AND to output condition
   	YFlow oflow = new YFlow(ANDtask,newOutput);
   	ANDtask.setPostset(oflow);
      	
    return yNet;
     
 }*/   	  	
}