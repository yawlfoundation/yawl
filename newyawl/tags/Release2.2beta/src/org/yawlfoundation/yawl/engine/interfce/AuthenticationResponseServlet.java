/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * @author Lachlan Aldred
 * Date: 18/03/2005
 * Time: 15:36:44
 */
public class AuthenticationResponseServlet extends HttpServlet {

    private InterfaceBWebsideController _controller;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName =
                context.getInitParameter("InterfaceBWebSideController");
        //If you need to get through an auth proxy firewall and have configured it in the
        //web.xml file they will be retrieved for use.
        String userName = context.getInitParameter("UserName");
        String password = context.getInitParameter("Password");
        String proxyHost = context.getInitParameter("ProxyHost");
        String proxyPort = context.getInitParameter("ProxyPort");
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceBWebsideController) controllerClass.newInstance();
            //here the URL of the YAWL Engine get retrieved from the web.xml file.
            _controller.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
            _controller.setRemoteAuthenticationDetails(
                    userName, password, proxyHost, proxyPort);
            context.setAttribute("controller", _controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        AuthenticationConfig authConfig = _controller.getAuthenticationConfig();
        String userName = authConfig.getUserName() != null ? authConfig.getUserName() : "";
        String password = authConfig.getPassword() != null ? authConfig.getPassword() : "";
        String proxyHost = authConfig.getProxyHost() != null ? authConfig.getProxyHost() : "";
        String proxyPort = authConfig.getProxyPort() != null ? authConfig.getProxyPort() : "";

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();

        output.append(
                "<html><head><title>Authentication Details</title>" +
                "</head><body>" +
                "<H3>Please Enter Authentication Details</H3>" +
                "<table width='60%'><form method='post' action='" + request.getContextPath() +
                "/authServlet' name='availableForm'>" +
                "<tr>" +
                "<td colspan='2'>" +
                "<p>If this custom YAWL Service needs to invoke web services that lie beyond " +
                "an authenticating proxy server then you will need to enter some authentication " +
                "parameters to help the system work properly.</p>" +
                "<p>They can be entered into this form.   Alternatively for a permanent approach try " +
                "editing the web.xml file.</p>" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td colspan='2'>&nbsp;</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>User Name : </td>" +
                "<td height='30' align='center'>" +
                "<input type='text' width='25' name='userName' value='" + userName + "' />" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Password : </td>" +
                "<td height='30' align='center'>" +
                "<input type='password' width='25' name='password' value='" + password + "' />" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Proxy Host : </td>" +
                "<td height='30' align='center'>" +
                "<input type='text' width='25' name='proxyHost' value='" + proxyHost + "' />" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Proxy Port : </td>" +
                "<td height='30' align='center'>" +
                "<input type='text' width='25' name='proxyPort' value='" + proxyPort + "' />" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td height='30' align='left'>" +
                "<input type='submit' name='submit' value=' Submit ' /></td>" +
                "<td height='30' align='left'>" +
                "<input type='reset' value='  Clear  ' />" +
                "</td>" +
                "</tr>" +
                "</form>" +
                "</table>" +
                "</body>" +
                "</html>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("userName");
        String password = request.getParameter("password");
        String proxyHost = request.getParameter("proxyHost");
        String proxyPort = request.getParameter("proxyPort");
        String falsePW = "";
        for (int i = 0; i < password.length(); i++) {
            falsePW += "*";
        }
        _controller.setRemoteAuthenticationDetails(
                username, password, proxyHost, proxyPort);

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();

        output.append("<html><head><title>Authentication Details</title>" +
                "</head><body>" +
                "<H3>Details Entered</H3>" +
                "<table width='60%'>" +
                "<tr>" +
                "<td colspan='2'>" +
                "<p>Thank you for entering your details.   " +
                "They have been entered into this custom YAWL Service module.</p>" +
                "<p>Please feel free to check that the values are correct.</p>" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td colspan='2'>&nbsp;</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>User Name : </td>" +
                "<td height='30' align='center'>" + username + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Password : </td>" +
                "<td height='30' align='center'>" + falsePW + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Proxy Host : </td>" +
                "<td height='30' align='center'>" + proxyHost + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align='left'>Proxy Port : </td>" +
                "<td height='30' align='center'>" + proxyPort + "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


}
