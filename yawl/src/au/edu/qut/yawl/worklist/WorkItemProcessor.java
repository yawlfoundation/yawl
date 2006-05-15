/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.InterfaceD;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.forms.SchemaCreator;
import au.edu.qut.yawl.worklist.model.*;
import org.jdom.JDOMException;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Handles the interaction between the YAWL worklist and 3rd party applications
 * using Interface D.
 * @author Guy Redding 26/11/2004
 *
 */
public class WorkItemProcessor {

    private static boolean debug = false;


    /**
     * Empty Constructor
     */
    public WorkItemProcessor() {
    }


    /**
     * Performs the tasks required by YAWL to display a dynamic xform to launch a case.
     * These include:
     * creating the schema and instance files for a workitem and session, posting the
     * schema and instance to the form-generating and processing engine, cleanup of
     * temporary files, posting a request to the form engine to display the xform.
     * (need a file context for creating a temporary file).
     * @param context
     * @param caseID
     * @param sessionHandle
     * @param _worklistController
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     * @throws IOException
     * @throws JDOMException
     * @throws URISyntaxException
     */
    public static void executeCasePost(ServletContext context, String caseID,
                                       String sessionHandle, WorklistController _worklistController)
            throws YSchemaBuildingException, YSyntaxException, IOException,
            JDOMException, URISyntaxException {

        String yawlServlet = new String(context.getInitParameter("YAWLXForms") + "/YAWLServlet");
        String xfServlet = new String(context.getInitParameter("YAWLXForms") + "/XFServlet");
        File directory = (File) context.getAttribute("javax.servlet.context.tempdir");

        // create schema and instance files to send to Chiba - not part of InterfaceD
        SchemaCreator.createCaseSchema(caseID, sessionHandle, _worklistController, directory);

        // send schema and instance files to Chiba - include post in InterfaceD
        String schemadata = getFile(SchemaCreator.getSchemaPath());

        Map parameters = Collections.synchronizedMap(new TreeMap());
        parameters.put("schema", SchemaCreator.getSchemaName());

        // post schema using InterfaceD
        InterfaceD id = new InterfaceD();
        id.connect(yawlServlet);
        id.postData(schemadata, parameters); // send schema file

        // post instance using InterfaceD
        String instancedata = getFile(SchemaCreator.getInstancePath());

        parameters.clear();
        parameters.put("instance", SchemaCreator.getInstanceName());

        id.connect(yawlServlet);
        id.postData(instancedata, parameters); // send instance file

        // post command using InterfaceD
        SpecificationData specData = _worklistController.getSpecificationData(caseID, sessionHandle);
        List inputParams = specData.getInputParams();

        StringBuffer input = new StringBuffer();

        for (int j = 0; j < inputParams.size(); j++) {
            YParameter inputParam = (YParameter) inputParams.get(j);

            if (inputParam.getElementName() != null) { // work item input param
                input.append(inputParam.getElementName() + ",");
            }
        }

        parameters.clear();
        parameters.put("inputParams", input.toString());

        if (debug) System.out.println("inputParams: " + input.toString());

        parameters.put("schema", SchemaCreator.getSchemaName());
        parameters.put("instance", SchemaCreator.getInstanceName());
        parameters.put("form", SchemaCreator.getFormName());
        parameters.put("root", SchemaCreator.getRootTagName());

        id.connect(xfServlet);
        id.postCommand(parameters);

        SchemaCreator.setSpecID(null); // reset specID (must be null for next time)
    }


