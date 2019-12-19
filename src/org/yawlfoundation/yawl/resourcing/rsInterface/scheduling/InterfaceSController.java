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

package org.yawlfoundation.yawl.resourcing.rsInterface.scheduling;

import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *  InterfaceSController passes scheduling event calls from the resource service to a
 *  listening scheduling service. This class is a member class of Interface S.
 *
 *  InterfaceB_EnvironmentBasedServer was used as a template for this class.

 *  @author Michael Adams
 *  @date 15/10/2010
 */

public class InterfaceSController extends HttpServlet {

    private InterfaceS_Service _controller;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName = context.getInitParameter("InterfaceS_Service");
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceS_Service) controllerClass.newInstance();
            context.setAttribute("controller", _controller);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
                                                 throws IOException, ServletException {
        _controller.doGet(request, response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/xml; charset=UTF-8");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }

    
    // receives the post and calls the specified Exception service method
    private String processPostQuery(HttpServletRequest request) {

        // unpack the strings
        String xml = request.getParameter("xml");

        switch (actionToNotifyType(request.getParameter("action"))) {
            case ResourceGatewayServer.NOTIFY_UTILISATION_STATUS_CHANGE:
               _controller.handleUtilisationStatusChangeEvent(xml);
               break;
            default: return "<failure>Unknown action: '" + request.getParameter("action") +
                            "'</failure>";
        }
        return "<success/>";
    }


    /** @return the 'action' converted to an 'int' notify type, or -1 if invalid */
    private int actionToNotifyType(String action) {
        try {
            return Integer.parseInt(action);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


    private long strToLong(String s) {
        if (s == null) return -1;
        try {
            return new Long(s);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


}