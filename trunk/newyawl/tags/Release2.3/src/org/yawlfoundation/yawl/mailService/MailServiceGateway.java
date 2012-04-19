/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
  *  Initialises the Simple Mail Service with values from 'web.xml'.
  *
  *  @author Michael Adams
  *  @date 24/06/2011
  *
  */

public class MailServiceGateway extends HttpServlet {

    private static final Logger _log = Logger.getLogger(MailServiceGateway.class);


    /** Read settings from web.xml and use them to initialise the service */
    public void init() {
        try {
            MailService service = MailService.getInstance();
            ServletContext context = getServletContext();
            service.setHost(context.getInitParameter("host"));
            service.setUser(context.getInitParameter("mailUserName"));
            service.setPassword(context.getInitParameter("mailPassword"));
            service.setFromName(context.getInitParameter("senderName"));
            service.setFromAddress(context.getInitParameter("senderAddress"));
            service.setPort(StringUtil.strToInt(context.getInitParameter("port"), 25));
        }
        catch (Exception e) {
            _log.error("Simple Mail Service Initialisation Exception", e);
        }
    }


    public void destroy() { }


   public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException { }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }

}
