package au.edu.qut.yawl.forms;

import java.util.List;

//import javax.servlet.RequestDispatcher;

import org.jdom.Element;

import au.edu.qut.yawl.engine.interfce.AuthenticationConfig;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
//import au.edu.qut.yawl.worklist.WorkItemProcessor;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
//import au.edu.qut.yawl.worklist.model.WorklistController;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedServer; 

public class JSPFormsService extends InterfaceBWebsideController{

	private String sessionHandle = new String();
	
	// get checked out workitem and redirect to the specified JSP Form.
	// information needed:
	// 1) name of form
	// 2) location of form
	// 3) caseID
	// 4) taskID
	// 5) userID
	// 6) submission type (ie: submit, save, suspend, cancel, edit)
	public void handleEnabledWorkItemEvent(WorkItemRecord _wir){
		
		InterfaceB_EnvironmentBasedServer ib;
		
/*		WorkItemProcessor wip = new WorkItemProcessor();
		WorklistController _worklistController = new WorklistController();
		
        _worklistController.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
        _worklistController.setUpInterfaceAClient(context.getInitParameter("InterfaceA_BackEnd"));
        context.setAttribute("au.edu.qut.yawl.worklist.model.WorklistController",
                _worklistController);
*/		
		try {
            if (!checkConnection(sessionHandle)) {
                sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (!successful(sessionHandle)) {
                _logger.error("Unsuccessful");
            } 
            else {
                WorkItemRecord child = checkOut(_wir.getID(), sessionHandle);

                if (child != null) {
                    List children = super.getChildren(_wir.getID(), sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        if (WorkItemRecord.statusFired.equals(itemRecord.getStatus())) {
                            checkOut(itemRecord.getID(), sessionHandle);
                        }
                    }
                    children = super.getChildren(_wir.getID(), sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        super._model.addWorkItem(itemRecord);

                        Element inputData = itemRecord.getWorkItemData();
                        Element element = (Element) inputData.getChildren().get(0);


                    }
                }
            }
/*
			try{
				wip.executeCasePost(null, _wir.getSpecificationID(), sessionHandle,
					_worklistController, _wir.userID, null);

				String url = wip.getRedirectURL(null, _wir.specData, sessionHandle);
				
                RequestDispatcher requestDispatcher =
                    application.getRequestDispatcher("/instanceAdder");
                requestDispatcher.forward(request, response);
				
				//response.sendRedirect(response.encodeURL(url));
			}
	
			catch(Exception e){
				e.printStackTrace();
			}
*/
        }
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord){
		
	}
	
	
	public void setRemoteAuthenticationDetails(String userName, String password,
			String httpProxyHost, String proxyPort){
		
        AuthenticationConfig auth = AuthenticationConfig.getInstance();
        auth.setProxyAuthentication(userName, password, httpProxyHost, proxyPort);
	}
	
}
