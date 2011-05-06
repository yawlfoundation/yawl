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

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Transaction;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Maintains the resource calendar. An entry in the calendar denotes that a resource
 * is NOT available between the entry's start and end times. An entry may refer to an
 * individual resource (by id), or all human resources (eg. a public holiday), or all
 * non-human resources (eg. an equipment maintenance run) or all resources (both human
 * and non-human).
 *
 * Author: Michael Adams
 * Creation Date: 12/03/2010
 */
public class ResourceCalendar {

    public static enum Status {
        nil, unknown, available, unavailable, requested,
        reserved, busy, hardBlocked, softBlocked
    }

    public static enum ResourceGroup { AllResources, HumanResources, NonHumanResources }

    private static final String TRANSIENT_FLAG = "__transient__entry__flag__";

    private static ResourceCalendar _me;
    private final Persister _persister;
    private Transaction _tx;


    private ResourceCalendar() {
        _persister = Persister.getInstance();
    }

    public static ResourceCalendar getInstance() {
        if (_me == null) _me = new ResourceCalendar();
        return _me;
    }


    public Transaction beginTransaction() {
        _tx = _persister.beginTransaction();
        return _tx;
    }

    public void commitTransaction() {
        _persister.commit();
        _tx = null;
    }

    public void rollBackTransaction() {
        _persister.rollback();
        _tx = null;
    }

    private boolean getCommitFlag() {
        return (_tx == null);
    }


    /**
     * Adds an entry to the Calendar table.
     * @param resource the resource to add the entry for 
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param status the resource's schedule status
     * @param workload the percentage workload (1-100)
     * @param agent the id of the user or service that is adding the entry
     * @param comment an optional comment string
     * @return the identifier of the added entry
     * @throws CalendarException if invalid parameters are passed, or the addition
     * otherwise fails
     */
    public long addEntry(AbstractResource resource, long startTime, long endTime,
                           Status status, int workload, String agent, String comment)
            throws CalendarException {
        if (resource != null) {
            return addEntry(resource.getID(), startTime, endTime, status, workload, agent, comment);
        }
        else throw new CalendarException("Failed to add entry: resource is null.");
    }


    /**
     * Adds an entry to the Calendar table.
     * @param entry an Entry value (for multiple resources)
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param status the resource's schedule status
     * @param agent the id of the user or service that is adding the entry
     * @param comment an optional comment string
     * @return the identifier of the added entry
     * @throws CalendarException if invalid parameters are passed, or the addition
     * otherwise fails
     */
    public long addEntry(ResourceGroup entry, long startTime, long endTime,
                         Status status, String agent, String comment) throws CalendarException {
        return addEntry(getEntryString(entry), startTime, endTime, status, 100, agent, comment);
    }


    /**
     * Gets the calendar entry associated with the entry identifier passed
     * @param entryID the entry identifier
     * @return the matching calendar entry, or null if the entry can't be found
     */
    public CalendarEntry getEntry(long entryID) {
        if (entryID < 1) return null;
        return (CalendarEntry) _persister.get(CalendarEntry.class, entryID);
    }


    /**
     * Gets the current list of calendar entries for a resource
     * @param resource the resource to get the entries for
     * @return the list of all entries corresponding to the resource
     */
    public List getEntries(AbstractResource resource) {
        return getEntries(resource.getID());
    }


    /**
     * Gets the current list of calendar entries for a Resource Entry type
     * @param entry the Entry type
     * @return the list of entries corresponding to the Entry type
     */
    public List getEntries(ResourceGroup entry) {
        return getEntries(getEntryString(entry));
    }


    /**
     * Removes all entries from the Calendar table prior to the timestamp passed.
     * @param priorTo timestamp that marks when to delete entries up to. Note: priorTo
     * must be a timestamp prior to the current time.
     * @return the number of entries deleted
     */
    public int clean(long priorTo) {
        if (priorTo > System.currentTimeMillis()) return -1;
        return _persister.execUpdate("DELETE FROM CalendarEntry AS ce WHERE ce.endTime<" +
                String.valueOf(priorTo), getCommitFlag());
    }


