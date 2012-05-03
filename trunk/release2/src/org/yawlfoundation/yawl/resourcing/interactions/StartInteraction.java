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

package org.yawlfoundation.yawl.resourcing.interactions;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *  This class describes the requirements of a task at the start phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  v0.1, 02/08/2007
 */

public class StartInteraction extends AbstractInteraction {

    public StartInteraction(int initiator) {
        super(initiator) ;
    }

    public StartInteraction() { super(); }

    public StartInteraction(String ownerTaskID) { super(ownerTaskID) ; }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        if (e != null) parseInitiator(e, nsYawl) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<start ");
        xml.append("initiator=\"").append(getInitiatorString()).append("\">");
        xml.append("</start>");
        return xml.toString();
    }

}
