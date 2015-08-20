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

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.calendar.ResourceCalendar;
import org.yawlfoundation.yawl.resourcing.calendar.ResourceScheduler;
import org.yawlfoundation.yawl.resourcing.calendar.TimeSlot;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.util.XNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


/**
 *  The Calendar Gateway class acts as an API gateway between the resource service and
 *  its calendar sub-service.
 *
 *  @author Michael Adams
 *  31/08/2010
 */

public class ResourceCalendarGateway extends HttpServlet {

    private static ResourceManager _rm;
    private static ResourceCalendar _calendar;
    private static ResourceScheduler _scheduler;

    private static final String _success = "<success/>";
    private static final String _noResource = "<failure>Unknown Resource.</failure>";
    private static final String _nullResource =
            "<failure>Null Resource record supplied.</failure>";
    private static final String _invalidResource =
            "<failure>Invalid Resource record supplied - unable to parse.</failure>";
    private static final String _noService =
            "<failure>Not connected to Resource Service.</failure>";
    private static final String _noAction =
            "<failure>Resource Calendar Gateway called with invalid action.</failure>";


    public void init() {
        _rm = ResourceManager.getInstance();
        _calendar = ResourceCalendar.getInstance();
        _scheduler = ResourceScheduler.getInstance();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String result = "";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        String id = req.getParameter("id");

        if (action == null) {
            throw new IOException("ResourceCalendarGateway called with null action.");
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
        else if (action.equalsIgnoreCase("disconnect")) {
            if (_rm != null) {
                _rm.serviceDisconnect(handle);
                result = "true";
            }
            else result = _noService;
        }
        else if (validConnection(handle)) {
            if (action.equals("getAvailability")) {
                long fromDate = strToLong(req.getParameter("from"));
                long toDate = strToLong(req.getParameter("to"));
                String resourceXML = req.getParameter("resourceXML");
                if (resourceXML != null) {
                    List<TimeSlot> slots = _scheduler.getAvailability(resourceXML, fromDate, toDate);
                    if (slots != null) {
                        result = timeSlotsToXML(new XNode("timeslots"), slots);
                    }
                    else result = _invalidResource;
                }
                else result = _nullResource;
            }
            else if (action.equals("getResourceAvailability")) {
                long fromDate = strToLong(req.getParameter("from"));
                long toDate = strToLong(req.getParameter("to"));
                AbstractResource resource = _rm.getOrgDataSet().getResource(id);
                if (resource != null) {
                    List<TimeSlot> slots = _calendar.getAvailability(resource, fromDate, toDate);
                    result = timeSlotsToXML(id, slots);
                }
                else result = _noResource;
            }
            else if (action.equals("setBlockedDuration")) {
                AbstractResource resource = _rm.getOrgDataSet().getResource(id);
                if (resource != null) {
                    resource.setBlockedDuration(req.getParameter("duration"));
                    result = String.valueOf(resource.getBlockedDuration());
                }
                else result = _noResource;
            }
            else if (action.equals("getReservations")) {
                String resources = req.getParameter("resource");
                long fromDate = strToLong(req.getParameter("from"));
                long toDate = strToLong(req.getParameter("to"));
                result = _scheduler.getReservations(resources, fromDate, toDate);
            }
            else if (action.equals("saveReservations")) {
                String plan = req.getParameter("plan");
                String checkStr = req.getParameter("checkOnly");
                boolean checkOnly = (checkStr != null) && checkStr.equalsIgnoreCase("true");
                String agent = _rm.getUserIDForSessionHandle(handle);
                result = _scheduler.saveReservations(plan, agent, checkOnly);
            }
            else if (action.equals("registerStatusChangeListener")) {
                String uri = req.getParameter("uri");
                result = _rm.registerCalendarStatusChangeListener(uri, handle);
            }
            else if (action.equals("removeStatusChangeListener")) {
                String uri = req.getParameter("uri");
                _rm.removeCalendarStatusChangeListener(uri, handle);
                result = _success;
            }
            else result = _noAction;
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


    private long strToLong(String s) {
        if (s == null) return -1;
        try {
            return new Long(s);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }


    private String timeSlotsToXML(String id, List<TimeSlot> slots) {
        if (slots == null) return null;
        XNode node = new XNode("timeslots");
        node.addAttribute("id", id);
        return timeSlotsToXML(node, slots);
    }


    private String timeSlotsToXML(XNode node, List<TimeSlot> slots) {
         for (TimeSlot slot : slots) {
             node.addChild(slot.toXNode());
         }
         return node.toString();
     }


    private String fail(String msg) {
        return "<failure>" + msg + "</failure>";
    }

}