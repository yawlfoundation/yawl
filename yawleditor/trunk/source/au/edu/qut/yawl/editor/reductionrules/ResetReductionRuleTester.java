package au.edu.qut.yawl.editor.reductionrules;


import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.exceptions.*;
import au.edu.qut.yawl.editor.analyser.ResetWFNet;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import org.jdom.JDOMException;

public class ResetReductionRuleTester{
	

 private static final int FSPR = 1;
 private static final int FPPR = 2;
 private static final int FSTR = 3;
 private static final int FPTR = 4;
 private static final int ELTR = 5;
 private static final int DEAR = 6;
 private static final int FESR = 7;
 
 
    /**
     * This method is used for the analysis.
     * @fileURL - xml file format of the YAWL model
     *  
     */
     public String test(String fileURL,int ruleType) throws IOException,YSchemaBuildingException, YSyntaxException, JDOMException, NumberFormatException {
    	
       YSpecification specs = (YSpecification) YMarshal.unmarshalSpecifications(fileURL).get(0);
       String msg = "";
       boolean isReducible = false;
       YDecomposition decomposition;
	   YNet decomRootNet;
	   ResetWFNet decomResetNet;
	   
	   Set decompositions = new HashSet(specs.getDecompositions());
	   if (decompositions.size()>0)
	   {
	        for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
	            decomposition = (YDecomposition) iterator.next();
		        YSpecification decomSpecs = decomposition.getSpecification();
		        if (decomposition instanceof YNet) {
			        decomRootNet = (YNet) decomposition;
			        
			        ResetReductionRule rule = null;
			        
			        
			        switch(ruleType){
			        
			        case FSPR:{
			          rule = new FSPRrule();
			          break;
			        }
	                case FPPR:
			        { rule = new FPPRrule();
			          break;
			        }
			        case FSTR:
			        { rule = new FSTRrule();
			          break;
			        }
			       case FPTR:
			        { rule = new FPTRrule();
			          break;
			        }
			       case ELTR:
			        { rule = new ELTRrule();
			          break;
			        }
			      case DEAR:
			        { rule = new DEARrule();
			          break;
			        }
			      case FESR:
			        { rule = new DEARrule();
			          break;
			        }
	                }
	                
	                if (rule != null)
	               { //Using clone here so that reduction rules do not affect original net.
	               
	                 ResetWFNet originalNet = new ResetWFNet(decomRootNet);
	                 	 msg += "Rule name:"+ convertName(ruleType)+
                	  " Original net:"+ originalNet.getNetElements().size(); 
                	  
                	 ResetWFNet reducedNet = rule.reduce(originalNet);     		                       
                	 if (reducedNet == null)
		             { msg += " Nothing to reduce for "+ originalNet.getID();
		              
		             }
	                 else
	                 { msg += " Reduced net:"+ reducedNet.getNetElements().size();
	                  isReducible = true;        
	               //   newSpecification.setDecomposition((YDecomposition) reducedNet);       
	                 }
	               }//end if - null
	          }//endfor
	 		
	 	/*	if (isReducible)
	 		{
	 			String ruleName = convertName(ruleType);
	 			String  fileName = fileURL.substring(0,fileURL.indexOf(".")) +"_"+
	 		                  		ruleName +"_Reduced"
	 		                  		+ ".xml";
	 			String exportFileName = fileName;
	 			exportEngineSpecificationToFile(exportFileName,newSpecification);
	 	// This one does not work as it does not accept the filename
	 	//	openNewEditorInstance(exportFileName);	
	 	
	 	   }
	 	*/   	
        }
       }
      return msg;         
    }
    
    
    
 private String convertName(int ruleType){
   String ruleName = "";
   switch(ruleType){
   

    case FSPR:
	  { ruleName = "FSPRrule";
		break;
      }
	case FPPR:
	  { ruleName = "FPPRrule";
		break;
      }
    case FSTR:
	  { ruleName = "FSTRrule";
		break;
      }
    case FPTR:
	  { ruleName = "FPTRrule";
		break;
      }
    case DEAR:
	  { ruleName = "DEARrule";
		break;
      } 
    case FESR:
	  { ruleName = "FESRrule";
		break;
      }         
 	}
 	return ruleName;
 	
 }
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
  
  private void openNewEditorInstance(String exportFileName)
  {
  	String commandArray = "java -jar YAWLEditor.jar"; 
    commandArray += " " + exportFileName; 
    
     try {
      Process resultProcess = Runtime.getRuntime().exec(commandArray);

     }
     catch (Exception e)
     {
     	e.printStackTrace();
     	System.out.println("Unexpected Error while opening editor."+commandArray);
     }
  	
  } 	  	   	
 public static void main(String[] args) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException, NumberFormatException {

	 if (args.length < 2)
	 { System.out.println("Please enter a yawl file and reduction rule");
	 }
	   int ruleType = Integer.parseInt(args[1]);
	   ResetReductionRuleTester tester = new ResetReductionRuleTester();
	   String output = tester.test(args[0],ruleType);
	   System.out.println(output);
	 }
     
       
   
  }	 
	
	 
