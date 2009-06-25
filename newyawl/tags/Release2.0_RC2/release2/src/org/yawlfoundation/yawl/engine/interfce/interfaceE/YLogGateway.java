package org.yawlfoundation.yawl.engine.interfce.interfaceE;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *  The Log Gateway class acts as an API gateway between YAWL and its process logs.
 *
 *  @author Michael Adams
 *  v0.1, 29/10/2007
 *
 *  Last Date: 1/2009
 */

public class YLogGateway extends HttpServlet {

    private static final Logger _log = Logger.getLogger(YLogGateway.class);
    private YLogManager _logMgr = YLogManager.getInstance() ;
    private EngineGatewayImpl _engine ;

    private final String _noEngine = "<failure>Not connected to YAWL Engine.</failure>";


    public void init() {
        try {
            _engine = new EngineGatewayImpl(false) ;            // get engine reference
        }
        catch (YPersistenceException ype) {
             _log.error("Could not connect to YAWL Engine.", ype);
        }
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {

       String result = "";
       String action = req.getParameter("action");
       String handle = req.getParameter("sessionHandle");

       if (action == null) {
           throw new IOException("YLogGateway called with null action.");
       }
       else if (action.equalsIgnoreCase("connect")) {
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
           else if (action.equalsIgnoreCase("getCaseEventTime")) {
               String eventType = req.getParameter("eventtype");
               if (eventType != null) {
                   String caseID = req.getParameter("caseid") ;
                   result = _logMgr.getCaseEventTime(caseID, eventType);
               }
               else {
                   String caseEventID = req.getParameter("eventid") ;
                   result = _logMgr.getCaseEventTime(caseEventID);                   
               }
           }
           else if (action.equalsIgnoreCase("getWorkItemDataForChildWorkItemEventID")) {
               String childEventID = req.getParameter("eventid") ;
               result = _logMgr.getChildWorkItemData(childEventID) ;
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



    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);
    }


    private boolean validConnection(String handle) {
        String result = _engine.checkConnectionForAdmin(handle) ;
        return result.equalsIgnoreCase("Permission Granted");
    }
}