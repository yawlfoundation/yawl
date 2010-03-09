/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;


/**
 * An abstract class designed to be implemented by YAWL custom services. It provides
 * access to, and receives event notifications from, the Engine via Interface B.
 *
 * @author Lachlan Aldred
 * Date: 19/03/2004
 * Time: 11:44:49
 *
 * @author Michael Adams (refactored and enhanced for v2.0 2008-09)
 */

public abstract class InterfaceBWebsideController {
    protected InterfaceB_EnvironmentBasedClient _interfaceBClient;
    protected IBControllerCache _model;
    private AuthenticationConfig _authConfig4WS;
    protected String _report;

    protected static final String XSD_STRINGTYPE = "string";
    protected static final String XSD_ANYURI_TYPE = "anyURI";
    protected static final String XSD_NCNAME_TYPE = "NCName";
    protected static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    protected static final String DEFAULT_ENGINE_USERNAME = "admin";
    protected static final String DEFAULT_ENGINE_PASSWORD = "YAWL";
    protected Category _logger = Logger.getLogger(getClass());
    protected SAXBuilder _builder = new SAXBuilder();


    /**
     * Constructs a controller.
     */
    public InterfaceBWebsideController() {
        _model = new IBControllerCache();
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
     * Receives notification from the engine of an enabled workitem. Typically, a
     * custom service will implement this method to check out the work item, and
     * process it as appropriate for the service.
     * @param enabledWorkItem the enabled work item
     */
    public abstract void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem);


