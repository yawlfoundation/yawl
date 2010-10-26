/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarEntry;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarException;
import org.yawlfoundation.yawl.resourcing.calendar.ResourceCalendar;
import org.yawlfoundation.yawl.resourcing.calendar.ScheduleStateException;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;

/**
 * @author Michael Adams
 * @date 18/10/2010
 */
public class UtilisationLogger {

    private ResourceGatewayServer _gateway;
    private static UtilisationLogger _me;
    private ResourceCalendar _calendar;
    private ResourceManager _rm;
    private Persister _persister;

    private UtilisationLogger() {
        _rm = ResourceManager.getInstance();
        _gateway = _rm.getGatewayServer();
        _calendar = ResourceCalendar.getInstance();
        _persister = Persister.getInstance();
    }

    public static UtilisationLogger getInstance() {
        if (_me == null) _me = new UtilisationLogger();
        return _me;
    }


//    public void announceUtilisationRequest(String caseID, String activityID, long timestamp)
//            throws IOException {
//        if (_gateway.hasSchedulingListener()) {
//            _gateway.announceUtilisationRequest(caseID, activityID, timestamp);
//        }
//        else {
//            throw new IOException("Could not announce utilisation request for case '" +
//            caseID + "', activity '" + activityID + "': no scheduling service defined.");
//        }
//    }
//
//
//    public void announceUtilisationRelease(String caseID, String activityID, long timestamp)
//            throws IOException {
//        if (_gateway.hasSchedulingListener()) {
//            _gateway.announceUtilisationRelease(caseID, activityID, timestamp);
//        }
//        else {
//            throw new IOException("Could not announce utilisation release for case '" +
//            caseID + "', activity '" + activityID + "': no scheduling service defined.");
//        }
//    }


    public String saveReservations(String planXML, boolean checkOnly) {
        XNode planNode = new XNodeParser(true).parse(planXML);
        UtilisationPlan plan = saveReservations(new UtilisationPlan(planNode), checkOnly);
        return plan.toXML();
    }


    public UtilisationPlan saveReservations(UtilisationPlan plan, boolean checkOnly) {
        for (Activity activity : plan.getActivityList()) {
            long from = activity.getFromAsLong();
            long to = activity.getToAsLong();
            if ((from > 0) && (to > 0)) {
                for (Reservation reservation : activity.getReservationList()) {
                    if (checkOnly) checkReservation(reservation, from, to);
                    else {
                        UtilisationLogEntry logEntry = new UtilisationLogEntry();
                        logEntry.setCaseID(plan.getCaseID());
                        logEntry.setActivityID(activity.getActivityID());
                        saveReservation(reservation, from, to, logEntry);
                    }
                }
            }    
        }
        return plan;
    }


    public String getReservations(String resource, long from, long to) {
        XNode resNode = new XNodeParser(true).parse(resource);
        XNode setNode = new XNode("UtilisationPlans");
        for (UtilisationPlan plan : getReservations(new UtilisationResource(resNode), from, to)) {
            setNode.addChild(plan.toXNode());
        }
        return setNode.toString();
    }


    public Set<UtilisationPlan> getReservations(UtilisationResource uResource, long from, long to) {
        int i = -1;
        Map<String, UtilisationPlan> planSet = new Hashtable<String, UtilisationPlan>();
        for (AbstractResource resource : getActualResourceList(uResource)) {
            for (Object o : _calendar.getTimeSlotEntries(resource, from, to)) {
                CalendarEntry calEntry = (CalendarEntry) o;
                UtilisationLogEntry logEntry = getLogEntry(calEntry.getEntryID());
                String caseID = null;
                String activityID = null;
                UtilisationPlan plan = null;
                if (logEntry != null) {
                    caseID = logEntry.getCaseID();
                    activityID = logEntry.getActivityID();
                    plan = planSet.get(caseID);
                }

                if (plan != null) {
                    Activity activity = activityID != null ? plan.getActivity(activityID) : null;

                    // add calEntry to plan
                    if (activity == null) {
                        plan.addActivity(reconstructActivity(calEntry, activityID));
                    }
                    else {
                        activity.addReservation(reconstructReservation(calEntry));
                    }
                }
                else {
                    // make a new plan
                    plan = reconstructPlan(logEntry, calEntry);
                    String index = (caseID != null) ? caseID : "" + i--;
                    planSet.put(index, plan);
                }
            }
        }        
        return new HashSet<UtilisationPlan>(planSet.values());
    }


    private void checkReservation(Reservation reservation, long from, long to) {
  //      if (reservation == null) return;     todo
    }

    
    private void saveReservation(Reservation reservation, long from, long to,
                                 UtilisationLogEntry logEntry) {
        if (reservation == null) return;
        try {
            if (reservation.getReservationID() != null) {
                updateReservation(reservation, logEntry);
            }
            else {
                AbstractResource resource = getActualResource(reservation.getResource());
                makeReservation(reservation, resource, from, to, logEntry);
            }
        }
        catch (Exception e) {
            reservation.setError(e.getMessage());
        }
    }


