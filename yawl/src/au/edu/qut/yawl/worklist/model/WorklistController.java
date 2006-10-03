/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 18:56:33
 * 
 */
public class WorklistController extends InterfaceBWebsideController {
    private InterfaceA_EnvironmentBasedClient _interfaceAClient;

    private WorklistDBConnector _dbConnector;
    private boolean _orgModelConnectionOK;
    private static final String YAWL_AUTHENTICATION_QUERY = "YawlResourceAuthorisationQuery";
    private static final String YAWL_ALLOCATION_QUERY = "YawlResourceAllocationQuery";

    public WorklistController() {
        super();
        try {
            _dbConnector = WorklistDBConnector.getInstance();
            _orgModelConnectionOK = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(
                    "\n##################################################\n" +
                    "#\tFAILED TO CONNECT TO YAWL DB CONTAINING ORG MODEL.\n" +
                    "#\tWORKLIST CANNOT ASSIGN TASKS TO ROLES.\n" +
                    "##################################################\n");
            _orgModelConnectionOK = false;
        }
    }


    public List getAvailableWork(String username, String sessionHandle) throws JDOMException, IOException {
        List items = _interfaceBClient.getCompleteListOfLiveWorkItems(sessionHandle);
        List availableItems = new Vector();
        _model.updateWorkItems(items);
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            WorkItemRecord record = (WorkItemRecord) iterator.next();
            if (record.getStatus().equals(YWorkItem.Status.Enabled) ||
                    record.getStatus().equals(YWorkItem.Status.Fired)||
                    record.getStatus().equals(YWorkItem.Status.Suspended)) {
                availableItems.add(record);
            }
        }
        if (_orgModelConnectionOK) {
            availableItems = filterItemsForUser(username, availableItems, sessionHandle);
        }
        return availableItems;
    }

    private List filterItemsForUser(String username, List availableItems, String sessionHandle) {
        if (isAdministrator(sessionHandle)) {
            return availableItems;
        }
        List filteredItems = new ArrayList();
        for (int i = 0; i < availableItems.size(); i++) {
            WorkItemRecord workItemRecord = (WorkItemRecord) availableItems.get(i);
            Element workItemData = workItemRecord.getWorkItemData();
            if (containsEnablementData(workItemData)) {
                System.out.println("new XMLOutputter().outputString(workItemData) = " + new XMLOutputter().outputString(workItemData));
                String allocationQuery = workItemData.getChildText(YAWL_ALLOCATION_QUERY);
                System.out.println("allocationQuery = " + allocationQuery);
                String authenticationQuery = workItemData.getChildText(YAWL_AUTHENTICATION_QUERY);
                System.out.println("authenticationQuery = " + authenticationQuery);
                try {
                    allocationQuery = unmarshallSQLQuery(allocationQuery);
                    try {
                    List allocatedResources =
                            _dbConnector.whichUsersForThisQuery(allocationQuery);
                    System.out.println("\nAllocatedResources = " + allocatedResources);
                    if (null != authenticationQuery) {
                        authenticationQuery = unmarshallSQLQuery(authenticationQuery);
                        List authorisedResources = _dbConnector.whichUsersForThisQuery(
                                authenticationQuery);
                        System.out.println("\nAuthorisedResources = " + authorisedResources);
                        allocatedResources = intersectResources(allocatedResources, authorisedResources);
                    }

                    if (allocatedResources.contains(username)) {
                        filteredItems.add(workItemRecord);
                    }
                    } catch (YQueryException e) {
                    e.printStackTrace();
                    Problem warning = new Problem();
                    warning.setTimeStamp(new Date());
                    warning.setSource(workItemRecord.getID());
                    warning.setMessageType(Problem.EMPTY_RESOURCE_SET_MESSAGETYPE);
                    _dbConnector.saveWarning(warning);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (HibernateException e) {
                    e.printStackTrace();
                } catch (YPersistenceException e) {
                    e.printStackTrace();
                }
            }
            //there is no organisational resrictions over work item - allocate to anyone.
            else {
                filteredItems.add(workItemRecord);
            }
        }
        return filteredItems;
    }

    private static List intersectResources(List allocatedResources, List authorisedResources) {
        List intersection = new ArrayList();
        for (int j = 0; j < authorisedResources.size(); j++) {
            String authorisedUser = (String) authorisedResources.get(j);
            if (allocatedResources.contains(authorisedUser)) {
                intersection.add(authorisedUser);
            }
        }
        return intersection;
    }

    private boolean containsEnablementData(Element workItemData) {
        if (workItemData == null) {
            return false;
        }
        String allocationQuery = workItemData.getChildText(YAWL_ALLOCATION_QUERY);
        return allocationQuery != null;
    }

    private String unmarshallSQLQuery(String sqlQuery) {
        sqlQuery = sqlQuery.replaceAll("\\$apos;", "'");
        System.out.println("sqlQuery = " + sqlQuery);
        return sqlQuery;
    }

    private boolean isAdministrator(String sessionHandle) {
        try {
            return _interfaceBClient.isAdministrator(sessionHandle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List getActiveWork(String userid, String sessionHandle) throws JDOMException, IOException {
        List items = _interfaceBClient.getCompleteListOfLiveWorkItems(sessionHandle);
        List availableItems = new Vector();
        _model.updateWorkItems(items);
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            WorkItemRecord record = (WorkItemRecord) iterator.next();
            if (record.getStatus().equals(YWorkItem.Status.Deadlocked) ||
                    (record.getStatus().equals(YWorkItem.Status.Executing) &&
                    record.getWhoStartedMe().equals(userid))) {
                availableItems.add(record);
            }
        }
        return availableItems;
    }


    public YParametersSchema getParamsForTask(String specID, String taskID, String sessionHandle) {
        TaskInformation taskInfo = _model.getTaskInformation(specID, taskID);
        if (taskInfo != null) {
            return taskInfo.getParamSchema();
        }
        return null;
    }


    public String getMarshalledOutputParamsForTask(String specificationID, String taskID, String sessionHandle) throws IOException {
        YParametersSchema params = getParamsForTask(specificationID, taskID, sessionHandle);

        String dataRootElementNm = _model.getDataRootElementName(specificationID, taskID, sessionHandle);

        return Marshaller.getOutputParamsInXML(params, dataRootElementNm);
    }


    public String getDataForWorkItem(String workItemID) {
        return _model.getDataForWorkItemID(workItemID);
    }


    public void saveWorkItem(String workItemID, String data) {
        _model.setDataForWorkItemID(workItemID, data);
    }


    public void unsaveWorkItem(String workItemID) {
        _model.unsaveWorkItem(workItemID);
    }


    public String checkPermissionToAddInstances(String workItemID, String sessionHandle) throws IOException {
        return _interfaceBClient.checkPermissionToAddInstances(
                workItemID,
                sessionHandle);
    }


    public String createNewInstance(String workItemID,
                                    String paramValueForMICreation,
                                    String sessionHandle) throws IOException {
        return _interfaceBClient.createNewInstance(
                workItemID, paramValueForMICreation, sessionHandle);
    }


    public WorkItemRecord addWorkItem(String workItemXML) {
        WorkItemRecord itemRecord = Marshaller.unmarshalWorkItem(workItemXML);
        _model.addWorkItem(itemRecord);
        return itemRecord;
    }


    public String suspendWorkItem(String workItemID, String sessionHandle) throws IOException {
        return _interfaceBClient.rollbackWorkItem(workItemID, sessionHandle);
    }


    public String launchCase(String specID, String caseData, String sessionHandle) throws IOException {
        return _interfaceBClient.launchCase(specID, caseData, sessionHandle);
    }


    public List getCases(String specID, String sessionHandle) throws IOException {
        String casesAsXML = _interfaceBClient.getCases(specID, sessionHandle);
        if (_interfaceBClient.successful(casesAsXML)) {
            List cases = Marshaller.unmarshalCaseIDs(casesAsXML);
            return cases;
        }
        return null;
    }

    public String getCaseState(String caseID, String sessionHandle) throws IOException {
        String result = _interfaceBClient.getCaseState(caseID, sessionHandle);
        StringBuffer html = new StringBuffer();
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(new StringReader(result));
            List kids = doc.getRootElement().getChildren();
            for (int i = 0; i < kids.size(); i++) {
                html.append("<tr><td>&nbsp</td><td><pre>\n");
                Element yawlElement = (Element) kids.get(i);
                String id = yawlElement.getAttributeValue("id");
                String name = yawlElement.getAttributeValue("name");
                if (yawlElement.getName().equals("task")) {
                    html.append(
                            "    Task ");
                    if (name.equals("null")) {
                        html.append(
                                " id: " + id + "\n");
                    } else {
                        html.append(
                                " name: " + name + "\n");
                    }
                    List kidsOfKids = yawlElement.getChildren();
                    for (int j = 0; j < kidsOfKids.size(); j++) {
                        Element internalCondition = (Element) kidsOfKids.get(j);
                        html.append(
                                "        Internal Condition id:"
                                + internalCondition.getAttributeValue("id") + "\n"
                        );
                        List idientifiers = internalCondition.getChildren();
                        for (int k = 0; k < idientifiers.size(); k++) {
                            Element identifier = (Element) idientifiers.get(k);
                            html.append(
                                    "            Identifier: " + identifier.getText() + "\n"
                            );
                        }
                    }
                } else {
                    html.append(
                            "    Condition ");
                    if (name.equals("null")) {
                        html.append(
                                " id: " + id + "\n");
                    } else {
                        html.append(
                                " name: " + name + "\n");
                    }
                    List idientifiers = yawlElement.getChildren();
                    for (int k = 0; k < idientifiers.size(); k++) {
                        Element identifier = (Element) idientifiers.get(k);
                        html.append(
                                "            Identifier: " + identifier.getText() + "\n"
                        );
                    }
                }
                html.append("</pre></td></tr>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return html.toString();
    }

    public String cancelCase(String caseID, String sessionHandle) throws IOException {
        return _interfaceBClient.cancelCase(caseID, sessionHandle);
    }

    /**
     * Implements InterfaceBWebsideController.  It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In the case of a worklist
     * it does nothing because work items are pulled from the engine.
     * @param workItemRecord
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    }

    /**
     * Your opportunity to provide a welcome screen for your Custom YAWL Service.
     * For instance you could redirect to a JSP or just write do get typical
     * servlet doGet code inside your implementation.  You could probably
     * get away with leaving the implementation empty too.
     * @param request the request
     * @param response the response.
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
    }

    public boolean checkConnectionForAdmin(String sessionHandle) {
        String msg = _interfaceAClient.checkConnection(sessionHandle);
        return _interfaceBClient.successful(msg);
    }


    /**
     * Return a set of yawl services
     * @param sessionHandle use to ensure authentication.
     * @return yawl service objects
     */
    public Set getRegisteredYAWLServices(String sessionHandle) {
        return _interfaceAClient.getRegisteredYAWLServices(sessionHandle);
    }

    public void setUpInterfaceAClient(String backEndURI) {
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(backEndURI);
    }

    public String uploadSpecification(String specification, String filename, String sessionHandle) {
        return _interfaceAClient.uploadSpecification(specification, filename, sessionHandle);
    }

    public String unloadSpecification(String specID, String sessionHandle) throws IOException {
        return _interfaceAClient.unloadSpecification(specID, sessionHandle);
    }

    public String createUser(String userName, String password,
                             boolean isAdmin, String sessionHandle) throws IOException {
        return _interfaceAClient.createUser(userName, password, isAdmin, sessionHandle);
    }

    public List getUsers(String sessionHandle) {
        return _interfaceAClient.getUsers(sessionHandle);
    }


    /**
     * Adds a YAWL service to the engine.  It doesn't create the service. the service is assumed
     * to already exist.
     * @param serviceURI URI of it
     * @param serviceDocumentation
     * @param sessionHandle
     * @return result message success / failure ...reason
     */
    public String addYAWLService(String serviceURI, String serviceDocumentation, String sessionHandle) throws IOException {
        YAWLServiceReference service = new YAWLServiceReference(serviceURI, null);
        service.setDocumentation(serviceDocumentation);
        return _interfaceAClient.setYAWLService(service, sessionHandle);
    }


    public String removeYAWLService(String serviceURI, String sessionHandle) throws IOException {
        return _interfaceAClient.removeYAWLService(serviceURI, sessionHandle);
    }


    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[2];
        YParameter param;

        param = new YParameter(null, YParameter._ENABLEMENT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, YAWL_AUTHENTICATION_QUERY, XSD_NAMESPACE);
        param.setDocumentation("This is the DB query for authenticating the user.");
        params[0] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_NCNAME_TYPE, YAWL_ALLOCATION_QUERY, XSD_NAMESPACE);
        param.setDocumentation("This is the DB query for authorising the user.");
        params[1] = param;

        return params;
    }


    public static void main(String[] args) {
        List allocatedRes = Arrays.asList(new String[]{"fred", "peter", "sue"});
        List authorisedRes = Arrays.asList(new String[]{"peter"});

        List intersection = intersectResources(allocatedRes, authorisedRes);
        System.out.println("intersection = " + intersection);
    }

}
