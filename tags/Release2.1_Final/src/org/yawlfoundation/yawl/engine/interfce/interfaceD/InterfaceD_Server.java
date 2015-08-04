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

package org.yawlfoundation.yawl.engine.interfce.interfaceD;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Receives workitem events from an interface client.
 *
 * 
 * @author Lachlan Aldred
 * Date: 16/09/2005
 * Time: 15:48:08
 */
public class InterfaceD_Server extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private InterfaceD_Controller _controller;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName =
                context.getInitParameter("InterfaceBWebSideController");
        //If you need to get through an auth proxy firewall and have configured it in the
        //web.xml file they will be retrieved for use.
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceD_Controller) controllerClass.newInstance();
            //here the URL of the YAWL Engine get retrieved from the web.xml file.
            context.setAttribute("controller", _controller);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {

    }



    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String workitemStr = request.getParameter("workitem");
        WorkItemRecord workitem = Marshaller.unmarshalWorkItem(workitemStr);
        _controller.processWorkItem(workitem);
    }

}
