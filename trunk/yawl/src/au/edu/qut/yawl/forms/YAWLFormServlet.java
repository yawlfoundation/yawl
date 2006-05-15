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
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This is the  servlet  that receives POST  submissions from  3rd-party applications.  If
 * launching a case in YAWL then the servlet sets up a RequestDispatcher to the LaunchCase
 * jsp and sends it the case data along with the spec id. Otherwise if editing a work item
 * then  the servlet sets  up a  RequestDispatcher to the  WorkItemProcessor jsp page  and
 * transfers  the XML in  the  POST  input  stream  into  work item  output  that the YAWL
 * WorkListController can check in.
 * @author Guy Redding 20/05/2005
 */
public class YAWLFormServlet extends HttpServlet {

    private WorklistController _worklistController = null;
    private boolean debug = false;


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
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

        String specID = request.getHeader("specID");
        String workItemID = request.getHeader("workItemID");
        String sessionHandle = request.getHeader("sessionHandle");
        String userid = request.getHeader("userid");

        if (debug) {
            System.out.println("case specID = '" + specID + "'");
            System.out.println("workItemID = '" + workItemID + "'");
            System.out.println("sessionHandle = '" + sessionHandle + "'");
            System.out.println("userid = '" + userid + "'");
        }

        if (theInstanceData != null) { // and what if theInstanceData is null?

            // removes the instance header encoding, since it causes problems in the YAWL check-in
            int headerstart = theInstanceData.indexOf("<?");
            int headerstop = theInstanceData.indexOf("?>") + 2;

            theInstanceData.delete(headerstart, headerstop);

            // deletes any attributes belonging to the root element that
            // have been inserted by chiba, assumes that the root tagname has not
            // been changed by this or any other process in the meantime. --> needs a fix
            int start = theInstanceData.indexOf("<" + SchemaCreator.getRootTagName()) + SchemaCreator.getRootTagName().length() + 1;

            int stop = theInstanceData.indexOf("/>");

            // in case the root element doesn't end with "/>"
            if (stop < start) {
                stop = theInstanceData.indexOf(">");
            }

            theInstanceData.delete(start, stop);

            if (workItemID.compareTo("") != 0) {
                WorkItemRecord workitem = _worklistController.getRemotelyCachedWorkItem(workItemID);
                inputData = workitem.getDataListString();
                outputData = new String(theInstanceData);

                workitem = null;
                _worklistController = null;
            } else if (specID.compareTo("") != 0) {
                inputData = new String(theInstanceData);
                if (debug) {
                    System.out.println("case inputData = " + inputData);
                }
            }
        }

        request.setAttribute("sessionHandle", sessionHandle);
        request.setAttribute("userid", userid);

        if (specID.compareTo("") != 0) {
            request.setAttribute("caseData", inputData);
            request.setAttribute("specID", specID);

            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/launchCase");
            rd.forward(request, response);
        } else if (workItemID.compareTo("") != 0) {
            request.setAttribute("inputData", inputData);
            request.setAttribute("outputData", outputData);
            request.setAttribute("workItemID", workItemID);
            request.setAttribute("submit", "Submit Work Item");

            RequestDispatcher rd = getServletConfig().getServletContext().getRequestDispatcher("/workItemProcessor");
            rd.forward(request, response);
        }
    }
}