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

package org.yawlfoundation.yawl.smsModule;

import javax.servlet.http.HttpServlet;

/**
 * The sole use of this servlet is to load the sms server's url from web.xml
 *
 * Author: Michael Adams
 * Creation Date: 10/12/2008
 */
public class SMSGateway extends HttpServlet {

    public void init() {
        String sendURI = getServletContext().getInitParameter("SendURI");
        String receiveURI = getServletContext().getInitParameter("ReceiveURI");

        if (sendURI != null) {
            SMSSender._sendURI = sendURI;
        }
        if (receiveURI != null) {
            SMSSender._receiveURI = receiveURI;
        }        
    }
}
