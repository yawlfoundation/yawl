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
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;

/**
 * @author Michael Adams
 * @date 18/10/2010
 */
public class UtilisationLogger {

    private static UtilisationLogger _me;
    private ResourceCalendar _calendar;
    private ResourceManager _rm;
    private Persister _persister;

    private UtilisationLogger() {
        _rm = ResourceManager.getInstance();
        _calendar = ResourceCalendar.getInstance();
        _persister = Persister.getInstance();
    }


    /**
     * Gets an instance of this UtilisationLogger object
     * @return this UtilisationLogger instance
     */
    public static UtilisationLogger getInstance() {
        if (_me == null) _me = new UtilisationLogger();
        return _me;
    }


    /**
     * Saves and/or updates the reservations contained in a utilisation plan
     * @param planXML an XML reresentation of a utilisation plan
     * @param agent the user id of the user or service that generated the plan
     * @param checkOnly if true, checks if the reservations would succeed only, makes no
     * changes
     * @return an XML representation of the updated plan
     */
    public String saveReservations(String planXML, String agent, boolean checkOnly) {
        XNode planNode = new XNodeParser(true).parse(planXML);
        UtilisationPlan plan = saveReservations(new UtilisationPlan(planNode), agent, checkOnly);
        return plan.toXML();
    }


    /**
     * Saves and/or updates the reservations contained in a utilisation plan
     * @param plan the utilisation plan
     * @param agent the user id of the user or service that generated the plan
     * @param checkOnly if true, checks if the reservations would succeed only, makes no
     * changes
     * @return the updated plan
     */
    public UtilisationPlan saveReservations(UtilisationPlan plan, String agent, boolean checkOnly) {
        for (Activity activity : plan.getActivityList()) {
            long from = activity.getFromAsLong();
            long to = activity.getToAsLong();
            if ((from > 0) && (to > 0)) {
                for (Reservation reservation : activity.getReservationList()) {
                    if (checkOnly) checkReservation(reservation, from, to);
                    else {
                        UtilisationLogEntry logEntry = new UtilisationLogEntry();
                        logEntry.setCaseID(plan.getCaseID());
                        logEntry.setPhase(plan.getPhase());
                        logEntry.setActivityID(activity.getActivityID());
                        logEntry.setAgent(agent);
                        saveReservation(reservation, from, to, logEntry);
                    }
                }
            }    
        }
        return plan;
    }


    /**
     * Gets all the utilisation plans in which a resource is involved for the specified
     * period
     * @param resource an XML representation of a utilisation resource object, containing
     * reference(s) to the resources to get plans for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return an XML representation of the matching utilisation plans
     */
    public String getReservations(String resource, long from, long to) {
        XNode resNode = new XNodeParser(true).parse(resource);
        XNode setNode = new XNode("UtilisationPlans");
        for (UtilisationPlan plan : getReservations(new UtilisationResource(resNode), from, to)) {
            setNode.addChild(plan.toXNode());
        }
        return setNode.toString();
    }