    /**
     * Checks if the resource is available within the specified period. Will return
     * true if there is not an entry in the calendar table for the resource that
     * covers the period within its duration
     * @param idsToIgnore the primary key(s) of entries to exclude from the check. An
     * empty Set (or null) means exclude none, otherwise the corresponding entry is
     * excluded (used when updating an entry)
     * @param resource the resource to check availability for
     * @param from the start of a time range to search for
     * @param to the end of a time range to search for
     * @return true if available, false if not
     */
    public boolean isAvailable(Set<Long> idsToIgnore, AbstractResource resource,
                               long from, long to) {
        if (resource == null) return false;
        List list = getTimeSlotEntries(resource, from, to);

        int excludeCount = 0;
        if ((idsToIgnore != null) && (! list.isEmpty())) {
            for (Object o : list) {
                CalendarEntry entry = (CalendarEntry) o;
                if (idsToIgnore.contains(entry.getEntryID())) ++excludeCount;
            }
        }
        return list.size() == excludeCount;
    }


    /**
     * Checks if the resource is currently available. Will return true if there is not
     * an entry in the calendar table for the resource that covers the current time
     * within its duration
     * @param resource the resource to check availability for
     * @return true if available, false if not
     */
    public boolean isAvailable(AbstractResource resource) {
        long now = System.currentTimeMillis();
        return isAvailable(null, resource, now, now);
    }


    /**
     * Checks if the resource is available within the specified period. Will return
     * true if the workload total of all the entries in the calendar table for the
     * resource that covers the period within its duration, plus the workload passed,
     * doesn't exceed 100%
     * @param idsToIgnore the primary key(s) of entries to exclude from the check. An
     * empty Set (or null) means exclude none, otherwise the corresponding entry is
     * excluded (used when updating an entry)
     * @param resource the resource to check availability for
     * @param from the start of a time range to search for
     * @param to the end of a time range to search for
     * @param workload the percentage workload to check for (0-100)
     * @return true if available, false if not
     */
    public boolean isAvailable(Set<Long> idsToIgnore, AbstractResource resource,
                               long from, long to, int workload) {
        if (resource == null) return false;
        if (workload == 100) return isAvailable(idsToIgnore, resource, from, to);
        List list = getTimeSlotEntries(resource, from, to);
        if (list != null) {
            for (Object o : list) {
                CalendarEntry entry = (CalendarEntry) o;
                if ((idsToIgnore == null) || (! idsToIgnore.contains(entry.getEntryID()))) {
                    workload += entry.getWorkload();
                }
            }
        }
        return workload < 100;
    }


    public boolean isAvailable(AbstractResource resource, long from, long to, int workload) {
        return isAvailable(null, resource, from, to, workload);
    }

    /**
     * Checks if the resource is available within the specified period. Will return
     * true if the workload total of all the entries in the calendar table for the
     * resource that covers the period within its duration, plus the workload passed,
     * doesn't exceed 100%
     * @param resource the resource to check availability for
     * @param entry a CalendarEntry object containing the required parameters
     * @return true if available, false if not
     */
    public boolean isAvailable(AbstractResource resource, CalendarEntry entry) {
        return !((resource == null) || (entry == null)) &&
                isAvailable(resource, entry.getStartTime(), entry.getEndTime(),
                        entry.getWorkload());
    }

    /**
     * Gets the list of calendar entries within a specified period for a resource.
     * Covers 4 overlapping possibilities :
     *   1. A record's time range is wholly within start <-> end
     *   2. start <-> end is wholly within a record's time range
     *   3. start is prior to the record's time range, and end falls within it
     *   4. end is after the record's time range, and start falls within it
     *
     * @param resource the resource to get the entries for
     * @param from the start of a time range to search for (-ve value for all start times)
     * @param to the end of a time range to search for (-ve value for all end times)
     * @return the matching list of calendar entries (as CalendarEntry objects)
     */
    public List getTimeSlotEntries(AbstractResource resource, long from, long to) {
        if (resource == null) return Collections.EMPTY_LIST;         // empty list

        if (to <= 0) to = Long.MAX_VALUE;
        return _persister.createQuery(
                "FROM CalendarEntry AS ce " +
                "WHERE ce.resourceID IN (:idlist) " +
                "AND ce.startTime < :end AND ce.endTime > :start " +
                "ORDER BY ce.startTime")
                .setParameterList("idlist", createIDListForQuery(resource))
                .setLong("start", from)
                .setLong("end", to)
                .list();
    }


