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

import org.jdom.JDOMException;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.reductionrules.*;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;

import javax.swing.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class YAWLResetAnalyser{

  public static final String RESET_NET_ANALYSIS_PREFERENCE = "resetNetAnalysisCheck";
  public static final String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";
  public static final String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";
  public static final String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";
  public static final String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";
  public static final String SHOW_OBSERVATIONS_PREFERENCE = "resetShowObservationsCheck";
  public static final String USE_YAWLREDUCTIONRULES_PREFERENCE	= "yawlReductionRules";
  public static final String USE_RESETREDUCTIONRULES_PREFERENCE	= "resetReductionRules";
  public static final String ORJOINCYCLE_ANALYSIS_PREFERENCE = "resetOrjoinCycleCheck";

  public static AnalysisDialog messageDlg ;
      /**
     * This method is used for the analysis.
     * @fileURL - xml file format of the YAWL model
     * @options - five options (weak soundness w, soundness s, cancellation set c, orjoins o, vicious circles v)
     * @useYAWLReductionRules - reduce the net using YAWL reduction rules
     * @useResetReductionRules - reduce the net using Reset reduction rules
     * returns a xml formatted string with warnings and observations.
     */
     public String analyse(String fileURL,String options,boolean useYAWLReductionRules,boolean useResetReductionRules) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException {
       long startTime = System.currentTimeMillis();
       YSpecification specs;
       StringBuffer msgBuffer = new StringBuffer(400);
       try {
          specs = YMarshal.unmarshalSpecifications(fileURL).get(0);
       }
       catch (YSyntaxException yse) {
           String msg = yse.getMessage().trim();
           msg = msg.substring(0, msg.indexOf(":")) + ".";
           JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
               msg + "\nAnalysis cannot proceed until these issues are fixed.\n" +
               "Please validate the specification for more detailed information.",
               "Error validating specification",
               JOptionPane.ERROR_MESSAGE);
           return "";
       }

       // TODO: Check whether reset net analysis is needed at all.

       //Check if there is decomposition
	   YDecomposition decomposition;
	   YNet decomRootNet,reducedYNet;
	   ResetWFNet decomResetNet, reducedNet;
     messageDlg = new AnalysisDialog("Specification");
     messageDlg.setTitle("Analyse Specification");
	   	   
	   Set decompositions = new HashSet(specs.getDecompositions());
	   if (decompositions.size()>0)
	   {
	        for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
	            decomposition = (YDecomposition) iterator.next();
		        if (decomposition instanceof YNet) {

		        decomRootNet = (YNet) decomposition;
		       //System.out.println(decomRootNet.getID());

                //Added for immutable OR-join message - check for OR-joins before reduction
		        boolean OriginalYNetContainsORJoins = containsORjoins(decomRootNet);

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
	          decomResetNet.setAnalysisDialog(messageDlg);

		        YAWLReachabilityUtils utils = new YAWLReachabilityUtils(decomRootNet);
		        utils.setAnalysisDialog(messageDlg);

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

                                 //It is possible that YAWL reduction rules were applied first, thus removing OR-joins.
                                 if (useYAWLReductionRules && OriginalYNetContainsORJoins)
                                 {
                                  msgBuffer.append(formatXMLMessage("There are no OR-joins in the reduced net of "+ decomRootNet.getID()+". Please recheck this property without using the YAWL reduction rules.",true));
                                 }
		           	 else
                                 {
		           	 msgBuffer.append(formatXMLMessage("There are no OR-joins in the net "+decomRootNet.getID()+".",true));
		           	}
		           	}

		           }
		           
		           catch (Exception e)
		           {
		            msgBuffer.append(formatXMLMessage(e.toString(),false));

		           }
		           
		   	  }
		   	 //do orjoins in a cycle check (vicious circle)
		   	  if (options.indexOf("v") >= 0)
		   	  {
		   	     try{
			   	   	OrjoinInCycleUtils Orutils = new OrjoinInCycleUtils();
			   	    Object[] results = Orutils.checkORjoinsInCycle(decomRootNet);
		            msgBuffer.append((String)results[1]);
		           	}

		           catch (Exception e)
		           {
		            msgBuffer.append(formatXMLMessage(e.toString(),false));

		           }

		   	  }
	         } //endif
	   		} //end for
	   }//end if


	long endTime = System.currentTimeMillis();
	long duration = endTime - startTime;
	messageDlg.write("Duration: " + duration + " millisecs");
  messageDlg.finished();        
	return formatXMLMessageForEditor(msgBuffer.toString());
    }
    

    //Use to check for OR-joins in YNet
    private boolean containsORjoins(YNet net)
    {
        List tasks = net.getNetTasks();
        for (Iterator t = tasks.iterator(); t.hasNext();)
        { 	 YTask task = (YTask) t.next();
             if (task.getJoinType() == YTask._OR)
             {
            	 return true;
             }
        }
        return false;

    }

    private YNet reduceNet(YNet originalNet){
	messageDlg.write("# Elements in the original YAWL net (" + originalNet.getID() + "): "
                   + originalNet.getNetElements().size());
	YAWLReductionRule rule;

    YNet reducedNet_t, reducedNet;
    reducedNet = originalNet;
    int loop = 0;
    String rules = "FSPY"; 
   	String rulesmsg = "";                       
   	                     
   	//Added to ensure that FOR rule is only applied to OR-joins without cycles
   	OrjoinInCycleUtils utils = new OrjoinInCycleUtils();
    Object[] results = utils.checkORjoinsInCycle(originalNet);
  	Boolean containsORjoinCycles = (Boolean) results[0];

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
        {rules = "ELPY ";
         rule = new ELPYrule();
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
        { rules = "FIE";
		  rule = new FIErule();
	      reducedNet_t = rule.reduce(reducedNet);
	      
	 //this rule should only be applied to nets without OR-joins in a cycle.
     if (reducedNet_t == null && !containsORjoinCycles)
        { rules = "FOR";
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
}
}// 11 endif
 if (reducedNet_t == null)
 { //if (reducedNet != originalNet)
   //{  
        loop --;
        messageDlg.write("YAWL Reduction rules: "+ loop + " rules "+ rulesmsg);
        messageDlg.write("Reduced net size: "+ reducedNet.getNetElements().size());
        return reducedNet;
   //}
   //else 
   //return null; 
 }  
 else{
 rulesmsg += rules + ";";
 reducedNet = reducedNet_t;                                                     
 
}
} while (reducedNet != null);//end while
   return null;
                     
 } 
    
 private ResetWFNet reduceNet(ResetWFNet originalNet){
 messageDlg.write("# Elements in the original reset net: "+ originalNet.getNetElements().size());
 ResetReductionRule rule;
 
 ResetWFNet reducedNet_t, reducedNet;
// reducedNet = originalNet;
//a copy of original net
   reducedNet = new ResetWFNet(originalNet);
   reducedNet.setAnalysisDialog(messageDlg);
     
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
     messageDlg.write("Reset Reduced net "+ loop + " rules: "+ rulesmsg);
     messageDlg.write("Reduced net size:"+ reducedNet.getNetElements().size());
     return reducedNet;
   }
   else 
   {return null; 
   }
 }    
 else
 { rulesmsg += rules + ";";
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

}