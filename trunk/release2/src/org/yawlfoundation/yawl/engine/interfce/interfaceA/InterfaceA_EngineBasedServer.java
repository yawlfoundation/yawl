/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceA;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGateway;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Enumeration;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 *
 * @author Michael Adams (refactored for v2.0, 06/2008)
 */
public class InterfaceA_EngineBasedServer extends HttpServlet {
    private EngineGateway _engine;
    private static final boolean _debug = false;
    private static final Logger logger = Logger.getLogger(InterfaceA_EngineBasedServer.class);

    public void init() throws ServletException {

        /**
         * Initialise logging
         */
        ServletContext context = getServletContext();

        try {
            String persistOn = context.getInitParameter("EnablePersistance") ;
            boolean enablePersist = "true".equalsIgnoreCase(persistOn);

            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                _engine = new EngineGatewayImpl(enablePersist);
                context.setAttribute("engine", _engine);
            }
        } catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
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
        ServletUtils.finalizeResponse(outputWriter, output);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
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
        }
        ServletUtils.finalizeResponse(outputWriter, output);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

    }


    protected void doDelete(HttpServletRequest request, HttpServletResponse responce) {

    }


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################

    private String processGetQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");

        try {
            if (_debug) {
                debug(request, "Get") ;
            }
            
            if (action != null) {
                if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnectionForAdmin(sessionHandle));
                }
                else if (action.equals("getUsers")) {
                    msg.append(_engine.getUsers(sessionHandle));
                }
                else if (action.equals("getList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                }
                else if (action.equals("getYAWLServices")) {
                    msg.append(_engine.getYAWLServices(sessionHandle));
                }
            }
        } catch (RemoteException e) {
            logger.error("Exception in Interface B with action: " + action, e);
        }
        if (msg.length() == 0) {
            msg.append(
                    "<failure><reason>" +
                    "Invalid action or exception was thrown." +
                    "</reason></failure>");
        }
        if (_debug) {
            logger.debug("return = " + msg);
        }
        return msg.toString();
    }


    private String processPostQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String userID = request.getParameter("userID");
        String password = request.getParameter("password");

        try {
            if (_debug) {
                debug(request, "Post");
            }

            if (action != null) {
                if ("connect".equals(action)) {
                    msg.append(_engine.connect(userID, password));
                }
                else if ("createUser".equals(action) || "createAdmin".equals(action)) {
                    boolean isAdmin = ("createAdmin".equals(action));
                    msg.append(_engine.createUser(userID, password, isAdmin, sessionHandle));
                }
                else if ("deleteUser".equals(action)) {
                    msg.append(_engine.deleteUser(userID, sessionHandle));
                }
                else if ("newPassword".equals(action)) {
                    msg.append(_engine.changePassword(password, sessionHandle));
                }
                else if ("newYAWLService".equals(action)) {
                    String serviceStr = request.getParameter("service");
                    msg.append(_engine.addYAWLService(serviceStr, sessionHandle));
                }
                else if ("removeYAWLService".equals(action)) {
                    String serviceURI = request.getParameter("serviceURI");
                    msg.append(_engine.removeYAWLService(serviceURI, sessionHandle));
                }
                else if ("unload".equals(action)) {
                    YSpecificationID specID = makeYSpecificationID(request);
                    msg.append(_engine.unloadSpecification(specID, sessionHandle));
                }
            }
            else if (request.getRequestURI().endsWith("/upload")) {
                sessionHandle = request.getHeader("YAWLSessionHandle");
                String fileName = request.getHeader("filename");
                StringBuffer specification = new StringBuffer();
                ServletInputStream in = request.getInputStream();
                int i = in.read();
                while (i != -1) {
                    specification.append((char) i);
                    i = in.read();
                }
                msg.append(_engine.loadSpecification(specification.toString(),
                                                     fileName, sessionHandle));
            }
        }
        catch (Exception e) {
            logger.error("Exception in Interface B with action: " + action, e);
        }
        if (msg.length() == 0) {
            msg.append("<failure><reason>" +
                       "Null action or exception was thrown." +
                       "</reason></failure>");
        }
        if (_debug) {
            logger.debug("return = " + msg);
        }
        return msg.toString();
    }

    
    private YSpecificationID makeYSpecificationID(HttpServletRequest request) {
        String version = "0.1" ;
        String handle = request.getParameter("sessionHandle") ;
        String id = request.getParameter("specID");
        String verParam = request.getParameter("version");

        try {
            if (verParam == null) verParam = _engine.getLatestSpecVersion(id, handle);
            if (verParam != null) version = verParam;
        }
        catch (Exception e) {
            // nothing to do
        }

        return new YSpecificationID(id, new YSpecVersion(version));
    }


    private void debug(HttpServletRequest request, String service) {
        logger.debug("\nInterfaceA_EngineBasedServer::do" + service + "() " +
                "request.getRequestURL = " + request.getRequestURL());
        logger.debug("\nInterfaceA_EngineBasedServer::do" + service +
                "() request.parameters = ");
        Enumeration paramNms = request.getParameterNames();
        while (paramNms.hasMoreElements()) {
            String name = (String) paramNms.nextElement();
            logger.debug("\trequest.getParameter(" + name + ") = " +
                    request.getParameter(name));
        }
    }
}