    /**
     * Gets the list of calendar entries for a resource.
     * @param resource the resource to get the entries for
     * @return the matching list of calendar entries (as CalendarEntry objects)
     */
    public List getTimeSlotEntries(AbstractResource resource) {
        return getTimeSlotEntries(resource, -1, -1);
    }


    public List getEntries(long from, long to) {
        if (to <= 0) to = Long.MAX_VALUE;
        return _persister.createQuery(
                "FROM CalendarEntry AS ce " +
                "WHERE ce.startTime < :end AND ce.endTime > :start " +
                "ORDER BY ce.startTime")
                .setLong("start", from)
                .setLong("end", to)
                .list();
    }


    public List<TimeSlot> getAvailability(AbstractResource resource, long startTime,
                                          long endTime) {
        List<TimeSlot> available = new ArrayList<TimeSlot>();

        if (endTime > startTime) {

            // get each unavailable slot for the resorce
            List entries = getTimeSlotEntries(resource, startTime, endTime);
            if (! entries.isEmpty()) {
                long endOfPrevSlot = startTime;
                for (Object o : entries) {
                    CalendarEntry entry = (CalendarEntry) o;

                    // ignore entry if gap between slots is 0
                    if (entry.getStartTime() > endOfPrevSlot) {
                        available.add(new TimeSlot(endOfPrevSlot, entry.getStartTime(),
                                Status.available.name()));
                    }
                    if (entry.getWorkload() < 100) {
                        available.add(new TimeSlot(entry));       // partial availability
                    }
                    endOfPrevSlot = entry.getEndTime();
                }
                if (endOfPrevSlot < endTime) {
                    available.add(new TimeSlot(endOfPrevSlot, endTime, Status.available.name()));
                }
            }
            else available.add(new TimeSlot(startTime, endTime, Status.available.name()));
        }
        return available;
    }


    public long createEntry(AbstractResource resource, long startTime, long endTime,
                            String status, String comment, String agent)
            throws CalendarException, ScheduleStateException {
        return createEntry(resource, startTime, endTime, strToStatus(status), comment, agent);
    }


    public long createEntry(AbstractResource resource, long startTime, long endTime,
                            Status status, String comment, String agent)
            throws CalendarException, ScheduleStateException {
        long entryID = -1;
        switch (status) {
            case available:
                makeAvailable(resource, startTime, endTime); break;
            case unavailable:
                entryID = makeUnavailable(resource, startTime, endTime, agent, comment); break;
            case requested:
                entryID = request(resource, startTime, endTime, agent, comment); break;
            case reserved:
                entryID = reserve(resource, startTime, endTime, agent, comment); break;
            default: throw new CalendarException(
                    "Invalid status change request: " + status.name());
        }
        return entryID;
    }


    public long createEntry(AbstractResource resource, CalendarEntry entry)
            throws CalendarException, ScheduleStateException {
        long entryID = -1;
        switch (strToStatus(entry.getStatus())) {
            case available:
                makeAvailable(resource, entry.getStartTime(), entry.getEndTime()); break;
            case unavailable:
                entryID = makeUnavailable(resource, entry.getStartTime(),
                        entry.getEndTime(), entry.getAgent(), entry.getComment()); break;
            case requested:
            case reserved:
                entryID = addEntryIfAvailable(resource, entry); break;
            default: throw new CalendarException(
                    "Invalid status change request: " + entry.getStatus());
        }
        return entryID;
    }


    public boolean canCreateEntry(AbstractResource resource, long startTime, long endTime,
                                  String status, int workload) throws CalendarException {
        return canCreateEntry(resource, startTime, endTime, strToStatus(status), workload);
    }


    public boolean canCreateEntry(AbstractResource resource, long startTime, long endTime,
                                  Status status, int workload) {
        switch (status) {
            case available:
            case unavailable: return true;     // can always do
            case requested:
            case reserved:  return isAvailable(resource, startTime, endTime, workload);
            default: return false;
        }
    }


    public Status updateEntry(long entryID, String status)
            throws CalendarException, ScheduleStateException {
        return updateEntry(entryID, strToStatus(status));
    }


