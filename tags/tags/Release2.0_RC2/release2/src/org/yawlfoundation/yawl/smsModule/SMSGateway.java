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
