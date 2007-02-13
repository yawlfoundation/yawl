/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.forms.InstanceBuilder;
import au.edu.qut.yawl.forms.InterfaceD_XForm;
import au.edu.qut.yawl.schema.ElementCreationInstruction;
import au.edu.qut.yawl.schema.ElementReuseInstruction;
import au.edu.qut.yawl.schema.Instruction;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.WorklistController;
import au.edu.qut.yawl.worklist.model.YParametersSchema;


/**
 * Handles the interaction between the YAWL worklist and 3rd party applications
 * using Interface D.
 * @author Guy Redding 26/11/2004
 */
public class WorkItemProcessor {

	private static Logger logger = Logger.getLogger(WorkItemProcessor.class);
    
   
    /**
     * Empty Constructor
     */
    public WorkItemProcessor() {
    }


    /**
     * Performs the tasks required by YAWL to display a dynamic xform to launch a case.
     * These include:
     * creating the schema and instance for a workitem and session, posting the
     * schema and instance to the form-generating and processing engine, posting a 
     * request to the form engine to display the xform.
     * @param caseID
     * @param sessionHandle
     * @param _worklistController
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     * @throws IOException
     * @throws JDOMException
     * @throws URISyntaxException 
     */
	public void executeCasePost(ServletContext context, String caseID, 
    	String sessionHandle, WorklistController _worklistController, String userID,
    	String jsessionid)
            throws YSchemaBuildingException, YSyntaxException, IOException,
            JDOMException, URISyntaxException {

        Map parameters = Collections.synchronizedMap(new TreeMap());
    	InterfaceD_XForm idx = new InterfaceD_XForm(context.getInitParameter("YAWLXForms") + "/YAWLServlet");
    	
    	// set schema data
        String schema = createCaseSchema(caseID, sessionHandle, _worklistController);
        parameters.put("schema", schema);
        
        // set instance data
        SpecificationData specData = _worklistController.getSpecificationData(caseID, sessionHandle);
        InstanceBuilder ib = new InstanceBuilder(schema, specData.getRootNetID(), null);
        parameters.put("instance", ib.getInstance());
        
        // TODO need to query specData for the names of output params belonging to the case
        
        // set input parameter data (if any)
        //parameters.put("inputparams", getInputOutputParams(specData.getInputParams()));
        
        parameters.put("root", specData.getRootNetID());
        parameters.put("task", specData.getID());
        parameters.put("specID", specData.getID());
        parameters.put("JSESSIONID", jsessionid);
        
        // send (post) data to yawlXForms thru interfaceD
        idx.sendWorkItemData(parameters, userID, specData.getID(), sessionHandle);
    }

    
    /**
     * Creates a schema to launch a case.
     * @param _specID the specid.
     * @param _sessionHandle
     * @param _worklistController
     * @param context
     * @throws IOException
     * @throws JDOMException
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     * @throws URISyntaxException
     */
    public String createCaseSchema(String _specID, String _sessionHandle,
                                        WorklistController _worklistController)
            throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException, URISyntaxException {

        SpecificationData specData = _worklistController.getSpecificationData(_specID, _sessionHandle);
        List inputParams = specData.getInputParams();

        List instructions = Collections.synchronizedList(new ArrayList());

        for (int i = 0; i < inputParams.size(); i++) {
            YParameter inputParam = (YParameter) inputParams.get(i);

            if (null != inputParam.getElementName()) {

                String elementName = inputParam.getElementName();
                ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);
                instructions.add(instruction);
            } 
            else if (null != inputParam.getDataTypeName()) {

                String elementName = inputParam.getName();
                String typeName = inputParam.getDataTypeName();
                boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                ElementCreationInstruction instruction = new ElementCreationInstruction(
                        elementName,
                        typeName,
                        isPrimitiveType);
                instructions.add(instruction);
            }
            
            //if (inputParam.getElementName() != null) { // work item input param
            //    input.append(inputParam.getElementName()).append(",");
            //}
            else if (inputParam.isUntyped()) {
//            	 due to a bug, YAWL converts untyped parameters into creation parameters
                //UntypedElementInstruction instruction = new UntypedElementInstruction();
                //instructions.add(instruction);

                String elementName = inputParam.getName();
                //String typeName = inputParam.getDataTypeName();
                String typeName = "boolean";
                boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                ElementCreationInstruction instruction = new ElementCreationInstruction(
                        elementName,
                        typeName,
                        isPrimitiveType);
                instructions.add(instruction);
            }
            //else if (inputParam.getName() != null) {
            //    input.append(inputParam.getName()).append(",");
            //}
        }

