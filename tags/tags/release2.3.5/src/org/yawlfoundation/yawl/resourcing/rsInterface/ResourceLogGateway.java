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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;


/**
 *  The Log Gateway class acts as an API gateway between the resource service and
 *  its process logs.
 *
 *  @author Michael Adams
 *  03/02/2010
 */

public class ResourceLogGateway extends HttpServlet {

    private static final Logger _log = Logger.getLogger(ResourceLogGateway.class);
    private LogMiner _logDB ;
    private ResourceManager _rm;

    private final String _noService = "<failure>Not connected to Resource Service.</failure>";
    private final String _badPre = "<failure>Resource Log Gateway called with invalid ";
    private final String _badAction = _badPre + "action.</failure>";
    private final String _badEvent = _badPre + "event name.</failure>";
    private final String _badSpecID = _badPre + "specification ID.</failure>";
    private final String _badTimestamp = _badPre + "timestamp value.</failure>";
 

    public void init() {
        _logDB = LogMiner.getInstance() ;
        _rm = ResourceManager.getInstance();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {
        String result = "";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        String id = req.getParameter("id") ;

        if (action == null) {
            throw new IOException("ResourceLogGateway called with null action.");
        }
        else if (action.equalsIgnoreCase("connect")) {
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           if (_rm != null) {
               int interval = req.getSession().getMaxInactiveInterval();
               result = _rm.serviceConnect(userid, password, interval);
           }
           else result = _noService;
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
           if (_rm != null)
               result = String.valueOf(_rm.checkServiceConnection(handle));
           else result = _noService;
       }
       else if (validConnection(handle)) {
            long from = getLong(req.getParameter("from"));
            long to = getLong(req.getParameter("to"));

           if (action.equals("getCaseEvent")) {
               String launchStr = req.getParameter("launch") ;
               boolean launch = (launchStr != null) && launchStr.equalsIgnoreCase("true");
               result = _logDB.getCaseEvent(id, launch);
           }
           else if (action.equals("getCaseEvents")) {
               result = _logDB.getCaseEvents(id, from, to);
           }
           else if (action.equals("getCaseStartedBy")) {
               result = _logDB.getCaseStartedBy(id);
           }
           else if (action.equals("getWorkItemEvents")) {
               String fnStr = req.getParameter("fullname") ;
               boolean fullName = (fnStr != null) && fnStr.equalsIgnoreCase("true");
               result = _logDB.getWorkItemEvents(id, fullName, from, to);
           }
           else if (action.equals("getParticipantHistory")) {
               result = _logDB.getParticipantHistory(id, from, to);
           }
           else if (action.equals("getResourceHistory")) {
               result = _logDB.getResourceHistory(id, from, to);
           }
           else if (action.equals("getParticipantHistoryForEvent")) {
               String eventStr = req.getParameter("eventType");
               EventLogger.event event = EventLogger.getEventByName(eventStr);
               if (event != null) {
                   result = _logDB.getParticipantHistoryForEvent(id, event, from, to);
               }
               else result = _badEvent;     
           }
           else if (action.equals("getResourceHistoryForEvent")) {
               String eventStr = req.getParameter("eventType");
               EventLogger.event event = EventLogger.getEventByName(eventStr);
               if (event != null) {
                   result = _logDB.getResourceHistoryForEvent(id, event, from, to);
               }
               else result = _badEvent;
           }
           else if (action.equals("getWorkItemOffered")) {
               result = _logDB.getWorkItemOffered(id);
           }
           else if (action.equals("getWorkItemAllocated")) {
               result = _logDB.getWorkItemAllocated(id);
           }
           else if (action.equals("getWorkItemStarted")) {
               result = _logDB.getWorkItemStarted(id);
           }
           else if (action.equals("getCaseHistoryInvolvingParticipant")) {
               result = _logDB.getCaseHistoryInvolvingParticipant(id, from, to);
           }
           else if (action.equals("getSpecificationEvents")) {
               YSpecificationID specID = constructSpecID(req);
               result = (specID != null) ? _logDB.getSpecificationEvents(specID, from, to)
                                         : _badSpecID;
           }
           else if (action.equals("getSpecificationSetEvents")) {
               String setXML = req.getParameter("setxml");
               Set<YSpecificationID> idSet = constructSpecificationIDSet(setXML);
               result = (idSet != null) ? _logDB.getSpecificationEvents(idSet, from, to)
                                        : _badSpecID;
           }
           else if (action.equals("getSpecificationEventsByURI")) {
               result = _logDB.getSpecificationEventsByURI(id, from, to);
           }
           else if (action.equals("getSpecificationEventsByID")) {
               result = _logDB.getSpecificationEventsByID(id, from, to);
           }
           else if (action.equals("getSpecificationStatistics")) {
               YSpecificationID specID = constructSpecID(req);
               result = (specID != null) ? _logDB.getSpecificationStatistics(specID, from, to)
                                         : _badSpecID;
           }
           else if (action.equals("getTaskStatisticsForCase")) {
               result = _logDB.getTaskStatisticsForCase(id, from, to);
           }
           else if (action.equals("getTaskStatisticsForSpecification")) {
               YSpecificationID specID = constructSpecID(req);
               result = (specID != null) ?
                       _logDB.getTaskStatisticsForSpecification(specID, from, to) : _badSpecID;
           }
           else if (action.equals("getTaskStatisticsForSpecificationSet")) {
               String setXML = req.getParameter("setxml");
               Set<YSpecificationID> idSet = constructSpecificationIDSet(setXML);
               result = (idSet != null) ?
                       _logDB.getTaskStatisticsForSpecificationSet(idSet, from, to) :
                       _badSpecID;
           }
           else if (action.equals("getTaskStatisticsForSpecificationURI")) {
               result = _logDB.getTaskStatisticsForSpecificationURI(id, from, to);
           }
           else if (action.equals("getTaskStatisticsForSpecificationUID")) {
               result = _logDB.getTaskStatisticsForSpecificationUID(id, from, to);
           }
           else if (action.equals("getTaskStatistics")) {
               YSpecificationID specID = constructSpecID(req);
               String taskName = req.getParameter("taskname");
               result = (specID != null) ? _logDB.getTaskStatistics(specID, taskName, from, to)
                                         : _badSpecID;
           }
           else if (action.equals("getSpecificationIdentifiers")) {
               String key = req.getParameter("key");
               result = _logDB.getSpecificationIdentifiers(key);
           }
           else if (action.equals("getSpecificationXESLog")) {
               YSpecificationID specID = constructSpecID(req);
               result = (specID != null) ? _logDB.getSpecificationXESLog(specID) : _badSpecID;
           }
           else if (action.equals("getMergedXESLog")) {
               YSpecificationID specID = constructSpecID(req);
               String withDataStr = req.getParameter("withdata");
               boolean withData = (withDataStr != null) && withDataStr.equalsIgnoreCase("true");
               result = (specID != null) ? _logDB.getMergedXESLog(specID, withData) : _badSpecID;
           }
           else if (action.equals("getAllResourceEvents")) {
               result = _logDB.getAllResourceEvents();
           }
           else result = _badAction;
       }
       else throw new IOException("Invalid or disconnected session handle.");

       // generate the output
       res.setContentType("text/xml; charset=UTF-8");
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
            return _rm.checkServiceConnection(handle) ;
        }
        catch (Exception e) {
            return false;
        }
    }


    private long getLong(String s) {
        try {
            return new Long(s);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


    private YSpecificationID constructSpecID(HttpServletRequest req) {
        String version = req.getParameter("version") ;
        String uri = req.getParameter("uri") ;
        if ((uri != null) && (version != null)) {
            String identifier = req.getParameter("identifier") ;
            return new YSpecificationID(identifier, version, uri);
        }
        else return null;
    }


    private Set<YSpecificationID> constructSpecificationIDSet(String xml) {
        Set<YSpecificationID> specSet = new TreeSet<YSpecificationID>();
        XNode specs = new XNodeParser().parse(xml);
        if (specs != null) {
            for (XNode spec : specs.getChildren()) {
                specSet.add(new YSpecificationID(spec.getChildText("identifier"),
                        spec.getChildText("version"), spec.getChildText("uri")));
            }
        }
        return specSet;
    }

}