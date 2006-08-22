/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.URI;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 15/10/2004
 * Time: 11:28:22
 * 
 */
public interface EngineGateway extends Remote {

    /**
     * Inidicates if the engine has encountered some form of persistence failure in its lifetime.<P>
     *
     * @return boolean
     */
    public boolean enginePersistenceFailure();
    

    /**
     *
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getAvailableWorkItemIDs(String sessionHandle) throws RemoteException;

    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getWorkItemDetails(String workItemID, String sessionHandle) throws RemoteException;

    /**
     *
     * @param specID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getProcessDefinition(String specID, String sessionHandle) throws RemoteException;

    public String suspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String unsuspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String rollbackWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String completeWorkItem(String workItemID, String data, boolean force, String sessionHandle) throws RemoteException;

    public String startWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String createNewInstance(String workItemID, String paramValueForMICreation, String sessionHandle) throws RemoteException;

    public String describeAllWorkItems(String sessionHandle) throws RemoteException;

    public String connect(String userID, String password) throws RemoteException;

    public String checkConnection(String sessionHandle) throws RemoteException;

    public String checkConnectionForAdmin(String sessionHandle) throws RemoteException;

    public String getTaskInformation(String specificationID, String taskID, String sessionHandle) throws RemoteException;

    public String checkElegibilityToAddInstances(String workItemID, String sessionHandle) throws RemoteException;

    public String getSpecificationList(String sessionHandle) throws RemoteException;

    public String launchCase(String specID, String caseParams, URI completionObserverURI,String sessionHandle) throws RemoteException;

    public String getCasesForSpecification(String specID, String sessionHandle) throws RemoteException;

    public String getCaseState(String caseID, String sessionHandle) throws RemoteException;

    public String cancelCase(String caseID, String sessionHandle) throws RemoteException;

    public String getChildrenOfWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String getWorkItemOptions(String workItemID, String thisURL, String sessionHandle) throws RemoteException;

    public String loadSpecification(String specification, String fileName, String sessionHandle) throws RemoteException;

    public String unloadSpecification(String specID, String sessionHandle) throws RemoteException;

    public String createUser(String userName, String password, boolean isAdmin, String sessionHandle) throws RemoteException;

    public String getUsers(String sessionHandle) throws RemoteException;

    public String getYAWLServices(String sessionHandle) throws RemoteException;

    public String getYAWLServiceDocumentation(String yawlServiceURI, String sessionHandle) throws RemoteException;

    public String addYAWLService(String serviceStr, String sessionHandle) throws RemoteException;

    public String removeYAWLService(String serviceURI, String sessionHandle);

    public String deleteUser(String userName, String sessionHandle) throws RemoteException;

    public String changePassword(String password, String sessionHandle) throws RemoteException;

    public String setExceptionObserver(String observerURI) ;

    public String removeExceptionObserver() ;

    public String updateWorkItemData(String workItemID, String data, String sessionHandle);

    public String updateCaseData(String idStr, String data, String sessionHandle) ;

    public String restartWorkItem(String workItemID, String sessionHandle) throws RemoteException ;

    public String cancelWorkItem(String id, String fail, String sessionHandle) throws RemoteException ;
}
