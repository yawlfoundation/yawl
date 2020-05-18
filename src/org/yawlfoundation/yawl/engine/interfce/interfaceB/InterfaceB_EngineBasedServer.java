/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.elements.data.external.ExternalDataGatewayFactory;
import org.yawlfoundation.yawl.elements.predicate.PredicateEvaluatorFactory;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.engine.time.workdays.HolidayLoader;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;
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
public class InterfaceB_EngineBasedServer extends YHttpServlet {

    private EngineGateway _engine;
    private boolean _gatherPerfStats = false;

    public void init() throws ServletException {
        int maxWaitSeconds = 5;                             // a default

        try {
            ServletContext context = getServletContext();

            // set the path to external db gateway plugin classes (if any)
            String pluginPath = context.getInitParameter("ExternalPluginsPath");
            ExternalDataGatewayFactory.setExternalPaths(pluginPath);
            PredicateEvaluatorFactory.setExternalPaths(pluginPath);

            // init engine reference
            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                Class<? extends YEngine> engineImpl = getEngineImplClass();
                boolean persist = getBooleanFromContext("EnablePersistence");
                boolean enableHbnStats = getBooleanFromContext("EnableHibernateStatisticsGathering");
                boolean redundantMode = getBooleanFromContext("StartInRedundantMode");
                _engine = new EngineGatewayImpl(engineImpl, persist,
                        enableHbnStats, redundantMode);
                _engine.setActualFilePath(context.getRealPath("/"));
                context.setAttribute("engine", _engine);
            }

            // enable performance statistics gathering if requested
            _gatherPerfStats = getBooleanFromContext("EnablePerformanceStatisticsGathering");

            // set flag to disable logging (only if false) - enabled with persistence by
            // default
            String logStr = context.getInitParameter("EnableLogging");
            if ((logStr != null) && logStr.equalsIgnoreCase("false")) {
                _engine.disableLogging();
            }

            // add the reference to the default worklist
            _engine.setDefaultWorklist(context.getInitParameter("DefaultWorklist"));

            // set flag for generic admin account (only if set to true)
            String allowAdminID = context.getInitParameter("AllowGenericAdminID");
            if ((allowAdminID != null) && allowAdminID.equalsIgnoreCase("true")) {
                _engine.setAllowAdminID(true);
            }

            // override the max time that initialisation events wait for between
            // final engine init and server start completion
            int maxWait = StringUtil.strToInt(
                    context.getInitParameter("InitialisationAnnouncementTimeout"), -1);
            if (maxWait >= 0) maxWaitSeconds = maxWait;

            // set the country/region codes used for calculating work-day-only timers (if any)
            String timerLocationConfig = context.getInitParameter("WorkdayTimerGeoCodes");
            if (timerLocationConfig != null) {
                new HolidayLoader(false).startupCheck(timerLocationConfig);
            }

            // read the current version properties
            _engine.initBuildProperties(context.getResourceAsStream(
                               "/WEB-INF/classes/version.properties"));

            // init any 3rd party observer gateways
            String gatewayStr = context.getInitParameter("ObserverGateway");
            if (gatewayStr != null) {

                // split multiples on the semi-colon (if any)
                for (String gateway : gatewayStr.split(";")) {
                    registerObserverGateway(gateway);
                }
            }
        }
        catch (YPersistenceException e) {
            _log.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }

