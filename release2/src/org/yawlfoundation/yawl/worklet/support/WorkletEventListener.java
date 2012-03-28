/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.support;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Map;

/**
 * An abstract class to be extend by classes that wish to be notified of worklet
 * service events. A class must first be registered as a listener (see
 * WorkletGatewayClient#addListener) to receive events via this class.
 *
 * @author Michael Adams
 * @date 27/03/12
 */
public abstract class WorkletEventListener extends HttpServlet {

    public void destroy() {
        shutdown();
    }


    /**
     * Receives event notifications from the Worklet Service and passes them on to
     * extending classes via the appropriate method calls
     * @param req the http request
     * @param res the http response
     * @throws IOException
     * @throws ServletException
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String action = req.getParameter("action");
        if (action.equals("Shutdown")) {
            shutdown();
        }
        else {
            String ruleTypeStr = req.getParameter("ruletype");
            RuleType ruleType = (ruleTypeStr != null) ? RuleType.valueOf(ruleTypeStr) : null;
            Element caseData = JDOMUtil.stringToElement(req.getParameter("casedata"));
            String wirStr = req.getParameter("wir");
            WorkItemRecord wir = wirStr != null ? Marshaller.unmarshalWorkItem(wirStr) : null;

            if (action.equals("Selection")) {
                String caseIDs = req.getParameter("caseIDs");
                String wNames = req.getParameter("workletNames");
                Map<String, String> caseMap = new Hashtable<String, String>();
                if (caseIDs != null) {
                    String[] ids = caseIDs.split(",");
                    String[] names = wNames.split(",");
                    for (int i=0; i < ids.length; i++) {
                        caseMap.put(ids[i], names[i]);
                    }
                }
                selectionEvent(wir, caseMap);
            }
            else if (action.equals("CaseException")) {
                String caseID = req.getParameter("caseid");
                caseLevelExceptionEvent(caseID, caseData, ruleType);
            }
            else if (action.equals("ItemException")) {
                itemLevelExceptionEvent(wir, caseData, ruleType);
            }
        }
    }


    /**
     * Replies to browsers with a simple message
     * @param req the http request
     * @param res the http response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
        StringBuilder output = new StringBuilder();
        output.append("Greetings from the WorkletEventListener Servlet");
        ServletUtils.finalizeResponse(outputWriter, output);
    }

    /****************************************************************************/

    // methods requiring implementation by extending classes


    /**
     * Receives notification of a selection (substitution) by the worklet service
     * @param wir the workitem that has been replaced by a worklet
     * @param caseMap a map of [caseID, worklet name] pairs, each one representing a
     *                a worklet case launched for the workitem (one for single-instance
     *                tasks, several for multiple-instance tasks)
     */
    public abstract void selectionEvent(WorkItemRecord wir, Map<String, String> caseMap);


    /**
     * Receives notification of a selection (substitution) by the worklet service
     * @param caseID the case on which the exception has been raised
     * @param caseData the current caseData, used to evaluate the exception
     * @param ruleType the type of exception raised
     */
    public abstract void caseLevelExceptionEvent(String caseID, Element caseData,
                                                 RuleType ruleType);
   
    /**
     * Receives notification of a selection (substitution) by the worklet service
     * @param wir the workitem for which the exception has been raised
     * @param caseData the current caseData, used to evaluate the exception
     * @param ruleType the type of exception raised
     */
    public abstract void itemLevelExceptionEvent(WorkItemRecord wir, Element caseData,
                                                 RuleType ruleType);

    /**
     * Receives notification that the worklet service is shutting down
     */
    public abstract void shutdown();



}
