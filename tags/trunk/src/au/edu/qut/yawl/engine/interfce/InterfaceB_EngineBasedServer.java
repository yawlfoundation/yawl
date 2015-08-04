/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.exceptions.YPersistenceException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;


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



    public void init() throws ServletException {

        /**
         * Initialise logging
         */
        logger = Logger.getLogger(this.getClass());

        ServletContext context = getServletContext();

        /*
        ADDED FOR PERSISTANCE TO CHECK IF
        DATABASE IS ENABLED/DISABLED
        */
        try {
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                String persistOn = context.getInitParameter("EnablePersistance");
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
            StringTokenizer tokens = new StringTokenizer(request.getRequestURI(), "/");
            String secondLastPartOfPath = null;
            String lastPartOfPath = null;
            String temp = null;
            while (tokens.hasMoreTokens()) {
                secondLastPartOfPath = temp;
                temp = tokens.nextToken();
                if (!tokens.hasMoreTokens()) {
                    lastPartOfPath = temp;
                }
            }
            //if parameters exist do the while
            String sessionHandle = request.getParameter("sessionHandle");
            String action = request.getParameter("action");
            if (action != null) {
                if (action.equals("details")) {
                    String workItemID = lastPartOfPath;
                    msg.append(_engine.getWorkItemDetails(workItemID, sessionHandle));
                } else if (action.equals("startOne")) {
                    String userID = request.getParameter("user");
                    if (userID != null && sessionHandle != null) {
                        msg.append(_engine.startWorkItem(lastPartOfPath, sessionHandle));
                    }
                } else if (action.equals("verbose")) {
                    msg.append(_engine.describeAllWorkItems(sessionHandle));
                } else if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnection(sessionHandle));
                } else if (action.equals("taskInformation")) {
                    String specificationID = request.getParameter("specID");
                    String taskID = lastPartOfPath;
                    String results = _engine.getTaskInformation(
                            specificationID, taskID, sessionHandle);
                    msg.append(results);
                } else if (action.equals("checkAddInstanceEligible")) {
                    String workItemID = lastPartOfPath;
                    msg.append(_engine.checkElegibilityToAddInstances(
                            workItemID,
                            sessionHandle));
                } else if (action.equals("getSpecificationPrototypesList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                } else if (action.equals("getSpecification")) {
                    String specID = request.getParameter("specID");
                    msg.append(_engine.getProcessDefinition(specID, sessionHandle));
                } else if (action.equals("getCasesForSpecification")) {
                    String specID = lastPartOfPath;
                    msg.append(_engine.getCasesForSpecification(specID, sessionHandle));
                } else if (action.equals("getState")) {
                    String caseID = lastPartOfPath;
                    msg.append(_engine.getCaseState(caseID, sessionHandle));
                } else if (action.equals("getChildren")) {
                    String workItemID = lastPartOfPath;
                    msg.append(_engine.getChildrenOfWorkItem(workItemID, sessionHandle));
                }
            } else if ("ib".equals(lastPartOfPath)) {
                msg.append(_engine.getAvailableWorkItemIDs(sessionHandle));
            } else if ("workItem".equals(secondLastPartOfPath)) {
                msg.append(_engine.getWorkItemOptions(lastPartOfPath,
                        request.getRequestURL().toString(), sessionHandle));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
                            ServletUtils.urlDecodeData(request.getParameter(name)));
                }
            }
            StringTokenizer tokens = new StringTokenizer(request.getRequestURI(), "/");
            String lastPartOfPath = null;
            String temp = null;
            while (tokens.hasMoreTokens()) {
                temp = tokens.nextToken();
                if (!tokens.hasMoreTokens()) {
                    lastPartOfPath = temp;
                }
            }
            if (!"ib".equals(lastPartOfPath)) {
                if ("connect".equals(lastPartOfPath)) {
                    String userID = ServletUtils.urlDecodeData(request.getParameter("userid"));
                    String password = ServletUtils.urlDecodeData(request.getParameter("password"));
                    msg.append(_engine.connect(userID, password));
                } else {

                    String action = ServletUtils.urlDecodeData(request.getParameter("action"));
                    String workItemID = lastPartOfPath;
                    String sessionHandle = ServletUtils.urlDecodeData(request.getParameter("sessionHandle"));
                    if ("checkout".equals(action)) {
                        msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                    } else if (action.equals("checkin")) {
                        String data = ServletUtils.urlDecodeData(request.getParameter("data"));
                        msg.append(_engine.completeWorkItem(workItemID, data, false, sessionHandle));
                    } else if (action.equals("createInstance")) {
                        String paramValueForMICreation =
                            ServletUtils.urlDecodeData(request.getParameter("paramValueForMICreation"));
                        msg.append(_engine.createNewInstance(
                                workItemID,
                                paramValueForMICreation,
                                sessionHandle));
                    } else if (action.equals("suspend")) {
                        msg.append(_engine.suspendWorkItem(workItemID, sessionHandle));
                    } else if (action.equals("rollback")) {
                        msg.append(_engine.rollbackWorkItem(workItemID, sessionHandle));
                    } else if (action.equals("launchCase")) {
                        String specID = lastPartOfPath;
                        URI completionObserver = getCompletionObserver(request);
                        String caseParams = ServletUtils.urlDecodeData(request.getParameter("caseParams"));
                        msg.append(_engine.launchCase(specID, caseParams, completionObserver, sessionHandle));
                    } else if (action.equals("cancelCase")) {
                        String caseID = lastPartOfPath;
                        msg.append(_engine.cancelCase(caseID, sessionHandle));
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
