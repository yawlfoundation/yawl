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

    protected String valueOf(String s) {
        if (s.equals("${item:id}")) {
            s = _workItem.getIDString();
        }
        else if (s.equals("${task:id}")) {
            s = _workItem.getTaskID();
        }
        else if (s.equals("${spec:name}")) {
            s = _workItem.getSpecName();
        }
        else if (s.equals("${task:name}")) {
            s = _workItem.getTask().getName();
        }
        else if (s.equals("${spec:version}")) {
            s = _workItem.getSpecificationID().getVersionAsString();
        }
        else if (s.equals("${spec:key}")) {
            s = _workItem.getSpecificationID().getIdentifier();
        }
        else if (s.equals("${item:handlingService:name}")) {
            YClient client = _workItem.getExternalClient();
            s = (client != null) ? client.getUserName() : "n/a";
        }
        else if (s.equals("${item:handlingService:uri}")) {
            YClient client = _workItem.getExternalClient();
            if ((client != null) && (client instanceof YAWLServiceReference)) {
                s = ((YAWLServiceReference) client).getURI();
            }
            else s = "n/a";
        }
        else if (s.equals("${item:handlingService:doco}")) {
            YClient client = _workItem.getExternalClient();
            s = (client != null) ? client.getDocumentation() : "n/a";
        }
        else if (s.equals("${item:codelet}")) {
            s = _workItem.getCodelet();
        }
        else if (s.equals("${item:customForm}")) {
            s = _workItem.getCustomFormURL().toString();
        }
        else if (s.equals("${item:enabledTime}")) {
            s = dateTimeString(_workItem.getEnablementTime().getTime());
        }
        else if (s.equals("${item:firedTime}")) {
            s = dateTimeString(_workItem.getFiringTime().getTime());
        }
        else if (s.equals("${item:startedTime}")) {
            s = dateTimeString(_workItem.getStartTime().getTime());
        }
        else if (s.equals("${item:status}")) {
            s = _workItem.getStatus().toString();
        }
        else if (s.equals("${task:doco}")) {
            s = _workItem.getTask().getDocumentationPreParsed();
        }
        else if (s.equals("${task:decomposition:name}")) {
            s = _workItem.getTask().getDecompositionPrototype().getName();
        }
        else if (s.equals("${item:timer:status}")) {
            s = _workItem.getTimerStatus();
        }
        else if (s.equals("${item:timer:expiry}")) {
            long expiry = _workItem.getTimerExpiry();
            s = (expiry > 0) ? dateTimeString(expiry) : "Nil";
        }
        else if (s.startsWith("${item:attribute:")) {
            String value = getAttributeValue(_workItem.getAttributes(), s);
            s = (value != null) ? value : "n/a";
        }
        else if (s.startsWith("${expression:")) {
            s = evaluateQuery(s, _workItem.getDataElement());
        }
        else {
            s = super.valueOf(s);
        }
        return s;
    }    
}
