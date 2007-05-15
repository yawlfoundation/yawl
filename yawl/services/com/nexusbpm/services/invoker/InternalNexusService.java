/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import com.nexusbpm.services.NexusServiceConstants;
import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.data.Variable;

/**
 * Base class for internal Nexus services.
 * <p>
 * Classes that extend this class must implement {@link #execute(NexusServiceData)},
 * {@link #getDocumentation()}, and {@link #getServiceName()}.
 * 
 * @author Nathan Rose
 */
public abstract class InternalNexusService extends InterfaceBInternalServiceController {
	protected final Logger LOG = Logger.getLogger(getClass());
	
	public abstract NexusServiceData execute(NexusServiceData data);
	
    /**
     * Recieves messages from the engine notifying an enabled task and acts accordingly.
     * In this case it takes the message, tries to check out the work item, and if
     * successful it unmarshals the Nexus data and calls {@link #execute(NexusServiceData)}.
     * @param enabledWorkItem
     */
	@Override
    public final void handleEnabledWorkItemEvent(String enabledWorkItem) {
		WorkItemRecord workItemRecord = Marshaller.unmarshalWorkItem(enabledWorkItem);
        try {
			List executingChildren = checkOutAllInstancesOfThisTask(workItemRecord);
			
			for(int i = 0; i < executingChildren.size(); i++) {
				WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);
				Element inputData = itemRecord.getWorkItemData();
				List<Content> serviceData = inputData.cloneContent();
				NexusServiceData data = null;
				
//				// TODO service name variable shouldn't be needed anymore, so this should get removed at some point
//				for(Iterator<Content> iter = serviceData.iterator(); iter.hasNext();) {
//					Content c = iter.next();
//					if(c instanceof Element && ((Element) c).getName().equals(NexusServiceConstants.SERVICENAME_VAR)) {
//						iter.remove();
//					}
//				}
				data = NexusServiceData.unmarshal(serviceData);
				System.out.println("Work item " + workItemRecord.getUniqueID() + " item in:");
				System.out.println(workItemRecord);
				System.out.println("Work item " + workItemRecord.getUniqueID() + " begin data in:");
				for (Variable var:data.getVariables()) {
					System.out.println("  " + var);
				}
				System.out.println("Work item " + workItemRecord.getUniqueID() + " end data in:");
				JythonPreprocessor preprocessor = new JythonPreprocessor( data );
		        preprocessor.evaluate();
				System.out.println("Work item " + workItemRecord.getUniqueID() + " begin processed input data:");
				for (Variable var:data.getVariables()) {
					System.out.println("  " + var);
				}
				System.out.println("Work item " + workItemRecord.getUniqueID() + " end processed input data:");

				NexusServiceData result = execute((NexusServiceData)data.clone());

				System.out.println("Work item " + workItemRecord.getUniqueID() + " begin output data:");
				for (Variable var:result.getVariables()) {
					System.out.println("  " + var);
				}
				System.out.println("Work item " + workItemRecord.getUniqueID() + " end output data:");

				preprocessor.setData( result );
		        preprocessor.restore();
				
				Element caseDataBoundForEngine = prepareReplyRootElement(workItemRecord);
				
				
				// convert reply back to XML
				if(result != null) {
					caseDataBoundForEngine.addContent(NexusServiceData.marshal(result));
				} else {
					LOG.error("Nexus Service '" + getServiceName() + "' (" +
							itemRecord.getCaseID() + ":" + itemRecord.getTaskID() +
							") returned no resulting data!");
				}
				
				String checkInResult = checkInWorkItem(itemRecord.getID(), inputData, caseDataBoundForEngine);
				
				LOG.debug("\nResult of item [" +
						itemRecord.getCaseID() + ":" + itemRecord.getTaskID() +
						"] checkin is : " + checkInResult);
			}
		} catch(Exception e) {
			e.printStackTrace(System.out);
			LOG.error("Error handling checkout and execution of service " + getServiceName(), e);
		}
    }
    
    @Override
	public final void handleCancelledWorkItemEvent(String workItemRecord) {
	}

	@Override
	public final void handleCompleteCaseEvent(String caseID, String casedata) {
	}

	public YParameter[] describeRequiredParams() {
        return new YParameter[0];
    }
}

