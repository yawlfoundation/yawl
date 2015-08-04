package org.yawlfoundation.yawl.engine.interfce.interfaceE;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogServer;

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
    private YLogServer _logMgr = YLogServer.getInstance() ;
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
        String key = req.getParameter("key");

        if (action == null) {
            throw new IOException("YLogGateway called with null action.");
        }
        else if (action.equalsIgnoreCase("connect")) {
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           if (_engine != null) {
               int interval = req.getSession().getMaxInactiveInterval();
               result = _engine.connect(userid, password, interval);
           }
           else result = _noEngine ;
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
           if (_engine != null)
               result = _engine.checkConnection(handle) ;
           else result = _noEngine ;
       }
       else if (validConnection(handle)) {
           if (action.equals("getAllSpecifications")) {
               result = _logMgr.getAllSpecifications();
           }
           else if (action.equals("getNetInstancesOfSpecification")) {
               if (key != null) {
                   result = _logMgr.getNetInstancesOfSpecification(new Long(key)) ;
               }
               else {
                   String identifier = req.getParameter("identifier");
                   String version = req.getParameter("version");
                   String uri = req.getParameter("uri");
                   YSpecificationID specID = new YSpecificationID(identifier, version, uri);
                   result = _logMgr.getNetInstancesOfSpecification(specID) ;
               }
           }
           else if (action.equals("getCompleteCaseLogsForSpecification")) {
               if (key != null) {
                   result = _logMgr.getCompleteCaseLogsForSpecification(new Long(key)) ;
               }
               else {
                   String identifier = req.getParameter("identifier");
                   String version = req.getParameter("version");
                   String uri = req.getParameter("uri");
                   YSpecificationID specID = new YSpecificationID(identifier, version, uri);
                   result = _logMgr.getCompleteCaseLogsForSpecification(specID) ;
               }
           }           
           else if (action.equals("getCaseEvents")) {
               if (key != null) {
                   result = _logMgr.getCaseEvents(new Long(key));
               }
               else {
                   String caseID = req.getParameter("caseid") ;
                   result = _logMgr.getCaseEvents(caseID);
               }
           }
           else if (action.equals("getDataForEvent")) {
               result = _logMgr.getDataForEvent(new Long(key)) ;
           }
           else if (action.equals("getDataTypeForDataItem")) {
               result = _logMgr.getDataTypeForDataItem(new Long(key)) ;
           }
           else if (action.equals("getTaskInstancesForCase")) {
               String caseID = req.getParameter("caseid") ;
               result = _logMgr.getTaskInstancesForCase(caseID);
           }
           else if (action.equals("getTaskInstancesForTask")) {
               result = _logMgr.getTaskInstancesForTask(new Long(key));
           }
           else if (action.equals("getCaseEvent")) {
               String event = req.getParameter("event");
               String caseID = req.getParameter("caseid") ;
               result = _logMgr.getCaseEvent(caseID, event);
           }
           else if (action.equals("getAllCasesStartedByService")) {
               String name = req.getParameter("name") ;
               result = _logMgr.getAllCasesStartedByService(name) ;
           }
           else if (action.equals("getAllCasesCancelledByService")) {
               String name = req.getParameter("name") ;
               result = _logMgr.getAllCasesCancelledByService(name) ;
           }
           else if (action.equals("getInstanceEvents")) {
               result = _logMgr.getInstanceEvents(new Long(key)) ;
           }
           else if (action.equals("getServiceName")) {
               result = _logMgr.getServiceName(new Long(key)) ;
           }
           else if (action.equals("getCompleteCaseLog")) {
               String caseID = req.getParameter("caseid") ;
               result = _logMgr.getCompleteCaseLog(caseID) ;
           }
           else if (action.equals("getEventsForTaskInstance")) {
               String itemID = req.getParameter("itemid") ;
               result = _logMgr.getEventsForTaskInstance(itemID) ;
           }
           else if (action.equals("getSpecificationXESLog")) {
               String identifier = req.getParameter("identifier");
               String version = req.getParameter("version");
               String uri = req.getParameter("uri");
               YSpecificationID specID = new YSpecificationID(identifier, version, uri);
               result = _logMgr.getSpecificationXESLog(specID);
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
        try {
            String result = _engine.checkConnection(handle) ;
            return result.equals("<success/>");
        }
        catch (Exception e) {
            return false;
        }
    }
}