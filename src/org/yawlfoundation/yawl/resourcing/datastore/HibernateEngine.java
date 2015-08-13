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

package org.yawlfoundation.yawl.resourcing.datastore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarEntry;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarLogEntry;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.AuditEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.SpecLog;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;

import java.util.Arrays;
import java.util.HashSet;


/**
 *  This singleton class provides db & persistence support via Hibernate.
 *
 *  @author Michael Adams
 *  @date 03/08/2007
 *
 *  last update: 26/08/2010 (for v2.2)
 */

public class HibernateEngine extends org.yawlfoundation.yawl.util.HibernateEngine {

    // instance reference
    private static HibernateEngine _me;

    // class references for config
    private static Class[] persistedClasses = {
            Participant.class, Role.class, Capability.class, Position.class,
            OrgGroup.class, UserPrivileges.class, NonHumanResource.class,
            WorkQueue.class, ResourceMap.class, PersistedAutoTask.class,
            CalendarEntry.class, WorkItemRecord.class, ResourceEvent.class,
            AuditEvent.class, SpecLog.class, CalendarLogEntry.class,
            NonHumanCategory.class, NonHumanSubCategory.class
    };


    private static final Logger _log = LogManager.getLogger(HibernateEngine.class);


    /*********************************************************************************/

    // Constructors and Initialisation //
    /***********************************/

    /** The constuctor - called from getInstance() */
    private HibernateEngine(boolean persistenceOn) throws HibernateException {
        super(persistenceOn, new HashSet<Class>(Arrays.asList(persistedClasses)));
    }


    /** returns the current HibernateEngine instance */
    public static HibernateEngine getInstance(boolean persistenceOn) {
        if (_me == null) {
            try {
                _me = new HibernateEngine(persistenceOn);
            }
            catch (HibernateException he) {
                _log.error("Could not initialise database connection.", he);
            }
        }
        return _me;
    }

}
