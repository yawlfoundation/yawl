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

package org.yawlfoundation.yawl.monitor;

import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.util.Hashtable;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 1/06/2010
 */
public class MonitorServlet extends HttpServlet {

    /** Read settings from web.xml and use them to initialise the service */
    public void init() {
        try {
            ServletContext context = getServletContext();

            // load the urls of the required interfaces
            Map<String, String> urlMap = new Hashtable<String, String>();
            String engineGateway = context.getInitParameter("EngineGateway");
            if (engineGateway != null) urlMap.put("engineGateway", engineGateway);
            String engineLogGateway = context.getInitParameter("EngineLogGateway");
            if (engineGateway != null) urlMap.put("engineLogGateway", engineLogGateway);
            String resourceGateway = context.getInitParameter("ResourceGateway");
            if (engineGateway != null) urlMap.put("resourceGateway", resourceGateway);
            String resourceLogGateway = context.getInitParameter("ResourceLogGateway");
            if (resourceLogGateway != null) urlMap.put("resourceLogGateway", resourceLogGateway);

            MonitorClient.getInstance().initInterfaces(urlMap); 
        }
        catch (Exception e) {
            Logger.getLogger(this.getClass()).error("Monitor Service Initialisation Exception", e);
        }
    }

}
