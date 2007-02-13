/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;

import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.util.YVerificationMessage;

public interface YEngineInterface {
    
	public void executeServiceNotifications();
	
	public String getDataForSpecifications(boolean loadedOnly) throws YPersistenceException;
	public void addExceptionObserver(InterfaceX_EngineSideClient ix);
    public void addExceptionObserver(String observerURI);
    public List<String> addSpecifications(File specificationFile, boolean ignoreErors, List<YVerificationMessage> errorMessages) throws JDOMException, IOException, YPersistenceException;
    public void addYawlService(YAWLServiceReference yawlService) throws YPersistenceException;
    
    public void announceCancellationToEnvironment(URI yawlService, YWorkItem item);
    public void announceCancellationToExceptionService(YIdentifier caseID);
    public void announceTimeOutToExceptionService(YWorkItem item, List timeOutTaskIds);
    public void cancelCase(YIdentifier id) throws YPersistenceException;
    public void cancelWorkItem(YWorkItem workItem, boolean statusFail);
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException, YPersistenceException;
    public void completeWorkItem(String workItemId, String data, boolean force)
    	throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;
    public YWorkItem createNewInstance(String workItemID, String paramValueForMICreation) throws YStateException, YPersistenceException;
    
    public void dump();
    
    public Set getAllWorkItems() throws YPersistenceException;
    public Set<YWorkItem> getAvailableWorkItems();
    public YIdentifier getCaseID(String caseIDStr) throws YPersistenceException;
    public Set getCasesForSpecification(String specID) throws YPersistenceException;
    public Set getCasesForSpecification(String specID, Integer version) throws YPersistenceException;
    public Set getChildrenOfWorkItem(YWorkItem workItem) throws YPersistenceException;
    public Set getLoadedSpecifications() throws YPersistenceException;
    public String getLoadStatus(String specID) throws YPersistenceException;
    public YNetRunner getNetRunner(YIdentifier id) throws YPersistenceException;
    public String getProcessDefinition(String specID) throws YPersistenceException, YAWLException;
    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID);
    public Set getSpecIDs() throws YPersistenceException;
    public YSpecification getSpecification(String specID) throws YPersistenceException;
   
    public String getTaskInformation(String specificationID, String taskID) throws YPersistenceException;    
    
    public Set<YSpecification> getSpecifications(boolean loadedOnly) throws YPersistenceException;
    
    public String getSpecificationsByRestriction(String restriction) throws Exception;
    
    public String getStateForCase(String caseID) throws YPersistenceException;
    public String getStateForCase(YIdentifier caseID) throws YPersistenceException;
    public String getStateTextForCase(YIdentifier caseID) throws YPersistenceException;
    public YTask getTaskDefinition(String specificationID, String taskID) throws YPersistenceException;
    public Set getUsers();
    public YWorkItem getWorkItem(String workItemID) throws YPersistenceException;
    public Set getYAWLServices() throws YPersistenceException;
    public YEngine getYEngine();
    
   
    
    void initialise() throws YPersistenceException;
    
    public boolean isJournalising();
    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException;
    public boolean loadSpecification(YSpecification spec) throws YPersistenceException;
    public void registerInterfaceAClient(InterfaceAManagementObserver observer);
    public void registerInterfaceBObserver(InterfaceBClientObserver observer);
    public void registerInterfaceBObserverGateway(ObserverGateway gateway);
    public void removeExceptionObservers();
    public YAWLServiceReference removeYawlService(String serviceURI) throws YPersistenceException;

    public void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException;

    public void setJournalising(boolean arg);
    
    public YWorkItem startWorkItem(String workItemId, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;

    public void storeObject(Object obj) throws YPersistenceException;
    
    public YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException;
   	public void unloadSpecification(String specID) throws YStateException, YPersistenceException;

   	public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException;

  	public boolean updateCaseData(String idStr, String data) throws YPersistenceException;
  	
  	public boolean updateWorkItemData(String workItemID, String data);
    
  	public void setDao(DAO dao);
  	public DAO getDao();  	 
}
