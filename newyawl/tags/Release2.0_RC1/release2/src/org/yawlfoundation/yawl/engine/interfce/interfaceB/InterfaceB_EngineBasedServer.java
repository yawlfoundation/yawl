/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGateway;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.YProperties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Enumeration;


/**
 * Receives & responds to POST messages from custom services
 *
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 *
 * @author Michael Adams (refactored for v2.0, 06/2008; 12/2008)
 *
 */
public class InterfaceB_EngineBasedServer extends HttpServlet {
    private EngineGateway _engine;
    private static final Logger logger = Logger.getLogger(InterfaceB_EngineBasedServer.class);


    public void init() throws ServletException {

        try {
            ServletContext context = getServletContext();

            // load yawl.properties (if any)
            StringWriter out = new StringWriter(8192);
            InputStream in = context.getResourceAsStream(
                               "/WEB-INF/classes/yawl.properties");
            if (in != null) {
                InputStreamReader isr = new InputStreamReader(in);
                char[] buffer = new char[8192];
                int count;

                while ((count = isr.read(buffer)) > 0)
                   out.write(buffer, 0, count);

                isr.close();
                YProperties.getInstance().setProperties(out.toString());
            }

            // init engine reference
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                String persistOn = context.getInitParameter("EnablePersistence");
                boolean persist = "true".equalsIgnoreCase(persistOn);
                _engine = new EngineGatewayImpl(persist);
                context.setAttribute("engine", _engine);
            }
        }
        catch (IOException ioe) {
            logger.warn("Could not load static properties from file.");
        }
        catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);                 // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
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


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################

    private String processPostQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String workItemID = request.getParameter("workItemID");
        String specName = request.getParameter("specID");
        String version = request.getParameter("version");
        String taskID = request.getParameter("taskID");

        try {
            if (logger.isDebugEnabled()) {
                debug(request, "Post");
            }

            if (action != null) {
                if (action.equals("connect")) {
                    String userID = request.getParameter("userid");
                    String password = request.getParameter("password");
                    msg.append(_engine.connect(userID, password));
                }
                else if (action.equals("checkout")) {
                    msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("checkin")) {
                    String data = request.getParameter("data");
                    msg.append(_engine.completeWorkItem(workItemID, data, false,
                            sessionHandle));
                }
                else if (action.equals("launchCase")) {
                    String specID = request.getParameter("specID");
                    URI completionObserver = getCompletionObserver(request);
                    String caseParams = request.getParameter("caseParams");
                    msg.append(_engine.launchCase(specID, caseParams,
                            completionObserver, sessionHandle));
                }
                else if (action.equals("cancelCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.cancelCase(caseID, sessionHandle));
                }
                if (action.equals("details")) {
                    msg.append(_engine.getWorkItemDetails(workItemID, sessionHandle));
                }
                else if (action.equals("startOne")) {
                    String userID = request.getParameter("user");
                    msg.append(_engine.startWorkItem(userID, sessionHandle));
                }
                else if (action.equals("getLiveItems")) {
                    msg.append(_engine.describeAllWorkItems(sessionHandle));
                }
                else if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnection(sessionHandle));
                }
                else if (action.equals("taskInformation")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getTaskInformation(specID, taskID, sessionHandle));
                }
                else if (action.equals("getMITaskAttributes")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getMITaskAttributes(specID, taskID, sessionHandle));
                }
                else if (action.equals("getResourcingSpecs")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getResourcingSpecs(specID, taskID, sessionHandle));
                }
                else if (action.equals("checkIsAdmin")) {
                    msg.append(_engine.checkConnectionForAdmin(sessionHandle));
                }
                else if (action.equals("checkAddInstanceEligible")) {
                    msg.append(_engine.checkElegibilityToAddInstances(
                                                              workItemID, sessionHandle));
                }
                else if (action.equals("getSpecificationPrototypesList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                }
                else if (action.equals("getSpecification")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getProcessDefinition(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationDataSchema")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getSpecificationDataSchema(specID, sessionHandle));
                }
                else if (action.equals("getCasesForSpecification")) {
                    YSpecificationID specID = new YSpecificationID(specName, version);
                    msg.append(_engine.getCasesForSpecification(specID, sessionHandle));
                }
                else if (action.equals("getCaseState")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseState(caseID, sessionHandle));
                }
                else if (action.equals("getCaseData")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseData(caseID, sessionHandle));
                }
                else if (action.equals("getChildren")) {
                    msg.append(_engine.getChildrenOfWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("getCaseInstanceSummary")) {
                    msg.append(_engine.getCaseInstanceSummary(sessionHandle));
                }
                else if (action.equals("getWorkItemInstanceSummary")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getWorkItemInstanceSummary(caseID, sessionHandle));
                }
                else if (action.equals("getParameterInstanceSummary")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getParameterInstanceSummary(caseID, workItemID, sessionHandle));
                }
                else if (action.equals("createInstance")) {
                    String paramValueForMICreation =
                            request.getParameter("paramValueForMICreation");
                    msg.append(_engine.createNewInstance(workItemID,
                            paramValueForMICreation, sessionHandle));
                }
                else if (action.equals("suspend")) {
                    msg.append(_engine.suspendWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("rollback")) {
                    msg.append(_engine.rollbackWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("unsuspend")) {
                    msg.append(_engine.unsuspendWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("skip")) {
                    msg.append(_engine.skipWorkItem(workItemID, sessionHandle));
                }
            }  // action is null
            else if (request.getRequestURI().endsWith("ib")) {
                msg.append(_engine.getAvailableWorkItemIDs(sessionHandle));
            }
            else if (request.getRequestURI().contains("workItem")) {
                msg.append(_engine.getWorkItemOptions(workItemID,
                        request.getRequestURL().toString(), sessionHandle));
            }
            else logger.error("Interface B called with null action.");
        }
        catch (RemoteException e) {
            logger.error("Remote Exception in Interface B with action: " + action, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("InterfaceB_EngineBasedServer::doPost() result = " + msg + "\n");
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


    private void debug(HttpServletRequest request, String service) {
        logger.debug("\nInterfaceB_EngineBasedServer::do" + service + "() " +
                "request.getRequestURL = " + request.getRequestURL());
        logger.debug("\nInterfaceB_EngineBasedServer::do" + service +
                "() request.parameters = ");
        Enumeration paramNms = request.getParameterNames();
        while (paramNms.hasMoreElements()) {
            String name = (String) paramNms.nextElement();
            logger.debug("\trequest.getParameter(" + name + ") = " +
                    request.getParameter(name));
        }
    }

}
