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

package org.yawlfoundation.yawl.stateless.engine;

import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;

/**
 * A cache of active net runners.
 *
 * @author Michael Adams (split from YWorkItemRepository for v2.2)
 *
 */
public class YNetRunnerRepository extends ConcurrentHashMap<YIdentifier, YNetRunner> {

    private Map<String, YIdentifier> _idMap;

    public YNetRunnerRepository() {
        super();
        _idMap = new ConcurrentHashMap<String, YIdentifier>();
    }


    public YNetRunner add(YNetRunner runner) {
        return add(runner, runner.getCaseID()) ;
    }

    public YNetRunner add(YNetRunner netRunner, YIdentifier caseID) {
        _idMap.put(caseID.toString(), caseID);
        return this.putIfAbsent(caseID, netRunner);
    }


    public YNetRunner get(String caseID) {
        YIdentifier id = getCaseIdentifier(caseID);
        return (id != null) ? this.get(id) : null;
    }


    public YNetRunner get(YWorkItem workitem) {
        YNetRunner runner = null;
        YWorkItemStatus status = workitem.getStatus();
        YIdentifier caseID = workitem.getWorkItemID().getCaseID();
        if (status.equals(statusEnabled) || status.equals(statusIsParent) ||
                workitem.isEnabledSuspended()) {
            runner = get(caseID);
        }
        else if (workitem.hasLiveStatus() || workitem.hasCompletedStatus() ||
                status.equals(statusSuspended)) {
            runner = get(caseID.getParent());
        }
        return runner;     // may be null
    }


    public List<YNetRunner> getAllRunnersForCase(YIdentifier primaryCaseID) {
        List<YNetRunner> runners = new ArrayList<>();
        for (YNetRunner runner : this.values()) {
            if (primaryCaseID.equalsOrIsAncestorOf(runner.getCaseID())) {
                runners.add(runner);
            }
        }
        return runners;

    }


    public YIdentifier getCaseIdentifier(String caseID) {
        return _idMap.get(caseID);
    }


    public YNetRunner remove(YNetRunner runner) {
        return (runner != null) ? remove(runner.getCaseID().toString()) : null;
    }


    public YNetRunner remove(YIdentifier id) {
        return (id != null) ? remove(id.toString()) : null;
    }


    public YNetRunner remove(String caseID) {
        if (caseID != null) {
            YIdentifier id = _idMap.remove(caseID);
            if (id != null) return super.remove(id);
        }
        return null;
    }


    // pre: logger.isDebugEnabled
    public void dump(Logger logger) {
        logger.debug("\n*** DUMPING {} ENTRIES IN CASE_2_NETRUNNER MAP ***", this.size());
        int sub = 1;
        for (YIdentifier key : this.keySet()) {
             if (key == null) {
                 logger.debug("Key = NULL !!!");
             }
             else {
                 YNetRunner runner = this.get(key);
                 if (runner != null) {
                     logger.debug("Entry {} Key={}", sub++, key.get_idString());
                     logger.debug("    CaseID        {}", runner.get_caseID());
                     logger.debug("    YNetID        {}", runner.getSpecificationID().getUri());
                 }
             }
        }
        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");
    }

}