/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;


import java.io.IOException;
import java.io.StringReader;
import java.util.Set;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;


import au.edu.qut.yawl.worklist.model.WorkItemRecord;

public class InternalTestService extends InterfaceBInternalServiceController {


	public InternalTestService() {

	}

	
	
	public String getDocumentation() {
		return "This service is merely a test service for nothing important at all";
	}

	/**
	 * It recieves messages from the engine
	 * notifying an enabled task and acts accordingly.  In this case it takes the message,
	 * tries to check out the work item, and if successful it begins to start up a web service
	 * invokation.
	 * @param enabledWorkItem
	 */
	public void handleEnabledWorkItemEvent(YWorkItem enabledWorkItem) {
		System.out.println("Invoked the service: " + this.getServiceURI());
		try {

			YWorkItem child = checkOut(enabledWorkItem.getIDString());

			
			if (child != null) {
				Set<YWorkItem> children = getChildren(enabledWorkItem.getIDString());


				for (YWorkItem workitem : children) {


					try {
						SAXBuilder builder = new SAXBuilder();
						Document doc = builder.build(new StringReader(workitem.getDataString()));
						Element inputData = doc.getRootElement();

						Element element = (Element) inputData.getChildren().get(0);
						String notifytime = element.getText();
						System.out.println("Notified of: " + notifytime);

					} catch (JDOMException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					YTask task = getTaskInformation(workitem.getSpecificationID(),
							workitem.getTaskID());

					
					
					String outputdata = "<"+task.getDecompositionPrototype().getId()+">" +
					"<"+task.getDecompositionPrototype().getId()+"1>edited</"+task.getDecompositionPrototype().getId()+"1>" +
					"</"+task.getDecompositionPrototype().getId()+">";

					
					
					checkInWorkItem(workitem.getIDString(),
							outputdata);
				}
			}


			//	return report;
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
		System.out.println("Workitem was cancelled: " + this.getServiceURI());
	}


	/**
	 * By overriding this method one can process case completion events.
	 * @param caseID the id of the completed case.
	 */
	public void handleCompleteCaseEvent(String caseID, String casedata) {
		System.out.println("Case was completed: " + this.getServiceURI());
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