    /**
     * Receives notification from the engine that an active workitem has been
     * cancelled. A custom service will implement this method to take any cleanup
     * action on a previously checked out workitem.
     * 
     * @param workItemRecord the cancelled work item
     */
    public abstract void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord);


    /**
     * Receives notification from the engine that an active case has been
     * cancelled.  Override this method to take any necessary action.
     * @param caseID the id of the case that has been cancelled
     */
    public void handleCancelledCaseEvent(String caseID) { }


    /**
     * Receives notification from the engine that an active case has been
     * completed. By overriding this method a service can process case completion
     * events as required.
     * @param caseID the id of the completed case.
     * @param casedata the set of net-level data for the case when it completes
     */
    public void handleCompleteCaseEvent(String caseID, String casedata) { }


    /**
     * Receives notification from the engine that an active workitem's timer has
     * expired. Override this method to handle timer expiries on timed workitems.
     * @param expiredWorkItem the workitem that has an expired timer
     */
    public void handleTimerExpiryEvent(WorkItemRecord expiredWorkItem) { }


    /**
     * Receives notification from the engine that it has finished startup
     * initialisation and is now in a running state. Override this method to
     * handle final service initialisation tasks that require a running engine
     */
    public void handleEngineInitialisationCompletedEvent() { }


    /**
     * Receives notification from the engine that the status of a workitem has been
     * modified. Override this method to handle any effects a status change might have
     * on a custom service
     */
    public void handleWorkItemStatusChangeEvent(WorkItemRecord workItem, 
                                                String oldStatus, String newStatus) { }


    /**
     * Override this method if you wish to allow other tools to find out what
     * input parameters are required for your custom YAWL service to work.
     * @return an array of input parameters.
     */
    public YParameter[] describeRequiredParams() {
        return new YParameter[0];
    }


    /**
     * If a custom service is installed outside of a firewall then you will need to
     * set this method once.  Doing so will allow the WSIF code to access
     * Web services outside your organisation's firewall (http authenticating
     * proxy).
     *
     * NOTE:  If your custom YAWL service doesn't need to negotiate a
     * a firewall this method can be ignored
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
     * Override this method to provide a welcome screen for your Custom YAWL Service
     * For instance you could redirect to a JSP or just write typical
     * servlet doGet() code inside your subclass.
     * @param request the request
     * @param response the response.
     * @throws IOException if an error is detected when the servlet handles the GET request
     * @throws ServletException if the request for the GET could not be handled
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();

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
     * @return true if the session handle is valid and active; else false.
     * @throws IOException if a connection with the engine cannot be established.
     */
    public boolean checkConnection(String sessionHandle) throws IOException {
        String msg = _interfaceBClient.checkConnection(sessionHandle);
        return _interfaceBClient.successful(msg);
    }


    /**
     * Creates a session with the engine.  A session is automatically disconnected
     * after 60 minutes of inactivty (although this value can be changed via config
     * value in the web.xml file).
     * @param userID the userID
     * @param password the password.
     * @return a sessionhandle string
     * @throws IOException if a connection with the engine cannot be established.
     */
    public String connect(String userID, String password) throws IOException {
        return _interfaceBClient.connect(userID, password);
    }


    /**
     * Checks a work item out of the engine. Also stores a local copy of the active item.
     * @param workItemID the work item id.
     * @param sessionHandle a valid session handle
     * @return the resultant checked-out workitem.
     * @throws IOException if a connection with the engine cannot be established.
     * @throws YAWLException if the checkout was unsuccessful 
     */
    public WorkItemRecord checkOut(String workItemID, String sessionHandle)
            throws IOException, YAWLException {
        WorkItemRecord resultItem = null;
        String msg = _interfaceBClient.checkOutWorkItem(workItemID, sessionHandle);
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
     * @throws JDOMException if there is a problem parsing XML of input or output data
     * @deprecated replaced by checkInWorkItem(String workItemID, Element inputData, Element outputData, String sessionHandle)
     */
    public String checkInWorkItem(String workItemID, String inputData, String outputData, String sessionHandle) throws IOException, JDOMException {
        Element inputDataEl = null;
        Element outputDataEl = null;

        SAXBuilder builder = new SAXBuilder();
        try {
            Document inputDataDoc =
                    builder.build(
                            new StringReader(inputData)
                    );
            inputDataEl = inputDataDoc.getRootElement();

            Document outputDataDoc =
                    builder.build(
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
     * @deprecated since 2.1 - use checkInWorkItem(String, Element, Element, String, String)
     * Checks a work item into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data as an XML String.
     * @param outputData the output data as an XML String
     * @param sessionHandle a valid session handle
     * @return a diagnostic result of the action - in XML.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing XML of input or output data
     */
    public String checkInWorkItem(String workItemID, Element inputData,
                                  Element outputData, String sessionHandle)
            throws IOException, JDOMException {
        return checkInWorkItem(workItemID, inputData, outputData, null, sessionHandle);
    }

    /**
     * Checks a work item into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data as an XML String.
     * @param outputData the output data as an XML String
     * @param logPredicate a configurable logging string to be logged when the item completes
     * @param sessionHandle a valid session handle
     * @return a diagnostic result of the action - in XML.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing XML of input or output data
     */
    public String checkInWorkItem(String workItemID, Element inputData,
                                  Element outputData, String logPredicate, String sessionHandle)
            throws IOException, JDOMException {

        // first merge the input and output data together
        String mergedlOutputData = Marshaller.getMergedOutputData(inputData, outputData);
        String filteredOutputData;

        WorkItemRecord workitem = this.getCachedWorkItem(workItemID);
        YSpecificationID specID = new YSpecificationID(workitem.getSpecIdentifier(),
                                                       workitem.getSpecVersion(),
                                                       workitem.getSpecURI());
        SpecificationData specData = getSpecificationData(specID, sessionHandle);

        // Now if this is beta4 or greater then remove all those input only bits of data
        // by first preparing a list of output params to iterate over.
        if (!(specData.usesSimpleRootData())) {
            TaskInformation taskInfo = getTaskInformation(specID, workitem.getTaskID(),
                                                          sessionHandle);
            List<YParameter> outputParams = taskInfo.getParamSchema().getOutputParams();
            filteredOutputData = Marshaller.filterDataAgainstOutputParams(
                                                    mergedlOutputData, outputParams);
        }
        else {
            filteredOutputData = mergedlOutputData;
        }
        String result = _interfaceBClient.checkInWorkItem(workItemID, filteredOutputData,
                                                          logPredicate, sessionHandle);
        _model.removeRemotelyCachedWorkItem(workItemID);
        return result;
    }


    /**
     * Gets a locally stored copy of a work item.
     * @param workItemID the id of the workitem to retrieve
     * @return a local (to the custom service) cached copy of the workitem.
     */
    public WorkItemRecord getCachedWorkItem(String workItemID) {
        return _model.getWorkItem(workItemID);
    }


    /**
     * Gets a copy of a workitem form the engine's cache
     * @param workItemID the id of the workitem to retrieve
     * @param sessionHandle a valid sesion handle
     * @return a remote (to the custom service) cached copy of the workitem.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing the XML result
     */
    public WorkItemRecord getEngineStoredWorkItem(String workItemID, String sessionHandle)
            throws JDOMException, IOException {
        List<WorkItemRecord> items = _interfaceBClient.getCompleteListOfLiveWorkItems(sessionHandle);
        for (WorkItemRecord record : items) {
            if (record.getID().equals(workItemID)) {
                return record;
            }
        }
        return null;
    }


    /**
     * Gets metadata of a task.
     * @deprecated superseded by getTaskInformation(YSpecificationID, String, String)
     * @param specID the specification id
     * @param taskID the task id
     * @param sessionHandle a valid session handle
     * @return a task metadata description
     * @throws IOException if there is a problem contacting the engine.
     */
    public TaskInformation getTaskInformation(String specID, String taskID,
                                              String sessionHandle) throws IOException {
       return getTaskInformation(new YSpecificationID(specID), taskID, sessionHandle);
    }


    /**
     * Gets metadata of a task.
     * @param specID the specification id
     * @param taskID the task id
     * @param sessionHandle a valid session handle
     * @return a task metadata description
     * @throws IOException if there is a problem contacting the engine.
     */
    public TaskInformation getTaskInformation(YSpecificationID specID, String taskID,
                                              String sessionHandle) throws IOException {
        TaskInformation taskInfo = _model.getTaskInformation(specID, taskID);
        if (taskInfo == null) {
            String taskInfoASXML = _interfaceBClient.getTaskInformationStr(
                    specID, taskID, sessionHandle);
            taskInfo = _interfaceBClient.parseTaskInformation(taskInfoASXML);
            _model.setTaskInformation(specID, taskID, taskInfo);
        }
        return taskInfo;
    }


    /**
     * Gets a reference to the local cache
     * @return a reference to the cache
     */
    public IBControllerCache getModel() {
        return _model;
    }


    /**
     * Gets a reference to the authentication configuration for the local service (if any)
     * @return the authentication object for this instantiation
     */

    public AuthenticationConfig getAuthenticationConfig() {
        return _authConfig4WS;
    }


    /**
     * Retrieve a list of all the child work items for a given parent work item
     * @param workItemID the id of the parent workitem
     * @param sessionHandle a valid session handle
     * @return the list of child workitems
     * @throws IOException if there is a problem contacting the engine.
     */
    public List<WorkItemRecord> getChildren(String workItemID, String sessionHandle)
            throws IOException {
        return _interfaceBClient.getChildrenOfWorkItem(workItemID, sessionHandle);
    }


    /**
     * Logs the failure of a client to contact the YAWL engine.
     * gives some suggestion of why?
     * @param e the thrown exception
     * @param backEndURIStr the uri of the engine
     */
    public static void logContactError(IOException e, String backEndURIStr) {
        Logger.getLogger(InterfaceBWebsideController.class).error(
                "[error] problem contacting YAWL engine at URI [" + backEndURIStr + "]");
        if (e.getStackTrace() != null) {
            Logger.getLogger(InterfaceBWebsideController.class).error("line of code := " +
                    e.getStackTrace()[0].toString());
        }
    }


    /**
     * Checks an interface return message for success .
     * @param input the return message
     * @return true if the message indicates success; false if otherwise
     */
    public boolean successful(String input) {
        return _interfaceBClient.successful(input);
    }


    /**
     * Gets a list of all specifications currently loaded in the engine
     * @param sessionHandle a valid session handle
     * @return the full list of specifications
     * @throws IOException if there is a problem contacting the engine.
     */
    public List<SpecificationData> getSpecificationPrototypesList(String sessionHandle)
            throws IOException {
        return _interfaceBClient.getSpecificationList(sessionHandle);
    }


    /**
     * Gets the specification data object with the spec id.  If called it will return the
     * specdata object and the entire XML representation of the specification.
     * @deprecated superseded by getSpecificationData(YSpecificationID, String)
     * @param specID the specification id
     * @param sessionHandle a valid session handle
     * @return a specification data object
     * @throws IOException if there is a problem contacting the engine.
     */
    public SpecificationData getSpecificationData(String specID, String sessionHandle)
            throws IOException {
        return getSpecificationData(new YSpecificationID(specID), sessionHandle);
    }


    /**
     * Gets the specification data object with the spec id.  If called it will return the
     * specdata object including the entire XML representation of the specification.
     * @param specID the specification id
     * @param sessionHandle a valid session handle
     * @return a specification data object
     * @throws IOException if there is a problem contacting the engine.
     */
    public SpecificationData getSpecificationData(YSpecificationID specID, String sessionHandle)
            throws IOException {
        List<SpecificationData> specs = _interfaceBClient.getSpecificationList(sessionHandle);
        for (SpecificationData data : specs) {
            if (data.getID().getKey().equals(specID.getKey())) {
                String specAsXML = data.getAsXML();
                if (specAsXML == null) {
                    specAsXML = _interfaceBClient.getSpecification(specID, sessionHandle);
                    data.setSpecAsXML(specAsXML);
                    _model.setSpecificationData(data);
                }
                return data;
            }
        }
        return null;
    }


    /**
     * Utility method for implementers to use for helping to check all instances
     * of a given task out of the engine.
     * @param enabledWorkItem an enabled WorkItemRecord.
     * @param sessionHandle a valid session handle with the engine.
     * @return a list of work item records that correspond to the executing work-items
     * that should be checked back into the engine when the task is complete.
     * @throws IOException if there is a problem communicating with the engine.
     * @throws YAWLException if the checkout is unsuccessful
     */
    protected List<WorkItemRecord> checkOutAllInstancesOfThisTask(
            WorkItemRecord enabledWorkItem, String sessionHandle)
            throws IOException, YAWLException {
        if (null == enabledWorkItem) 
            throw new IllegalArgumentException("Param enabledWorkItem cannot be null.");

        if (!enabledWorkItem.getStatus().equals(WorkItemRecord.statusEnabled))
            throw new IllegalArgumentException("Param enabledWorkItem must be enabled.");

        // first of all checkout an enabled work item
        WorkItemRecord result = checkOut(enabledWorkItem.getID(), sessionHandle);

        _logger.debug("Result of item [" + enabledWorkItem.getID() +
                "] checkout is : " + result);

        // if the work item has any children
        List<WorkItemRecord> mixedChildren = getChildren(enabledWorkItem.getID(), sessionHandle);
        for (WorkItemRecord itemRecord : mixedChildren) {
            if (WorkItemRecord.statusFired.equals(itemRecord.getStatus())) {
                _logger.debug("Result of item [" +
                        itemRecord.getID() + "] checkout is : " +
                        checkOut(itemRecord.getID(), sessionHandle));
            }
        }
        return getChildren(enabledWorkItem.getID(), sessionHandle);
    }


    /**
     * Utility method to prepare the reply element for checking a work item back into
     * the engine.
     * @param enabledWorkItem the enabled work item.
     * @param sessionHandle the session handle
     * @return an empty root element correctly named according to the version
     * of the process specification.
     * @throws IOException if there is a problem communicating with the engine.
     */
    protected Element prepareReplyRootElement(WorkItemRecord enabledWorkItem, String sessionHandle)
            throws IOException {
        Element replyToEngineRootDataElement;
        YSpecificationID specID = new YSpecificationID(enabledWorkItem.getSpecIdentifier(),
                                                       enabledWorkItem.getSpecVersion(),
                                                       enabledWorkItem.getSpecURI());

        //prepare reply root element.
        SpecificationData sdata = getSpecificationData(specID, sessionHandle);

        TaskInformation taskInfo = getTaskInformation(specID,
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


    /**
     * Retrieve the resourcing specifications for a given task
     * @param specID the specification id
     * @param taskID the task id
     * @param sessionHandle a valid sessionhandle
     * @return the task's resourcing specification, or null if it doesn't have one (pre v2.0)
     * @throws IOException if there is a problem communicating with the engine.
     */
    protected Element getResourcingSpecs(YSpecificationID specID, String taskID,
                                         String sessionHandle) throws IOException {
        String result = _interfaceBClient.getResourcingSpecs(specID, taskID, sessionHandle) ;
        if ((result != null) && (! result.equals("")) && (! result.equals("null")))
            return JDOMUtil.stringToElement(result);
        else
            return null ;
    }
}
