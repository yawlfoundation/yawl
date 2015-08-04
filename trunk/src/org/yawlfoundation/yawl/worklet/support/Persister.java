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

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.worklet.admin.AdministrationTask;
import org.yawlfoundation.yawl.worklet.exception.CaseMonitor;
import org.yawlfoundation.yawl.worklet.exception.HandlerRunner;
import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.selection.CheckedOutChildItem;
import org.yawlfoundation.yawl.worklet.selection.CheckedOutItem;
import org.yawlfoundation.yawl.worklet.selection.LaunchEvent;

import java.util.Arrays;
import java.util.HashSet;


/**
 *  The Persister class provides persistence support for the Worklet Service.
 *
 *  @author Michael Adams
 */

public class Persister extends HibernateEngine {

    private static Class[] classes = {
            CheckedOutItem.class, CheckedOutChildItem.class, AdministrationTask.class,
            CaseMonitor.class, HandlerRunner.class, WorkletEvent.class, RdrNode.class,
            RdrTree.class, RdrTreeSet.class, RdrSet.class, RdrConclusion.class,
            LaunchEvent.class
    };

    private static Persister _me;


    /** The constuctor - called from getInstance() */
    private Persister(boolean persistenceOn) throws HibernateException {
        super(persistenceOn, new HashSet<Class>(Arrays.asList(classes)));
    }


    /** returns the current Persister instance */
    public static Persister getInstance(boolean persistenceOn) {
        if (_me == null) {
            try {
                _me = new Persister(persistenceOn);
            }
            catch (HibernateException he) {
                Logger.getLogger(Persister.class).error(
                        "Failed to instantiate persistence engine", he);
            }
        }
        return _me;
    }


    public static Persister getInstance() { return getInstance(false); }


    public static void insert(Object o) { persist(o, DB_INSERT); }

    public static void update(Object o) { persist(o, DB_UPDATE); }

    public static void delete(Object o) { persist(o, DB_DELETE); }


    private static void persist(Object o, int action) {
        if (_me != null && _me.isPersisting()) _me.exec(o, action, true);
    }

}
