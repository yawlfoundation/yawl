/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.forms;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
 
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.WorklistController;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;

/**
 * This servlet receives GET and POST submissions from 3rd-party applications.  If
 * launching a case in YAWL then the servlet sets up a RequestDispatcher to the LaunchCase
 * jsp and sends it the case data along with the spec ID. Otherwise if editing a work item
 * then  the servlet sets  up a  RequestDispatcher to the  WorkItemProcessor jsp page  and
 * transfers  the XML in  the  POST  input  stream  into  work item  output  that the YAWL
 * WorkListController can check in.
 * 
 * @author Guy Redding 20/05/2005
 */
public class YAWLFormServlet extends HttpServlet {
	
	private WorklistController _worklistController = null;
	private static Logger logger = Logger.getLogger(YAWLFormServlet.class);
	
	
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	HttpSession session = request.getSession(true);
    	
    	System.out.println("YFS doGet");
    	
        String inputData = new String();
        String outputData = new String();

        String submit = request.getParameter("submit");
        
        StringBuffer theInstanceData = null;
        
        if (submit.compareTo("htmlForm") == 0){
        	theInstanceData = new StringBuffer((String) session.getAttribute("inputData"));
        	
        	System.out.println("session attribute: "+theInstanceData); 
        }
        else{
	        ServletInputStream in = request.getInputStream();
	        theInstanceData = new StringBuffer();
	        
	        int i = in.read();
	        while (i != -1) {
	            theInstanceData.append((char) i);
	            i = in.read();
	        }
	        System.out.println("YFS XFORM theInstanceData: "+theInstanceData);
        }
	        
        ServletContext context = getServletContext();
        _worklistController = (WorklistController) context.getAttribute(
                "au.edu.qut.yawl.worklist.model.WorklistController");
         
        if (_worklistController == null) {
            _worklistController = new WorklistController();
            _worklistController.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
            _worklistController.setUpInterfaceAClient(context.getInitParameter("InterfaceA_BackEnd"));
            context.setAttribute("au.edu.qut.yawl.worklist.model.WorklistController",
                    _worklistController);
        }
        
        String specID = request.getParameter("specID");
        String workItemID = request.getParameter("workItemID");
        String sessionHandle = request.getParameter("sessionHandle");
        String userID = request.getParameter("userID");
        
        request.setAttribute("sessionHandle", sessionHandle);
        request.setAttribute("userid", userID);
        
        Element inputDataEl = null;
        Element outputDataEl = null;
        
        if (workItemID.compareTo("null") != 0) {
        	if (theInstanceData != null){
        		
	            WorkItemRecord workitem = _worklistController.getCachedWorkItem(workItemID);
	            
	            if (workitem == null){
	            	System.out.println("***YFS WorkItemRecord not found.***");
	            }
	            else{
		            inputData = workitem.getDataListString();
		            outputData = new String(theInstanceData);
/*		            
		            System.out.println("YFS G Input Data: "+inputData);
		            System.out.println("YFS G Output Data: "+outputData);
		            
		            File xmlInput = new File("xmlGetInput.xml");
		            FileWriter fwi = new FileWriter(xmlInput);
		            System.out.println(xmlInput.getPath());
		            fwi.write(inputData);
		            fwi.close();
		            
		            File xmlOutput = new File("xmlGetOutput.xml");
		            FileWriter fwo = new FileWriter(xmlOutput);
		            fwo.write(outputData);
		            fwo.close();
*/	            
		            SAXBuilder _builder = new SAXBuilder();
		            
		            try {
		                Document inputDataDoc = _builder.build(new StringReader(inputData));
		                inputDataEl = inputDataDoc.getRootElement();
		                
		                Document outputDataDoc = _builder.build(new StringReader(outputData));
		                outputDataEl = outputDataDoc.getRootElement();
		            } catch (JDOMException e) {
		                e.printStackTrace();
		                
	                    // due to JDOM exception can't submit a workitem, just forward an empty request
	                    RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
	                    rd.forward(request, response);
	                    return;
		            }
	            }
        	}
       }
        
