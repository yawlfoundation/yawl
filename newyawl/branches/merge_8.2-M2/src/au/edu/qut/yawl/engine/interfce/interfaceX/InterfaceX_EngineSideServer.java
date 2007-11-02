/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package au.edu.qut.yawl.engine.interfce.interfaceX;

import au.edu.qut.yawl.engine.interfce.EngineGateway;
import au.edu.qut.yawl.engine.interfce.EngineGatewayImpl;
import au.edu.qut.yawl.exceptions.YPersistenceException;

import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.rmi.RemoteException;


/**
 *  InterfaceX_EngineSideServer receives posts from the exception service and passes
 *  them as method calls to the Engine.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EngineBasedServer was used as a template for this class.
 *
  *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_EngineSideServer extends HttpServlet {

    private EngineGateway _engine;
    private static Logger logger = null;


    public void init() throws ServletException {
        logger = Logger.getLogger(this.getClass());
        ServletContext context = getServletContext();

        try {
            // get reference to engine
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {

                // turn on persistence if required
                String persistOn = context.getInitParameter("EnablePersistance");
                boolean persist = "true".equalsIgnoreCase(persistOn);
                _engine = new EngineGatewayImpl(persist);
                context.setAttribute("engine", _engine);
            }
            // turn on exception monitoring if required
            String exServiceOn = context.getInitParameter("EnableExceptionService");
            if (exServiceOn.equalsIgnoreCase("true")) {
                String observerURI = context.getInitParameter("ExceptionObserverURI");
                if (observerURI != null) _engine.setExceptionObserver(observerURI);
            }
        }
        catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.
        response.setContentType("text/xml");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processGetQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.
        response.setContentType("text/xml");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }




    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################
    private String processGetQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        return msg.toString();
    }


    // pass the POST request as a method call to the engine
    private String processPostQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        StringTokenizer tokens = new StringTokenizer(request.getRequestURI(), "/");

        //find method call name at end of uri
        String lastPartOfPath = null;
        String temp;
        while (tokens.hasMoreTokens()) {
            temp = tokens.nextToken();
            if (!tokens.hasMoreTokens()) {
                lastPartOfPath = temp;
            }
        }

        // unpack the params
        String sessionHandle = request.getParameter("sessionHandle");
        String workitemID  = request.getParameter("workitemID");
        String data = request.getParameter("data");

        // call the specified method
        try {
            if ("setExceptionObserver".equals(lastPartOfPath)) {
                String observerURI = request.getParameter("observerURI");
                msg.append(_engine.setExceptionObserver(observerURI));
            }
            else if ("removeExceptionObserver".equals(lastPartOfPath)) {
                msg.append(_engine.removeExceptionObserver());
            }
            else if ("updateWorkItemData".equals(lastPartOfPath)) {
                msg.append(_engine.updateWorkItemData(workitemID, data, sessionHandle));
            }
            else if ("updateCaseData".equals(lastPartOfPath)) {
                String caseID = request.getParameter("caseID");
                msg.append(_engine.updateCaseData(caseID, data, sessionHandle));
            }
            else if ("completeWorkItem".equals(lastPartOfPath)) {
                msg.append(_engine.completeWorkItem(workitemID, data, true, sessionHandle));
            }
            else if ("continueWorkItem".equals(lastPartOfPath)) {
                msg.append(_engine.startWorkItem(workitemID, sessionHandle));
            }
            else if ("unsuspendWorkItem".equals(lastPartOfPath)) {
                msg.append(_engine.unsuspendWorkItem(workitemID, sessionHandle));
            }
            else if ("restartWorkItem".equals(lastPartOfPath)) {
                msg.append(_engine.restartWorkItem(workitemID, sessionHandle));
            }
            else if ("startWorkItem".equals(lastPartOfPath)) {
                msg.append(_engine.startWorkItem(workitemID, sessionHandle));
            }
            else if ("cancelWorkItem".equals(lastPartOfPath)) {
                String fail = request.getParameter("fail");
                msg.append(_engine.cancelWorkItem(workitemID, fail, sessionHandle));
            }
        }
        catch (RemoteException re) {
            logger.error("Remote Exception when calling engine", re);
        }
        return msg.toString();
    }

}
