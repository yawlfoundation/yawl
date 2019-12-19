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

package org.yawlfoundation.yawl.resourcing.calendar;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.calendar.utilisation.*;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
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

    // references to required objects
    private static ResourceScheduler _me;
    private final ResourceCalendar _calendar;
    private final ResourceManager _rm;
    private final CalendarLogger _uLogger;

    // set of added entry id's resulting from reassigned bookings
    private final Set<Long> _reassignedIDs;

    // set of update requests that have time changes - must be updated atomically
    private final Set<Reservation> _timeUpdates;

    
    private ResourceScheduler() {
        _rm = ResourceManager.getInstance();
        _calendar = ResourceCalendar.getInstance();
        _uLogger = new CalendarLogger();
        _reassignedIDs = new HashSet<Long>();
        _timeUpdates = new HashSet<Reservation>();
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
        if (planNode != null) {
            UtilisationPlan plan = saveReservations(new UtilisationPlan(planNode),
                    agent, checkOnly);
            return (plan != null) ? plan.toXML() : null;
        }
        else return new XNode("failure", "Malformed Plan XML").toString();
    }


    /**
     * Saves and/or updates the reservations contained in a utilisation plan
     * @param plan the utilisation plan
     * @param agent the user id of the user or service that generated the plan
     * @param checkOnly if true, checks if the reservations would succeed only, makes no
     * changes
     * @return the updated plan
     */
    public synchronized UtilisationPlan saveReservations(UtilisationPlan plan,
                                                         String agent, boolean checkOnly) {
        if (validatePlan(plan) && plan.hasActivities()) {
            clearCaches();
            _calendar.beginTransaction();
            if (checkOnly) checkReservations(plan);
            else saveReservations(plan, agent);
        }
        return plan;
    }


    /**
     * Gets all the utilisation plans in which a resource is involved for the specified
     * period
     * @param resourceXML an XML representation of a utilisation resource object,
     * containing reference(s) to the resources to get plans for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return an XML representation of the matching utilisation plans
     */
    public String getReservations(String resourceXML, long from, long to) {
        UtilisationResource resource = reconstituteResourceRecord(resourceXML);
        if (resource != null) {
            XNode setNode = new XNode("UtilisationPlans");
            for (UtilisationPlan plan : getReservations(resource, from, to)) {
                setNode.addChild(plan.toXNode());
            }
            return setNode.toString();
        }
        else return new XNode("failure", "Malformed Plan XML").toString();
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
    public Set<UtilisationPlan> getReservations(UtilisationResource uResource,
                                                long from, long to) {
        Map<String, UtilisationPlan> planSet = new Hashtable<String, UtilisationPlan>();
        if (uResource != null) {
            UtilisationReconstructor reconstructor = new UtilisationReconstructor();
            int i = -1;

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
                        Activity activity = plan.getActivity(activityName) ;

                        // add calEntry to plan
                        if (activity != null) {
                            activity.addReservation(
                                    reconstructor.reconstructReservation(calEntry));
                        }
                        else {
                            plan.addActivity(reconstructor.reconstructActivity(
                                    calEntry, activityName, phase));
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
        }
        return new HashSet<UtilisationPlan>(planSet.values());
    }


    /**
     * Gets the available time slots for a datetime range for each resource referenced
     * within the given resource XML record.
     * @param resourceXML an XML String equivalent of a UtilisationResource record
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return a consolidated list of timeslots when the resources are available
     */
    public List<TimeSlot> getAvailability(String resourceXML, long from, long to) {
        XNode resourceNode = new XNodeParser().parse(resourceXML);
        return (resourceNode != null) ?
                getAvailability(new UtilisationResource(resourceNode), from, to) : null;
    }


    /**
     * Gets the available time slots for a datetime range for each resource referenced
     * within the given resource XML record.
     * @param uResource a UtilisationResource record referencing resource(s)
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return a consolidated list of timeslots when the resources are available
     */
    public List<TimeSlot> getAvailability(UtilisationResource uResource, long from, long to) {
        List<TimeSlot> availableSlots = new ArrayList<TimeSlot>();
        for (AbstractResource resource : getActualResourceList(uResource)) {
            availableSlots.addAll(_calendar.getAvailability(resource, from, to));
        }
        return availableSlots;
    }



    /*******************************************************************************/

    /**
     * Seeks another AbstractResource that meets the original UtilisationResource
     * parameters and is available, and if found reassigns the calendar entry to the
     * found resource.
     * @param entry the CalendarEntry containing a reference to a resource that is no
     * longer available, but may be replaced by another resource of the same role,
     * capability or category
     * @param updating true if the caller is updating an existing calendar entry, false
     * if it is a new entry
     * @return true if the resource has been reassigned
     */
    protected boolean reassignEntryIfPossible(CalendarEntry entry, boolean updating) {
        CalendarLogEntry logEntry = _uLogger.getLogEntryForCalendarKey(entry.getEntryID());
        if ((logEntry != null) && logEntry.getPhase().equals(Activity.Phase.POU.name())) {
            UtilisationResource uResource = reconstituteResourceRecord(logEntry.getResourceRec());
            if (uResource != null) {
                return reassignEntry(entry, logEntry, uResource, updating);
            }
        }
        return false;
    }


    /**
     * Gets the set of all calendar entry ids for a case
     * @param caseID the id of case to get the entry ids for
     * @return the set of ids
     */
    protected Set<Long> getCalendarEntryIDsForCase(String caseID) {
        return _uLogger.getEntryIDsForCase(caseID);
    }


    /**
     * Gets a list of all calendar entries for a case
     * @param caseID the id of case to get the entry ids for
     * @return the list of calendar entries
     */
    protected List getCalendarEntriesForCase(String caseID) {
        return _uLogger.getLogEntriesForCase(caseID);
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
            String changer = calEntry.getAgent();
            String owner = logEntry.getAgent();
            if ((owner != null) && ((changer == null) || (! owner.equals(changer)))) {
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
                _rm.getClients().announceResourceCalendarStatusChange(owner, node.toString());
            }
        }
    }


    /*******************************************************************************/

    /**
     * Validates Plan values.
     * @param plan the plan to validate
     * @return true if the validation passed, false if failed
     */
    private boolean validatePlan(UtilisationPlan plan) {
        String caseID = plan.getCaseID();
        if (! _rm.getClients().isRunningCaseID(plan.getCaseID())) {
            plan.setError("There is no running case with id: " + caseID);
            return false;
        }
        return true;
    }


    /**
     * Validates Activity values.
     * @param activity the activity to validate
     * @return true if the validation passed, false if failed
     */
    private boolean validateActivity(Activity activity) {
        if (! activity.hasValidPhase()) {
            activity.setError("Invalid requestType: " + activity.getPhase());
            return false;
        }
        return activity.getFromAsLong() > 0 && activity.getToAsLong() > 0;
    }


    /**
     * Checks whether the reservations for the resources contained in the plan would
     * succeed against the current state of the calendar
     * @param plan the plan to check
     */
    private void checkReservations(UtilisationPlan plan) {
        for (Activity activity : plan.getActivityList()) {
            if (validateActivity(activity) && activity.hasReservation()) {
                long from = activity.getFromAsLong();
                long to = activity.getToAsLong();
                for (Reservation reservation : activity.getReservationList()) {
                    checkReservation(reservation, from, to);
                }
            }
        }
        commitOrRollback(false);      // rollback any changes to db when checking only
    }


    /**
     * Attempts to save the reservations for the resources contained in the plan to
     * the calendar
     * @param plan the plan to save
     * @param agent the service instigating the save
     */
    private void saveReservations(UtilisationPlan plan, String agent) {
        for (Activity activity : plan.getActivityList()) {
            if (validateActivity(activity)) {
                Set<Long> reservationIDs = new HashSet<Long>();
                if (activity.hasReservation()) {
                    long from = activity.getFromAsLong();
                    long to = activity.getToAsLong();
                    for (Reservation reservation : activity.getReservationList()) {
                        CalendarLogEntry logEntry = new CalendarLogEntry();
                        logEntry.setCaseID(plan.getCaseID());
                        logEntry.setPhase(activity.getPhase());
                        logEntry.setActivityName(activity.getName());
                        logEntry.setAgent(agent);
                        if (reservation.hasResource()) {
                            logEntry.setResourceRec(reservation.getResource().toXML());
                        }
                        saveReservation(reservation, from, to, logEntry);
                        reservationIDs.add(reservation.getReservationIDAsLong());
                    }
                }

                // remove any previous calendar entries made for this activity that are
                // no longer in the activity
                handleCancellations(plan.getCaseID(), activity.getName(), reservationIDs);
            }
        }

        // if plan contains any updates that changed start time, end time or workload,
        // check that no clashes were caused by the update(s) across all activites
        if (! _timeUpdates.isEmpty()) checkTimeUpdatesForClashes();

        // commit all changes iff no errors occurred during saving of plan 
        commitOrRollback(! plan.hasErrors());
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
                if (reservation.isUpdate()) {
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
            if (reservation.isUpdate()) {                          // pre-existing
                if (logEntry.getPhase().equals("EOU")) {           // end-of-utilisation
                    reconcileReservation(reservation, from, to, logEntry);
                }
                else {
                    updateReservation(reservation, from, to, logEntry);
                }    
            }
            else {                                                  // new
                if (reservation.hasResource()) {
                    checkIfPreviouslySaved(logEntry, from, to);
                    AbstractResource resource = getActualResourceIfAvailable(reservation,
                            from, to);
                    CalendarEntry calEntry = createCalendarEntry(reservation, resource,
                            from, to, logEntry.getAgent());
                    makeReservation(reservation, resource, calEntry, logEntry);
                }
                else throw new CalendarException("Reservation contains no resources.");
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
     * status-to-be in the reservation is invalid
     */
    private AbstractResource getActualResourceIfAvailable(Reservation reservation,
                                                          long from, long to)
            throws CalendarException {
        List<AbstractResource> actualList = getActualResourceList(reservation.getResource());
        try {
            // send a copy of the list because it is altered by the method called
            return getRandomAvailableResource(new ArrayList<AbstractResource>(actualList),
                      from, to, reservation.getStatusToBe(), reservation.getWorkload());
        }
        catch (CalendarException ce) {
            AbstractResource resource = shuffleEntryIfPossible(reservation.getResource(),
                    actualList, from, to);
            if (resource != null) {
                return resource;
            }
            else throw new CalendarException(ce.getMessage());
        }
    }


    /**
     * Extracts a single resource from the list passed that is available for the
     * specified period
     * @param resourceList the list of resources to choose from
     * @param from the start of the period
     * @param to the end of the period
     * @param status the desired booking status
     * @param workload the percentage workload required from the resource
     * @return an available resource from the list, if any
     * @throws CalendarException if the list is null or empty, or none of the
     * resources in the list is available
     */
    private AbstractResource getRandomAvailableResource(List<AbstractResource> resourceList,
                                          long from, long to, String status, int workload)
            throws CalendarException {
        if ((resourceList == null) || resourceList.isEmpty()) {
            throw new CalendarException("Failed to resolve resource.");     // none found
        }
        int resourceCount = resourceList.size();

        // while the list has resources, remove a random selection and check if a
        // reservation for it would succeed
        while (resourceList.size() > 0) {
            AbstractResource actual = resourceList.remove(
                    (int) Math.floor(Math.random() * resourceList.size()));
            if (_calendar.canCreateEntry(actual, from, to, status, workload)) {
                return actual;                                      // found a candidate
            }
        }
        throw new CalendarException(((resourceCount == 1) ? "Specified resource not" :
                "No specified resource") + " available for period.");
    }


    /**
     * Extracts a single resource from the list passed that is available for the
     * specified potential calendar entry
     * @param resourceList the list of resources to choose from
     * @param calEntry the potential calendar entry
     * @return an available resource from the list, if any
     * @throws CalendarException if the list is null or empty, or none of the
     * resources in the list is available
     */
    private AbstractResource getRandomAvailableResource(List<AbstractResource> resourceList,
                                          CalendarEntry calEntry) throws CalendarException {
        return getRandomAvailableResource(resourceList, calEntry.getStartTime(),
                calEntry.getEndTime(), calEntry.getStatus(), calEntry.getWorkload());
    }


    /**
     * Builds a list of resources from the ids specified in a reservation's resource data.
     * @param resource the resource data set of a reservation. May contain a single id,
     * OR a Role and (optional) Capability pair OR a Capability (and no Role) OR
     * a Category and (optional) Subcategory of a non-human resource.
     * @return a list of resources matching the ids specified
     */
    private List<AbstractResource> getActualResourceList(UtilisationResource resource) {
        List<AbstractResource> resourceList = new ArrayList<AbstractResource>();
        ResourceDataSet dataSet = _rm.getOrgDataSet();

        // if a single resource is specified, get it
        if (resource.getID() != null) {
            resourceList.add(getActualResource(resource.getID()));
        }

        // if there is a role (and optionally a capability), get all matching resources
        else if (resource.getRole() != null) {
            resourceList.addAll(dataSet.getRoleParticipantsWithCapability(
                    resource.getRole(), resource.getCapability()));
        }

        // if there's a capability but no role, get all those with that capability
        else if (resource.getCapability() != null) {
            resourceList.addAll(dataSet.getCapabilityParticipants(resource.getCapability()));
        }

        // if there is a category (and optionally a subcategory), get all matches
        else if (resource.getCategory() != null) {
            resourceList.addAll(dataSet.getNonHumanResources(
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
     * removes a resource from a list of resources, if a match is found
     * @param resourceList the list of resources
     * @param id the id of the resource to remove
     */
    private void removeResourceFromList(List<AbstractResource> resourceList, String id) {
        if ((resourceList != null) && (id != null)) {
            for (AbstractResource resource : resourceList) {
                if (resource.getID().equals(id)) {
                    resourceList.remove(resource);
                    break;
                }
            }
        }
    }


    /**
     * Updates the status of a reservation stored in the calendar.
     * @param reservation the updated reservation
     * @param logEntry the matching log entry of the update
     * @param from the reservation start time
     * @param to the reservation end time
     * @throws CalendarException if there's a problem locating the record or saving
     * the update
     * @throws ScheduleStateException if the status transition is invalid
     */
    private void updateReservation(Reservation reservation, long from, long to,
                                   CalendarLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        String statusToBe = reservation.getStatusToBe();
        int workload = reservation.getWorkload();
        long entryID = convertReservationID(reservation);
        CalendarEntry calEntry = getCalendarEntry(entryID);        // exc. if invalid id

        // ignore this update if there's no change to the reservation
        if (unchangedReservation(calEntry, statusToBe, from, to, workload)) return;

        checkRequestedStatusToBe(statusToBe);
        reservation.setStatus(calEntry.getStatus());               // current status
        if (statusToBe.equals(ResourceCalendar.Status.unavailable.name())) {
            reassignEntryIfPossible(calEntry, false);      // adds new rec if possible
        }

        // if not status change only
        if (! (calEntry.hasPeriod(from, to) && (calEntry.getWorkload() == workload))) {
            updateEntry(calEntry, from, to, workload);
            _timeUpdates.add(reservation);             // for final atomic clash checking
        }

        // status updates done separately since several 2ndary changes may be triggered
        if (! calEntry.getStatus().equals(statusToBe)) {
            ResourceCalendar.Status updatedStatus = _calendar.updateEntry(entryID, statusToBe);
            reservation.setStatus(updatedStatus.name());
            calEntry.setStatus(updatedStatus.name());
        }

        // if update(s) raised no exceptions, update log       
        logEntry.setCalendarKey(entryID);
        setResourceRecord(logEntry);
        _uLogger.log(logEntry, calEntry, false);

        if (logEntry.getPhase().equals("SOU")) {                 // start-of-utilisation
            setResourceBusy(logEntry.getCaseID(), reservation.getResource().getID(),
                    true);
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
        checkRequestedStatusToBe(statusToBe);
        reservation.setStatus(statusToBe);                   // assume success by default
        long entryID = _calendar.createEntry(resource, calEntry);

        // if the entry was successful, update the reservation record  
        if (entryID > 0) {
            reservation.setReservationID(String.valueOf(entryID));
            calEntry.setEntryID(entryID);    // for logging
            reservation.setStatus(statusToBe);
        }
        else {
            if (statusToBe.equals(ResourceCalendar.Status.requested.name()) ||
                statusToBe.equals(ResourceCalendar.Status.reserved.name())) {
                reservation.setStatus(ResourceCalendar.Status.unavailable.name());
            }
        }
        _uLogger.log(logEntry, calEntry, false);

        if (logEntry.getPhase().equals("SOU")) {                 // start-of-utilisation
            setResourceBusy(logEntry.getCaseID(), calEntry.getResourceID(), true);
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
                    addTransientCalendarEntry(resource, from, to, reservation);
                    reservation.setStatus(ResourceCalendar.Status.available.name());
                    hasAvailableResource = true;
                    break;
                }
            }
            if (! hasAvailableResource) {                       // no available resources
                reservation.setWarning("Reservation would fail.");
                reservation.setStatus(ResourceCalendar.Status.unavailable.name());
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
        if (! _calendar.hasStatus(entryID, reservation.getStatusToBe())) {
            if (_calendar.canUpdateEntry(entryID, reservation.getStatusToBe())) {
                String status = updateTransientCalendarEntry(entryID,
                        reservation.getStatusToBe());
                reservation.setStatus(getAvailabilityStatus(status, true));
            }
            else reservation.setWarning("Reservation would fail.");
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
        _uLogger.log(logEntry, calEntry, false);

        // if the reconcile throws no exception, mark the resource as available, if it
        // doesn't have a post-usage 'blocked' period
        if (calEntry.getChainID() > 0) {
            CalendarEntry blockedEntry = getCalendarEntry(calEntry.getChainID());
            if ((blockedEntry != null) &&
                (blockedEntry.getEndTime() < System.currentTimeMillis())) {
                    setResourceBusy(logEntry.getCaseID(),
                            reservation.getResource().getID(), false);
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
        entryIDs.addAll(_reassignedIDs);
        
        // compare stored reservations to current ones, and extract the differences
        for (long id : _uLogger.getEntryIDsForActivity(caseID, activityName)) {
            if (! entryIDs.contains(id)) {
                toCancel.add(id);
            }
        }
        for (long cancelledID : toCancel) {
            try {
                _calendar.makeAvailable(cancelledID);             // removes the entry
                logCancellation(cancelledID, caseID, activityName);
            }
            catch (CalendarException ce) {
                // safe to ignore - thrown by missing id in calendar, so no more to do
            }
        }
        _reassignedIDs.clear();
    }


    private void updateEntry(CalendarEntry entry, long from, long to, int workload)
           throws CalendarException, ScheduleStateException {
        entry.setStartTime(from);
        entry.setEndTime(to);
        entry.setWorkload(workload);
        _calendar.updateEntry(entry);
    }


    private void checkTimeUpdatesForClashes() {
        Set<Long> ignoreSet = new HashSet<Long>();
        for (Reservation reservation : _timeUpdates) {
            long id = reservation.getReservationIDAsLong();
            CalendarEntry entry = _calendar.getEntry(id);
            AbstractResource resource = getActualResource(entry.getResourceID());
            ignoreSet.add(id);
            if (! _calendar.isAvailable(ignoreSet, resource, entry.getStartTime(),
                    entry.getEndTime(), entry.getWorkload())) {
                reservation.setError("Resource is not available for updated period.");
            }
            ignoreSet.clear();
        }
        _timeUpdates.clear();
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
     * Adds a record to the calendar log for a reservation cancellation
     * @param entryID the id of the reservation that has been cancelled
     * @param caseID the reservation's case id
     * @param activityName the name of the activity that contained the reservation
     */
    private void logCancellation(long entryID, String caseID, String activityName) {
        CalendarLogEntry logEntry =
                new CalendarLogEntry(caseID, activityName, null, 0, "cancelled", entryID);
        _uLogger.log(logEntry, false);
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
     * Adds a calendar entry temporarily, to allow reservation checking to proceed
     * @param resource the resource to add
     * @param from the start of the period
     * @param to the end of the period
     * @param reservation the reservation containing the resource to add
     * @return the entry id of the newly constructed calendar entry
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     */
    private long addTransientCalendarEntry(AbstractResource resource, long from, long to,
                                      Reservation reservation) throws CalendarException {
        ResourceCalendar.Status status = _calendar.strToStatus(reservation.getStatusToBe());
        return _calendar.addTransientEntry(resource.getID(), from, to, status,
                                                   reservation.getWorkload(), null);
    }


    /**
     * Updates  a calendar entry temporarily, to allow reservation checking to proceed
     * @param entryID the entry to update
     * @param statusToBe the new status to check
     * @return the pre-update status of the entry
     * @throws CalendarException if the reservation's 'statusToBe' is invalid
     * @throws ScheduleStateException if the status change is invalid
     */
    private String updateTransientCalendarEntry(long entryID, String statusToBe)
            throws CalendarException, ScheduleStateException {
        return _calendar.updateTransientEntry(entryID, statusToBe);
    }


    /**
     * Checks that the status-to-be of an update reservation request is not the same as
     * its current status.
     * @param current the current status
     * @param toBe the requested status-to-be
     * @throws ScheduleStateException if the current and requested statuses are identical
     */
    private void compareStatuses(String current, String toBe) throws ScheduleStateException {
        if (current.equals(toBe)) {
            throw new ScheduleStateException("Reservation already has status of '" +
                                             toBe + "'.");
        }
    }


    /**
     * Clears the reassigned id cache
     */
    private void clearCaches() {
        _reassignedIDs.clear();
    }


    /**
     * Sets the relevant resource xml for a log entry
     * @param logEntry
     */
    private void setResourceRecord(CalendarLogEntry logEntry) {
        if (logEntry.getResourceRec() == null) {
            List list = _uLogger.getLogEntriesForCalendarKey(logEntry.getCalendarKey());
            if (list != null) {
                for (Object o : list) {
                    CalendarLogEntry priorEntry = (CalendarLogEntry) o;
                    String resourceRec = priorEntry.getResourceRec();
                    if (resourceRec != null) {
                        logEntry.setResourceRec(resourceRec);
                        break;
                    }
                }
            }
        }
    }


    /**
     * Marks a resource as being in use
     * @param id the id of the resource
     * @param busy true if 'busy', false if 'released'
     */
    private void setResourceBusy(String caseID, String id, boolean busy) {
        _rm.getOrgDataSet().logResourceBusy(caseID, id, busy);
    }


    /**
     * Do a final commit or rollback of all the additions and updates made to the
     * calendar and log for a complete actioning of a plan
     * @param commit if true, all changes are committed; if false, all changes are
     * rolled back
     */
    private void commitOrRollback(boolean commit) {
        if (commit) _calendar.commitTransaction();
        else _calendar.rollBackTransaction();
    }


    /**
     * Reassign a calendar entry to another resource in the same grouping (role,
     * capability or category/subcategory)
     * @param entry the calendar entry to reassign
     * @param logEntry the matching log entry for the calendar entry
     * @param uResource the resource xml specifying the group object
     * @param updating true if this is an plan update, false if it is a new plan
     * @return true if the entry was able to be reassigned to a different resource
     */
    private boolean reassignEntry(CalendarEntry entry, CalendarLogEntry logEntry,
                                  UtilisationResource uResource, boolean updating) {
        List<AbstractResource> resourceList = getActualResourceList(uResource);
        removeResourceFromList(resourceList, entry.getResourceID());
        try {
            AbstractResource resource = getRandomAvailableResource(resourceList,
                    entry);
            if (resource != null) {
                if (updating) {
                    entry.setResourceID(resource.getID());
                    _calendar.updateEntry(entry);
                }
                else {
                    CalendarEntry newEntry = entry.clone();
                    newEntry.setResourceID(resource.getID());
                    _calendar.addEntry(newEntry);
                    _uLogger.log(logEntry, newEntry, false);
                    _reassignedIDs.add(newEntry.getEntryID());
                }
                return true;                   // reassign successful
            }
        }
        catch (Exception ce) {
            // nothing to do - false will be returned below
        }
        return false;
    }


    /**
     * Find a resource that is currently booked for a period as the member of a grouping
     * (role, capability, category/subcategory), that can be replaced by another member
     * of that group, so that the resource can be used to satisfy another booking for
     * which no free resource could be found. For example, if role X contains resources
     * R1 and R2, and role Y also contains R1, and R1 has been chosen to satisfy an
     * entry E1 for X, and a request for an entry E2 of Y is received, then an attempt is
     * made in this method to swap R1 with R2 in E1, so that R1 can be used to satisfy
     * E2.
     * @param origResource the resource xml of the original entry
     * @param origResourceList the list of resources that could potential satisfy the
     * entry
     * @param from the start of the period
     * @param to the end of the period
     * @return the shuffled out resource, if the suffle is possible
     */
    private AbstractResource shuffleEntryIfPossible(UtilisationResource origResource,
                                          List<AbstractResource> origResourceList,
                                          long from, long to) {
        if (! origResource.hasIDOnly()) {

            // get all the current entries for the specified time
            List entries = _calendar.getEntries(from, to);
            if (entries != null) {

                // for each calendar entry covering the same time period
                for (Object o : entries) {
                    CalendarEntry entry = (CalendarEntry) o;

                    // if the activity hasn't started yet for this entry
                    if (isPhase(entry.getEntryID(), Activity.Phase.POU)) {

                        // if this entry's resource satsifies the original resource xml
                        // and it can be swapped with another, then swap it return it
                        AbstractResource shuffled = shuffleEntry(entry, origResourceList,
                                origResource);
                        if (shuffled != null) {
                            return shuffled;
                        }
                    }
                }
            }
        }
        return null;
    }


    /**
     * Find a resource that is currently booked for a period as the member of a grouping
     * (role, capability, category/subcategory), that can be replaced by another member
     * of that group, so that the resource can be used to satisfy another booking for
     * which no free resource could be found. For example, if role X contains resources
     * R1 and R2, and role Y also contains R1, and R1 has been chosen to satisfy an
     * entry E1 for X, and a request for an entry E2 of Y is received, then an attempt is
     * made in this method to swap R1 with R2 in E1, so that R1 can be used to satisfy
     * E2.
     * @param entry the entry to be shuffled
     * @param origResource the resource xml of the original entry
     * @param resourceList the list of resources that could potential satisfy the
     * entry
     * @return the shuffled out resource, if the suffle is possible
     */
    private AbstractResource shuffleEntry(CalendarEntry entry,
                                          List<AbstractResource> resourceList,
                                          UtilisationResource origResource) {
        AbstractResource resource = _rm.getOrgDataSet().getResource(entry.getResourceID());

        // if the entry's resource is a member of the list
        if ((resource != null) && resourceList.contains(resource)) {
            CalendarLogEntry logEntry = _uLogger.getLogEntryForCalendarKey(entry.getEntryID());
            UtilisationResource uResource = reconstituteResourceRecord(logEntry.getResourceRec());

            // ...and its resource xml is different to the resource xml passed, and
            // the entry can be reassigned, return the resource
            if ((uResource != null) && (! origResource.equals(uResource)) &&
                    reassignEntry(entry, logEntry, uResource, true)) {
                return resource;
            }
        }
        return null;
    }


    /**
     * Creates a UtilisationResource object from its xml equivalent
     * @param resourceRec the xml string
     * @return a populated resource record
     */
    private UtilisationResource reconstituteResourceRecord(String resourceRec) {
        if (resourceRec != null) {
            XNode node = new XNodeParser().parse(resourceRec);
            if (node != null) return new UtilisationResource(node);
        }
        return null;
    }


    /**
     * Checks if a log entry has the specified phase
     * @param calendarKey the id of the log entry to check
     * @param phase the phase to check for
     * @return true if the log entry matches the phase
     */
    private boolean isPhase(long calendarKey, Activity.Phase phase) {
        CalendarLogEntry logEntry = _uLogger.getLogEntryForCalendarKey(calendarKey);
        return (logEntry != null) && logEntry.getPhase().equals(phase.name());
    }


    /**
     * Consolidates a status into a simpler one in these cases:
     *  - if checkonly is true, status is one of unknown/available/unavailable
     *  - else, status is one of unknown/available/requested/reserved/unavailable
     * @param statusStr the status to consolidate
     * @param checkOnly true if the call is a check, false if it is a save
     * @return the consolidated status
     * @throws CalendarException if statusStr is not a valid status
     */
    private String getAvailabilityStatus(String statusStr, boolean checkOnly)
            throws CalendarException {
        switch (_calendar.strToStatus(statusStr)) {
            case nil:
            case unknown:   return ResourceCalendar.Status.unknown.name();
            case available: return ResourceCalendar.Status.available.name();
            case requested: if (! checkOnly) return ResourceCalendar.Status.requested.name();
            case reserved:  if (! checkOnly) return ResourceCalendar.Status.reserved.name();
            default:        return ResourceCalendar.Status.unavailable.name();
        }
    }


    /**
     * Checks that a supplied updated status for a save reservation request is one of
     * the four valid values: available, unavailable, requested, reserved
     * @param statusStr the status string to check
     * @throws CalendarException if the status string is invalid
     */
    private void checkRequestedStatusToBe(String statusStr)
            throws CalendarException {
        switch (_calendar.strToStatus(statusStr)) {
            case available:
            case unavailable:
            case requested:
            case reserved: return;
        }
        throw new CalendarException(
                "Invalid StatusToBe value for save reservation request:" + statusStr);
    }


    private void checkIfPreviouslySaved(CalendarLogEntry logEntry, long from, long to)
            throws CalendarException {
        for (Object o : _uLogger.getLogEntriesForReservation(logEntry.getCaseID(),
                logEntry.getActivityName(), logEntry.getResourceRec())) {
            CalendarLogEntry entry = (CalendarLogEntry) o;
            if (entry.getPhase().equals("POU")) {
                CalendarEntry calEntry = _calendar.getEntry(entry.getCalendarKey());
                if ((calEntry != null) && calEntry.hasPeriod(from, to)) {
                    throw new CalendarException(
                        "This reservation is a duplicate of one previously saved.");
                }
            }    
        }
    }


    private boolean unchangedReservation(CalendarEntry calEntry, String statusToBe,
                                         long from, long to, int workload) {
        return (calEntry != null) &&
                calEntry.hasPeriod(from, to) &&
                calEntry.getStatus().equals(statusToBe) &&
                calEntry.getWorkload() == workload;
    }

}
