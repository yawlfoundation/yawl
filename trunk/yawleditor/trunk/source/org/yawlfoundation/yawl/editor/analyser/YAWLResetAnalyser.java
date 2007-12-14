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

package org.yawlfoundation.yawl.editor.analyser;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.exceptions.*;

import java.io.IOException;
import java.util.*;
import org.jdom.JDOMException;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.yawlfoundation.yawl.editor.reductionrules.*;

public class YAWLResetAnalyser{

  public static final String RESET_NET_ANALYSIS_PREFERENCE = "resetNetAnalysisCheck";
  public static final String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";
  public static final String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";
  public static final String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";
  public static final String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";
  public static final String SHOW_OBSERVATIONS_PREFERENCE = "resetShowObservationsCheck";
  public static final String USE_YAWLREDUCTIONRULES_PREFERENCE	= "yawlReductionRules";
  public static final String USE_RESETREDUCTIONRULES_PREFERENCE	= "resetReductionRules";
      /**
     * This method is used for the analysis.
     * @fileURL - xml file format of the YAWL model
     * @options - four options (weak soundness w, soundness s, cancellation set c, orjoins o) 
     * @useYAWLReductionRules - reduce the net using YAWL reduction rules
     * @useResetReductionRules - reduce the net using Reset reduction rules
     * returns a xml formatted string with warnings and observations.
     */
     public String analyse(String fileURL,String options,boolean useYAWLReductionRules,boolean useResetReductionRules) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException {

       YSpecification specs = (YSpecification) YMarshal.unmarshalSpecifications(fileURL).get(0);
       StringBuffer msgBuffer = new StringBuffer(200);

    //   boolean useYAWLReductionRules = true;
    //   boolean useResetReductionRules = false;
       
       // TODO: Check whether reset net analysis is needed at all.

     //  while (listIt.hasNext()){

       //Check if there is decomposition
	   
	   YDecomposition decomposition;
	   YNet decomRootNet,reducedYNet;
	   ResetWFNet decomResetNet, reducedNet;
	   
	   YSpecification newSpecification = specs;
	   	   
	   Set decompositions = new HashSet(specs.getDecompositions());
	   if (decompositions.size()>0)
	   {
	        for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
	            decomposition = (YDecomposition) iterator.next();
		        if (decomposition instanceof YNet) {

		        decomRootNet = (YNet) decomposition;
		       //System.out.println(decomRootNet.getID());
		        //reduction rules
		        if (useYAWLReductionRules)
		        { reducedYNet = reduceNet(decomRootNet);
			        if (reducedYNet != null)
			        {
			        	decomRootNet = reducedYNet;
			            //  newSpecification.setDecomposition((YDecomposition) reducedYNet);     
			        }
		        }
		        		        
		        decomResetNet = new ResetWFNet(decomRootNet);
	            
		        YAWLReachabilityUtils utils = new YAWLReachabilityUtils(decomRootNet);
		        
		        if (useResetReductionRules)
		        {
		        	reducedNet = reduceNet(decomResetNet);
			        if (reducedNet != null)
			        {
			        	decomResetNet = reducedNet;
			        }
		        }
		        if (options.indexOf("w") >= 0)
		        {   
		            msgBuffer.append(decomResetNet.checkWeakSoundness());
		        }
		   	
		   	    //checking rechability set for bounded nets
		   	    if (options.indexOf("s") >= 0)
		        {  try{
		           	if (decomResetNet.containsORjoins())
		           	{
		           	  msgBuffer.append(utils.checkSoundness());
		           	}
		           	else
		           	{
		           	 msgBuffer.append(decomResetNet.checkSoundness());
		           	}

		           }
		           catch (Exception e)
		           {
		            msgBuffer.append(formatXMLMessage(e.toString(),false));

		           }
		          
		   	   }

		   	   if (options.indexOf("c") >= 0)
		   	   {
		   	      try{
		           	if (decomResetNet.containsORjoins())
		           	{

		           	  msgBuffer.append(utils.checkCancellationSets());
		           	}
		           	else
		           	{
		           	 msgBuffer.append(decomResetNet.checkCancellationSets());
		           	}

		           }
		           catch(ClassCastException ex)
		           {
		           	 
		           	 System.out.println(ex.getStackTrace().toString()+ex.getMessage());
		           }
		           catch (Exception e)
		           {
		            msgBuffer.append(formatXMLMessage(e.toString(),false));

		           }
		           	
		   	   }

		   	   //do unnecessary orjoin checks
		   	  if (options.indexOf("o") >= 0)
		   	  {
		   	     try{
		           	if (decomResetNet.containsORjoins())
		           	{

		           	  msgBuffer.append(utils.checkUnnecessaryORJoins());
		           	}
		           	else
		           	{
		           	 msgBuffer.append(formatXMLMessage("There are no OR-joins in the net "+decomRootNet.getID()+".",true));
		           	}

		           }
		           
		           catch (Exception e)
		           {
		            msgBuffer.append(formatXMLMessage(e.toString(),false));

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

    // }//end while
    
  // String exportFileName = fileURL+"reduced"; 
  //  exportEngineSpecificationToFile(exportFileName,newSpecification);
	return formatXMLMessageForEditor(msgBuffer.toString());

    }
    
    private YNet reduceNet(YNet originalNet){
//	System.out.println(" Original net:"+ originalNet.getNetElements().size());
	YAWLReductionRule rule;

    YNet reducedNet_t, reducedNet;
    reducedNet = originalNet;
    int loop = 0;
    String rules = "FSPY"; 
   	String rulesmsg = "";                       
   	                     
   do
    { loop++;
      rule	= new FSPYrule();
      reducedNet_t = rule.reduce(reducedNet);
      if (reducedNet_t == null)
      { rules = "FSTY";
        rule = new FSTYrule();
        reducedNet_t = rule.reduce(reducedNet);
     
        if (reducedNet_t == null) 
        { rules = "FPPY";
          rule = new FPPYrule();
          reducedNet_t = rule.reduce(reducedNet);
   
        if (reducedNet_t == null)
        {rules = "FPTY";
         rule = new FPTYrule();
      	 reducedNet_t = rule.reduce(reducedNet);
      	 
    	
    	if (reducedNet_t == null) 
        {rules = "FAPY";
	      rule = new FAPYrule();
	      reducedNet_t = rule.reduce(reducedNet);
       
       if (reducedNet_t == null)
        { rules = "FATY";
          rule = new FATYrule();
      	  reducedNet_t = rule.reduce(reducedNet);
       
       if (reducedNet_t == null) 
        {rules = "ELTY";
         rule = new ELTYrule();
      	 reducedNet_t = rule.reduce(reducedNet);
        
        if (reducedNet_t == null)
        {  rules = "FXOR";
	      rule = new FXORrule();
	      reducedNet_t = rule.reduce(reducedNet);
      	
      	if (reducedNet_t == null) 
        {rules = "FAND";
	      rule = new FANDrule();
	      reducedNet_t = rule.reduce(reducedNet);
	   
  	   if (reducedNet_t == null) 
        { rules = "FOR";   
		  rule = new FIErule();
	      reducedNet_t = rule.reduce(reducedNet);
	      
     if (reducedNet_t == null) 
        { rules = "FIE";   
		  rule = new FORrule();
	      reducedNet_t = rule.reduce(reducedNet);
     	}
     }
      }
     }
    }
   }
  }
 }
}
}// 10 endif
 if (reducedNet_t == null)
 { //if (reducedNet != originalNet)
   //{  
        loop --;
   //     System.out.println("YAWL Reduced net "+ loop + "rules "+ rulesmsg+ " size:"+ reducedNet.getNetElements().size());
        return reducedNet;
   //}
   //else 
   //return null; 
 }  
 else{
 rulesmsg += rules;
 reducedNet = reducedNet_t;                                                     
 
}
} while (reducedNet != null);//end while
   return null;
                     
 } 
    
 private ResetWFNet reduceNet(ResetWFNet originalNet){
// System.out.println(" Original net:"+ originalNet.getNetElements().size());
 ResetReductionRule rule;
 
 ResetWFNet reducedNet_t, reducedNet;
// reducedNet = originalNet;
//a copy of original net
   reducedNet = new ResetWFNet(originalNet);
   String rules = "FSPR"; 
   String rulesmsg = "";                       
   int loop=0;
   do
    { loop++;
      rule	= new FSPRrule();
      reducedNet_t = rule.reduce(reducedNet);
      
    if (reducedNet_t == null)
      { rules = "FSTR";
        rule = new FSTRrule();
        reducedNet_t = rule.reduce(reducedNet);
    
        if (reducedNet_t == null) 
        { rules = "FPPR";
          rule = new FPPRrule();
          reducedNet_t = rule.reduce(reducedNet);
   
        if (reducedNet_t == null)
        { rules = "FPTR";
         rule = new FPTRrule();
      	 reducedNet_t = rule.reduce(reducedNet);
    
        if (reducedNet_t == null)
        { rules = "DEAR";
	      rule = new DEARrule();
	      reducedNet_t = rule.reduce(reducedNet);
        
        if (reducedNet_t == null) 
        { rules = "ELTR";
         rule = new ELTRrule();
      	 reducedNet_t = rule.reduce(reducedNet);
      	 
     	if (reducedNet_t == null) 
        { rules = "FESR";
         rule = new FESRrule();
      	 reducedNet_t = rule.reduce(reducedNet);
     	}
    
       }
      }
     }
    }
    
 }//5 endif
 if (reducedNet_t == null)
 { if (reducedNet != originalNet)
   { loop --;
   //  System.out.println("Reset Reduced net "+ loop + "rules "+ rulesmsg+ " size:"+ reducedNet.getNetElements().size());
     return reducedNet;
   }
   else 
   {return null; 
   }
 }    
 else
 { rulesmsg += rules;
  reducedNet = reducedNet_t;                                                     
 }
} while (reducedNet != null);//end while
return null;
                     
 } 
    
              	  
    public String formatXMLMessageForEditor(String msg)
	 {
	 StringBuffer msgBuffer = new StringBuffer(200);
	  msgBuffer.append("<wofyawl><net><behavior>");
	  msgBuffer.append(msg);
	  msgBuffer.append("</behavior></net></wofyawl>");
	 
	  return msgBuffer.toString();
	 	
	 }
	 
	/**
	 * used for formatting xml messages.
	 * Message could be a warning or observation. 
	 */ 
    private String formatXMLMessage(String msg,boolean isObservation)
	 { 	  
	   StringBuffer msgBuffer = new StringBuffer(200);
	  if (isObservation)	
	  {
	      msgBuffer.append("<observation>");
	      msgBuffer.append(msg);
	      msgBuffer.append("</observation>");
		  
	  }
	  else
	  {
		  msgBuffer.append("<warning>");
	      msgBuffer.append(msg);
	      msgBuffer.append("</warning>");
	  } 	 
	   
	   
	  	return msgBuffer.toString();	
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
 
 private void exportEngineSpecificationToFile(String fullFileName, YSpecification specification) {
    try {
      PrintStream outputStream = 
        new PrintStream(
            new BufferedOutputStream(new FileOutputStream(fullFileName)),
            false,
            "UTF-8"
        );
      outputStream.println(getEngineSpecificationXML(specification));
       
      outputStream.close();
      System.out.println("file succesfully exported to:"+fullFileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }	
    private String getEngineSpecificationXML(YSpecification specification) {
    try {
       return YMarshal.marshal(specification);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}