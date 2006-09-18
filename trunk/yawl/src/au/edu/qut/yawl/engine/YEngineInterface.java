package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;

import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.util.YVerificationMessage;

public interface YEngineInterface {
    
	void initialise() throws YPersistenceException;
    
    public List<String> addSpecifications(File specificationFile, boolean ignoreErors, List<YVerificationMessage> errorMessages) throws JDOMException, IOException, YPersistenceException;
    public boolean loadSpecification(YSpecification spec);
    public void unloadSpecification(String specID) throws YStateException, YPersistenceException;
    public Set getSpecIDs();
    public Set getLoadedSpecifications();
    public YSpecification getSpecification(String specID);
    public YIdentifier getCaseID(String caseIDStr);
    public String getStateTextForCase(YIdentifier caseID);
    public String getStateForCase(YIdentifier caseID);
    public void registerInterfaceAClient(InterfaceAManagementObserver observer);
    public void registerInterfaceBObserver(InterfaceBClientObserver observer);
    public void registerInterfaceBObserverGateway(ObserverGateway gateway);
    public Set<YWorkItem> getAvailableWorkItems();
    public YSpecification getProcessDefinition(String specID);
    public YWorkItem getWorkItem(String workItemID);
    public Set getAllWorkItems();
    public YWorkItem startWorkItem(YWorkItem workItem, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;
    public YTask getTaskDefinition(String specificationID, String taskID);
    public void completeWorkItem(YWorkItem workItem, String data, boolean force)
    	throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException;
    public YWorkItem createNewInstance(YWorkItem workItem, String paramValueForMICreation) throws YStateException, YPersistenceException;
    public YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException;
    public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException;
    public void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException;
    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException;
    public Set getCasesForSpecification(String specID);
    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID);
    public Set getYAWLServices();
    public void addYawlService(YAWLServiceReference yawlService) throws YPersistenceException;
    public Set getChildrenOfWorkItem(YWorkItem workItem);
    public void announceCancellationToEnvironment(YAWLServiceReference yawlService, YWorkItem item);
    public Set getUsers();
    public String getLoadStatus(String specID);
    public YAWLServiceReference removeYawlService(String serviceURI) throws YPersistenceException;
    public boolean isJournalising();
    public void setJournalising(boolean arg);
    public void dump();
    public void storeObject(Object obj) throws YPersistenceException;
    public boolean updateWorkItemData(String workItemID, String data);

    public boolean updateCaseData(String idStr, String data);

    public void cancelWorkItem(YWorkItem workItem, boolean statusFail);
    
    public void announceCancellationToExceptionService(InterfaceX_EngineSideClient ixClient,
                                                     YIdentifier caseID);

    public void announceTimeOutToExceptionService(InterfaceX_EngineSideClient ixClient,
                                                  YWorkItem item, List timeOutTaskIds);
    
    public void cancelCase(YIdentifier id) throws YPersistenceException;
   	public boolean setExceptionObserver(InterfaceX_EngineSideClient ix);

   	public boolean setExceptionObserver(String observerURI);


  	public boolean removeExceptionObserver();
  	
  	public YEngine getYEngine();
    
}
