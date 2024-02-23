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

package org.yawlfoundation.yawl.balancer.servlet;

import org.jdom2.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 13/6/18
 */
public class CaseCompletedObserver extends Interface_Client implements ObserverGateway {

    private static final String BAL_URL = "http://localhost:8080/balancer/ib";


    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public void announceCaseCompletion(YAWLServiceReference yawlService, YIdentifier caseID, Document caseData) {
        announce("obs_complete", caseID);
    }

    @Override
    public void announceCaseCompletion(Set<YAWLServiceReference> services, YIdentifier caseID, Document caseData) {
        announce("obs_complete", caseID);
    }

    @Override
    public void announceCaseCancellation(Set<YAWLServiceReference> services, YIdentifier id) {
        announce("obs_cancel", id);
    }


    private void announce(String event, YIdentifier caseID) {
        Map<String, String> params = prepareParamMap(event, null);
        params.put("caseid", caseID.toString());
        try {
            executePost(BAL_URL, params);
        }
        catch (IOException ioe) {
            // nothing more to do
        }
    }

    /*******************************************************************/
    
    @Override
    public void announceFiredWorkItem(YAnnouncement announcement) {

    }

    @Override
    public void announceCancelledWorkItem(YAnnouncement announcement) {

    }

    @Override
    public void announceTimerExpiry(YAnnouncement announcement) {

    }

   @Override
    public void announceCaseStarted(Set<YAWLServiceReference> services, YSpecificationID specID, YIdentifier caseID, String launchingService, boolean delayed) {

    }

    @Override
    public void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID) {

    }

    @Override
    public void announceWorkItemStatusChange(Set<YAWLServiceReference> services, YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus) {

    }

    @Override
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) {

    }


    @Override
    public void announceDeadlock(Set<YAWLServiceReference> services, YIdentifier id, Set<YTask> tasks) {

    }

    @Override
    public void shutdown() {

    }
}
