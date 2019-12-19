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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.resourcing.calendar.CalendarEntry;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarLogEntry;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Michael Adams
 * @date 2/11/2010
 */
public class UtilisationReconstructor {

    public UtilisationReconstructor() { }

    public UtilisationPlan reconstructPlan(CalendarLogEntry logEntry,
                                            CalendarEntry calEntry) {
        String caseID = null;
        String activityName = null;
        String phase = null;
        if (logEntry != null) {
            caseID = logEntry.getCaseID();
            activityName = logEntry.getActivityName();
            phase = logEntry.getPhase();
        }
        UtilisationPlan plan = new UtilisationPlan(caseID);
        plan.addActivity(reconstructActivity(calEntry, activityName, phase));
        return plan;
    }


    public Activity reconstructActivity(CalendarEntry calEntry, String name, String phase) {
        String from = StringUtil.longToDateTime(calEntry.getStartTime());
        String to = StringUtil.longToDateTime(calEntry.getEndTime());

        Activity activity = new Activity(name, null, null, from, to, null);
        activity.setPhase(phase);
        activity.addReservation(reconstructReservation(calEntry));
        return activity;
    }


    public Reservation reconstructReservation(CalendarEntry calEntry) {
        Reservation reservation = new Reservation();
        reservation.setReservationID(String.valueOf(calEntry.getEntryID()));
        reservation.setStatus(calEntry.getStatus());
        reservation.setResource(reconstructResource(calEntry));
        return reservation;
    }


    public UtilisationResource reconstructResource(CalendarEntry calEntry) {
        UtilisationResource resource = new UtilisationResource();
        resource.setID(calEntry.getResourceID());
        return resource;
    }

}