        if (_engine != null) {
            _engine.notifyServletInitialisationComplete(maxWaitSeconds);
        }
        else {
            _log.fatal("Failed to initialise Engine (unspecified failure). Please " +
                    "consult the logs for details");
            throw new UnavailableException("Unspecified engine failure");
        }
    }


    private void registerObserverGateway(String gatewayClassName) {
        ObserverGateway gateway ;
        try {
            Class gatewayClass = Class.forName(gatewayClassName);

            // If the class has a getInstance() method, call that method rather than
            // calling a constructor (& thus instantiating 2 instances of the class)
            try {
                Method instMethod = gatewayClass.getDeclaredMethod("getInstance");
                gateway = (ObserverGateway) instMethod.invoke(null);
            }

            // no getInstance(), so just create a plain new instance
            catch (NoSuchMethodException nsme) {
                gateway = (ObserverGateway) gatewayClass.newInstance();
            }

            if (gateway != null)
                _engine.registerObserverGateway(gateway);
            else
                _log.warn("Error registering external ObserverGateway '{}'.",
                        gatewayClassName);
        }
        catch (ClassNotFoundException e) {
            _log.warn("Unable to locate external ObserverGateway '" +
                    gatewayClassName + "'.", e);
        }
        catch (InstantiationException ie) {
            _log.warn("Unable to instantiate external ObserverGateway '" +
                    gatewayClassName +
                    "'. Perhaps it is missing a no-argument constructor.", ie);
        }
        catch (YAWLException ye) {
            _log.warn("Failed to register external ObserverGateway '" +
                    gatewayClassName + "'.", ye);
        }
        catch (Exception e) {
            _log.warn("Unable to instantiate external ObserverGateway '" +
                    gatewayClassName + "'.", e);
        }
    }


    private Class<? extends YEngine> getEngineImplClass() {
        String implClassName = getServletContext().getInitParameter("EngineImpl");
        if (! StringUtil.isNullOrEmpty(implClassName)) {
            try {
                Class c = Class.forName(implClassName);
                if (YEngine.class.isAssignableFrom(c)) {
                    return (Class<? extends YEngine>) c;
                }
                _log.warn("Class '{}' is not a superclass of YEngine.", implClassName);
            }
            catch (ClassNotFoundException e) {
                 _log.warn("Unable to locate external YEngine class '" +
                         implClassName + "'.", e);
            }
            _log.warn("Reverting to the default YEngine implementation.");
        }
        return null;
    }


    public void destroy() {
        _engine.shutdown();
        super.destroy();
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("HEAD".equals(request.getMethod())) return;
        doPost(request, response);                 // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuilder output = new StringBuilder();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            _log.fatal("************************************************************");
            _log.fatal("A failure has occurred whilst persisting workflow state to the");
            _log.fatal("database. Check the status of the database connection defined");
            _log.fatal("for the YAWL service, and restart the YAWL web application.");
            _log.fatal("Further information may be found within the Tomcat log files.");
            _log.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
        //todo find out how to provide a meaningful 500 message in the format of  a fault message.
    }


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################

    private String processPostQuery(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        String sessionHandle = request.getParameter("sessionHandle");
        String action = request.getParameter("action");
        String workItemID = request.getParameter("workItemID");
        String specIdentifier = request.getParameter("specidentifier");
        String specVersion = request.getParameter("specversion");
        String specURI = request.getParameter("specuri");
        String taskID = request.getParameter("taskID");
        long start = System.nanoTime();
        
        try {
            debug(request, "Post");
            if (_engine.isRedundantMode() && ! isAllowedRedundantAction(action)) {
                return fail("Unable to process request - engine is in redundant mode");
            }

            if (action != null) {
                if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnection(sessionHandle));
                }
                else if (action.equals("connect")) {
                    String userID = request.getParameter("userid");
                    String password = request.getParameter("password");
                    int interval = request.getSession().getMaxInactiveInterval();
                    msg.append(_engine.connect(userID, password, interval));
                }
                else if ("disconnect".equals(action)) {
                    msg.append(_engine.disconnect(sessionHandle));
                }
                else if (action.equals("checkout")) {
                    msg.append(_engine.startWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("checkin")) {
                    String data = request.getParameter("data");
                    String logPredicate = request.getParameter("logPredicate");
                    msg.append(_engine.completeWorkItem(workItemID, data, logPredicate, false,
                            sessionHandle));
                }
                else if (action.equals("rejectAnnouncedEnabledTask")) {
                    msg.append(_engine.rejectAnnouncedEnabledTask(workItemID, sessionHandle));
                }
                else if (action.equals("launchCase")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    URI completionObserver = getCompletionObserver(request);
                    String caseParams = request.getParameter("caseParams");
                    String logDataStr = request.getParameter("logData");
                    String caseID = request.getParameter("caseid");
                    String mSecStr = request.getParameter("mSec");
                    String startStr = request.getParameter("start");
                    String waitStr = request.getParameter("wait");
                    if (mSecStr != null) {
                        msg.append(_engine.launchCase(specID, caseParams,
                                   completionObserver, logDataStr,
                                   StringUtil.strToLong(mSecStr, 0), sessionHandle));
                    }
                    else if (startStr != null) {
                        long time = StringUtil.strToLong(startStr, 0);
                        Date date = time > 0 ? new Date(time) : new Date();
                        msg.append(_engine.launchCase(specID, caseParams,
                                   completionObserver, logDataStr, date, sessionHandle));
                    }
                    else if (waitStr != null) {
                        msg.append(_engine.launchCase(specID, caseParams,
                                   completionObserver, logDataStr,
                                   StringUtil.strToDuration(waitStr), sessionHandle));
                    }
                    else if (caseID != null) {
                        msg.append(_engine.launchCase(specID, caseParams,
                                completionObserver, caseID, logDataStr, sessionHandle));
                    }
                    else msg.append(_engine.launchCase(specID, caseParams,
                                    completionObserver, logDataStr, sessionHandle));
                }
                else if (action.equals("cancelCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.cancelCase(caseID, sessionHandle));
                }
                else if (action.equals("getWorkItem")) {
                    msg.append(_engine.getWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("startOne")) {
                    String userID = request.getParameter("user");
                    msg.append(_engine.startWorkItem(userID, sessionHandle));
                }
                else if (action.equals("getLiveItems")) {
                    msg.append(_engine.describeAllWorkItems(sessionHandle));
                }
                else if (action.equals("getAllRunningCases")) {
                    msg.append(_engine.getAllRunningCases(sessionHandle));
                }
                else if (action.equals("getWorkItemsWithIdentifier")) {
                    String idType = request.getParameter("idType");
                    String id = request.getParameter("id");
                    msg.append(_engine.getWorkItemsWithIdentifier(idType, id, sessionHandle));
                }
                else if (action.equals("getWorkItemsForService")) {
                    String serviceURI = request.getParameter("serviceuri");
                    msg.append(_engine.getWorkItemsForService(serviceURI, sessionHandle));
                }
                else if (action.equals("taskInformation")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getTaskInformation(specID, taskID, sessionHandle));
                }
                else if (action.equals("getMITaskAttributes")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getMITaskAttributes(specID, taskID, sessionHandle));
                }
                else if (action.equals("getResourcingSpecs")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
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
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getProcessDefinition(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationData")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getSpecificationData(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationDataSchema")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getSpecificationDataSchema(specID, sessionHandle));
                }
                else if (action.equals("getCasesForSpecification")) {
                    YSpecificationID specID =
                            new YSpecificationID(specIdentifier, specVersion, specURI);
                    msg.append(_engine.getCasesForSpecification(specID, sessionHandle));
                }
                else if (action.equals("getSpecificationForCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getSpecificationForCase(caseID, sessionHandle));
                }
                else if (action.equals("getSpecificationIDForCase")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getSpecificationIDForCase(caseID, sessionHandle));
                }
                else if (action.equals("getCaseState")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseState(caseID, sessionHandle));
                }
                else if (action.equals("exportCaseState")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.exportCaseState(caseID, sessionHandle));
                }
                else if (action.equals("exportAllCaseStates")) {
                    msg.append(_engine.exportAllCaseStates(sessionHandle));
                }
                else if (action.equals("importCases")) {
                    String xml = request.getParameter("xml");
                    msg.append(_engine.importCases(xml, sessionHandle));
                }
                else if (action.equals("getCaseData")) {
                    String caseID = request.getParameter("caseID");
                    msg.append(_engine.getCaseData(caseID, sessionHandle));
                }
                else if (action.equals("getChildren")) {
                    msg.append(_engine.getChildrenOfWorkItem(workItemID, sessionHandle));
                }
                else if (action.equals("getWorkItemExpiryTime")) {
                    msg.append(_engine.getWorkItemExpiryTime(workItemID, sessionHandle));
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
                else if (action.equals("getStartingDataSnapshot")) {
                    msg.append(_engine.getStartingDataSnapshot(workItemID, sessionHandle));
                }
                else if (action.equals("pollPerfStats")) {
                    msg.append(PerfReporter.poll());
                }
                if (_gatherPerfStats) PerfReporter.add(action, start);
            }  // action is null
            else if (request.getRequestURI().endsWith("ib")) {
                msg.append(_engine.getAvailableWorkItemIDs(sessionHandle));
            }
            else if (request.getRequestURI().contains("workItem")) {
                msg.append(_engine.getWorkItemOptions(workItemID,
                        request.getRequestURL().toString(), sessionHandle));
            }
            else _log.error("Interface B called with null action.");
        }
        catch (RemoteException e) {
            _log.error("Remote Exception in Interface B with action: " + action, e);
        }
        _log.debug("InterfaceB_EngineBasedServer::doPost() result = {}", msg);
        return msg.toString();
    }


    private URI getCompletionObserver(HttpServletRequest request) {
        String completionObserver = request.getParameter("completionObserverURI");
        if(completionObserver != null) {
            try {
                return new URI(completionObserver);
            } catch (URISyntaxException e) {
                _log.error("Failure to ", e);
            }
        }
        return null;
    }                                         


    private void debug(HttpServletRequest request, String service) {
        if (_log.isDebugEnabled()) {
            _log.debug("\nInterfaceB_EngineBasedServer::do{}() request.getRequestURL={}",
                    service, request.getRequestURL());
            _log.debug("\nInterfaceB_EngineBasedServer::do{}() request.parameters:", service);
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                _log.debug("\trequest.getParameter({}) = {}", name, request.getParameter(name));
            }
        }
    }

}
