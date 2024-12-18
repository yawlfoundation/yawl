/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.mailService;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;


/**
 *  Initialises the Simple Mail Service with values from 'web.xml'.
 *
 *  @author Michael Adams
 *  @date 24/06/2011
 *
 */

public class MailServiceGateway extends YHttpServlet {

    private Sessions _sessions;            // maintains sessions with external services

    /** Read settings from web.xml and use them to initialise the service */
    public void init() {

        // set up session connections
        _sessions = new Sessions();
        _sessions.setupInterfaceA(
                getServletContext().getInitParameter("InterfaceA_Backend"),
                getServletContext().getInitParameter("EngineLogonUserName"),
                getServletContext().getInitParameter("EngineLogonPassword"));

        try {
            MailService service = MailService.getInstance();
            service.setHost(getSetting("host"));
            service.setUser(getSetting("mailUserName"));
            service.setPassword(getSetting("mailPassword"));
            service.setFromName(getSetting("senderName"));
            service.setFromAddress(getSetting("senderAddress"));
            service.setPort(StringUtil.strToInt(getSetting("port"), 25));
            service.setTransportStrategy(getSetting("transportStrategy"));
        }
        catch (Exception e) {
            LogManager.getLogger(MailServiceGateway.class).error(
                    "Simple Mail Service Initialisation Exception", e);
        }
    }


    public void destroy() {
        if (_sessions != null) _sessions.shutdown();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        MailService service = MailService.getInstance();
        String result = "<success/>";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");

        if (action == null) {
            result = "<html><head>" +
                    "<title>YAWL Mail Service</title>" +
                    "</head><body>" +
                    "<H3>Welcome to the YAWL Mail Service \"Gateway\"</H3>" +
                    "<p> The Mail Service Gateway acts as a bridge between the Mail " +
                    "Service and client services to enable send emails).</p>" +
                    "</body></html>";
        }
        else if (action.equalsIgnoreCase("connect")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            result = _sessions.connect(userid, password);
        }
        else if (action.equalsIgnoreCase("checkConnection")) {
            result = String.valueOf(_sessions.checkConnection(handle));
        }
        else if (action.equals("disconnect")) {
            result = String.valueOf(_sessions.disconnect(handle));
        }
        else if (_sessions.checkConnection(handle)) {
            if (action.equalsIgnoreCase("sendMail")) {
                String xml = req.getParameter("xml");
                if (xml != null) {
                    result = service.sendMail(xml);
                }
                else {
                    String toName = req.getParameter("toName");
                    String toAddress = req.getParameter("toAddress");
                    String ccAddress = req.getParameter("ccAddress");
                    String bccAddress = req.getParameter("bccAddress");
                    String subject = req.getParameter("subject");
                    String content = req.getParameter("content");
                    if (toAddress == null) {
                        result = nullMsg("recipient address");
                    }
                    else if (subject == null) {
                        result = nullMsg("subject");
                    }
                    else if (content == null) {
                        result = nullMsg("content");
                    }
                    else {
                        result = service.sendMail(toName, toAddress, ccAddress,
                                bccAddress, subject, content);
                    }
                }
            }
            else if (action.equalsIgnoreCase("setHost")) {
                String host = req.getParameter("host");
                if (host != null) {
                    service.setHost(host);
                }
                else result = nullMsg("host");
            }
            else if (action.equalsIgnoreCase("setPort")) {
                String portStr = req.getParameter("port");
                if (portStr != null) {
                    int port = StringUtil.strToInt(portStr, 0);
                    if (port != 0) {
                        service.setPort(port);
                    }
                    else result = errMsg("Value for port parameter must be numeric");
                }
                else result = nullMsg("port");
            }
            else if (action.equalsIgnoreCase("setSenderUserid")) {
                String userid = req.getParameter("userid");
                if (userid != null) {
                    service.setUser(userid);
                }
                else result = nullMsg("sender userid");
            }
            else if (action.equalsIgnoreCase("setSenderName")) {
                String name = req.getParameter("name");
                if (name != null) {
                    service.setFromName(name);
                }
                else result = nullMsg("sender name");
            }
            else if (action.equalsIgnoreCase("setSenderPassword")) {
                String password = req.getParameter("password");
                if (password != null) {
                    service.setPassword(password);
                }
                else result = nullMsg("sender password");

            }
            else if (action.equalsIgnoreCase("setSenderAddress")) {
                String address = req.getParameter("address");
                if (address != null) {
                    service.setFromAddress(address);
                }
                else result = nullMsg("sender address");
            }
            else if (action.equalsIgnoreCase("setTransportStrategy")) {
                String strategy = req.getParameter("strategy");
                if (strategy != null) {
                    if (List.of("PLAIN", "SSL", "TLS").contains(strategy.toUpperCase())) {
                        service.setTransportStrategy(strategy);
                    }
                    else result = errMsg("Unsupported transport strategy: " + strategy);
                }
                else result = nullMsg("host");
            }
        }
        else errMsg("Invalid or disconnected session handle");

        // generate the output
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
        ServletUtils.finalizeResponse(outputWriter, result);
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }


    private String getSetting(String name) {
        String value = System.getProperty("yawl.mail." + name);
        if (value == null) {
            value = getServletContext().getInitParameter(name);
        }
        return value;
    }


    private String nullMsg(String param) {
        return errMsg("Value for " + param + "parameter cannot be null");
    }


    private String errMsg(String msg) {
        return StringUtil.wrap(msg, "error");
    }

}