    /**
     * Creates the URL to redirect to so that cases can be launched using a form.
     * @param context
     * @param specID
     * @param sessionHandle
     * @param sessionID
     * @param userID
     * @return the redirect URL
     */
    public static String getCaseRedirectURL(ServletContext context, String specID, String sessionHandle,
                                            String sessionID, String userID) {

        String url = new String((String) context.getInitParameter("YAWLXForms") +
                "/XFormsServlet?form=/forms/" + SchemaCreator.getFormName() +
                "&css=/styles/yawl.css&xslt=html4yawl.xsl&specID=" + specID +
                "&sessionHandle=" + sessionHandle + "&JSESSIONID=" + sessionID +
                "&userid=" + userID);

        if (debug) {
            System.out.println("getRedirectURL: " + url);
        }

        return url;
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
     * @throws URISyntaxException
     */
    public static void executeWorkItemPost(ServletContext context,
                                           String workItemID, String sessionHandle,
                                           WorklistController _worklistController)
            throws YSchemaBuildingException, YSyntaxException, IOException,
            JDOMException, URISyntaxException {

        String yawlServlet = new String(context.getInitParameter("YAWLXForms") + "/YAWLServlet");
        String xfServlet = new String(context.getInitParameter("YAWLXForms") + "/XFServlet");
        File directory = (File) context.getAttribute("javax.servlet.context.tempdir");

        SchemaCreator.createSchema(workItemID, sessionHandle, _worklistController, directory);

        // send schema and instance files to Chiba
        String schemadata = getFile(SchemaCreator.getSchemaPath());

        Map parameters = Collections.synchronizedMap(new TreeMap());
        parameters.put("schema", SchemaCreator.getSchemaName());

        InterfaceD id = new InterfaceD();
        id.connect(yawlServlet);
        id.postData(schemadata, parameters);

        String instancedata = getFile(SchemaCreator.getInstancePath());

        parameters.clear();
        parameters.put("instance", SchemaCreator.getInstanceName());

        id.connect(yawlServlet);
        id.postData(instancedata, parameters);

// retrieve list of input params to send to YAWLXForms that will display them
// as read-only fields.
        WorkItemRecord item = _worklistController.getRemotelyCachedWorkItem(workItemID);

        TaskInformation taskInfo = _worklistController.getTaskInformation(
                item.getSpecificationID(),
                item.getTaskID(),
                sessionHandle);

        YParametersSchema paramsSignature = taskInfo.getParamSchema();

        List inputParams = paramsSignature.getInputParams();

        StringBuffer input = new StringBuffer();

        for (int j = 0; j < inputParams.size(); j++) {
            YParameter inputParam = (YParameter) inputParams.get(j);

            if (inputParam.getElementName() != null) { // work item input param
                input.append(inputParam.getElementName() + ",");
            }
        }

        parameters.clear();
        parameters.put("inputParams", input.toString());
        parameters.put("schema", SchemaCreator.getSchemaName());
        parameters.put("instance", SchemaCreator.getInstanceName());
        parameters.put("form", SchemaCreator.getFormName());
        parameters.put("root", SchemaCreator.getRootTagName());

// send post to schema2xforms
        id.connect(xfServlet);
        id.postCommand(parameters);

        SchemaCreator.setWorkItemID(null); // reset workItemID (must be null for next time)
    }


    /**
     * Creates the URL to redirect to so that work items can be edited using a form.
     * @param context the ServletContext.
     * @param workItemID
     * @param sessionHandle
     * @param sessionID
     * @param userID
     * @return the redirect URL
     */
    public static String getWorkItemRedirectURL(ServletContext context, String workItemID, String sessionHandle,
                                                String sessionID, String userID) {

        String url = new String((String) context.getInitParameter("YAWLXForms") +
                "/XFormsServlet?form=/forms/" + SchemaCreator.getFormName() +
                "&css=/styles/yawl.css&xslt=html4yawl.xsl&workItemID=" + workItemID +
                "&sessionHandle=" + sessionHandle + "&JSESSIONID=" + sessionID +
                "&userid=" + userID);

        if (debug) {
            System.out.println("getRedirectURL: " + url);
        }

        return url;
    }


    /**
     * Returns the contents of the given fileName as a string.
     * @param fileName
     * @throws IOException
     */
    private static String getFile(String fileName)
            throws IOException {

// get the file to post
        StringBuffer sb = new StringBuffer();
        String temp = new String();
        BufferedReader file = new BufferedReader(new FileReader(fileName));
// file will reside temporarily in the "Tomcat/bin" directory if running Catalina,
// resides in "Windows/System" directory if not in Catalina mode
        File f = new File(fileName);

        temp = file.readLine();

        while (temp != null) {
            sb.append(temp);
            temp = file.readLine();
        }

// clean-up (delete) file, it is no longer required at this point
        if (f.exists() == true) {
            if (debug) {
                System.out.println("File exists: " + f.getName());
            }

            if (f.delete() == false) {
                if (debug) {
                    System.out.println("File NOT DELETED: " + f.getName());
                }
            }
        }

        return sb.toString();
    }
}