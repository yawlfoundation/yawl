/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.InputStream;

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
     * Indicates if the engine has encountered some form of persistence failure in its lifetime.<P>
     *
     * @return boolean
     */
    public boolean enginePersistenceFailure();

    public void registerObserverGateway(ObserverGateway gateway);

    public void setDefaultWorklist(String url);

    public void setAllowAdminID(boolean allow);

    public void disableLogging();

    public void setHibernateStatisticsEnabled(boolean enable);

    public void shutdown();

    public void initBuildProperties(InputStream stream);

    public void notifyServletInitialisationComplete();
    
    public void setActualFilePath(String path);

    /**
     *
     * @param sessionHandle
     * @return the ids of all currently active workitems
     * @throws RemoteException
     */
    public String getAvailableWorkItemIDs(String sessionHandle) throws RemoteException;

    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return the full workitem record matching the id passed
     * @throws RemoteException
     */
    public String getWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    /**
     *
     * @param specID
     * @param sessionHandle
     * @return the specification matching the id passed
     * @throws RemoteException
     */
    public String getProcessDefinition(YSpecificationID specID, String sessionHandle) throws RemoteException;

    public String suspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String unsuspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String rollbackWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String completeWorkItem(String workItemID, String data, String logPredicate, boolean force, String sessionHandle) throws RemoteException;

    public String startWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String skipWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String createNewInstance(String workItemID, String paramValueForMICreation, String sessionHandle) throws RemoteException;

    public String describeAllWorkItems(String sessionHandle) throws RemoteException;

    public String getWorkItemsWithIdentifier(String idType, String itemID, String sessionHandle) throws RemoteException;

    public String connect(String userID, String password, long timeOutSeconds) throws RemoteException;

    public String checkConnection(String sessionHandle) throws RemoteException;

    public String checkConnectionForAdmin(String sessionHandle) throws RemoteException;

    public String getTaskInformation(YSpecificationID specificationID, String taskID, String sessionHandle) throws RemoteException;

    public String checkElegibilityToAddInstances(String workItemID, String sessionHandle) throws RemoteException;

    public String getSpecificationList(String sessionHandle) throws RemoteException;

    public String getSpecificationDataSchema(YSpecificationID specID, String sessionHandle)
                                                   throws RemoteException ;


    public String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String caseID, String logDataStr, String sessionHandle) throws RemoteException;
    
    public String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String logDataStr, String sessionHandle) throws RemoteException;

    public String getCasesForSpecification(YSpecificationID specID, String sessionHandle) throws RemoteException;

    public String getAllRunningCases(String sessionHandle) throws RemoteException;

    public String getCaseState(String caseID, String sessionHandle) throws RemoteException;

    public String cancelCase(String caseID, String sessionHandle) throws RemoteException;

    public String getChildrenOfWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    public String getWorkItemOptions(String workItemID, String thisURL, String sessionHandle) throws RemoteException;

    public String loadSpecification(String specification, String sessionHandle) throws RemoteException;

    public String unloadSpecification(YSpecificationID specID, String sessionHandle) throws RemoteException;

    public String createAccount(String userName, String password, String doco, String sessionHandle) throws RemoteException;

    public String updateAccount(String userName, String password, String doco, String sessionHandle) throws RemoteException;

    public String getAccounts(String sessionHandle) throws RemoteException;

    public String getBuildProperties(String sessionHandle) throws RemoteException;

    public String getYAWLServices(String sessionHandle) throws RemoteException;

    public String getYAWLServiceDocumentation(String yawlServiceURI, String sessionHandle) throws RemoteException;

    public String addYAWLService(String serviceStr, String sessionHandle) throws RemoteException;

    public String removeYAWLService(String serviceURI, String sessionHandle);

    public String deleteAccount(String userName, String sessionHandle) throws RemoteException;

    public String changePassword(String password, String sessionHandle) throws RemoteException;

    public String getClientAccount(String userID, String sessionHandle) throws RemoteException;

    public String getClientPassword(String userID, String sessionHandle) throws RemoteException;

    public String addInterfaceXListener(String observerURI) ;

    public String removeInterfaceXListener(String observerURI) ;

    public String updateWorkItemData(String workItemID, String data, String sessionHandle);

    public String updateCaseData(String idStr, String data, String sessionHandle) ;

    public String restartWorkItem(String workItemID, String sessionHandle) throws RemoteException ;

    public String cancelWorkItem(String id, String fail, String sessionHandle) throws RemoteException ;

    public String getLatestSpecVersion(String id, String sessionHandle) throws RemoteException ;

    public String getMITaskAttributes(YSpecificationID specificationID, String taskID,
                                      String sessionHandle) throws RemoteException ;

    public String getResourcingSpecs(YSpecificationID specificationID, String taskID,
                                     String sessionHandle) throws RemoteException ;

    public String getCaseData(String caseID, String sessionHandle) throws RemoteException;

    public String getCaseInstanceSummary(String sessionHandle) throws RemoteException;

    public String getWorkItemInstanceSummary(String caseID, String sessionHandle)
            throws RemoteException;

    public String getParameterInstanceSummary(String caseID, String itemID, String sessionHandle)
            throws RemoteException ;

    public String rejectAnnouncedEnabledTask(String itemID, String sessionHandle) throws RemoteException ;

    public String getExternalDBGateways(String sessionHandle) throws RemoteException ;

    public String setHibernateStatisticsEnabled(boolean enabled, String sessionHandle);

    public String isHibernateStatisticsEnabled(String sessionHandle);

    public String getHibernateStatistics(String sessionHandle);
}