    public Status updateEntry(long entryID, Status status)
            throws CalendarException, ScheduleStateException {
        switch (status) {
            case available: makeAvailable(entryID); break;
            case unavailable: makeUnavailable(entryID); break;
            case reserved: confirm(entryID); break;
            case requested: unconfirm(entryID); break;
            default: throw new CalendarException(
                    "Invalid status change request: " + status.name());
        }
        return status;
    }


    public boolean canUpdateEntry(long entryID, String status)
            throws CalendarException, ScheduleStateException {
        return canUpdateEntry(entryID, strToStatus(status));
    }


    public boolean canUpdateEntry(long entryID, Status status)
            throws CalendarException, ScheduleStateException {
        switch (status) {
            case available:
            case unavailable: return true;                // can always do
            case reserved: return canConfirm(entryID);
            case requested: return canUnconfirm(entryID);
            default: return false;
        }
    }


    public CalendarEntry reconcileEntry(AbstractResource resource, long entryID,
                                        long startTime, long endTime)
              throws CalendarException {
        CalendarEntry entry = getEntry(entryID);
        CalendarEntry blockedEntry = getEntry(entry.getChainID());
        entry.setStartTime(startTime);
        long reservedEndTime = entry.getEndTime();
        if (reservedEndTime != endTime) {
            if (blockedEntry != null) {
                updateBlockedEntry(blockedEntry, endTime);
                checkForClashes(resource, blockedEntry);
            }    
        }
        entry.setEndTime(endTime);
        updateEntry(entry);
        checkForClashes(resource, entry);

        return entry;
    }


    public List<TimeSlot> makeAvailable(AbstractResource resource, long startTime, long endTime)
            throws ScheduleStateException {

        // all trans to available valid
        List removedEntries = getTimeSlotEntries(resource, startTime, endTime);
        removeEntries(resource, startTime, endTime);
        return entriesToTimeSlots(removedEntries);
    }


    public void makeAvailable(long entryID) throws CalendarException {
        CalendarEntry entry = getEntry(entryID);
        if (entry != null) {
            removeEntry(entryID);                           // all trans to available valid
            removeBlockedEntry(entry.getChainID());
            notifyStatusChange(entry);
        }
        else throw new CalendarException("Unknown calendar entry id: " + entryID);
    }


    public long makeUnavailable(AbstractResource resource, long startTime,
                                          long endTime, String agent, String comment)
            throws ScheduleStateException, CalendarException {

        // all trans to unavailable valid
        reassignOrRemoveEntries(resource, startTime, endTime);      // remove all current
        return addEntry(resource, startTime, endTime, Status.unavailable, 100, agent, comment);
     }


    public long makeUnavailable(AbstractResource resource, CalendarEntry calEntry)
            throws ScheduleStateException, CalendarException {

        // all trans to unavailable valid
        reassignOrRemoveEntries(resource, calEntry.getStartTime(), calEntry.getEndTime());
        return addEntry(calEntry);
    }


    private void makeUnavailable(CalendarEntry entry)
            throws ScheduleStateException, CalendarException {
        if (entry != null) {
            entry.setStatus(Status.unavailable.name());
            updateEntry(entry);
            notifyStatusChange(entry);
        }
    }    


    private void makeUnavailable(long entryID)
            throws ScheduleStateException, CalendarException {
        makeUnavailable(getEntry(entryID));
    }


    public long reserve(AbstractResource resource, long startTime, long endTime, String agent,
                        String comment) throws ScheduleStateException, CalendarException {

        // must be currently available
        return addEntryIfAvailable(resource, startTime, endTime, Status.reserved, agent, comment);
    }


    public long reserve(AbstractResource resource, CalendarEntry calEntry)
            throws ScheduleStateException, CalendarException {
        return addEntryIfAvailable(resource, calEntry);
    }


    public void cancel(long entryID) throws ScheduleStateException, CalendarException {

        // must be currently reserved
        removeEntry(entryID, Status.reserved);
    }


    public long request(AbstractResource resource, long startTime, long endTime,
                     String agent, String comment) throws ScheduleStateException, CalendarException {

        // must be currently available
        return addEntryIfAvailable(resource, startTime, endTime, Status.requested, agent, comment);
    }


