/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.exceptions.YAWLException;

import javax.xml.datatype.Duration;
import java.io.InputStream;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

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
    boolean enginePersistenceFailure();

    void registerObserverGateway(ObserverGateway gateway) throws YAWLException;

    void setDefaultWorklist(String url);

    void setAllowAdminID(boolean allow);

    void disableLogging();

    void setHibernateStatisticsEnabled(boolean enable);

    void shutdown();

    void initBuildProperties(InputStream stream);

    void notifyServletInitialisationComplete(int maxWaitSeconds);
    
    void setActualFilePath(String path);

    /**
     *
     * @param sessionHandle
     * @return the ids of all currently active workitems
     * @throws RemoteException
     */
    String getAvailableWorkItemIDs(String sessionHandle) throws RemoteException;

    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return the full workitem record matching the id passed
     * @throws RemoteException
     */
    String getWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String getWorkItemExpiryTime(String workItemID, String sessionHandle)
            throws RemoteException;

    /**
     *
     * @param specID
     * @param sessionHandle
     * @return the specification matching the id passed
     * @throws RemoteException
     */
    String getProcessDefinition(YSpecificationID specID, String sessionHandle) throws RemoteException;

    String suspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String unsuspendWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String rollbackWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String completeWorkItem(String workItemID, String data, String logPredicate, boolean force, String sessionHandle) throws RemoteException;

    String startWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String skipWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String getStartingDataSnapshot(String workItemID, String sessionHandle) throws RemoteException;

    String createNewInstance(String workItemID, String paramValueForMICreation, String sessionHandle) throws RemoteException;

    String describeAllWorkItems(String sessionHandle) throws RemoteException;

    String getWorkItemsWithIdentifier(String idType, String itemID, String sessionHandle) throws RemoteException;

    String getWorkItemsForService(String serviceURI, String sessionHandle) throws RemoteException;

    String connect(String userID, String password, long timeOutSeconds) throws RemoteException;

    String checkConnection(String sessionHandle) throws RemoteException;

    String disconnect(String sessionHandle) throws RemoteException;

    String checkConnectionForAdmin(String sessionHandle) throws RemoteException;

    String getTaskInformation(YSpecificationID specificationID, String taskID, String sessionHandle) throws RemoteException;

    String checkElegibilityToAddInstances(String workItemID, String sessionHandle) throws RemoteException;

    String getSpecificationList(String sessionHandle) throws RemoteException;

    String getSpecificationData(YSpecificationID specID, String sessionHandle) throws RemoteException ;

    String getSpecificationDataSchema(YSpecificationID specID, String sessionHandle)
                                                   throws RemoteException ;


    String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String caseID, String logDataStr, String sessionHandle) throws RemoteException;
    
    String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String logDataStr, String sessionHandle) throws RemoteException;

    String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String logDataStr, long mSec, String sessionHandle) throws RemoteException;

    String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String logDataStr, Date expiry, String sessionHandle) throws RemoteException;
 
    String launchCase(YSpecificationID specID, String caseParams, URI completionObserverURI, String logDataStr, Duration duration, String sessionHandle) throws RemoteException;
  
    String getCasesForSpecification(YSpecificationID specID, String sessionHandle) throws RemoteException;

    String getSpecificationForCase(String caseID, String sessionHandle) throws RemoteException;

    String getSpecificationIDForCase(String caseID, String sessionHandle) throws RemoteException;

    String getAllRunningCases(String sessionHandle) throws RemoteException;

    String getCaseState(String caseID, String sessionHandle) throws RemoteException;

    String cancelCase(String caseID, String sessionHandle) throws RemoteException;

    String getChildrenOfWorkItem(String workItemID, String sessionHandle) throws RemoteException;

    String getWorkItemOptions(String workItemID, String thisURL, String sessionHandle) throws RemoteException;

    String loadSpecification(String specification, String sessionHandle) throws RemoteException;

    String unloadSpecification(YSpecificationID specID, String sessionHandle) throws RemoteException;

    String createAccount(String userName, String password, String doco, String sessionHandle) throws RemoteException;

    String updateAccount(String userName, String password, String doco, String sessionHandle) throws RemoteException;

    String getAccounts(String sessionHandle) throws RemoteException;

    String getBuildProperties(String sessionHandle) throws RemoteException;

    String getYAWLServices(String sessionHandle) throws RemoteException;

    String getYAWLServiceDocumentation(String yawlServiceURI, String sessionHandle) throws RemoteException;

    String addYAWLService(String serviceStr, String sessionHandle) throws RemoteException;

    String removeYAWLService(String serviceURI, String sessionHandle);

    String deleteAccount(String userName, String sessionHandle) throws RemoteException;

    String changePassword(String password, String sessionHandle) throws RemoteException;

    String getClientAccount(String userID, String sessionHandle) throws RemoteException;

    String getClientPassword(String userID, String sessionHandle) throws RemoteException;

    String addInterfaceXListener(String observerURI) ;

    String removeInterfaceXListener(String observerURI) ;

    String updateWorkItemData(String workItemID, String data, String sessionHandle);

    String updateCaseData(String idStr, String data, String sessionHandle) ;

    String restartWorkItem(String workItemID, String sessionHandle) throws RemoteException ;

    String cancelWorkItem(String id, String data, String fail, String sessionHandle) throws RemoteException ;

    String getLatestSpecVersion(String id, String sessionHandle) throws RemoteException ;

    String getMITaskAttributes(YSpecificationID specificationID, String taskID,
                                      String sessionHandle) throws RemoteException ;

    String getResourcingSpecs(YSpecificationID specificationID, String taskID,
                                     String sessionHandle) throws RemoteException ;

    String getCaseData(String caseID, String sessionHandle) throws RemoteException;

    String getCaseInstanceSummary(String sessionHandle) throws RemoteException;

    String getWorkItemInstanceSummary(String caseID, String sessionHandle)
            throws RemoteException;

    String getParameterInstanceSummary(String caseID, String itemID, String sessionHandle)
            throws RemoteException ;

    String rejectAnnouncedEnabledTask(String itemID, String sessionHandle) throws RemoteException ;

    String getExternalDBGateways(String sessionHandle) throws RemoteException ;

    String setHibernateStatisticsEnabled(boolean enabled, String sessionHandle);

    String isHibernateStatisticsEnabled(String sessionHandle);

    String getHibernateStatistics(String sessionHandle);
}
