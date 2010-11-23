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

package org.yawlfoundation.yawl.resourcing.calendar;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.calendar.utilisation.*;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;

/**
 * The implementation of server side of the calendar gateway (Interface S).
 * @author Michael Adams
 * @date 2/11/2010
 */
public class ResourceScheduler {

    private static ResourceScheduler _me;
    private ResourceCalendar _calendar;
    private ResourceManager _rm;
    private CalendarLogger _uLogger;

    private ResourceScheduler() {
        _rm = ResourceManager.getInstance();
        _calendar = ResourceCalendar.getInstance();
        _uLogger = new CalendarLogger();
    }


    /**
     * Gets an instance of this ResourceScheduler object
     * @return this ResourceScheduler instance
     */
    public static ResourceScheduler getInstance() {
        if (_me == null) _me = new ResourceScheduler();
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
        if (validatePlan(plan) && plan.hasActivities()) {
            for (Activity activity : plan.getActivityList()) {
                if (validateActivity(activity)) {
                    Set<Long> reservationIDs = new HashSet<Long>();
                    long from = activity.getFromAsLong();
                    long to = activity.getToAsLong();
                    for (Reservation reservation : activity.getReservationList()) {
                        if (checkOnly) checkReservation(reservation, from, to);
                        else {
                            CalendarLogEntry logEntry = new CalendarLogEntry();
                            logEntry.setCaseID(plan.getCaseID());
                            logEntry.setPhase(activity.getPhase());
                            logEntry.setActivityName(activity.getName());
                            logEntry.setAgent(agent);
                            logEntry.setResourceRec(reservation.getResource().toXML());
                            saveReservation(reservation, from, to, logEntry);
                            reservationIDs.add(reservation.getReservationIDAsLong());
                        }
                    }
                    handleCancellations(plan.getCaseID(), activity.getName(), reservationIDs);
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
        UtilisationReconstructor reconstructor = new UtilisationReconstructor();
        int i = -1;
        Map<String, UtilisationPlan> planSet = new Hashtable<String, UtilisationPlan>();

        // for all of the calendar entries and for each referenced resource
        for (AbstractResource resource : getActualResourceList(uResource)) {
            for (Object o : _calendar.getTimeSlotEntries(resource, from, to)) {
                CalendarEntry calEntry = (CalendarEntry) o;
                CalendarLogEntry logEntry =
                        _uLogger.getLogEntryForCalendarKey(calEntry.getEntryID());
                String caseID = null;
                String activityName = null;
                String phase = null;
                UtilisationPlan plan = null;
                if (logEntry != null) {
                    caseID = logEntry.getCaseID();
                    activityName = logEntry.getActivityName();
                    phase = logEntry.getPhase();
                    plan = planSet.get(caseID);
                }

                if (plan != null) {
                    Activity activity = (activityName == null) ? null :
                                         plan.getActivity(activityName) ;

                    // add calEntry to plan
                    if (activity == null) {
                        plan.addActivity(
                            reconstructor.reconstructActivity(calEntry, activityName, phase));
                    }
                    else {
                        activity.addReservation(
                                reconstructor.reconstructReservation(calEntry));
                    }
                }
                else {
                    // make a new plan
                    plan = reconstructor.reconstructPlan(logEntry, calEntry);
                    String index = (caseID != null) ? caseID : "" + i--;
                    planSet.put(index, plan);
                }
            }
        }
        return new HashSet<UtilisationPlan>(planSet.values());
    }


    /**
     * Builds an appropriate data set and announces a status change for a calendar record.
     * @param calEntry the calendar record that has had a status change
     */
    protected void notifyStatusChange(CalendarEntry calEntry) {
        XNode node = new XNode("StatusChange");
        long reservationID = calEntry.getEntryID();
        CalendarLogEntry logEntry = _uLogger.getLogEntryForCalendarKey(reservationID);
        if (logEntry != null) {

            // only notify if the changer is different to the original 'owner'
            if (! logEntry.getAgent().equals(calEntry.getAgent())) {
                node.addChild("CaseId", logEntry.getCaseID());
                node.addChild("ActivityName", logEntry.getActivityName());
                node.addChild("ReservationId", reservationID);

                XNode resource = new XNodeParser().parse(logEntry.getResourceRec());
                if (resource != null) {
                    resource.getChild("Id").setText(calEntry.getResourceID());
                    node.addChild(resource);
                }
                
                node.addChild("OldStatus", logEntry.getStatus());
                node.addChild("NewStatus", calEntry.getStatus());
                _rm.announceResourceCalendarStatusChange(logEntry.getAgent(), node.toString());
            }
        }
    }


    /*******************************************************************************/

    private boolean validatePlan(UtilisationPlan plan) {
        String caseID = plan.getCaseID();
        if (! _rm.isRunningCaseID(plan.getCaseID())) {
            plan.setError("There is no running case with id: " + caseID);
            return false;
        }
        return true;
    }


    private boolean validateActivity(Activity activity) {
        if (! activity.hasValidPhase()) {
            activity.setError("Invalid requestType: " + activity.getPhase());
            return false;
        }
        return activity.getFromAsLong() > 0 && activity.getToAsLong() > 0;
    }


    /**
     * Checks if a reservation for a period would succeed. If it would not, the check
     * will raise an exception that results in an error message being added to the
     * reservation.
     * @param reservation the reservation to check
     * @param from the start of the period
     * @param to the end of the period
     */
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


    /**
     * Attempts to save a reservation to the calendar. If the save doesn't succeed,
     * an exception will be raised that results in an error message being added to the
     * reservation.
     * @param reservation the reservation to save
     * @param from the start of the period
     * @param to the end of the period
     * @param logEntry a partially populated log entry
     */
    private void saveReservation(Reservation reservation, long from, long to,
                                 CalendarLogEntry logEntry) {
        if (reservation == null) return;
        try {
            if (reservation.getReservationID() != null) {          // pre-existing
                if (logEntry.getPhase().equals("EOU")) {           // end-of-utilisation
                    reconcileReservation(reservation, from, to, logEntry);
                }
                else {
                    updateReservation(reservation, logEntry);
                }    
            }
            else {                                                  // new
                AbstractResource resource = getActualResourceIfAvailable(reservation, from, to);
                CalendarEntry calEntry =
                        createCalendarEntry(reservation, resource, from, to, logEntry.getAgent());
                makeReservation(reservation, resource, calEntry, logEntry);
            }
        }
        catch (Exception e) {
            reservation.setError(e.getMessage());
        }
    }


    /**
     * Derives one resource from the resource specified in a reservation that is
     * available for the period requested.
     * @param reservation the reservation to save
     * @param from the start of the period
     * @param to the end of the period
     * @return the available resource, if possible
     * @throws CalendarException if a resource could not be resolved from the ids in
     * the reservation, or if no resource is available for the period, or if the
     * status to be in the reservation is invalid
     */
    private AbstractResource getActualResourceIfAvailable(Reservation reservation, long from, long to)
            throws CalendarException {
        List<AbstractResource> actualList = getActualResourceList(reservation.getResource());
        if ((actualList == null) || actualList.isEmpty()) {
            throw new CalendarException("Failed to resolve resource.");     // none found
        }

        // while the list has resources, remove a random selection and check if the
        // reservation would succeed
        while (actualList.size() > 0) {
            AbstractResource actual =
                    actualList.remove((int) Math.floor(Math.random() * actualList.size()));
            if (_calendar.canCreateEntry(actual, from, to, reservation.getStatusToBe(),
                    reservation.getWorkload())) {
                return actual;                                      // found a candidate
            }
        }
        throw new CalendarException("No specified resource available for period.");
    }


    /**
     * Builds a list of resources from the ids specified in a reservation's resource data.
     * @param resource the resource data set of a reservation. May contain a single id,
     * OR a Role and (optional) Capability pair OR a Category and (optional) Subcategory
     * of a non-human resource.
     * @return a list of resources matching the ids specified
     */
    private List<AbstractResource> getActualResourceList(UtilisationResource resource) {
        List<AbstractResource> resourceList = new ArrayList<AbstractResource>();

        // if a single resource is specified, get it
        if (resource.getID() != null) {
            resourceList.add(getActualResource(resource.getID()));
        }

        // if there is a role (and optionally a capability), get all matching resources
        else if (resource.getRole() != null) {
            resourceList.addAll(_rm.getOrgDataSet().getRoleParticipantsWithCapability(
                    resource.getRole(), resource.getCapability()));
        }

        // if there is a category (and optionally a subcategory), get all matches
        else if (resource.getCategory() != null) {
            resourceList.addAll(_rm.getOrgDataSet().getNonHumanResources(
                    resource.getCategory(), resource.getSubcategory()));
        }
        return resourceList;
    }


    /**
     * Gets the resource (participant or non-human) matching the specified id.
     * @param resourceID the id of the resource
     * @return the matching resource
     */
    private AbstractResource getActualResource(String resourceID) {
        return _rm.getOrgDataSet().getResource(resourceID);
    }


    /**
     * Updates the status of a reservation stored in the calendar.
     * @param reservation the updated reservation
     * @param logEntry the matching log entry of the update
     * @throws CalendarException if there's a problem locating the record or saving
     * the update
     * @throws ScheduleStateException if the status transition is invlaid
     */
    private void updateReservation(Reservation reservation, CalendarLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        long entryID = convertReservationID(reservation);
        CalendarEntry calEntry = getCalendarEntry(entryID);
        reservation.setStatus(calEntry.getStatus());               // current status
        ResourceCalendar.Status statusToBe =
                    _calendar.updateEntry(entryID, calEntry.getStatus());

        // if update raised no exceptions, update reservation and log
        reservation.setStatus(statusToBe.name());
        calEntry.setStatus(statusToBe.name());
        _uLogger.log(logEntry, calEntry);

        if (logEntry.getPhase().equals("SOU")) {                 // start-of-utilisation
            setResourceAvailable(logEntry.getCaseID(), reservation.getResource().getID(),
                    false);
        }
    }


    /**
     * Stores a reservation in the calendar.
     * @param reservation the reservation to store
     * @param resource the resource to make the reservation for
     * @param calEntry the calendar entry to save, representing the resource
     * @param logEntry the entry to save to the calendar log
     * @throws CalendarException if there's a problem saving the reservation
     * @throws ScheduleStateException if the status transition is invlaid
     */
    private void makeReservation(Reservation reservation, AbstractResource resource,
                                 CalendarEntry calEntry, CalendarLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        String statusToBe = reservation.getStatusToBe();
        reservation.setStatus(ResourceCalendar.Status.nil.name());     // default status
        long entryID = _calendar.createEntry(resource, calEntry);

        // if the entry was successful, update the reservation record  
        if (entryID > 0) reservation.setReservationID(String.valueOf(entryID));
        reservation.setStatus(statusToBe);
        reservation.getResource().setID(calEntry.getResourceID());
        _uLogger.log(logEntry, calEntry);

        if (logEntry.getPhase().equals("SOU")) {                 // start-of-utilisation
            setResourceAvailable(logEntry.getCaseID(), calEntry.getResourceID(), false);
        }
    }


    /**
     * Checks whether a new reservation can by made for the specified resource and period
     * (i.e. there are no conflicts with existing reservations).
     * @param reservation the reservation to store
     * @param from the start of the period
     * @param to the end of the period
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     */
    private void checkMakeReservation(Reservation reservation, long from, long to)
            throws CalendarException {
        boolean hasAvailableResource = false;

        // if at least one resource is available, then check succeeds
        if (reservation.hasResource()) {
            for (AbstractResource resource : getActualResourceList(reservation.getResource())) {
                if (_calendar.canCreateEntry(resource, from, to, reservation.getStatusToBe(),
                        reservation.getWorkload())) {
                    reservation.getResource().setID(resource.getID());  // available resource
                    hasAvailableResource = true;
                    break;
                }
            }
            if (! hasAvailableResource) {                       // no available resources
                reservation.setWarning("Reservation would fail.");
            }
        }
        else reservation.setWarning("No resource specified for reservation.");
    }


    /**
     * Checks whether updating the reservation in the calendar will succeed.
     * @param reservation the reservation to check
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     * @throws ScheduleStateException if the update involves an invalid status transition
     */
    private void checkUpdateReservation(Reservation reservation)
            throws CalendarException, ScheduleStateException {
        long entryID = convertReservationID(reservation);
        if (! _calendar.canUpdateEntry(entryID, reservation.getStatusToBe())) {
            reservation.setWarning("Reservation would fail.");
        }
    }


    /**
     * Reconciles and updates an EOU (end-of-utilisation) plan with the stored SOU
     * (start-of-utilisation) plan.
     * @param reservation the reservation to reconcile
     * @param from the start of the period
     * @param to actual end of the period
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     * @param logEntry the log entry associated with the reservation
     * @throws CalendarException if the reconciliation causes a clash in the
     * calendar
     */
    private void reconcileReservation(Reservation reservation, long from, long to,
                                      CalendarLogEntry logEntry)
            throws CalendarException {
        AbstractResource resource = getActualResource(reservation.getResource().getID());
        CalendarEntry calEntry = _calendar.reconcileEntry(
                resource, convertReservationID(reservation), from, to);
        _uLogger.log(logEntry, calEntry);

        // if the reconcile throws no exception, mark the resource as available, if it
        // doesn't have a post-usage 'blocked' period
        if (calEntry.getChainID() > 0) {
            CalendarEntry blockedEntry = getCalendarEntry(calEntry.getChainID());
            if ((blockedEntry != null) &&
                (blockedEntry.getEndTime() < System.currentTimeMillis())) {
                    setResourceAvailable(logEntry.getCaseID(),
                            reservation.getResource().getID(), true);
            }
        }
    }


    /**
     * Removes any reservations made in an earlier version of the plan that are no
     * longer included in the current plan
     * @param caseID the case id of the plan
     * @param activityName the name of the activity
     * @param entryIDs the ids of all the reservations for the activity
     */
    private void handleCancellations(String caseID, String activityName, Set<Long> entryIDs) {
        Set<Long> toCancel = new HashSet<Long>();

        // compare stored reservations to current ones, and extract the differences
        for (long id : _uLogger.getEntryIDsForActivity(caseID, activityName)) {
            if (! entryIDs.contains(id)) {
                toCancel.add(id);
            }
        }
        for (long cancelledID : toCancel) {
            try {
                _calendar.makeAvailable(cancelledID);             // removes the entry
            }
            catch (CalendarException ce) {
                // safe to ignore - thrown by missing id in calendar, so no more to do
            }
        }
    }


    /**
     * Converts the reservation id stored in a reservation record to its original
     * 'long' type
     * @param reservation the reservation with the id
     * @return the id as a long value
     * @throws CalendarException if the conversion is unsuccessful
     */
    private long convertReservationID(Reservation reservation) throws CalendarException {
        long entryID = StringUtil.strToLong(reservation.getReservationID(), -1);
        if (entryID == -1) {
            throw new CalendarException("Invalid reservation id: " +
                    reservation.getReservationID());
        }
        return entryID;
    }


    /**
     * Gets a Calendar entry matching the specified id
     * @param entryID the id to search for
     * @return the matching calendar entry
     * @throws CalendarException if no match is found
     */
    private CalendarEntry getCalendarEntry(long entryID) throws CalendarException {
        CalendarEntry entry = _calendar.getEntry(entryID);
        if (entry == null) {
            throw new CalendarException("Unknown reservation with id: " + entryID);
        }
        return entry;
    }


    /**
     * Creates a new CalendarEntry object, ready for storing in the calendar.
     * @param reservation a Reservation record
     * @param resource the resource associated with the reservation
     * @param from the start of the period
     * @param to the end of the period
     * @param agent the userid of the user or service creating the entry
     * @return the newly constructed calendar entry
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     */
    private CalendarEntry createCalendarEntry(Reservation reservation,
             AbstractResource resource, long from, long to, String agent)
             throws CalendarException {
        ResourceCalendar.Status status = _calendar.strToStatus(reservation.getStatusToBe());
        return new CalendarEntry(resource.getID(), from, to, status,
                                 reservation.getWorkload(), agent, null);
    }


    /**
     * Marks a resource as being in use
     * @param id the id of the resource
     * @param available true if 'available', false if 'in use'
     */
    private void setResourceAvailable(String caseID, String id, boolean available) {
        _rm.getOrgDataSet().setResourceAvailability(caseID, id, available);
    }

}