    public long request(AbstractResource resource, CalendarEntry calEntry)
            throws ScheduleStateException, CalendarException {
        return addEntryIfAvailable(resource, calEntry);
    }

    public void unrequest(long entryID) throws ScheduleStateException, CalendarException {

        // must be currently requested
        removeEntry(entryID, Status.requested);
    }


    public void confirm(long entryID) throws ScheduleStateException, CalendarException {

        // must be currently requested
        updateStatus(entryID, Status.requested, Status.reserved);
    }


    public void unconfirm(long entryID) throws ScheduleStateException, CalendarException {

        // must be currently reserved
        updateStatus(entryID, Status.reserved, Status.requested);
    }


    public boolean canConfirm(long entryID) throws CalendarException {
        return canUpdateStatus(entryID, Status.requested);
    }


    public boolean canUnconfirm(long entryID) throws ScheduleStateException, CalendarException {
        return canUpdateStatus(entryID, Status.reserved);
    }


    public Status strToStatus(String name) throws CalendarException {
        try {
            return Status.valueOf(name);
        }
        catch (Exception e) {
            throw new CalendarException("Invalid status: " + name);
        }
    }


    public long addTransientEntry(CalendarEntry entry) throws CalendarException {
        entry.setComment(TRANSIENT_FLAG);
        return addEntry(entry);
    }


    public long addTransientEntry(String id, long startTime, long endTime, Status status,
                                  int workload, String agent)
            throws CalendarException {
        return addEntry(id, startTime, endTime, status, workload, agent, TRANSIENT_FLAG);
    }


    public String updateTransientEntry(long entryID, String newStatus)
            throws CalendarException {
        String oldStatus = null;
        CalendarEntry entry = getEntry(entryID);
        if (entry != null) {
            oldStatus = entry.getStatus();
            entry.setStatus(newStatus);
            updateEntry(entry);
        }
        return oldStatus;
    }


    public void removeTransientEntries(Map<Long, String> transientMap) {
        CalendarEntry entry;
        Transaction tx = _persister.getOrBeginTransaction();
        for (Long entryID : transientMap.keySet()) {
            String status = transientMap.get(entryID);
            if (status.equals(TRANSIENT_FLAG)) {
                try {
                    entry = (CalendarEntry) _persister.load(CalendarEntry.class, entryID);
                    _persister.delete(entry, tx);
                }
                catch (ObjectNotFoundException onfe) {
                    // nothing to remove if not found
                }
            }
            else {
                entry = (CalendarEntry) _persister.get(CalendarEntry.class, entryID);
                if (entry != null) {
                    entry.setStatus(status);
                    _persister.update(entry, tx);
                }
            }
        }
    }


    public Set<String> freeResourcesForCase(String caseID) throws CalendarException {
        Set<String> resourceIDs = new HashSet<String>();
        List logList = getLogEntriesForCase(caseID);
        if (logList != null) {
            Transaction tx = _persister.getOrBeginTransaction();
            for (Object o : logList) {
                CalendarLogEntry logEntry = (CalendarLogEntry) o;
                CalendarEntry calEntry = (CalendarEntry) _persister.get(
                        CalendarEntry.class, logEntry.getCalendarKey());
                if (calEntry != null) {
                    if (! hasBlockedStatus(calEntry)) {
                        if (logEntry.getPhase().equals("SOU")) {
                            resourceIDs.add(calEntry.getResourceID());
                        }    
                        _persister.delete(calEntry, tx);
                        notifyStatusChange(calEntry);
                    }
                }
            }
        }
        return resourceIDs;
    }

    /*******************************************************************************/

    /**
     * Adds an entry to the Calendar table.
     * @param id either the resource's id or an Entry value (for multiple resources)
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param status the resource's schedule status
     * @param workload the percentage workload (1-100)
     * @param agent the id of the user or service that is adding the entry
     * @param comment an optional comment string
     * @return the identifier of the added entry
     * @throws CalendarException if invalid parameters are passed, or the addition
     * otherwise fails
     */
    private long addEntry(String id, long startTime, long endTime, Status status,
                          int workload, String agent, String comment)
            throws CalendarException {
        return addEntry(new CalendarEntry(id, startTime, endTime, status,
                                          workload, agent, comment));
    }