        XMLToolsForYAWL xmlToolsForYawl = new XMLToolsForYAWL();
        String schemaLibrary = specData.getSchemaLibrary();
        xmlToolsForYawl.setPrimarySchema(schemaLibrary);
        String myNewSchema = xmlToolsForYawl.createYAWLSchema(
        	(Instruction[]) instructions.toArray(
        		new Instruction[instructions.size()])
        		, specData.getRootNetID());
        
       return (myNewSchema);
    }
    

    /**
     * Performs the tasks required by YAWL to display a dynamic xform to edit a work item.
     * These include:
     * creating the schema and instance files for a workitem and session, posting the
     * schema and instance to the form-generating and processing engine, cleanup of
     * temporary files, posting a request to the form engine to display the xform.
     * (need a file context for creating a temporary file).
     * @param context
     * @param workItemID
     * @param sessionHandle
     * @param _worklistController
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     * @throws IOException
     * @throws JDOMException
     */
    public void executeWorkItemPost(ServletContext context, String workItemID, 
    	String sessionHandle, WorklistController _worklistController, String userID,
    	String jsessionid)
            throws YSchemaBuildingException, YSyntaxException, IOException,
            JDOMException {

        Map parameters = Collections.synchronizedMap(new TreeMap());
        InterfaceD_XForm idx = new InterfaceD_XForm(context.getInitParameter("YAWLXForms") + "/YAWLServlet");
        
        // set schema data
        String schema = createSchema(workItemID, sessionHandle, _worklistController);
        parameters.put("schema", schema);
                
        // retrieve list of input params to send to YAWLXForms that will display them
        // as read-only fields.
        WorkItemRecord item = _worklistController.getCachedWorkItem(workItemID);
        TaskInformation taskInfo = _worklistController.getTaskInformation(
                item.getSpecificationID(), item.getTaskID(), sessionHandle);
        
        System.out.println("WIP item.dataliststring: "+item.getDataListString());
        
        // set instance data
        System.out.println("WIP item.schema: "+schema);

        
        InstanceBuilder ib = new InstanceBuilder(schema, taskInfo.getDecompositionID(), item.getDataListString());
        parameters.put("instance", ib.getInstance());
        
        // set input params (if any exist)
        YParametersSchema paramsSignature = taskInfo.getParamSchema();
        parameters.put("inputparams", getInputOnlyParams(paramsSignature.getInputParams(), paramsSignature.getOutputParams()));
        parameters.put("root", taskInfo.getDecompositionID());
        parameters.put("task", taskInfo.getTaskName());
        parameters.put("workItemID", item.getID());
        parameters.put("JSESSIONID", jsessionid);
        
        // send (post) data to yawlXForms thru interfaceD
        idx.sendWorkItemData(parameters, item, userID, sessionHandle);
    }


    /**
     * Creates the URL to redirect to so that cases can be started using a form.
     * @param context
     * @param specData
     * @return the redirect URL to the form to launch a case.
     */
    public String getRedirectURL(ServletContext context, SpecificationData specData,
    		String jsessionid) {
    	
        String url = context.getInitParameter("YAWLXForms") +
                "/XFormsServlet?form=/forms/" + this.getFormName(specData) +
                "&css=yawl.css&xslt=html4yawl.xsl&JSESSIONID="+jsessionid;

        return url;
    }
    
    
    /**
     * Creates the URL to redirect to so that tasks can be edited using a form.
     * @param context
     * @param taskInfo
     * @return the redirect URL to the form to edit a task.
     */
    public String getRedirectURL(ServletContext context, TaskInformation taskInfo,
    		String jsessionid){

      String url = context.getInitParameter("YAWLXForms") +
              "/XFormsServlet?form=/forms/" + this.getFormName(taskInfo) +
              "&css=yawl.css&xslt=html4yawl.xsl&JSESSIONID="+jsessionid;

      return url;
  }

    /**
     * Builds a schema from the XSD component found in YAWL that will be 
     * the foundation for building a form for editing work items.
     * @param _workItemID The work item to build a schema for.
     * @param _sessionHandle The session handle for the current YAWL session.
     * @param worklistController An instance of a work list controller.
     * @throws IOException
     * @throws JDOMException
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     */
    private String createSchema(String _workItemID, String _sessionHandle,
                                    WorklistController worklistController)
            throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException {
    	
    	// this method is a replacement for the SchemaCreator class,
    	// fixing a design error since that class was a Singleton.
    	String myNewSchema = new String();
        WorkItemRecord item = worklistController.getCachedWorkItem(_workItemID);

        if (item != null) {
            //first of all get the task information which contains the parameter signatures.
            TaskInformation taskInfo = worklistController.getTaskInformation(
                    item.getSpecificationID(), item.getTaskID(), _sessionHandle);

            String specID = taskInfo.getSpecificationID();

            //next get specification data which will contain the input schema library
            //that we are going to use to build the schema that we want for this task.
            SpecificationData specData = worklistController.getSpecificationData(specID, _sessionHandle);

            //now we get the parameters signature for the task
            YParametersSchema paramsSignature = taskInfo.getParamSchema();

            //now for each input param build an instruction
            List inputParams = paramsSignature.getInputParams();
            List instructions = Collections.synchronizedList(new ArrayList());

            // this is an XML string containing the instance data for the current task
            // this can be the instance file, and if identical elements are found in
            // the schema during instance creation, the creation of new elements in
            // the instance will be ignored.

            for (int i = 0; i < inputParams.size(); i++) {
                YParameter inputParam = (YParameter) inputParams.get(i);

                if (null != inputParam.getElementName()) {

                    String elementName = inputParam.getElementName();
                    ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);
                    instructions.add(instruction);
                } else if (null != inputParam.getDataTypeName()) {

                    String elementName = inputParam.getName();
                    String typeName = inputParam.getDataTypeName();
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName, typeName, isPrimitiveType);
                    instructions.add(instruction);
                }
                // currently we convert untyped parameters into creation parameters, due to a bug in YAWL
                else if (inputParam.isUntyped()) {
                    //UntypedElementInstruction instruction = new UntypedElementInstruction();
                    //instructions.add(instruction);

                    String elementName = inputParam.getName();
                    //String typeName = inputParam.getDataTypeName();
                    String typeName = "boolean";
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName, typeName, isPrimitiveType);
                    instructions.add(instruction);
                }
            }
            
            //for each output param build an instruction
            List outputParams = paramsSignature.getOutputParams();

            for (int i = 0; i < outputParams.size(); i++) {

                YParameter outputParam = (YParameter) outputParams.get(i);

                if (null != outputParam.getElementName()) {

                    String elementName = outputParam.getElementName();
                    ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter

                    if (instructions.contains(instruction) == true) {

                        // Matching element REUSE instruction found.
                        Instruction tempInstruction;
                        int position = instructions.indexOf(instruction);
                        tempInstruction = (Instruction) instructions.get(position);
                        if (tempInstruction.getElementName().compareTo(instruction.getElementName()) == 0) {
                            instructions.remove(position);
                        }
                    } 
                    else {
                    	logger.debug("No matching element REUSE instruction found: " + elementName);
                    }

                    instructions.add(instruction);
                } else if (null != outputParam.getDataTypeName()) {

                    String elementName = outputParam.getName();
                    String typeName = outputParam.getDataTypeName();
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(outputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName, typeName, isPrimitiveType);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter
                    Instruction[] ins = (Instruction[]) instructions.toArray(new Instruction[instructions.size()]);

                    boolean match = false;
                    for (int j = 0; j < ins.length; j++) {
                        if (ins[j].getElementName().compareTo(elementName) == 0) {
                            match = true;
                            ins[j] = instruction; // replace old instruction with this one
                        }
                    }

                    if (match == true) {
                        // convert updated array back to the instructions arraylist
                        instructions.clear();
                        for (int j = 0; j < ins.length; j++) {
                            instructions.add(ins[j]);
                        }
                    } 
                    else {
                        instructions.add(instruction);
                    }
                }
                
                // currently we convert untyped parameters into creation parameters, due to a bug in YAWL
                else if (outputParam.isUntyped()) {
                    //UntypedElementInstruction instruction = new UntypedElementInstruction();
                    //instructions.add(instruction);

                    String elementName = outputParam.getName();
                    //String typeName = outputParam.getDataTypeName();
                    String typeName = "boolean";
                    //boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(outputParam.getDataTypeNameSpace());
                    boolean isPrimitiveType = true;
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName, typeName, isPrimitiveType);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter
                    Instruction[] ins = (Instruction[]) instructions.toArray(new Instruction[instructions.size()]);
                    boolean match = false;

                    for (int j = 0; j < ins.length; j++) {
                    	logger.debug(j + ".");
                        if (ins[j].getElementName().compareTo(elementName) == 0) {
                            match = true;
                            ins[j] = instruction; // replace old instruction with this one
                        }
                    }

                    if (match == true) {
                        // convert updated array back to the instructions arraylist
                        instructions.clear();
                        for (int j = 0; j < ins.length; j++) {
                            instructions.add(ins[j]);
                        }
                    } 
                    else {
                        instructions.add(instruction);
                    }
                }
            }

            XMLToolsForYAWL xmlToolsForYawl = new XMLToolsForYAWL();
            String schemaLibrary = specData.getSchemaLibrary();
            xmlToolsForYawl.setPrimarySchema(schemaLibrary);
            myNewSchema = xmlToolsForYawl.createYAWLSchema(
                    (Instruction[]) instructions.toArray(new Instruction[instructions.size()]),
                    taskInfo.getDecompositionID());          
        }
        
        return(myNewSchema);
    }
    
    
    /**
     * Given a list of input and output parameters for a task, this method returns
     * the <i>input only</i> parameters by filtering out those parameters which are
     * found in both the input and output parameter lists. 
     * @param inputParams
     * @param outputParams
     * @return
     */
    private String getInputOnlyParams(List inputParams, List outputParams){
        
    	StringBuffer input = new StringBuffer();
    	boolean found = false;
    	
    	for (int i = 0; i < inputParams.size(); i++){
    		YParameter inputParam = (YParameter) inputParams.get(i);

	    	for (int j = 0; ((j < outputParams.size()) && (found == false)); j++) {
	            YParameter outputParam = (YParameter) outputParams.get(j);

	            // check if "input param element name" exists and is not also input/output
	            if (inputParam.getElementName() != null && outputParam.getElementName() != null){
		            if (outputParam.getElementName().compareTo(inputParam.getElementName()) == 0) {
		                found = true;
		            }
	            }
	            // check if "input param name" exists and is not also input/output
	            if (inputParam.getName() != null && outputParam.getName() != null){
	            	if (outputParam.getName().compareTo(inputParam.getName()) == 0) {
	            		found = true;
	            	}
	            }
	        }
	    	
	    	if (found == false){ 
	    		if (inputParam.getElementName() != null){
	    			input.append(inputParam.getElementName() + ",");
	    			logger.debug("Add inputonly param element name: "+inputParam.getElementName());
	    		}
	    		else if (found == false && (inputParam.getName() != null)){
		    		input.append(inputParam.getName() + ",");
		    		logger.debug("Add inputonly element name: "+inputParam.getName());
		    	}
	    	}
	    	found = false;
    	}
    	return input.toString();
    }
    
    
    /**
     * Returns the name of the form to edit a work item.
     * @param taskInfo
     * @return
     */
    private String getFormName(TaskInformation taskInfo){
        return (taskInfo.getTaskName()+".xhtml");
    }
    
    
    /**
     * Returns the name of the form to launch a case.
     * @param specData
     * @return
     */
    private String getFormName(SpecificationData specData){
        return (specData.getID()+".xhtml");
    }


    public String executePDFWorkItemPost(ServletContext context, String workItemID, String decompositionID,
    	String sessionHandle, WorklistController _worklistController, String userID)
            throws YSchemaBuildingException, YSyntaxException, IOException,
            JDOMException {

        WorkItemRecord item = _worklistController.getCachedWorkItem(workItemID);
        TaskInformation taskInfo = _worklistController.getTaskInformation(
                item.getSpecificationID(), item.getTaskID(), sessionHandle);

		HashMap map = new HashMap();
	
		logger.debug("workitem is: " + item.getDataListString());
	
		StringBuffer xmlBuff = new StringBuffer();
		xmlBuff.append("<workItem>");
		xmlBuff.append("<taskID>" + item.getTaskID() + "</taskID>");
		xmlBuff.append("<caseID>" + item.getCaseID() + "</caseID>");
		xmlBuff.append("<uniqueID>" + item.getUniqueID() + "</uniqueID>");
		xmlBuff.append("<specID>" + item.getSpecificationID() + "</specID>");
		xmlBuff.append("<status>" + item.getStatus() + "</status>");
		xmlBuff.append("<data>" + item.getDataListString() + "</data>");
		xmlBuff.append("<enablementTime>" + item.getEnablementTime() + "</enablementTime>");
		xmlBuff.append("<firingTime>" + item.getFiringTime() + "</firingTime>");
		xmlBuff.append("<startTime>" + item.getStartTime() + "</startTime>");
		xmlBuff.append("<assignedTo>" + item.getWhoStartedMe() + "</assignedTo>");
		xmlBuff.append("</workItem>");
			
		map.put("decompositionID",decompositionID);
		map.put("workitem",xmlBuff.toString());
		map.put("username",_worklistController.getUsername());
		Interface_Client.executePost("http://localhost:8080/worklist/handler",map); // TODO: remove localhost reference
		logger.debug("Calling the pdf handler");
		
		return item.getSpecificationID()+item.getTaskID()+item.getUniqueID()+".pdf";
    }

}