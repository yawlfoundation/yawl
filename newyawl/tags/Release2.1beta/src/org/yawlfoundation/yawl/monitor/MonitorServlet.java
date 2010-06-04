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
