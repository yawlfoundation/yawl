/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.deployment;

import java.util.List;

import org.jdom.Element;

import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

public class InternalTestService extends InterfaceBInternalServiceController {
	public InternalTestService() throws YPersistenceException {
	}
	
	public String getDocumentation() {
		return "This service is merely a test service for nothing important at all";
	}
	
	public String getServiceName() {
		return "InternalTestService";
	}

	/**
	 * It recieves messages from the engine notifying an enabled task and acts accordingly.
	 * In this case it takes the message, tries to check out the work item, and if successful
	 * it prints out the information passed in and checks the work item back in with some
	 * data.
	 */
	public void handleEnabledWorkItemEvent(String enabledWorkItem) {
		WorkItemRecord workItemRecord = Marshaller.unmarshalWorkItem(enabledWorkItem);
		System.out.println("Invoked the test service");
		try {
			WorkItemRecord child = checkOut(workItemRecord.getID());
			
			if (child != null) {
				List<WorkItemRecord> children = getChildren(workItemRecord.getID());

				for (WorkItemRecord workitem : children) {
					Element dataRoot = workitem.getWorkItemData();
					if(dataRoot != null) {
						System.out.println("Notified of: " + dataRoot.getName());
						for(Object content : dataRoot.getContent()) {
							if(content instanceof Element) {
								System.out.println(
										((Element)content).getName() +
										":" +
										((Element)content).getText());
							}
						}
					}
					
					TaskInformation task = getTaskInformation(
							workitem.getSpecificationID(),
							workitem.getTaskID());
					
					String outputdata = "<"+task.getDecompositionID()+">" +
					"<"+task.getDecompositionID()+"1>edited</"+task.getDecompositionID()+"1>" +
					"</"+task.getDecompositionID()+">";
					String inputData = "<" + task.getDecompositionID() + "/>";
					
					System.out.println("output:\n" + outputdata);
					checkInWorkItem(workitem.getID(),inputData,outputdata);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Just prints a notification to standard out.
	 * @param workItemRecord a "snapshot" of the work item cancelled in
	 * the engine.
	 */
	public void handleCancelledWorkItemEvent(String workItemRecord) {
		System.out.println("Workitem was cancelled in the test service");
	}


	/**
	 * Just prints a notification to standard out.
	 * @param caseID the id of the completed case.
	 */
	public void handleCompleteCaseEvent(String caseID, String casedata) {
		System.out.println("Case was completed in the test service");
	}
}
