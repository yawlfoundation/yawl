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

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.YPredicateParser;

import java.net.URL;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2010
 */
public class YLogPredicateWorkItemParser extends YPredicateParser {

    private YWorkItem _workItem;

    public YLogPredicateWorkItemParser(YWorkItem item) {
        super();
        _workItem = item;
    }

    protected String valueOf(String predicate) {
        String resolved = "n/a";
        if (predicate.equals("${item:id}")) {
            resolved = _workItem.getIDString();
        }
        else if (predicate.equals("${task:id}")) {
            resolved = _workItem.getTaskID();
        }
        else if (predicate.equals("${spec:name}")) {
            resolved = _workItem.getSpecName();
        }
        else if (predicate.equals("${task:name}")) {
            resolved = _workItem.getTask().getName();
        }
        else if (predicate.equals("${spec:version}")) {
            resolved = _workItem.getSpecificationID().getVersionAsString();
        }
        else if (predicate.equals("${spec:key}")) {
            resolved = _workItem.getSpecificationID().getIdentifier();
        }
        else if (predicate.equals("${item:handlingservice:name}")) {
            YClient client = _workItem.getExternalClient();
            if (client != null) {
                resolved = client.getUserName();
            }
        }
        else if (predicate.equals("${item:handlingservice:uri}")) {
            YClient client = _workItem.getExternalClient();
            if ((client instanceof YAWLServiceReference)) {
                resolved = ((YAWLServiceReference) client).getURI();
            }
        }
        else if (predicate.equals("${item:handlingservice:doco}")) {
            YClient client = _workItem.getExternalClient();
            if (client != null) {
                resolved = client.getDocumentation();
            }
        }
        else if (predicate.equals("${item:codelet}")) {
            resolved = _workItem.getCodelet();
        }
        else if (predicate.equals("${item:customform}")) {
            URL url = _workItem.getCustomFormURL();
            if (url != null) {
                resolved = url.toString();
            }
        }
        else if (predicate.equals("${item:enabledtime}")) {
            resolved = dateTimeString(_workItem.getEnablementTime().getTime());
        }
        else if (predicate.equals("${item:firedtime}")) {
            resolved = dateTimeString(_workItem.getFiringTime().getTime());
        }
        else if (predicate.equals("${item:startedtime}")) {
            resolved = dateTimeString(_workItem.getStartTime().getTime());
        }
        else if (predicate.equals("${item:status}")) {
            resolved = _workItem.getStatus().toString();
        }
        else if (predicate.equals("${task:doco}")) {
            resolved = _workItem.getTask().getDocumentationPreParsed();
        }
        else if (predicate.equals("${task:decomposition:name}")) {
            resolved = _workItem.getTask().getDecompositionPrototype().getID();
        }
        else if (predicate.equals("${item:timer:status}")) {
            resolved = _workItem.getTimerStatus();
        }
        else if (predicate.equals("${item:timer:expiry}")) {
            long expiry = _workItem.getTimerExpiry();
            resolved = (expiry > 0) ? dateTimeString(expiry) : "Nil";
        }
        else if (predicate.startsWith("${item:attribute:")) {
            resolved = getAttributeValue(_workItem.getAttributes(), predicate);
        }
        else if (predicate.startsWith("${expression:")) {
            resolved = evaluateQuery(predicate, _workItem.getDataElement());
        }
        else {
            resolved = super.valueOf(predicate);
        }
        if (resolved == null || "null".equals(resolved) || predicate.equals(resolved)) {
            resolved = "n/a";
        }
        return resolved;
    }    
}