    /**
     * Adds an entry to the Calendar table.
     * @param entry the CalendarEntry to add
     * @return the identifier of the added entry
     * @throws CalendarException if invalid parameters are passed, or the addition
     * otherwise fails
     */
    protected long addEntry(CalendarEntry entry) throws CalendarException {
        if (entry.getEndTime() > entry.getStartTime()) {
            if (_tx != null) _persister.insert(entry, _tx);
            else _persister.insert(entry);
            return entry.getEntryID();
        }
        else throw new CalendarException("Failed to add Entry: End time is before Start time.");
    }


    /**
     * Gets the current list of calendar entries for a resource
     * @param id the resource's id or the Entry type to get the entries for
     * @return the list of corresponding entries
     */
    private List getEntries(String id) {
        return _persister.createQuery("FROM CalendarEntry AS ce WHERE ce.resourceID=:id")
                    .setString("id", id)
                    .list();
    }


    public List getEntries(String id, long from, long to, boolean immediateCommit) {
        List entries = getEntries(id, from, to);
        if (immediateCommit) commitTransaction();
        return entries;
    }

    public List getEntries(String id, long from, long to) {
        if (id == null) {
            return getEntries(from, to);
        }
        return _persister.createQuery("FROM CalendarEntry AS ce WHERE ce.resourceID=:id" +
                " AND ce.startTime < :end AND ce.endTime > :start " +
                " ORDER BY ce.startTime")
                .setString("id", id)
                .setLong("start", from)
                .setLong("end", to)
                .list();
    }


    public List getEntries(ResourceGroup group, long from, long to, boolean immediateCommit) {
        List entries = getEntries(group, from, to);
        if (immediateCommit) commitTransaction();
        return entries;
    }

    public List getEntries(ResourceGroup group, long from, long to) {
        return getEntries(getEntryString(group), from, to);
    }


    public List getEntries(AbstractResource resource, long from, long to, boolean immediateCommit) {
        List entries = getEntries(resource, from, to);
        if (immediateCommit) commitTransaction();
        return entries;
    }

    public List getEntries(AbstractResource resource, long from, long to) {
        if (resource == null) {
            return getEntries(from, to);
        }
        return getTimeSlotEntries(resource, from, to);
    }


    /**
     * Updates and saves an existing calendar entry
     * @param entry the entry to update
     */
    public void updateEntry(CalendarEntry entry) {
        _persister.update(entry, getCommitFlag());
    }


    /**
     * Removes an existing calendar entry
     * @param entryID the id of the calendar entry to remove
     * @return true if the removal was successful
     */
    private boolean removeEntry(long entryID) {
        return _persister.execUpdate("DELETE FROM CalendarEntry AS ce WHERE ce.entryID="
                + entryID, getCommitFlag()) > 0;
    }


    /**
     * Removes an existing calendar entry
     * @param entry the calendar entry to remove
     */
    private void removeEntry(CalendarEntry entry) {
        _persister.delete(entry, getCommitFlag());
    }


    /**
     * Removes all entries for a resource
     * @param resource the resource to remove the entries for
     * @return the number of entries removed
     */
    private int removeEntries(AbstractResource resource) {
        return removeEntries(resource.getID());
    }


    /**
     * Removes all entries for a resource
     * @param id the identifier of the resource to remove the entries for
     * @return the number of entries removed
     */
    private int removeEntries(String id) {
        return _persister.execUpdate("DELETE FROM CalendarEntry AS ce WHERE ce.resourceID='"
                + id + "'", getCommitFlag());
    }


    /**
     * Removes all entries for a resource within a specified period
     * @param resource the resource to remove the entries for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return the number of entries removed
     */
    private int removeEntries(AbstractResource resource, long from, long to) {
        return removeEntries(resource.getID(), from, to);
    }


    /**
     * Removes all entries for a resource within a specified period
     * @param id the identifier of the resource to remove the entries for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @return the number of entries removed
     */
    private int removeEntries(String id, long from, long to) {
        String cmd = String.format(
                "DELETE FROM CalendarEntry AS ce WHERE ce.resourceID='%s' " +
                "AND ce.startTime < %d AND ce.endTime > %d", id, to, from);
        return _persister.execUpdate(cmd, getCommitFlag());
    }


