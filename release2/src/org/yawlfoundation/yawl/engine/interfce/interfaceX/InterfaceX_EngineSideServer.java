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

package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.EngineGateway;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;


/**
 *  InterfaceX_EngineSideServer receives posts from the exception service and passes
 *  them as method calls to the Engine.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EngineBasedServer was used as a template for this class.
 *
  *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_EngineSideServer extends HttpServlet {

    private EngineGateway _engine;
    private static final Logger logger = Logger.getLogger(InterfaceX_EngineSideServer.class);


    public void init() throws ServletException {
        ServletContext context = getServletContext();

        try {
            // get reference to engine
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {

                // turn on persistence if required
                String persistOn = context.getInitParameter("EnablePersistence");
                boolean persist = "true".equalsIgnoreCase(persistOn);
                _engine = new EngineGatewayImpl(persist);
                context.setAttribute("engine", _engine);
            }
            // add interface X monitoring if required
            String listenerURI = context.getInitParameter("InterfaceXListener");
            if (listenerURI != null) {
                for (String uri : listenerURI.split(";")) {
                    _engine.addInterfaceXListener(uri);
                }    
            }

        }
        catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        ServletUtils.finalizeResponse(outputWriter, output);
    }


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################

    
    // pass the POST request as a method call to the engine
    private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();

        // unpack the params
        String action = request.getParameter("action");
        String sessionHandle = request.getParameter("sessionHandle");
        String workitemID  = request.getParameter("workitemID");
        String data = request.getParameter("data");

        // call the specified method
        try {
            if ("addInterfaceXListener".equals(action)) {
                String listenerURI = request.getParameter("listenerURI");
                msg.append(_engine.addInterfaceXListener(listenerURI));
            }
            else if ("removeInterfaceXListener".equals(action)) {
                String listenerURI = request.getParameter("listenerURI");
                msg.append(_engine.removeInterfaceXListener(listenerURI));
            }
            else if ("updateWorkItemData".equals(action)) {
                msg.append(_engine.updateWorkItemData(workitemID, data, sessionHandle));
            }
            else if ("updateCaseData".equals(action)) {
                String caseID = request.getParameter("caseID");
                msg.append(_engine.updateCaseData(caseID, data, sessionHandle));
            }
            else if ("completeWorkItem".equals(action)) {
                String logPredicate = request.getParameter("logPredicate");
                msg.append(_engine.completeWorkItem(workitemID, data, logPredicate,
                        true, sessionHandle));
            }
            else if ("continueWorkItem".equals(action)) {
                msg.append(_engine.startWorkItem(workitemID, sessionHandle));
            }
            else if ("unsuspendWorkItem".equals(action)) {
                msg.append(_engine.unsuspendWorkItem(workitemID, sessionHandle));
            }
            else if ("restartWorkItem".equals(action)) {
                msg.append(_engine.restartWorkItem(workitemID, sessionHandle));
            }
            else if ("startWorkItem".equals(action)) {
                msg.append(_engine.startWorkItem(workitemID, sessionHandle));
            }
            else if ("cancelWorkItem".equals(action)) {
                String fail = request.getParameter("fail");
                msg.append(_engine.cancelWorkItem(workitemID, fail, sessionHandle));
            }
        }
        catch (RemoteException re) {
            logger.error("Remote Exception when calling engine", re);
        }
        return msg.toString();
    }

}
