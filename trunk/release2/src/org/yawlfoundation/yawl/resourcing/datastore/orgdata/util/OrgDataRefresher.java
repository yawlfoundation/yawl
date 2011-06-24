/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.datastore.orgdata.util;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * When enabled via context configuration, repeatedly refreshes the externally sourced
 * org data after each specified period.
 * @author Michael Adams
 * @date 20/06/11
 */
public class OrgDataRefresher {

    private final ScheduledExecutorService _scheduler;
    private final ResourceManager _rm;
    private ScheduledFuture<?> _refresherTask;

    public OrgDataRefresher(ResourceManager rm, long period) {
        _scheduler = Executors.newScheduledThreadPool(1);
        _rm = rm;
        refresh(period);
    }


    public List<Runnable> cancel() {
        _refresherTask.cancel(true);
        return _scheduler.shutdownNow();
    }


    private void refresh(long period) {
        _refresherTask = _scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                ResourceDataSet orgDataSet = _rm.getOrgDataSet();
                boolean authenticatesExternally = orgDataSet.isUserAuthenticationExternal();
                boolean allowExternalMods = orgDataSet.isExternalOrgDataModsAllowed();
                _rm.loadResources();
                orgDataSet = _rm.getOrgDataSet();         // it changes after the load
                orgDataSet.setExternalUserAuthentication(authenticatesExternally);
                orgDataSet.setAllowExternalOrgDataMods(allowExternalMods);
            }
        }, period, period, TimeUnit.MINUTES);
    }

}
