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

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 22/10/2009
 */
public class AuditEvent extends BaseEvent {

    private String _userid ;

    public AuditEvent() { }

    public AuditEvent(String userid, EventLogger.audit event) {
        super(event.name());
        _userid = userid;
    }

    public String get_userid() { return _userid; }

    public void set_userid(String userid) { _userid = userid; }

    public String toXML() {
        StringBuilder xml = new StringBuilder(String.format("<event key=\"%d\">", _id));
        xml.append(StringUtil.wrap(_userid, "userid"))
           .append(super.toXML())
           .append("</event>") ;
        return xml.toString();
    }

    public void fromXML(Element xml) {
     	  super.fromXML(xml);
        _userid = xml.getChildText("userid");
    }
    
}
