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

package org.yawlfoundation.yawl.resourcing.datastore.orgdata.util;

import org.apache.log4j.LogManager;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 1. When called directly (by the ResourceManager) will refresh the externally sourced
 * org data.
 * 2. When enabled via context configuration, repeatedly refreshes the externally sourced
 * org data after each specified period.
 * @author Michael Adams
 * @date 20/06/11
 */
public class OrgDataRefresher {

    private final ScheduledExecutorService _scheduler;
    private final ResourceManager _rm;
    private ScheduledFuture<?> _refresherTask;


    /**
     * Constructs an instance to be used for 'manual' refreshing (via a call to refresh())
     * @param rm the ResourceManager instance
     */
    public OrgDataRefresher(ResourceManager rm) {
        _scheduler = null;
        _rm = rm;
    }

    /**
     * Constructs an instance that starts a timer to refresh org data every 'period'
     * @param rm the ResourceManager instance
     * @param period the number of minutes to wait between refreshes
     */
    public OrgDataRefresher(ResourceManager rm, long period) {
        _scheduler = Executors.newScheduledThreadPool(1);
        _rm = rm;
        refresh(period);
    }


    /**
     * Refreshes the org data
     */
    public void refresh() {
        new RefreshRunnable().run();
    }


    /**
     * Stop the refresher timer (if it was started)
     * @return a list of the cancelled, scheduled refresh commands (if any)
     */
    public List<Runnable> cancel() {
        if (_refresherTask != null) {
            _refresherTask.cancel(true);
            return _scheduler.shutdownNow();
        }
        return null;
    }

    /*********************************************************************************/

    /**
     * Started a scheduled task to run every 'period' minutes
     * @param period the number of minutes to wait between refreshes
     */
    private void refresh(long period) {
        _refresherTask = _scheduler.scheduleAtFixedRate(new RefreshRunnable(),
                period, period, TimeUnit.MINUTES);
    }


    /*********************************************************************************/

    class RefreshRunnable implements Runnable {
        public void run() {
            LogManager.getLogger(this.getClass()).debug("--> Refresh Org Data starts");
            ResourceDataSet orgDataSet = _rm.getOrgDataSet();
            Map<String, QueueSet> qMap = saveQueueSets(orgDataSet);
            boolean authenticatesExternally = orgDataSet.isUserAuthenticationExternal();
            boolean allowExternalMods = orgDataSet.isExternalOrgDataModsAllowed();
            _rm.setOrgDataRefreshing(true);
            _rm.loadResources();
            orgDataSet = _rm.getOrgDataSet();         // it changes after the load
            reattachQueueSets(orgDataSet, qMap);
            orgDataSet.setExternalUserAuthentication(authenticatesExternally);
            orgDataSet.setAllowExternalOrgDataMods(allowExternalMods);
            _rm.setOrgDataRefreshing(false);
            LogManager.getLogger(this.getClass()).debug("Refresh Org Data ends <---");
        }

        Map<String, QueueSet> saveQueueSets(ResourceDataSet orgDataSet) {
            Map<String, QueueSet> qMap = new Hashtable<String, QueueSet>();
            for (Participant p : orgDataSet.getParticipants()) {
                qMap.put(p.getID(), p.getWorkQueues());
            }
            return qMap;
        }

        void reattachQueueSets(ResourceDataSet orgDataSet, Map<String, QueueSet> qMap) {
            for (Participant p : orgDataSet.getParticipants()) {
                if (qMap.containsKey(p.getID())) {
                    p.setWorkQueues(qMap.remove(p.getID()));
                }
            }

            // leftover queues = deleted participants
            for (QueueSet q : qMap.values()) {
                _rm.handleWorkQueuesOnRemoval(null, q);
            }
        }
    }

}
