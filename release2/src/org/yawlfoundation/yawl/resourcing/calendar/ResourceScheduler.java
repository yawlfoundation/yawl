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
        if (validatePlan(plan)) {
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
                UtilisationPlan plan = null;
                if (logEntry != null) {
                    caseID = logEntry.getCaseID();
                    activityName = logEntry.getActivityName();
                    plan = planSet.get(caseID);
                }

                if (plan != null) {
                    Activity activity = (activityName == null) ? null :
                                         plan.getActivity(activityName) ;

                    // add calEntry to plan
                    if (activity == null) {
                        plan.addActivity(
                                reconstructor.reconstructActivity(calEntry, activityName));
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
                                 CalendarLogEntry logEntry) {
        if (reservation == null) return;
        try {
            if (reservation.getReservationID() != null) {
                String phase = logEntry.getPhase();
                if (phase.equals("EOU")) {                         // end-of-utilisation
                    reconcileReservation(reservation, from, to, logEntry);
                }
                else {
                    updateReservation(reservation, logEntry);
                }    
            }
            else {
                AbstractResource resource = getActualResourceIfAvailable(reservation, from, to);
                CalendarEntry calEntry =
                        createCalendarEntry(reservation, resource, from, to, logEntry);
                makeReservation(reservation, resource, calEntry, logEntry);
            }
        }
        catch (Exception e) {
            reservation.setError(e.getMessage());
        }
    }


    private AbstractResource getActualResourceIfAvailable(Reservation reservation, long from, long to)
            throws CalendarException, ScheduleStateException {
        List<AbstractResource> actualList = getActualResourceList(reservation.getResource());
        if ((actualList == null) || actualList.isEmpty()) {
            throw new CalendarException("Failed to resolve resource.");
        }
        while (actualList.size() > 0) {
            AbstractResource actual =
                    actualList.remove((int) Math.floor(Math.random() * actualList.size()));
            if (_calendar.canCreateEntry(actual, from, to, reservation.getStatusToBe(),
                    reservation.getWorkload())) {
                return actual;
            }
        }
        throw new CalendarException("No specified resource available for period.");
    }


    private List<AbstractResource> getActualResourceList(UtilisationResource resource) {
        List<AbstractResource> resourceList = new ArrayList<AbstractResource>();
        if (resource.getID() != null) {
            resourceList.add(getActualResource(resource.getID()));
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


    private AbstractResource getActualResource(String resourceID) {
        return _rm.getOrgDataSet().getResource(resourceID);
    }

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
            setInUse(reservation.getResource().getID(), true);
        }
    }


    private void makeReservation(Reservation reservation, AbstractResource resource,
                                 CalendarEntry calEntry, CalendarLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        String statusToBe = reservation.getStatusToBe();
        reservation.setStatus(ResourceCalendar.Status.Nil.name());     // default status
        long entryID = _calendar.createEntry(resource, calEntry);
        if (entryID > 0) reservation.setReservationID(String.valueOf(entryID));
        reservation.setStatus(statusToBe);
        reservation.getResource().setID(calEntry.getResourceID());
        _uLogger.log(logEntry, calEntry);
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


    private void reconcileReservation(Reservation reservation, long from, long to,
                                      CalendarLogEntry logEntry)
            throws CalendarException, ScheduleStateException {
        this.getActualResourceIfAvailable(reservation, from, to);
        AbstractResource resource = getActualResource(reservation.getResource().getID());
        CalendarEntry calEntry = _calendar.reconcileEntry(
                resource, convertReservationID(reservation), from, to);
        _uLogger.log(logEntry, calEntry);

        // if the reconcile throws no exception, mark the resource as available
        if (calEntry.getChainID() > 0) {
            CalendarEntry blockedEntry = getCalendarEntry(calEntry.getChainID());
            if ((blockedEntry != null) &&
                (blockedEntry.getEndTime() > System.currentTimeMillis())) {
                    setInUse(reservation.getResource().getID(), false);
            }
        }
    }


    private void handleCancellations(String caseID, String activityName, Set<Long> entryIDs) {
        Set<Long> toCancel = new HashSet<Long>();
        for (long id : _uLogger.getEntryIDsForActivity(caseID, activityName)) {
            if (! entryIDs.contains(id)) {
                toCancel.add(id);
            }
        }
        for (long cancelledID : toCancel) {
            try {
                _calendar.makeAvailable(cancelledID);
            }
            catch (CalendarException ce) {
                // safe to ignore - thrown by missing id in calendar, so no more to do
            }
        }
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
             AbstractResource resource, long from, long to, CalendarLogEntry logEntry)
             throws CalendarException {
        ResourceCalendar.Status status = _calendar.strToStatus(reservation.getStatusToBe());
        return new CalendarEntry(resource.getID(), from, to, status,
                                 reservation.getWorkload(), logEntry.getAgent(), null);
    }


    private void setInUse(String id, boolean inUse) {
        AbstractResource resource = _rm.getOrgDataSet().getResource(id);
        if (resource != null) resource.setAvailable(! inUse);
    }

}