    private AbstractResource getActualResource(UtilisationResource resource)
            throws CalendarException {
        return randomSelection(getActualResourceList(resource));
    }

    
    private List<AbstractResource> getActualResourceList(UtilisationResource resource) {
        List<AbstractResource> resourceList = new ArrayList<AbstractResource>();
        if (resource.getID() != null) {
            resourceList.add(_rm.getOrgDataSet().getResource(resource.getID()));
        }
        else if (resource.getRole() != null) {
            Role role = _rm.getOrgDataSet().getRole(resource.getRole());
            if (role != null) {

                // filter role members by capability
                if (resource.getCapability() != null) {
                    Capability cap = _rm.getOrgDataSet().getCapability(resource.getCapability());
                    for (AbstractResource member : role.getResources()) {
                        if (((Participant) member).getCapabilities().contains(cap)) {
                           resourceList.add(member);
                        }
                    }
                }
                else resourceList.addAll(role.getResources());
            }
        }
        else if (resource.getCategory() != null) {
            resourceList.addAll(_rm.getOrgDataSet().getNonHumanResources(
                    resource.getCategory(), resource.getSubcategory()));
        }
        return resourceList;
    }


    private AbstractResource randomSelection(List<AbstractResource> resources)
            throws CalendarException {
        if ((resources == null) || resources.isEmpty()) {
            throw new CalendarException("Failed to resolve resource.");
        }
        else if (resources.size() == 1) {
            return resources.get(0);
        }
        else {
            return resources.get((int) Math.floor(Math.random() * resources.size()));
        }
    }


    private void updateReservation(Reservation reservation, UtilisationLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        long entryID = StringUtil.strToLong(reservation.getReservationID(), -1);
        if (entryID == -1) {
            throw new CalendarException("Invalid reservation id: " +
                    reservation.getReservationID());
        }
        CalendarEntry entry = _calendar.getEntry(entryID);
        if (entry != null) {
            reservation.setStatus(entry.getStatus());               // current status
            ResourceCalendar.Status statusToBe =
                    _calendar.strToStatus(reservation.getStatusToBe());
            switch (statusToBe) {
                case Available : _calendar.makeAvailable(entryID); break;
                case Reserved  : _calendar.confirm(entryID); break;
                case Requested : _calendar.unconfirm(entryID); break;
                default: throw new CalendarException(
                        "Invalid status change request: " + statusToBe.name());
            }
            reservation.setStatus(statusToBe.name());        // if no errors, new status
            log(logEntry, entry.getResourceID(), statusToBe, entryID);
        }
        else throw new CalendarException("Unknown reservation with id: " +
                    reservation.getReservationID());
    }


    private void makeReservation(Reservation reservation, AbstractResource resource,
                                 long from, long to, UtilisationLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        long entryID = 0;
        reservation.setStatus(ResourceCalendar.Status.Nil.name());     // default status
        ResourceCalendar.Status statusToBe =
                _calendar.strToStatus(reservation.getStatusToBe());
        switch (statusToBe) {
            case Available: _calendar.makeAvailable(resource, from, to); break;
            case Unavailable: entryID = _calendar.makeUnavailable(resource, from, to, null); break;
            case Requested: entryID = _calendar.book(resource, from, to, null); break;
            case Reserved: entryID = _calendar.reserve(resource, from, to, null); break;
            default: throw new CalendarException(
                    "Invalid status change request: " + statusToBe.name());
        }
        if (entryID > 0) reservation.setReservationID(String.valueOf(entryID));
        reservation.setStatus(statusToBe.name());
        reservation.getResource().setID(resource.getID());
        log(logEntry, resource.getID(), statusToBe, entryID);
    }


    private void log(UtilisationLogEntry logEntry, String resourceID,
                     ResourceCalendar.Status status, long entryID) {
        logEntry.setResourceID(resourceID);
        logEntry.setStatus(status.name());
        logEntry.setTimestamp(new Date().getTime());
        logEntry.setCalendarKey(entryID);
        _persister.insert(logEntry);
    }


    private UtilisationLogEntry getLogEntry(long calEntryID) {
        List list = _persister.selectWhere("UtilisationLogEntry", "calendarKey=" + calEntryID);
        return (list == null) || list.isEmpty() ? null : (UtilisationLogEntry) list.get(0);
    }


    private UtilisationPlan reconstructPlan(UtilisationLogEntry logEntry,
                                            CalendarEntry calEntry) {
        String caseID = (logEntry != null) ? logEntry.getCaseID() : null;
        String activityID = (logEntry != null) ? logEntry.getActivityID() : null;

        UtilisationPlan plan = new UtilisationPlan(caseID);
        plan.addActivity(reconstructActivity(calEntry, activityID));
        return plan;
    }


    private Activity reconstructActivity(CalendarEntry calEntry, String id) {
        String from = StringUtil.longToDateTime(calEntry.getStartTime());
        String to = StringUtil.longToDateTime(calEntry.getEndTime());

        Activity activity = new Activity(null, id, null, from, to, null);
        activity.addReservation(reconstructReservation(calEntry));
        return activity;
    }


    private Reservation reconstructReservation(CalendarEntry calEntry) {
        Reservation reservation = new Reservation();
        reservation.setReservationID(String.valueOf(calEntry.getEntryID()));
        reservation.setStatus(calEntry.getStatus());
        reservation.setResource(reconstructResource(calEntry));
        return reservation;
    }


    private UtilisationResource reconstructResource(CalendarEntry calEntry) {
        UtilisationResource resource = new UtilisationResource();
        resource.setID(calEntry.getResourceID());
        return resource;
    }

}
