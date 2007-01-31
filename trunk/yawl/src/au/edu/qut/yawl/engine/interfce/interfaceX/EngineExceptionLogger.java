/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce.interfaceX;

import java.net.URI;
import java.util.List;

import org.jdom.Document;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient.Handler;
import au.edu.qut.yawl.events.YServiceError;

public class EngineExceptionLogger implements ExceptionGateway {

	public void announceCaseCancellation(String caseID) {
		// TODO Auto-generated method stub

	}

	public void announceCheckCaseConstraints(String specID, String caseID,
			String data, boolean preCheck) {
		// TODO Auto-generated method stub

	}

	public void announceCheckWorkItemConstraints(YWorkItem item, Document data,
			boolean preCheck) {
		// TODO Auto-generated method stub

	}

	public void announceConstraintViolation(YWorkItem item) {
		// TODO Auto-generated method stub

	}

	public void announceResourceUnavailable(YWorkItem item) {
		// TODO Auto-generated method stub

	}

	public void announceTimeOut(YWorkItem item, List taskList) {
		// TODO Auto-generated method stub

	}

	public void announceWorkitemAbort(YWorkItem item) {
		// TODO Auto-generated method stub

	}
	
    public void announceServiceUnavailable(YWorkItem item, URI ref){
    	item.getCaseID().addError(new YServiceError(ref.toString(), item.getIDString(), item.getTaskID()));
    }
    
    public void announceServiceError(YWorkItem item, URI ref){
    }

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

}
