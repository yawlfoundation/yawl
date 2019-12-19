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

package org.yawlfoundation.yawl.resourcing.interactions;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.HashMap;
import java.util.Map;

/**
 *  Base class for the Offer, Allocate and Start interaction points.
 *
 *  @author Michael Adams
 *  v0.1, 02/08/2007
 */

public abstract class AbstractInteraction {

    // possible initiator values
    public static final int USER_INITIATED = 0;
    public static final int SYSTEM_INITIATED = 1;

    protected int _initiator = USER_INITIATED;                         // by default

    protected String _ownerTaskID ;                     // which task owns this int point


    // CONSTRUCTORS //

    public AbstractInteraction() {}                                    // for reflection

    public AbstractInteraction(String ownerTaskID) {
        _ownerTaskID = ownerTaskID ;
    }

    public AbstractInteraction(int initiator) {

        // default to USER if initiator value anything other than SYSTEM
        if (initiator == SYSTEM_INITIATED) _initiator = initiator ;
    }


    // SETTER & GETTERS //

    public String getOwnerTaskID() { return _ownerTaskID; }

    public void setOwnerTaskID(String ownerTaskID) { _ownerTaskID = ownerTaskID; }


    public boolean setInitiator(int i) {
        if ((i == USER_INITIATED) || (i == SYSTEM_INITIATED)) {
            _initiator = i ;
            return true ;
        }
        else return false;                                     // invalid value passed

    }


    public int getInitiator() { return _initiator ; }


    public String getInitiatorString() {
        return isSystemInitiated() ? "system" : "user" ;
    }

    public boolean isSystemInitiated() { return _initiator == SYSTEM_INITIATED; }


    public void parseInitiator(Element e, Namespace nsYawl) throws ResourceParseException {
        if (e == null)
            throw new ResourceParseException("Missing resource specification.");

        String initiator = e.getAttributeValue("initiator") ;
        if (initiator == null)
            throw new ResourceParseException(
                  "Missing attribute 'initiator' on resourcing element: " + e.getName());

        if (initiator.equals("system"))  _initiator = SYSTEM_INITIATED;
    }

    
    public Map<String, String> parseParams(Element e, Namespace nsYawl) {
        HashMap<String, String> result = new HashMap<String, String>() ;
        Element eParams = e.getChild("params", nsYawl);
        if (eParams != null) {
            for (Element eParam : eParams.getChildren("param", nsYawl)) {
                result.put(eParam.getChildText("key", nsYawl),
                           eParam.getChildText("value", nsYawl));
             }
        }
        return result ;
    }    
}
