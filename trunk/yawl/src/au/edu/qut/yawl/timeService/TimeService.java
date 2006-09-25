/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.timeService;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.AuthenticationConfig;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import org.jdom.Element;

import java.util.*;

public class TimeService extends InterfaceBWebsideController {

	public static TimeService t = null;
	
	public TimeService() {
		super();
		t = this;
		
		List l = DAOFactory.getDAO().retrieveAll();
		/*
		 * Restart runners
		 * */
		for (int i = 0; i < l.size();i++) {
			InternalRunner runner = (InternalRunner) l.get(i);
			runner.start();
		}
		
	}

	public static TimeService getTimeService() {
		return t;
	}
	
    private String _sessionHandle = null;

    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (!successful(_sessionHandle)) {
                _logger.error("Unsuccessful");
            } else {
                WorkItemRecord child = checkOut(workItemRecord.getID(), _sessionHandle);

                if (child != null) {
                    List children = super.getChildren(workItemRecord.getID(), _sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        if (YWorkItem.Status.Fired.equals(itemRecord.getStatus())) {
                            checkOut(itemRecord.getID(), _sessionHandle);
                        }
                    }
                    children = super.getChildren(workItemRecord.getID(), _sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        //System.out.println("WebServiceController::processEnabledAnnouncement() itemRecord = " + itemRecord);
                        super._model.addWorkItem(itemRecord);

                        System.out.println("added: " + itemRecord.getID());

                        Element inputData = itemRecord.getWorkItemData();
                        System.out.println(inputData);
                        Element element = (Element) inputData.getChildren().get(0);
                        String notifytime = element.getText();
                        System.out.println("Notified of: " + notifytime);

                        if (notifytime != null && !notifytime.equals("")) {
                        	InternalRunner runner = null;
                            try {
                                int notify = Integer.parseInt(notifytime);
                                runner = new InternalRunner(notify, itemRecord,  _sessionHandle);
                            } catch (Exception e) {
                                runner=new InternalRunner(notifytime, itemRecord, _sessionHandle);
                            }
                            if (runner.getTime() > 0) {
                            	runner.saveInternalRunner();
                            	runner.start();
                            }
                        }
                    }
                }
            }
            //	return report;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[1];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE,"time", XSD_NAMESPACE);
        param.setDocumentation("Amount of Time the TimeService will wait before returning");
        params[0] = param;

        return params;
    }

    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    }

    public String processRequestToCancel(WorkItemRecord workItemRecord) {
        return "Cancelled";
    }


    public void setRemoteAuthenticationDetails(String userName, String password,
                                               String httpProxyHost, String proxyPort) {
        AuthenticationConfig auth = AuthenticationConfig.getInstance();

        auth.setProxyAuthentication(userName, password, httpProxyHost, proxyPort);
    }

    public synchronized void finish(WorkItemRecord itemRecord, String _sessionHandle) throws Exception {
        try {
            System.out.println("Checking in work Item: " + itemRecord.getID());

            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (!successful(_sessionHandle)) {
                _logger.error("Unsuccessful");
            } else {

            	TaskInformation taskinfo = getTaskInformation(itemRecord.getSpecificationID(),
            			itemRecord.getTaskID(),
            			_sessionHandle);

            	checkInWorkItem(itemRecord.getID(),
            			itemRecord.getWorkItemData(),
            			new Element(taskinfo.getDecompositionID()),
            			_sessionHandle);
            }

        } catch (Exception e) {
        	e.printStackTrace();
            throw e;
        }

    }
}

