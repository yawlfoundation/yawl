package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *  The Log Gateway class acts as an API gateway between the resource service and
 *  its process logs.
 *
 *  @author Michael Adams
 *  03/02/2010
 */

public class ResourceLogGateway extends HttpServlet {

    private static final Logger _log = Logger.getLogger(ResourceLogGateway.class);
    private LogMiner _logDB ;
    private ResourceManager _rm;

    private final String _noService = "<failure>Not connected to Resource Service.</failure>";
    private final String _noAction = "<failure>Resource Log Gateway called with invalid action.</failure>";


    public void init() {
        _logDB = LogMiner.getInstance() ;
        _rm = ResourceManager.getInstance();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {
        String result = "";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        String key = req.getParameter("key");

        if (action == null) {
            throw new IOException("ResourceLogGateway called with null action.");
        }
        else if (action.equalsIgnoreCase("connect")) {
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           if (_rm != null)
               result = _rm.serviceConnect(userid, password);
           else result = _noService;
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
           if (_rm != null)
               result = String.valueOf(_rm.checkServiceConnection(handle));
           else result = _noService;
       }
       else if (validConnection(handle)) {
           if (action.equals("getCaseStartedBy")) {
               String caseID = req.getParameter("caseid") ;
               result = _logDB.getCaseStartedBy(caseID);
           }
           else result = _noAction; 
       }
       else throw new IOException("Invalid or disconnected session handle.");

       // generate the output
       res.setContentType("text/html");
       PrintWriter out = res.getWriter();
       out.write(result);
       out.flush();
       out.close();
    }



    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);
    }


    private boolean validConnection(String handle) {
        try {
            return _rm.checkServiceConnection(handle) ;
        }
        catch (Exception e) {
            return false;
        }
    }
}