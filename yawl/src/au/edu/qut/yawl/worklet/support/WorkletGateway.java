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

import org.apache.log4j.Logger;

 /**
  *  The WorkletGateway class acts as a gateway between the Worklet
  *  Service and the external RDREditor. As of this release, the only 
  *  functionality it provides is to trigger a running worklet replacement
  *  due to an addtion to the ruleset (by the editor), but future
  *  implementations may extend this gateway for other purposes.
  *
  *  @author Michael Adams
  *  BPM Group, QUT Australia
  *  m3.adams@qut.edu.au
  *  v0.7, 10/12/2005
  */

public class WorkletGateway extends HttpServlet {

    private static Logger _log = Logger.getLogger(
                                      "au.edu.qut.yawl.worklet.support.WorkletGateway");


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {

        _log.info("Received a request from the RDR Editor to replace " +
    	           "a running worklet.");

    	try {

            // workitem id for replacing passed with the request
    		String itemid = req.getQueryString() ;

    		// get the service instance and call replace
     	  	WorkletService ws = WorkletService.getInstance() ;
    	 	String result = ws.replaceWorklet(itemid) ;   	
    	
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
