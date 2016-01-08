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

package org.yawlfoundation.yawl.worklet.support;

import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.worklet.admin.AdministrationTask;
import org.yawlfoundation.yawl.worklet.exception.CaseMonitor;
import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.selection.LaunchEvent;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;

import java.util.Arrays;
import java.util.HashSet;


/**
 *  The Persister class provides persistence support for the Worklet Service.
 *
 *  @author Michael Adams
 */

public class Persister extends HibernateEngine {

    private static Class[] classes = {
            AdministrationTask.class, CaseMonitor.class,
            LaunchEvent.class, RdrNode.class, RdrTree.class, RdrTreeSet.class,
            RdrSet.class, RdrConclusion.class, WorkletEvent.class,
            WorkItemRecord.class, WorkletRunner.class, WorkletSpecification.class
    };

    private static Persister INSTANCE;


    /** The constructor - called from getInstance() */
    private Persister(boolean persistenceOn) throws HibernateException {
        super(persistenceOn, new HashSet<Class>(Arrays.asList(classes)));
    }


    /** returns the current Persister instance */
    public static Persister getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new Persister(true);
            }
            catch (HibernateException he) {
                LogManager.getLogger(Persister.class).error(
                        "Failed to instantiate persistence engine", he);
            }
        }
        return INSTANCE;
    }


    public static boolean insert(Object o) { return persist(o, DB_INSERT); }

    public static boolean update(Object o) { return persist(o, DB_UPDATE); }

    public static boolean delete(Object o) { return persist(o, DB_DELETE); }

    public static boolean insert(Object o, boolean commit) {
        return persist(o, DB_INSERT, commit);
    }

    public static boolean update(Object o, boolean commit) {
        return persist(o, DB_UPDATE, commit);
    }

    public static boolean delete(Object o, boolean commit) {
        return persist(o, DB_DELETE, commit);
    }

    private static boolean persist(Object o, int action) {
        return persist(o, action, true);
    }

    private static boolean persist(Object o, int action, boolean commit) {
        return INSTANCE != null && INSTANCE.isPersisting() && INSTANCE.exec(o, action, commit);
    }

}