    /**
     * Creates a list of ids to match against for a query. An entry may match the
     * resource's particular id, or 'AllResources' (applies to all) or all resources
     * of its type (human or non-human)
     * @param resource the resource to query for
     * @return the list of possible id values to match
     */
    private List<String> createIDListForQuery(AbstractResource resource) {
        return createIDListForQuery(resource.getID(), (resource instanceof Participant));
    }


    private List<String> createIDListForQuery(String resourceID, boolean isParticipant) {
        return Arrays.asList(
                   resourceID,
                   getEntryString(ResourceGroup.AllResources),
                   getEntryString(isParticipant ?
                                   ResourceGroup.HumanResources :
                                   ResourceGroup.NonHumanResources)
        );
    }


    /**
     * Returns a string corresponding to the entry type
     * @param entry the entry type
     * @return its matching string
     * @pre entry is a valid Entry type
     */
    public String getEntryString(ResourceGroup entry) {
        switch (entry) {
            case AllResources : return "ALL_RESOURCES";
            case HumanResources : return "ALL_HUMAN_RESOURCES";
            case NonHumanResources : return "ALL_NONHUMAN_RESOURCES";
        }
        return "UNDEFINED";
    }


    /**
     * Converts a list of CalendarEntry objects to a list of TimeSlot objects
     * @param entries the list of CalendarEntry objects
     * @return the corresponding list of TimeSlot objects
     */
    private List<TimeSlot> entriesToTimeSlots(List entries) {
        List<TimeSlot> slots = new ArrayList<TimeSlot>();
        if (entries != null) {
            for (Object o : entries) {
                slots.add(new TimeSlot((CalendarEntry) o));
            }
        }
        return slots;
    }


    private boolean canUpdateStatus(long entryID, Status requiredStatus)
            throws CalendarException {
        CalendarEntry entry = getEntry(entryID);
        if (entry != null) {
            return entry.getStatus().equals(requiredStatus.name());
        }
        else throw new CalendarException("Unknown calendar entry id: " + entryID);
    }


    public boolean hasStatus(long entryID, String status) throws CalendarException {
        CalendarEntry entry = getEntry(entryID);
        if (entry != null) {
            return entry.getStatus().equals(status);
        }
        else throw new CalendarException("Unknown calendar entry id: " + entryID);
    }


    private void updateStatus(long entryID, Status oldStatus, Status newStatus)
            throws ScheduleStateException, CalendarException {
        CalendarEntry entry = getEntryWithStatus(entryID, oldStatus);
        entry.setStatus(newStatus.name());
        updateEntry(entry);
        notifyStatusChange(entry);
    }


    private void removeEntry(long entryID, Status status)
            throws ScheduleStateException, CalendarException {
        removeEntry(getEntryWithStatus(entryID, status));
    }


    private void removeEntries(List<CalendarEntry> entries) {
        if ((entries != null) && (! entries.isEmpty())) {
            Transaction tx = _persister.getOrBeginTransaction();
            for (CalendarEntry entry : entries) {
                CalendarEntry blocked = (CalendarEntry) _persister.get(
                        CalendarEntry.class, entry.getChainID());
                if (blocked != null) _persister.delete(blocked, tx);
                _persister.delete(entry, tx);
                notifyStatusChange(entry);
            }
        }
    }


    private void updateBlockedEntry(CalendarEntry blockedEntry, long time) {
        if (blockedEntry != null) {
            long blockDuration = blockedEntry.getEndTime() - blockedEntry.getStartTime();
            blockedEntry.setStartTime(time + 1);
            blockedEntry.setEndTime(time + blockDuration);
            updateEntry(blockedEntry);
        }
    }


    private void checkForClashes(AbstractResource resource, CalendarEntry entry)
            throws CalendarException {
        List list = getTimeSlotEntries(resource, entry.getStartTime(), entry.getEndTime());
        if (list.size() > 1) {
            String clashID = " ";
            for (Object o : list) {
                long id = ((CalendarEntry) o).getEntryID();
                if (id != entry.getEntryID()) {
                    clashID += id + " ";
                }
            }
            throw new CalendarException(
                    "Reconciled reservation has caused a clash for resource '" +
                    entry.getResourceID() + "' with reservation id(s): " + clashID);
        }
    }


