/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.support;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import au.edu.qut.yawl.worklet.WorkletService;
import au.edu.qut.yawl.worklet.exception.ExceptionService;

import org.apache.log4j.Logger;

 /**
  *  The WorkletGateway class acts as a gateway between the Worklet Selection
  *  Service and the external RDREditor. It initialises the service with values from
  *  'web.xml' and provides functionality to trigger a running worklet replacement
  *  due to an addtion to the ruleset (by the editor). Future
  *  implementations may extend this gateway for other purposes.
  *
  *  @author Michael Adams
  *  BPM Group, QUT Australia
  *  m3.adams@qut.edu.au
  *  v0.8, 13/08/2006
  */

public class WorkletGateway extends HttpServlet {

     private static Logger _log = Logger.getLogger("au.edu.qut.yawl.worklet.support.WorkletGateway");

     public void init() {
         if (! Library.wsInitialised) {
             try {
                 ServletContext context = getServletContext();

                 Library.setHomeDir(context.getRealPath("/"));
                 Library.setRepositoryDir(context.getInitParameter("Repository"));

                 String persistStr = context.getInitParameter("EnablePersistance");
                 Library.setPersist(persistStr.equalsIgnoreCase("TRUE"));
         
                 WorkletService.getInstance().completeInitialisation();
                 ExceptionService.getInst().completeInitialisation();
             }
             catch (Exception e) {
                 _log.error("Gateway Initialisation Exception", e);
             }
             finally {
                 Library.setServicetInitialised();
             }
         }
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {

        String result = "";

        try {
            String action = req.getParameter("action");
            if (action == null) {
       		    result = "<html><head>" +
		        "<title>Worklet Dynamic Process Selection and Exception Service</title>" +
		        "</head><body>" +
		        "<H3>Welcome to the Worklet Dynamic Process Selection and " +
                "Exception Service \"Gateway\"</H3>" +
                "<p> The Worklet Gateway acts as a bridge between the Worklet " +
                    "Service and the external RDREditor (it isn't meant to be browsed " +
                    " to directly). It provides the " +
                    "functionality to trigger a running worklet replacement " +
                    "due to an addtion to the ruleset (by the editor).</p>" +
                "</body></html>";
            }
            else if (action.equalsIgnoreCase("replace")) {
                _log.info("Received a request from the RDR Editor to replace " +
                          "a running worklet.");

                String caseID = req.getParameter("caseID");
                String itemID = req.getParameter("itemID");
                String trigger = req.getParameter("trigger");
                int exType = Integer.parseInt(req.getParameter("exType"));

                // get the service instance and call replace
                if (exType == WorkletService.XTYPE_SELECTION) {
            	  	WorkletService ws = WorkletService.getInstance() ;
        	    	result = ws.replaceWorklet(itemID) ;
                }
                else {
                    ExceptionService ex = ExceptionService.getInst();
                    result = ex.replaceWorklet(exType, caseID, itemID, trigger);
                }
            }

            // generate the output
	    	res.setContentType("text/html");
	        PrintWriter out = res.getWriter();
	        out.write(result);   
	        out.flush();
	        out.close();
         }
    	 catch (Exception e) {
    	 	_log.error("Exception in doGet()", e);
    	 }
	}


     public void doPost(HttpServletRequest req, HttpServletResponse res)
                                 throws IOException, ServletException {

         String itemid = req.getQueryString() ;
          _log.info("The workitem id passed is: " + itemid);

     }
}
