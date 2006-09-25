/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.exceptions.YAWLException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;


/**
 * This class is an API for accessing the YAWL engine webapp.  It uses by default XML-over-HTTP
 * as a transport mechanism.
 * @author Lachlan Aldred
 * Date: 19/03/2004
 * Time: 11:44:49
 */
public abstract class InterfaceBWebsideController {
    protected InterfaceB_EnvironmentBasedClient _interfaceBClient;
    protected WorklistModel _model;
    private AuthenticationConfig _authConfig4WS;

    protected static final String XSD_STRINGTYPE = "string";
    protected static final String XSD_ANYURI_TYPE = "anyURI";
    protected static final String XSD_NCNAME_TYPE = "NCName";
    protected static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    protected static final String DEFAULT_ENGINE_USERNAME = "admin";
    protected static final String DEFAULT_ENGINE_PASSWORD = "YAWL";
    protected Category _logger = Logger.getLogger(getClass());
    protected SAXBuilder _builder = new SAXBuilder();

    /*
      May be a better way of doing this
      check with lachlan
     */
    protected String username = "";
    public String getUsername() {
	return username;
    }	

    /**
     * Constructs a controller.
     */
    public InterfaceBWebsideController() {
        _model = new WorklistModel();
        _authConfig4WS = AuthenticationConfig.getInstance();
    }

    /**
     * Provided that you set up the web.xml file according to the YAWL doumentation
     * you do not need to call this method.  It allows implementors to set up
     * their webapp so that it has the right objects, and correctly addressed
     * pointers to the engine.
     * @param backEndURI the uri of the engine.
     */
    public void setUpInterfaceBClient(String backEndURI) {
        _interfaceBClient = new InterfaceB_EnvironmentBasedClient(backEndURI);
    }


