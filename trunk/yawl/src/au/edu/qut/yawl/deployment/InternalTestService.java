/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;


import java.util.List;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;
import au.edu.qut.yawl.exceptions.YPersistenceException;
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
	 * It recieves messages from the engine
	 * notifying an enabled task and acts accordingly.  In this case it takes the message,
	 * tries to check out the work item, and if successful it begins to start up a web service
	 * invokation.
	 * @param enabledWorkItem
	 */
	public void handleEnabledWorkItemEvent(YWorkItem enabledWorkItem) {
		System.out.println("Invoked the test service");
		try {
			WorkItemRecord child = checkOut(enabledWorkItem.getIDString());
			
			if (child != null) {
				List<WorkItemRecord> children = getChildren(enabledWorkItem.getIDString());

				for (WorkItemRecord workitem : children) {
					System.out.println("Notified of: " + workitem.getWorkItemData().getText());
					
					TaskInformation task = getTaskInformation(
							workitem.getSpecificationID(),
							workitem.getTaskID());
					
					String outputdata = "<"+task.getDecompositionID()+">" +
					"<"+task.getDecompositionID()+"1>edited</"+task.getDecompositionID()+"1>" +
					"</"+task.getDecompositionID()+">";
					String inputData = "<" + task.getDecompositionID() + "/>";
					
					checkInWorkItem(workitem.getID(),inputData,outputdata);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * By implementing this method and deploying a web app containing the implementation
	 * the YAWL engine will send events to this method notifying your custom
	 * YAWL service that an active work item has been cancelled by the
	 * engine.
	 * @param workItemRecord a "snapshot" of the work item cancelled in
	 * the engine.
	 */
	public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
		System.out.println("Workitem was cancelled in the test service");
	}


	/**
	 * By overriding this method one can process case completion events.
	 * @param caseID the id of the completed case.
	 */
	public void handleCompleteCaseEvent(String caseID, String casedata) {
		System.out.println("Case was completed in the test service");
	}

	/**
	 * Override this method if you wish to allow other tools to find out what
	 * input parameters are required for your custom YAWL service to work.
	 * @return an array of input parameters.
	 */
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[1];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE,"time", XSD_NAMESPACE);
        param.setDocumentation("Amount of Time the TimeService will wait before returning");
        params[0] = param;

        return params;
    }

}
