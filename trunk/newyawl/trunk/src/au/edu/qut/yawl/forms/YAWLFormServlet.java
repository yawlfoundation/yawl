/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.forms;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.WorklistController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
//import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;

/**
 * This is the  servlet  that receives POST  submissions from  3rd-party applications.  If
 * launching a case in YAWL then the servlet sets up a RequestDispatcher to the LaunchCase
 * jsp and sends it the case data along with the spec ID. Otherwise if editing a work item
 * then  the servlet sets  up a  RequestDispatcher to the  WorkItemProcessor jsp page  and
 * transfers  the XML in  the  POST  input  stream  into  work item  output  that the YAWL
 * WorkListController can check in.
 * @author Guy Redding 20/05/2005
 */
public class YAWLFormServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private WorklistController _worklistController = null;
    private boolean debug = false; // TODO log4j 


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	if (debug) System.out.println("--- YAWLFormServlet ---");
    	
        //HttpSession session = request.getSession(true);
        String inputData = null;
        String outputData = null;

        response.setContentType("text/html");

        StringBuffer theInstanceData = new StringBuffer();
        ServletInputStream in = request.getInputStream();

        int i = in.read();
        while (i != -1) {
            theInstanceData.append((char) i);
            i = in.read();
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
        String userid = request.getParameter("userID");

        request.setAttribute("sessionHandle", sessionHandle);
        request.setAttribute("userid", userid);
        
        if (theInstanceData != null) {
        	if (workItemID.compareTo("null") != 0) {
	    			
                WorkItemRecord workitem = _worklistController.getCachedWorkItem(workItemID);
                
                if (workitem != null) {
	                inputData = workitem.getDataListString();
	                outputData = new String(theInstanceData);
                }
                System.out.println("XFormOUTPUT: " + outputData);
                System.out.println("XFormInput: " + inputData);
                Element inputDataEl = null;
                Element outputDataEl = null;
                SAXBuilder _builder = new SAXBuilder();
                
                try {
                    Document inputDataDoc = _builder.build(new StringReader(inputData));
                    inputDataEl = inputDataDoc.getRootElement();

                    Document outputDataDoc = _builder.build(new StringReader(outputData));
                    outputDataEl = outputDataDoc.getRootElement();
                } catch (JDOMException e) {
                    e.printStackTrace();
                }
                
                workitem = null;
                _worklistController = null; 
                request.setAttribute("inputData", inputDataEl);
                request.setAttribute("outputData", outputDataEl);
                request.setAttribute("workItemID", workItemID);
                request.setAttribute("submit", "Submit Work Item");
                RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
                rd.forward(request, response);
            }
    		else if (specID.compareTo("null") != 0) {
                inputData = new String(theInstanceData);
                request.setAttribute("caseData", inputData);
                request.setAttribute("specID", specID);
                RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/launchCase");
                rd.forward(request, response);
            }
            else {
                if (debug) System.out.println("Both workItemID and specID were 'null'. ");
            }
        } 
        else {
        	if (debug) System.out.println("theInstanceData = " + theInstanceData);
        }
    }
}