    /**
     * It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invokation.
     * @param enabledWorkItem
     */
    public abstract void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem);

    /**
     * By implementing this method and deploying a web app containing the implementation
     * the YAWL engine will send events to this method notifying your custom
     * YAWL service that an active work item has been cancelled by the
     * engine.
     * @param workItemRecord a "snapshot" of the work item cancelled in
     * the engine.
     */
    public abstract void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord);


    /**
     * By overriding this method one can process case completion events.
     * @param caseID the id of the completed case.
     */
    public void handleCompleteCaseEvent(String caseID, String casedata) {

    }

    /**
     * Override this method if you wish to allow other tools to find out what
     * input parameters are required for your custom YAWL service to work.
     * @return an array of input parameters.
     */
    public YParameter[] describeRequiredParams() {
        return new YParameter[0];
    }


    /**
     * If you are going outside of a firewall then you will need to
     * set this method once.  Doing so will allow the WSIF code to access
     * Web services outside your organisation's firewall (http authenticating
     * proxy).
     *
     * NOTE:  If your custom YAWL service doesn't need to negotiate a
     * a firewall merely provide an empty implementation of this method.
     *
     * @param userName mandatory
     * @param password mandatory
     * @param httpProxyHost optional (if using a non authenticating proxy firewall).
     * @param proxyPort optional (if using a non authenticating proxy firewall).
     */
    public void setRemoteAuthenticationDetails(String userName, String password,
                                               String httpProxyHost, String proxyPort) {
        _authConfig4WS.setProxyAuthentication(
                userName, password, httpProxyHost, proxyPort);
    }


    /**
     * Your opportunity to provide a welcome screen for your Custom YAWL Service
     * when you override this method.
     * For instance you could redirect to a JSP or just write typical
     * servlet doGet() code inside your subclass.
     * @param request the request
     * @param response the response.
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();

        output.append(
                "<html><head><title>Your Custom YAWL Service Welcome Page</title>" +
                "</head><body>" +
                "<H3>Please create a welcome page for your custom YAWL Service</H3>" +
                "<p>One option is to just override the method " +
                "\"doGet()\" of  InterfaceBWebsideController, when you extend this " +
                "class with your own code.</p>" +
                "<p>Alternatively you could redirect to a JSP of your design.</p>" +
                "</body>" +
                "</html>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();


    }


    /**
     * Checks if the sessionhandle with the engine is currently active.
     * @param sessionHandle the session handle
     * @return true if the session handle is OK; else false.
     * @throws IOException if the engine cannot be connected with.
     */
    public boolean checkConnection(String sessionHandle) throws IOException {
        String msg = _interfaceBClient.checkConnection(sessionHandle);
        return Interface_Client.successful(msg);
    }


    /**
     * Creates a session with the engine.  These typically last for ONE HOUR.
     * @param userID the userID
     * @param password the password.
     * @return a sessionhandle string
     * @throws IOException if the engine cannot be connected with.
     */
    public String connect(String userID, String password) throws IOException {
	this.username = userID;
        return _interfaceBClient.connect(userID, password);
    }


    /**
     * Checks a work item out of the engine.  Also stores a local copy of the active item.
     * @param workItemID the work item id.
     * @param sessionHandle the session handle
     * @return the resultant checked-out workitem.
     * @throws IOException if the engine cannot be connected with.
     */
    public WorkItemRecord checkOut(String workItemID, String sessionHandle) throws IOException, YAWLException {
        WorkItemRecord resultItem = null;
        
        if (!checkConnection(sessionHandle)) {
        	sessionHandle = connect(DEFAULT_ENGINE_USERNAME,DEFAULT_ENGINE_PASSWORD);
        }
        
        String msg = _interfaceBClient.checkOutWorkItem(workItemID, sessionHandle);
        _logger.debug("Response message: " + msg);
        if (successful(msg)) {
            try {
                Document doc = _builder.build(new StringReader(msg));
                Element workItemElem = doc.getRootElement().getChild("workItem");
                resultItem = Marshaller.unmarshalWorkItem(workItemElem);
                _model.addWorkItem(resultItem);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new YAWLException(msg);
        }
        return resultItem;
    }


    /**
     * Checks a work item back into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data.
     * @param outputData the output data.
     * @param sessionHandle the session handle.
     * @return a diagnostic result of the action - in XML.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing XML of input data or output data
     * @deprecated replaced by checkInWorkItem(String workItemID, Element inputData, Element outputData, String sessionHandle)
     */
    public String checkInWorkItem(String workItemID, String inputData, String outputData, String sessionHandle) throws IOException, JDOMException {
        Element inputDataEl = null;
        Element outputDataEl = null;

	if (!checkConnection(sessionHandle)) {
	    sessionHandle = connect(DEFAULT_ENGINE_USERNAME,DEFAULT_ENGINE_PASSWORD);
	}


        try {
            Document inputDataDoc =
                    _builder.build(
                            new StringReader(inputData)
                    );
            inputDataEl = inputDataDoc.getRootElement();

            Document outputDataDoc =
                    _builder.build(
                            new StringReader(outputData)
                    );
            outputDataEl = outputDataDoc.getRootElement();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return checkInWorkItem(workItemID, inputDataEl, outputDataEl, sessionHandle);
    }


    /**
     * Checks a work item into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data as an XML String.
     * @param outputData the output data as an XML String
     * @param sessionHandle
     * @return a diagnostic result of the action - in XML.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing XML of input data or output data
     */
    public String checkInWorkItem(String workItemID, Element inputData, Element outputData, String sessionHandle) throws IOException, JDOMException {
        //first merge the input and output data together
        String mergedlOutputData = Marshaller.getMergedOutputData(inputData, outputData);

        if (!checkConnection(sessionHandle)) {
        	sessionHandle = connect(DEFAULT_ENGINE_USERNAME,DEFAULT_ENGINE_PASSWORD);
        }


        //Now if this is beta4 or greater then remove all those input only bits of data
        //by first preparing a list of output params to iterate over.
        
        /*
         * This method call should not use get cached workitem since
         * the service may have crashed and been restored. The 
         * cache would then be empty.
         * */
        WorkItemRecord workitem = null;
        try {
        	workitem = getWorkItem(workItemID, sessionHandle);
        } catch (IOException e) {
        	throw e;
        } catch (JDOMException e) {
        	throw e;
        }
        

        SpecificationData specData = getSpecificationData(
                workitem.getSpecificationID(), sessionHandle);

        String filteredOutputData;

        if (!(specData.usesSimpleRootData())) {
            TaskInformation taskInfo = getTaskInformation(
                    workitem.getSpecificationID(),
                    workitem.getTaskID(), sessionHandle);
            List outputParams = taskInfo.getParamSchema().getOutputParams();
	    System.out.println(mergedlOutputData);
            filteredOutputData = Marshaller.filterDataAgainstOutputParams(
                    mergedlOutputData, outputParams);
        } else {
            filteredOutputData = mergedlOutputData;
        }
        System.out.println( "filtered output data:" + filteredOutputData);

        String result = _interfaceBClient.checkInWorkItem(workItemID, filteredOutputData, sessionHandle);
        _model.removeRemotelyCachedWorkItem(workItemID);
        return result;
        
    }

    
    public WorkItemRecord getWorkItem(String workItemID,String sessionHandle) throws IOException, JDOMException {
    	WorkItemRecord record = getCachedWorkItem(workItemID);
    	if (record==null) {
    		record = getEngineStoredWorkItem(workItemID, sessionHandle);
    		_model.addWorkItem(record);
        }
    	return record;
    }
    /**
     * Gets a locally stored copy of a work item.
     * @param workItemID
     * @return a local (to the custom service) cached copy of the workitem.
     */
    public WorkItemRecord getCachedWorkItem(String workItemID) {
        return _model.getWorkItem(workItemID);
    }

    public WorkItemRecord getEngineStoredWorkItem(String workItemID, String sessionHandle) throws JDOMException, IOException {
        List items = _interfaceBClient.getCompleteListOfLiveWorkItems(sessionHandle);
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            WorkItemRecord record = (WorkItemRecord) iterator.next();
            if (record.getID().equals(workItemID)) {
                return record;
            }
        }
        return null;
    }


    public TaskInformation getTaskInformation(String specificationID, String taskID, String sessionHandle) throws IOException {
        TaskInformation taskInfo = _model.getTaskInformation(specificationID, taskID);
        if (taskInfo == null) {
            String taskInfoASXML = _interfaceBClient.getTaskInformationStr(
                    specificationID, taskID, sessionHandle);
	    System.out.println( "task info as XML:" + taskInfoASXML);
	    try {
            taskInfo = InterfaceB_EnvironmentBasedClient.parseTaskInformation(taskInfoASXML);
            _model.setTaskInformation(specificationID, taskID, taskInfo);
	    }
	    catch( Exception e ) {
	    	//todo: Question to Nathan: i had a look down the stack for places
            //todo: where an exception can occur, and it seems that you have
            //todo: added lines of code to marshaller that can indeed throw a
            //todo: runtimeexception.  This is not good practice because runtime
            //todo: exceptions do not affect method signatures allowing for all
            //todo: sorts of programming errors to creep in.
            //todo: I hope you plan to remove this runtime exception throwing code.
            StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		System.out.println( sw.toString() );
    		throw new RuntimeException( e );
	    }
        }
        return taskInfo;
    }


    public WorklistModel getModel() {
        return _model;
    }


    public List getChildren(String workItemID, String sessionHandle) {
        return _interfaceBClient.getChildrenOfWorkItem(workItemID, sessionHandle);
    }


    public AuthenticationConfig getAuthenticationConfig() {
        return _authConfig4WS;
    }


    /**
     * logs the failure of client to contact the YAWL engine.
     * gives some suggestion of why?
     * @param e
     * @param backEndURIStr
     */
    public static void logContactError(IOException e, String backEndURIStr) {
        System.out.println("[error] problem contacting YAWL engine at URI [" +
                backEndURIStr + "]");
        if (e.getStackTrace() != null) {
            System.out.println("line of code := " +
                    e.getStackTrace()[0].toString());
        }
    }


    /**
     * checks to see if the inupt string doesn't contain a <failure>..</failure> message.
     * @param input
     */
    public boolean successful(String input) {
        return Interface_Client.successful(input);
    }


    public List getSpecificationPrototypesList(String sessionHandle)
            throws IOException {
        return _interfaceBClient.getSpecificationList(sessionHandle);
    }


    /**
     * Gets the specification data object with the spec id.  If called it will return the
     * specdata object and the entire XML representation of the specification.
     * @see au.edu.qut.yawl.worklist.model.SpecificationData
     * @param specID
     * @param sessionHandle
     * @return a Specification data object for spec id else null.
     * @throws java.io.IOException
     */
    public SpecificationData getSpecificationData(String specID, String sessionHandle) throws IOException {
//        if(_model.getSpecificationData(specID) == null){
            List specs = _interfaceBClient.getSpecificationList(sessionHandle);
            for (int i = 0; i < specs.size(); i++) {

                SpecificationData data = (SpecificationData) specs.get(i);

                if (data.getID().equals(specID)) {

                    String specAsXML = data.getAsXML();
                    if (specAsXML == null) {

                        specAsXML = _interfaceBClient.getSpecification(specID, sessionHandle);
                        data.setSpecAsXML(specAsXML);
                        _model.setSpecificationData(data);
                    }
                    return data;
                }
            }
//        } else {
//            return _model.getSpecificationData(specID);
//        }
        return null;
    }


    /**
     * Utility method for implementers to use for helping to check all instances
     * of a given task out of the engine.
     * @param enabledWorkItem an enabled WorkItemRecord.
     * @param sessionHandle the session handle with the engine.
     * @return a list of work item records that correspond to the executing work-items
     * that should be checked back into the engine when the task is complete.
     * @throws IOException if there is a problem communicating with the engine.
     */
    protected List checkOutAllInstancesOfThisTask(
            WorkItemRecord enabledWorkItem, String sessionHandle)
            throws IOException, YAWLException {
        if (null == enabledWorkItem) {
            throw new IllegalArgumentException("Param enabledWorkItem cannot be null.");
        }
        if (!enabledWorkItem.getStatus().equals(YWorkItem.Status.Enabled)) {
            throw new IllegalArgumentException("Param enabledWorkItem must be enabled.");
        }

        //first of all checkout an enabled work item
        WorkItemRecord result =
                checkOut(enabledWorkItem.getID(), sessionHandle);

        _logger.debug("Result of item [" + enabledWorkItem.getID() +
                "] checkout is : " + result);



        //if the work item has any children
        List mixedChildren = getChildren(enabledWorkItem.getID(), sessionHandle);
        for (int i = 0; i < mixedChildren.size(); i++) {
            WorkItemRecord itemRecord = (WorkItemRecord) mixedChildren.get(i);
            if (YWorkItem.Status.Fired.equals(itemRecord.getStatus())) {
                _logger.debug("Result of item [" +
                        itemRecord.getID() + "] checkout is : " +
                        checkOut(itemRecord.getID(), sessionHandle));
            }
        }
        List executingChildrenOfWorkItem;
        executingChildrenOfWorkItem = getChildren(
                enabledWorkItem.getID(),
                sessionHandle);

        return executingChildrenOfWorkItem;
    }


    /**
     * Utility method to prepare the reply element for checking work item back into
     * the engine.
     * @param enabledWorkItem the enabled work item.
     * @param sessionHandle the session handle
     * @return an empty root element correctly named according to the version
     * of the process specification.
     * @throws IOException if there is a problem communicating with the engine.
     */
    protected Element prepareReplyRootElement(WorkItemRecord enabledWorkItem, String sessionHandle) throws IOException {
        Element replyToEngineRootDataElement;

        //prepare reply root element.
        SpecificationData sdata = getSpecificationData(
                enabledWorkItem.getSpecificationID(),
                sessionHandle);

        System.out.println("_model = " + _model);
        System.out.println("enabledWorkItem = " + enabledWorkItem.toXML());

        TaskInformation taskInfo = getTaskInformation(
                enabledWorkItem.getSpecificationID(),
                enabledWorkItem.getTaskID(),
                sessionHandle);

        String decompID = taskInfo.getDecompositionID();
        if (sdata.usesSimpleRootData()) {
            replyToEngineRootDataElement = new Element("data");
        } else {
            replyToEngineRootDataElement = new Element(decompID);
        }
        return replyToEngineRootDataElement;
    }
}
