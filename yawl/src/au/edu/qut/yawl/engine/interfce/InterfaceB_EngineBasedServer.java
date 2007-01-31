/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;
import au.edu.qut.yawl.util.configuration.ServiceConfiguration;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 * 
 */
public class InterfaceB_EngineBasedServer extends HttpServlet {
    private EngineGateway _engine;
    private static Logger logger = null;

    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
        /**
         * Initialise logging
         */
        logger = Logger.getLogger(this.getClass());
        logger.error("***************************************************************************");
        logger.error("* initializing main yawl engine servlet *");
        logger.error("* official servlet name: " + config.getServletName());
        logger.error("***************************************************************************");
        ServletContext context = getServletContext();

        /*
        ADDED FOR PERSISTENCE TO CHECK IF
        DATABASE IS ENABLED/DISABLED
        */
        try {
			ServiceConfiguration sc = new ServiceConfiguration(config);
			BootstrapConfiguration.setInstance(sc);
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                String persistOn = context.getInitParameter("EnablePersistence");
                boolean persist = "true".equalsIgnoreCase(persistOn);
                _engine = new EngineGatewayImpl(persist);
                context.setAttribute("engine", _engine);
            }
        } catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.
        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        String queryResult = processGetQuery(request);
        output.append(queryResult);
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            logger.fatal("Error follows: ");
            logger.fatal(URLDecoder.decode(queryResult));
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.
        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        String queryResult = processPostQuery(request);
        output.append(queryResult);
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            logger.fatal("Error follows: ");
            logger.fatal(URLDecoder.decode(queryResult));
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
        //todo find out how to provide a meaningful 500 message in the format of  a fault message.
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
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("\nInterfaceB_EngineBasedServer::doGet() request.getRequestURL = "
                        + request.getRequestURL());
                logger.debug("InterfaceB_EngineBasedServer::doGet() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    logger.debug("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            }
            //if parameters exist do the while
            String sessionHandle = request.getParameter("sessionHandle");
            String action = request.getParameter("action");
            String workItemID = request.getParameter("workItemID");
            String specID = request.getParameter("specID");
            if (action != null) {
                if (action.equals("details")) {
                    msg.append(_engine.getWorkItemDetails(workItemID, sessionHandle));
                } else if (action.equals("startOne")) {
                    String userID = request.getParameter("user");
                    if (userID != null && sessionHandle != null) {
                        msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                    }
                } else if (action.equals("verbose")) {
                    msg.append(_engine.describeAllWorkItems(sessionHandle));
                } else if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnection(sessionHandle));
                } else if (action.equals("taskInformation")) {
                    String specificationID = request.getParameter("specID");
                    String taskID = request.getParameter( "taskID" );
                    msg.append(_engine.getTaskInformation(specificationID, taskID, sessionHandle));
                } else if (action.equals("checkAddInstanceEligible")) {
                    msg.append(_engine.checkElegibilityToAddInstances(
                            workItemID,
                            sessionHandle));
                } else if (action.equals("getSpecificationPrototypesList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                } else if (action.equals("getSpecification")) {
                    msg.append(_engine.getProcessDefinition(specID, sessionHandle));
                } else if (action.equals("getCasesForSpecification")) {
                    msg.append(_engine.getCasesForSpecification(specID, sessionHandle));
                } else if (action.equals("getState")) {
                    String caseID = request.getParameter( "caseID" );
                    msg.append(_engine.getCaseState(caseID, sessionHandle));
                } else if (action.equals("getChildren")) {
                    msg.append(_engine.getChildrenOfWorkItem(workItemID, sessionHandle));
                } else if (action.equals("getSpecificationsByRestriction")) {
                	String restriction = request.getParameter("restriction");
                	msg.append(_engine.getSpecificationsByRestriction(restriction, sessionHandle));
                } else {
                	msg.append( "<failure>Unknown action:" + action + "</failure>" );
                }
//            } else if ("ib".equals(lastPartOfPath)) {
//                msg.append(_engine.getAvailableWorkItemIDs(sessionHandle));
//            } else if ("workItem".equals(secondLastPartOfPath)) {
//                msg.append(_engine.getWorkItemOptions(lastPartOfPath,
//                        request.getRequestURL().toString(), sessionHandle));
            } else {
            	System.out.println("\nInterfaceB_EngineBasedServer::processGetQuery() request.getRequestURL = "
                        + request.getRequestURL());
                System.out.println("InterfaceB_EngineBasedServer::processGetQuery() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    System.out.println("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            	msg.append( "<failure><reason>No action was specified</reason></failure>" );
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("InterfaceB_EngineBasedServer::doGet() result = " + msg);
            logger.debug("\n");
        }
        return msg.toString();
    }


    private String processPostQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("\nInterfaceB_EngineBasedServer::doPost() " +
                        "request.getRequestURL = " + request.getRequestURL());
                logger.debug("InterfaceB_EngineBasedServer::doPost() request.parameters = ");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    logger.debug("\trequest.getParameter(" + name + ") = " +
                            request.getParameter(name));
                }
            }
            String sessionHandle = request.getParameter("sessionHandle");
            String action = request.getParameter("action");
            String workItemID = request.getParameter("workItemID");
            if (action != null) {
                if ("connect".equals(action)) {
                    String userID = request.getParameter("userid");
                    String password = request.getParameter("password");
                    msg.append(_engine.connect(userID, password));
                } else if ("checkout".equals(action)) {
                        msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                } else if (action.equals("checkin")) {
                    String data = request.getParameter("data");
                    msg.append(_engine.completeWorkItem(workItemID, data, false, sessionHandle));
                } else if (action.equals("createInstance")) {
                    String paramValueForMICreation =
                            request.getParameter("paramValueForMICreation");
                    msg.append(_engine.createNewInstance(
                            workItemID,
                            paramValueForMICreation,
                            sessionHandle));
                } else if (action.equals("suspend")) {
                    msg.append(_engine.suspendWorkItem(workItemID, sessionHandle));
                } else if (action.equals("rollback")) {
                    msg.append(_engine.rollbackWorkItem(workItemID, sessionHandle));
                } else if (action.equals("launchCase")) {
                    String specID = request.getParameter("specID");
                    URI completionObserver = getCompletionObserver(request);
                    String caseParams = request.getParameter("caseParams");
                    msg.append(_engine.launchCase(specID, caseParams, completionObserver, sessionHandle));
                } else if (action.equals("cancelCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.cancelCase(caseID, sessionHandle));
                } else {
                	msg.append( "<failure><reason>unrecognized action:" + action + "</reason></failure>" );
                }
            } else {
            	System.out.println("\nInterfaceB_EngineBasedServer::doPost() request.getRequestURL = "
                        + request.getRequestURL());
                System.out.println("InterfaceB_EngineBasedServer::doPost() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    System.out.println("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            	msg.append( "<failure><reason>No action was specified</reason></failure>" );
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("InterfaceB_EngineBasedServer::doPost() result = " + msg);
            logger.debug("\n");
        }
        return msg.toString();
    }

    private URI getCompletionObserver(HttpServletRequest request) {
        String completionObserver = request.getParameter("completionObserverURI");
        if(completionObserver != null) {
            try {
                return new URI(completionObserver);
            } catch (URISyntaxException e) {
                logger.error("Failure to ", e);
            }
        }
        return null;
    }
}