    /**
     * Gets the set of utilisation plans in which a resource is involved for the specified
     * period
     * @param uResource a utilisation resource object, containing references to the
     * resource(s) to get plans for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return the set of matching utilisation plans
     */
    public Set<UtilisationPlan> getReservations(UtilisationResource uResource, long from, long to) {
        int i = -1;
        Map<String, UtilisationPlan> planSet = new Hashtable<String, UtilisationPlan>();

        // for all of the calendar entries for each referenced resource
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


    public void notifyStatusChange(CalendarEntry calEntry) {
        XNode node = new XNode("StatusChange");
        long reservationID = calEntry.getEntryID();
        UtilisationLogEntry logEntry = getLogEntry(reservationID);
        if (logEntry != null) {
            node.addChild("caseID", logEntry.getCaseID());
            node.addChild("activityID", logEntry.getActivityID());
            node.addChild("oldStatus", logEntry.getStatus());
        }
        node.addChild("reservationID", reservationID);
        node.addChild("newStatus", calEntry.getStatus());
        node.addChild("agent", calEntry.getAgent());
        node.addChild("startTime", calEntry.getStartTime());
        node.addChild("endTime", calEntry.getEndTime());
        _rm.announceResourceCalendarStatusChange(node.toString());
    }


    /*******************************************************************************/

    private void checkReservation(Reservation reservation, long from, long to) {
        if (reservation != null) {
            try {
                if (reservation.getReservationID() != null) {
                    checkUpdateReservation(reservation);
                }
                else {
                    checkMakeReservation(reservation, from, to);
                }
            }
            catch (Exception e) {
                reservation.setError(e.getMessage());
            }
        }
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
                CalendarEntry calEntry =
                        createCalendarEntry(reservation, resource, from, to, logEntry);
                makeReservation(reservation, resource, calEntry, logEntry);
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
            resourceList.addAll(_rm.getOrgDataSet().getRoleParticipantsWithCapability(
                    resource.getRole(), resource.getCapability()));
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
        return (resources.size() == 1) ? resources.get(0) :
                resources.get((int) Math.floor(Math.random() * resources.size()));
    }


    private void updateReservation(Reservation reservation, UtilisationLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        long entryID = convertReservationID(reservation);
        CalendarEntry calEntry = getCalendarEntry(entryID);
        reservation.setStatus(calEntry.getStatus());               // current status
        ResourceCalendar.Status statusToBe =
                _calendar.updateEntry(entryID, calEntry.getStatus());

        // if update raised no exceptions, update reservation and log
        reservation.setStatus(statusToBe.name());
        calEntry.setStatus(statusToBe.name());
        log(logEntry, calEntry);
    }


    private void makeReservation(Reservation reservation, AbstractResource resource,
                                 CalendarEntry calEntry, UtilisationLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        String statusToBe = reservation.getStatusToBe();
        reservation.setStatus(ResourceCalendar.Status.Nil.name());     // default status
        long entryID = _calendar.createEntry(resource, calEntry);
        if (entryID > 0) reservation.setReservationID(String.valueOf(entryID));
        reservation.setStatus(statusToBe);
        reservation.getResource().setID(calEntry.getResourceID());
        log(logEntry, calEntry);
    }


    private void checkMakeReservation(Reservation reservation, long from, long to)
            throws CalendarException, ScheduleStateException {

        // if at least one resource is available, then check succeeds
        for (AbstractResource resource : getActualResourceList(reservation.getResource())) {
             if (_calendar.canCreateEntry(resource, from, to, reservation.getStatusToBe(),
                     reservation.getWorkload())) {
                 reservation.getResource().setID(resource.getID());  // available resource
                 reservation.setWarning("Reservation would succeed.");
                 break;
             }
        }
        if (! reservation.hasWarning()) {                       // no available resources
            reservation.setWarning("Reservation would fail.");
        }
    }

    
    private void checkUpdateReservation(Reservation reservation)
            throws CalendarException, ScheduleStateException {
        long entryID = convertReservationID(reservation);
        if (_calendar.canUpdateEntry(entryID, reservation.getStatusToBe())) {
            reservation.setWarning("Reservation would succeed.");
        }
        else reservation.setWarning("Reservation would fail.");
    }


    private long convertReservationID(Reservation reservation) throws CalendarException {
        long entryID = StringUtil.strToLong(reservation.getReservationID(), -1);
        if (entryID == -1) {
            throw new CalendarException("Invalid reservation id: " +
                    reservation.getReservationID());
        }
        return entryID;
    }


    private CalendarEntry getCalendarEntry(long entryID) throws CalendarException {
        CalendarEntry entry = _calendar.getEntry(entryID);
        if (entry == null) {
            throw new CalendarException("Unknown reservation with id: " + entryID);
        }
        return entry;
    }


    private CalendarEntry createCalendarEntry(Reservation reservation,
             AbstractResource resource, long from, long to, UtilisationLogEntry logEntry)
             throws CalendarException {
        ResourceCalendar.Status status = _calendar.strToStatus(reservation.getStatusToBe());
        return new CalendarEntry(resource.getID(), from, to, status,
                                 reservation.getWorkload(), logEntry.getAgent(), null);    
    }


    private void log(UtilisationLogEntry logEntry, CalendarEntry calEntry) {
        logEntry.setResourceID(calEntry.getResourceID());
        logEntry.setStatus(calEntry.getStatus());
        logEntry.setTimestamp(new Date().getTime());
        logEntry.setWorkload(calEntry.getWorkload());
        logEntry.setCalendarKey(calEntry.getEntryID());
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
