package au.edu.qut.yawl.editor.reductionrules;


import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import org.jdom.JDOMException;

public class ReductionRuleTester{
	
 private static final int FOR = 1;
 private static final int FIE = 2;
 private static final int FSPY = 3;
 private static final int FPPY = 4;
 private static final int FAPY = 5;
 private static final int FSTY = 6;
 private static final int FPTY = 7;
 private static final int FATY = 8;
 private static final int ELTY = 9;
 private static final int FAND = 10;
 private static final int FXOR = 11;
 
 
    /**
     * This method is used for the analysis.
     * @fileURL - xml file format of the YAWL model
     *  
     */
     public String test(String fileURL,int ruleType) throws IOException,YSchemaBuildingException, YSyntaxException, JDOMException, NumberFormatException {
    	
       List specifications = null;
       try {
         YMarshal.unmarshalSpecifications(fileURL);
       } catch (YPersistenceException ype) {
         ype.printStackTrace();
         return null;
       }
       
       ListIterator listIt = specifications.listIterator();
       String msg = "";
       boolean isReducible = false;
      
       while (listIt.hasNext()){
       
       YSpecification specs = (YSpecification) listIt.next();
       YSpecification newSpecification = specs;
	   //Check if there is decomposition
	   Set decompositions = new HashSet(specs.getDecompositions());
	   if (decompositions.size()>0)
	   { 
	        for (Iterator iterator = decompositions.iterator(); iterator.hasNext();) {
	            YDecomposition decomposition = (YDecomposition) iterator.next();
		        YSpecification decomSpecs = decomposition.getParent();
		        if (decomposition instanceof YNet) {
			        YNet decomRootNet = (YNet) decomposition;
			        
			        YAWLReductionRule rule = null;
			        
			        
			        switch(ruleType){
			        
			        case FOR:{
			          rule = new FORrule();
			          break;
			        }
	                case FIE:
			        { rule = new FIErule();
			          break;
			        }
			        case FSPY:{
			          rule = new FSPYrule();
			          break;
			        }
	                case FPPY:
			        { rule = new FPPYrule();
			          break;
			        }
			        case FAPY:
			        { rule = new FAPYrule();
			          break;
			        }
			        case FSTY:
			        { rule = new FSTYrule();
			          break;
			        }
			        case FPTY:
			        { rule = new FPTYrule();
			          break;
			        }
			        case FATY:
			        { rule = new FATYrule();
			          break;
			        }
			        case ELTY:
			        { rule = new ELTYrule();
			          break;
			        }
			        case FAND:
			        { rule = new FANDrule();
			          break;
			        }
			        case FXOR:
			        { rule = new FXORrule();
			          break;
			        }
	                }
	                
	                if (rule != null)
	               { //Using clone here so that reduction rules do not affect original net.
	                 YNet clone = (YNet) decomRootNet.clone();
	                 YNet reducedNet = rule.reduce(decomRootNet); 
                	 msg += "Rule name:"+ convertName(ruleType);   	        		                       
                	 if (reducedNet == null)
		             { msg += " Nothing to reduce for "+ decomRootNet.getId();
		              
		             }
	                 else
	                 { msg += " Original net:"+clone.getNetElements().size()+
	                         " Reduced net:"+reducedNet.getNetElements().size();
	                  isReducible = true;        
	                  newSpecification.setDecomposition((YDecomposition) reducedNet);       
	                 }
	               }//end if - null
	          }//endfor
	 		
	 		if (isReducible)
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
        }
       }
      }
      return msg;         
    }
    
    
    
 private String convertName(int ruleType){
   String ruleName = "";
   switch(ruleType){
   
	case FOR:
	  { ruleName = "FORrule";
		break;
      }
	case FIE:
	  { ruleName = "FIErule";
		break;
      }  
    case FSPY:
	  { ruleName = "FSPYrule";
		break;
      }
	case FPPY:
	  { ruleName = "FPPYrule";
		break;
      }
    case FAPY:
	  { ruleName = "FAPYrule";
		break;
      }   
    case FSTY:
	  { ruleName = "FSTYrule";
		break;
      }
    case FPTY:
	  { ruleName = "FPTYrule";
		break;
      }
    case FATY:
	  { ruleName = "FATYrule";
		break;
      } 
    case ELTY:
	  { ruleName = "ELTYrule";
		break;
      }   
    case FAND:
	  { ruleName = "FANDrule";
		break;
      } 
    case FXOR:
	  { ruleName = "FXORrule";
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
	   ReductionRuleTester tester = new ReductionRuleTester();
	   String output = tester.test(args[0],ruleType);
	   System.out.println(output);
	 }
     
       
   
  }	 
	
	 