        if (submit.equals("submit") || submit.equals("htmlForm")){
        	
        	System.out.println("YFS submit || htmlForm");
        	
        	if (workItemID.compareTo("null") != 0) {        		
	            request.setAttribute("inputData", inputDataEl); // check for null?
	            request.setAttribute("outputData", outputDataEl);
	            request.setAttribute("workItemID", workItemID);
	            request.setAttribute("submit", "Submit Work Item");
	            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
	            rd.forward(request, response);
	            return;
	        }
			else if (specID.compareTo("null") != 0) {
	            inputData = new String(theInstanceData);
	            request.setAttribute("caseData", inputData);
	            request.setAttribute("specID", specID);	            
	            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/launchCase");
	            rd.forward(request, response);
	            return;
	        }
            else {
                logger.debug("Both workItemID and specID were 'null'. ");
            }
        }
        else if (submit.equals("cancel")) {
        	request.setAttribute("submit", "Cancel");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/availableWork");
            rd.forward(request, response);
            return;
        }
        else if (submit.equals("suspend")) {
        	request.setAttribute("outputData", outputDataEl);
            request.setAttribute("workItemID", workItemID);
        	request.setAttribute("submit", "Suspend Task");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
            rd.forward(request, response);
            return;
        }
        else if (submit.equals("save")) {        	
        	request.setAttribute("outputData", outputData);
            request.setAttribute("workItemID", workItemID);
        	request.setAttribute("submit", "Save Work Item");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
            rd.forward(request, response);
            return;
        }
        else {
        	logger.debug("theInstanceData = " + theInstanceData);
        }
    }
	
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	System.out.println("YFS doPost");
    	
        String inputData = new String();
        String outputData = new String();

        String submit = request.getParameter("submit");
        
        StringBuffer theInstanceData = null;
        
        if (submit.compareTo("htmlForm") == 0){
        	theInstanceData = new StringBuffer(request.getParameter("inputData"));
        	System.out.println("YFS HTMLFORM");
        	System.out.println("YFS inputData: ."+theInstanceData+".");
        }
        else{
	        ServletInputStream in = request.getInputStream();
	        theInstanceData = new StringBuffer();
	        
	        int i = in.read();
	        while (i != -1) {
	            theInstanceData.append((char) i);
	            i = in.read();
	        }
        	System.out.println("YFS theInstanceData: "+theInstanceData);
        }
	        
        ServletContext context = getServletContext();
        _worklistController = (WorklistController) context.getAttribute(
                "au.edu.qut.yawl.worklist.model.WorklistController");
         
        if (_worklistController == null) {
            _worklistController = new WorklistController();
            _worklistController.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
            _worklistController.setUpInterfaceAClient(context.getInitParameter("InterfaceA_BackEnd"));
            context.setAttribute("au.edu.qut.yawl.worklist.model.WorklistController",
                    _worklistController);
        }
        
        String specID = request.getParameter("specID");
        String workItemID = request.getParameter("workItemID");
        String sessionHandle = request.getParameter("sessionHandle");
        String userID = request.getParameter("userID");
        
        request.setAttribute("sessionHandle", sessionHandle);
        request.setAttribute("userid", userID);
        
        Element inputDataEl = null;
        Element outputDataEl = null;
        
        if (workItemID.compareTo("null") != 0) {
        	if (theInstanceData != null){
        		
	            WorkItemRecord workitem = _worklistController.getCachedWorkItem(workItemID);
	            
	            if (workitem == null){
	            	System.out.println("***YFS WorkItemRecord not found.***");
	            }
	            else{
		            inputData = workitem.getDataListString();
		            outputData = new String(theInstanceData);
/*		            
		            System.out.println("YFS P Input Data: "+inputData);
		            System.out.println("YFS P Output Data: "+outputData);
		            
		            File xmlInput = new File("xmlPostInput.xml");
		            FileWriter fwi = new FileWriter(xmlInput);
		            System.out.println(xmlInput.getPath());
		            fwi.write(inputData);
		            fwi.close();
		            
		            File xmlOutput = new File("xmlPostOutput.xml");
		            FileWriter fwo = new FileWriter(xmlOutput);
		            fwo.write(outputData);
		            fwo.close();
*/		            
		            SAXBuilder _builder = new SAXBuilder();
		            
		            try {
		                Document inputDataDoc = _builder.build(new StringReader(inputData));
		                inputDataEl = inputDataDoc.getRootElement();
		                
		                Document outputDataDoc = _builder.build(new StringReader(outputData));
		                outputDataEl = outputDataDoc.getRootElement();
		            } catch (JDOMException e) {
		                e.printStackTrace();
		                
	                    // due to exception can't submit a workitem, just forward an empty request
	                    RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
	                    rd.forward(request, response);
	                    return;
		            }
	            }
        	}
       }
        
        if (submit.equals("submit") || submit.equals("htmlForm")){
        	
        	System.out.println("YFS submit || htmlForm");
        	
        	if (workItemID.compareTo("null") != 0) {        		
	            request.setAttribute("inputData", inputDataEl); // check for null?
	            request.setAttribute("outputData", outputDataEl);
	            request.setAttribute("workItemID", workItemID);
	            request.setAttribute("submit", "Submit Work Item");
	            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
	            rd.forward(request, response);
	            return;
	        }
			else if (specID.compareTo("null") != 0) {
	            inputData = new String(theInstanceData);
	            request.setAttribute("caseData", inputData);
	            request.setAttribute("specID", specID);	            
	            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/launchCase");
	            rd.forward(request, response);
	            return;
	        }
            else {
                logger.debug("Both workItemID and specID were 'null'. ");
            }
        }
        else if (submit.equals("cancel")) {
        	request.setAttribute("submit", "Cancel");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/availableWork");
            rd.forward(request, response);
            return;
        }
        else if (submit.equals("suspend")) {
        	request.setAttribute("outputData", outputDataEl);
            request.setAttribute("workItemID", workItemID);
        	request.setAttribute("submit", "Suspend Task");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
            rd.forward(request, response);
            return;
        }
        else if (submit.equals("save")) {        	
        	request.setAttribute("outputData", outputDataEl);
            request.setAttribute("workItemID", workItemID);
        	request.setAttribute("submit", "Save Work Item");
            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
            rd.forward(request, response);
            return;
        }
        else {
        	logger.debug("theInstanceData = " + theInstanceData);
        }
    }
}