    private CalendarEntry getEntryWithStatus(long entryID, Status status)
            throws ScheduleStateException, CalendarException {
        CalendarEntry entry = getEntry(entryID);
        if (entry != null) {
            if (entry.getStatus().equals(status.name())) {
                return entry;
            }
            else throw new ScheduleStateException("Status change failed: entry requires '" +
                    status.name() + "' status, but has a status of '" +
                    entry.getStatus() + "'");
        }
        else throw new CalendarException("Unknown calendar entry id: " + entryID);
    }


    private long addEntryIfAvailable(AbstractResource resource, long startTime,
                                     long endTime, Status status, String agent, String comment)
            throws ScheduleStateException, CalendarException {
        CalendarEntry entry = new CalendarEntry(resource.getID(), startTime, endTime,
                                                 status, 100, agent, comment);
        return addEntryIfAvailable(resource, entry);
    }


    private long addEntryIfAvailable(AbstractResource resource, CalendarEntry calEntry)
            throws ScheduleStateException, CalendarException {
        if (isAvailable(resource, calEntry)) {
            calEntry.setResourceID(resource.getID());
            long entryID = addEntry(calEntry);
            addBlockedEntry(resource, calEntry);
            return entryID;
        }
        else throw new ScheduleStateException("Resource not available for timeslot.");
    }


    private void addBlockedEntry(AbstractResource resource, CalendarEntry entry)
            throws CalendarException {
        long duration = resource.getBlockedDuration();
        if (duration > 0) {
            Status status = resource.getBlockType().equals("Soft") ?
                    Status.softBlocked : Status.hardBlocked;
            CalendarEntry blockedEntry = new CalendarEntry(resource.getID(),
                    entry.getEndTime() + 1, entry.getEndTime() + duration, status,
                    100, entry.getAgent(), entry.getComment());

            entry.setChainID(addEntry(blockedEntry));
            updateEntry(entry);
        }
    }


    private void removeBlockedEntry(CalendarEntry entry) {
        removeBlockedEntry(entry.getChainID());
    }


    private void removeBlockedEntry(long chainID) {
        if (chainID > 0) {
            removeEntry(chainID);
        }
    }


    private boolean hasBlockedStatus(CalendarEntry entry) throws CalendarException {
        return hasBlockedStatus(entry.getStatus());
    }


    private boolean hasBlockedStatus(String status) throws CalendarException {
        return hasBlockedStatus(strToStatus(status));
    }


    private boolean hasBlockedStatus(Status status) {
        switch (status) {
            case hardBlocked:
            case softBlocked: return true;
            default: return false;
        }
    }



    private void reassignOrRemoveEntries(AbstractResource resource,
                                                long startTime, long endTime) {
        List<CalendarEntry> unreassigned = reassignEntries(resource, startTime, endTime);
        removeEntries(unreassigned);
    }


    private List<CalendarEntry> reassignEntries(AbstractResource resource,
                                                long startTime, long endTime) {
        List<CalendarEntry> unassigned = new ArrayList<CalendarEntry>();
        List entries = getTimeSlotEntries(resource, startTime, endTime);
        if (entries != null) {
            for (Object o : entries) {
                CalendarEntry entry = (CalendarEntry) o;
                if (! reassignEntry(entry)) {
                    unassigned.add(entry);
                }
                
            }
        }
        return unassigned;
    }


    private boolean reassignEntry(CalendarEntry entry) {
        return ResourceScheduler.getInstance().reassignEntryIfPossible(entry, true);
    }


    private void notifyStatusChange(CalendarEntry entry) {
        ResourceScheduler.getInstance().notifyStatusChange(entry);
    }


    private Set<Long> getEntryIDsForCase(String caseID) {
        Set<Long> ids = ResourceScheduler.getInstance().getCalendarEntryIDsForCase(caseID);
        return (ids != null) ? ids : new HashSet<Long>();
    }


    private List getLogEntriesForCase(String caseID) {
        return ResourceScheduler.getInstance().getCalendarEntriesForCase(caseID);
    }


}
