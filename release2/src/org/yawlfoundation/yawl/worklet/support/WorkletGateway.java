/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.worklet.support;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
  *  The WorkletGateway class acts as a gateway between the Worklet Selection
  *  Service and the external RDREditor. It initialises the service with values from
  *  'web.xml' and provides functionality to trigger a running worklet replacement
  *  due to an addtion to the ruleset (by the editor). Future
  *  implementations may extend this gateway for other purposes.
  *
  *  @author Michael Adams
  *  v0.8, 13/08/2006
  */

public class WorkletGateway extends HttpServlet {

     private static Logger _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.support.WorkletGateway");

     public void init() {
         if (! Library.wsInitialised) {
             try {
                 ServletContext context = getServletContext();

                 Library.setHomeDir(context.getRealPath("/"));
                 Library.setRepositoryDir(context.getInitParameter("Repository"));

                 String persistStr = context.getInitParameter("EnablePersistence");
                 Library.setPersist(persistStr.equalsIgnoreCase("TRUE"));

                 String engineURI = context.getInitParameter("InterfaceB_BackEnd");
                 WorkletService.getInstance().initEngineURI(engineURI) ;
         
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


    public void doPost(HttpServletRequest req, HttpServletResponse res)
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
                _log.info("Received a request from the Rules Editor to replace " +
                          "a running worklet.");

                String itemID = req.getParameter("itemID");
                int exType = Integer.parseInt(req.getParameter("exType"));

                // get the service instance and call replace
                if (exType == WorkletService.XTYPE_SELECTION) {
            	  	  WorkletService ws = WorkletService.getInstance() ;
        	    	    result = ws.replaceWorklet(itemID) ;
                }
                else {
                    String caseID = req.getParameter("caseID");
                    String trigger = req.getParameter("trigger");
                    ExceptionService ex = ExceptionService.getInst();
                    result = ex.replaceWorklet(exType, caseID, itemID, trigger);
                }
            }
            else if (action.equalsIgnoreCase("refresh")) {
                String specID = req.getParameter("specid");
                WorkletService ws = WorkletService.getInstance() ;
                ws.refreshRuleSet(specID);
            }

            // generate the output
            OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
            ServletUtils.finalizeResponse(outputWriter, result);

         }
    	 catch (Exception e) {
    	 	_log.error("Exception in doPost()", e);
    	 }
	}


     public void doGet(HttpServletRequest req, HttpServletResponse res)
                                 throws IOException, ServletException {
         doPost(req, res);
     }
}
