package au.edu.qut.yawl.PDFforms;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.engine.interfce.*;
import au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution.*;
import java.io.IOException;
import java.util.Map;
import org.jdom.JDOMException;
import org.jdom.Element;
import javax.servlet.*;
import javax.servlet.http.*;


import au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution.InterfaceD_Server;


public class PDFserver implements InterfaceD_Controller {

    protected static final String DEFAULT_USERNAME = "admin";
    protected static final String DEFAULT_PASSWORD = "YAWL";    

    private String sessionHandle = null;

    public Element processWorkItem(WorkItemRecord workitem) {
/*
	
	//setUpInterfaceBClient("http://localhost:8080/yawl/ib");
	
	try {
	    //	    if (sessionHandle==null || !checkConnection(sessionHandle)) {	    
	    //sessionHandle = connect(DEFAULT_USERNAME,DEFAULT_PASSWORD);
	    //}
	       
	    System.out.println("Checking In data: " + workitem.getWorkItemData());
	    //System.out.println(sessionHandle);
	    
	    System.out.println(workitem.getID());
	    
	    request.setAttribute("inputData", workitem.getWorkItemData());
	    request.setAttribute("outputData", workitem.getWorkItemData());
	    request.setAttribute("workItemID", workItem.getID());
	    request.setAttribute("submit", "Submit Work Item");
	    RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
	    rd.forward(request, response);

	} catch (Exception e) {
	    e.printStackTrace();
	}
*/
	return null;
    }

    

}