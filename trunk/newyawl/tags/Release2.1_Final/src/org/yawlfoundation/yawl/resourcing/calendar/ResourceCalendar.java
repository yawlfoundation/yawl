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

import org.hibernate.Query;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains the resource calendar. An entry in the calendar denotes that a resource
 * is NOT available between the entry's start and end times. An entry may refer to an
 * individual resource, or all human resources (eg. a public holiday), or all
 * non-human resources (eg. an equipment maintenance run) or all resources (both human
 * and non-human)
 *
 * Author: Michael Adams
 * Creation Date: 12/03/2010
 */
public class ResourceCalendar {

    private Persister _persister;
    private static String _success = "<success/>";

    public enum Entry { AllResources, HumanResources, NonHumanResources }

    public ResourceCalendar() {
        _persister = Persister.getInstance();
    }


    /**
     * Adds an entry to the Calendar table.
     * @param resource the resource to add the entry for 
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param comment an optional comment string
     * @return a success or failure message
     */
    public String addEntry(AbstractResource resource, long startTime, long endTime, String comment) {
        if (resource == null) {
            return "<failure>Resource is null.</failure>";
        }
        return addEntry(resource.getID(), startTime, endTime, comment);
    }


    /**
     * Adds an entry to the Calendar table.
     * @param entry an Entry value (for multiple resources)
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param comment an optional comment string
     * @return a success or failure message
     */
    public String addEntry(Entry entry, long startTime, long endTime, String comment) {
        return addEntry(getEntryString(entry), startTime, endTime, comment);
    }


    /**
     * Adds an entry to the Calendar table.
     * @param id either the resource's id or an Entry value (for multiple resources)
     * @param startTime the date/time when the resource's unavailability starts
     * @param endTime the date/time when the resource's unavailability ends
     * @param comment an optional comment string
     * @return a success or failure message
     */
    private String addEntry(String id, long startTime, long endTime, String comment) {
        if (endTime < startTime) {
            return "<failure>Invalid times: End time comes before Start time.</failure>";
        }
        else {
            _persister.insert(new CalendarEntry(id, startTime, endTime, comment));
            return _success;
        }
    }


    /**
     * Gets the current list of calendar entries for a resource
     * @param resource the resource to get the entries for
     * @return the list of entries corresponding to the resource
     */
    public List getEntries(AbstractResource resource) {
        return getEntries(resource.getID());
    }


    /**
     * Gets the current list of calendar entries for an Entry type
     * @param entry the Entry type
     * @return the list of entries corresponding to the Entry type
     */
    public List getEntries(Entry entry) {
        return getEntries(getEntryString(entry));
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

    
    /**
     * Removes all entries from the Calendar table prior to the timestamp passed.
     * @param priorTo timestamp that marks when to delete entries up to. Note: priorTo
     * must be a timestamp prior to the current time.
     * @return the number of entries deleted
     */
    public int clean(long priorTo) {
        if (priorTo > System.currentTimeMillis()) return -1;
        return _persister.execUpdate(
                "DELETE FROM CalendarEntry WHERE endTime<" + String.valueOf(priorTo));
    }


    /**
     * Checks if the resource is currently available. Will return true if there is not
     * an entry in the calendar table for the resource that covers the current time
     * within its duration
     * @param resource the resource to check availability for
     * @return true if available, false if not
     */
    public boolean isAvailable(AbstractResource resource) {
        if (resource == null) return false;
        Query query = _persister.createQuery(
                "FROM CalendarEntry AS ce " +
                "WHERE ce.resourceID IN (:idlist) " +
                "AND ce.startTime <= :now AND ce.endTime >= :now")
                .setParameterList("idlist", createIDListForQuery(resource))
                .setLong("now", System.currentTimeMillis());
        return (query != null) && (! query.iterate().hasNext());
    }


    /**
     * Creates a list of ids to match against for a query. An entry may match the
     * resource's particular id, or 'AllResources' (applies to all) or all resources
     * of its type (human or non-human)
     * @param resource the resource to query for
     * @return the list of possible id values to match
     */
    private List<String> createIDListForQuery(AbstractResource resource) {
        List<String> idlist = new ArrayList<String>(3);
        idlist.add(resource.getID());
        idlist.add(getEntryString(Entry.AllResources));
        idlist.add(getEntryString((resource instanceof Participant) ?
                                   Entry.HumanResources : Entry.NonHumanResources));
        return idlist;
    }


    /**
     * Returns a string corresponding to the entry type
     * @param entry the entry type
     * @return its matching string
     * @pre entry is a valid Entry type
     */
    private String getEntryString(Entry entry) {
        switch (entry) {
            case AllResources : return "ALL_RESOURCES";
            case HumanResources : return "ALL_HUMAN_RESOURCES";
            case NonHumanResources : return "ALL_NONHUMAN_RESOURCES";
        }
        return "UNDEFINED";
    }
}
