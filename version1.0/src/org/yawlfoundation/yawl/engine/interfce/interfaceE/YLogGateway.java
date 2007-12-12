package org.yawlfoundation.yawl.engine.interfce.interfaceE;

import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *  The Log Gateway class acts as a gateway between YAWL and its process logs.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 29/10/2007
 *
 *  Last Date: 29/10/2007
 */

public class YLogGateway extends HttpServlet {

    private Logger _log = Logger.getLogger(this.getClass());
    private YLogManager _logMgr = YLogManager.getInstance() ;
    private EngineGatewayImpl _engine ;

    private final String _noEngine = "<failure>Not connected to YAWL Engine.</failure>";

    public void init() {
        try {
            _engine = new EngineGatewayImpl(false) ;
        }
        catch (YPersistenceException ype) {
             _log.error("Could not connect to YAWL Engine.", ype);
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {

       String result = "";


       String action = req.getParameter("action");
       String handle = req.getParameter("handle");

       if (action == null) {
              result = "<html><head>" +
           "<title>YAWL Process Log Gateway</title>" +
           "</head><body>" +
           "<H3>Welcome to the YAWL Process Log Gateway</H3>" +
           "<p> The Log Gateway acts as a bridge between YAWL's " +
               "process logs and the external world (it isn't meant to be browsed " +
               " to directly).</p>" +
           "</body></html>";
       }
       else if (action.equalsIgnoreCase("connect")) {
           System.out.println("**** doGet connect") ;
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           if (_engine != null)
               result = _engine.connect(userid, password);
           else result = _noEngine ;
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
           if (_engine != null)
               result = _engine.checkConnection(handle) ;
           else result = _noEngine ;
       }
       else if (action.equalsIgnoreCase("checkConnectionForAdmin")) {
           if (_engine != null)
               result = _engine.checkConnectionForAdmin(handle) ;
           else result = _noEngine ;
       }
       else if (validConnection(handle)) {
           if (action.equalsIgnoreCase("getCaseEventIDsForSpec")) {
               String specID = req.getParameter("specid");
               result = _logMgr.getCaseEventIDsForSpec(specID) ;
           }
           else if (action.equalsIgnoreCase("getParentWorkItemEventsForCase")) {
               String caseEventID = req.getParameter("eventid") ;
               result = _logMgr.getParentWorkItemEventsForCase(caseEventID) ;
           }
           else if (action.equalsIgnoreCase("getParentWorkItemEventsForCaseID")) {
               String caseID = req.getParameter("caseid") ;
               result = _logMgr.getParentWorkItemEventsForCaseID(caseID) ;
           }
           else if (action.equalsIgnoreCase("getChildWorkItemEventsForParent")) {
               String parentEventID = req.getParameter("eventid") ;
               result = _logMgr.getChildWorkItemEventsForParent(parentEventID) ;
           }
           else if (action.equalsIgnoreCase("getCaseEventsForSpec")) {
               String specID = req.getParameter("specid");
               result = _logMgr.getCaseEventsForSpec(specID) ;
           }
           else if (action.equalsIgnoreCase("getAllSpecIDs")) {
               result = _logMgr.getAllSpecIDs();
           }
           else if (action.equalsIgnoreCase("getAllCaseEventIDs")) {
               String eventType = req.getParameter("eventtype");
               result = _logMgr.getAllCaseEventIDs(eventType);
           }
       }
       else throw new IOException("Invalid or disconnected session handle.");

       // generate the output
       res.setContentType("text/html");
       PrintWriter out = res.getWriter();
       out.write(result);
       out.flush();
       out.close();
    }



    public void doPost(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException { }


    private boolean validConnection(String handle) {
        String result = _engine.checkConnectionForAdmin(handle) ;
        return result.equalsIgnoreCase("Permission Granted");
    }
}