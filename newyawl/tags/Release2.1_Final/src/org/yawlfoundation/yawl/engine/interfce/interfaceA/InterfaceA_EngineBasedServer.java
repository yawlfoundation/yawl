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

package org.yawlfoundation.yawl.engine.interfce.interfaceA;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
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
import java.util.Enumeration;


/**
 * An API between the YAWL Engine and custom services for the management of processes
 * and users.
 *
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 *
 * @author Michael Adams (refactored for v2.0, 06/2008; 12/2008)
 */
public class InterfaceA_EngineBasedServer extends HttpServlet {
    private EngineGateway _engine;
    private static final boolean _debug = false;
    private static final Logger logger = Logger.getLogger(InterfaceA_EngineBasedServer.class);


    public void init() throws ServletException {     

        ServletContext context = getServletContext();

        // read persistence flag from web.xml & get engine instance
        try {
            String persistOn = context.getInitParameter("EnablePersistence") ;
            boolean enablePersist = "true".equalsIgnoreCase(persistOn);

            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                _engine = new EngineGatewayImpl(enablePersist);
                context.setAttribute("engine", _engine);
            }
        } catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);                       // all gets redirected as posts
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


    private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String userID = request.getParameter("userID");
        String password = request.getParameter("password");

        try {
            if (_debug) {
                debug(request, "Post");
            }

            if (action != null) {
                if ("connect".equals(action)) {
                    int interval = request.getSession().getMaxInactiveInterval();
                    msg.append(_engine.connect(userID, password, interval));
                }
                else if ("checkConnection".equals(action)) {
                    msg.append(_engine.checkConnectionForAdmin(sessionHandle));
                }
                else if ("upload".equals(action)) {
                    String specXML = request.getParameter("specXML");
                    msg.append(_engine.loadSpecification(specXML, sessionHandle));
                }
                else if ("getAccounts".equals(action)) {
                    msg.append(_engine.getAccounts(sessionHandle));
                }
                else if ("getAccount".equals(action)) {
                    msg.append(_engine.getClientAccount(userID, sessionHandle));
                }
                else if ("getList".equals(action)) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                }
                else if ("getYAWLServices".equals(action)) {
                    msg.append(_engine.getYAWLServices(sessionHandle));
                }
                else if ("createAccount".equals(action)) {
                    String doco = request.getParameter("doco");
                    msg.append(_engine.createAccount(userID, password, doco, sessionHandle));
                }
                else if ("updateAccount".equals(action)) {
                    String doco = request.getParameter("doco");
                    msg.append(_engine.updateAccount(userID, password, doco, sessionHandle));
                }
                else if ("deleteAccount".equals(action)) {
                    msg.append(_engine.deleteAccount(userID, sessionHandle));
                }
                else if ("newPassword".equals(action)) {
                    msg.append(_engine.changePassword(password, sessionHandle));
                }
                else if ("getPassword".equals(action)) {
                    msg.append(_engine.getClientPassword(userID, sessionHandle));
                }
                else if ("getBuildProperties".equals(action)) {
                    msg.append(_engine.getBuildProperties(sessionHandle));
                }
                else if ("newYAWLService".equals(action)) {
                    String serviceStr = request.getParameter("service");
                    msg.append(_engine.addYAWLService(serviceStr, sessionHandle));
                }
                else if ("removeYAWLService".equals(action)) {
                    String serviceURI = request.getParameter("serviceURI");
                    msg.append(_engine.removeYAWLService(serviceURI, sessionHandle));
                }
                else if ("getExternalDBGateways".equals(action)) {
                    msg.append(_engine.getExternalDBGateways(sessionHandle));
                }

                else if ("unload".equals(action)) {
                    String specIdentifier = request.getParameter("specidentifier");
                    String version = request.getParameter("specversion");
                    String uri = request.getParameter("specuri");
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, version, uri);
                    msg.append(_engine.unloadSpecification(specID, sessionHandle));
                }
            }
        }
        catch (Exception e) {
            logger.error("Exception in Interface B with action: " + action, e);
        }
        if (msg.length() == 0) {
            msg.append("<failure><reason>Invalid action or exception was thrown." +
                       "</reason></failure>");
        }
        if (_debug) {
            logger.debug("return = " + msg);
        }
        return msg.toString();
    }


    private void debug(HttpServletRequest request, String service) {
        logger.debug("\nInterfaceA_EngineBasedServer::do" + service + "() " +
                "request.getRequestURL = " + request.getRequestURL());
        logger.debug("\nInterfaceA_EngineBasedServer::do" + service +
                "() request.parameters = ");
        Enumeration paramNms = request.getParameterNames();
        while (paramNms.hasMoreElements()) {
            String name = (String) paramNms.nextElement();
            logger.debug("\trequest.getParameter(" + name + ") = " +
                    request.getParameter(name));
        }
    }
}


