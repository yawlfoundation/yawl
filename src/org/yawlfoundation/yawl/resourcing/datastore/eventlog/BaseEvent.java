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

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Michael Adams
 * Creation Date: 4/02/2010
 */
public abstract class BaseEvent {

    protected static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    protected long _id ;                                           // hibernate PK
    protected String _event ;
    protected long _timeStamp ;

    public BaseEvent() {}

    public BaseEvent(String event)  {
        _event = event;
        _timeStamp = System.currentTimeMillis();
    }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_event() { return _event; }

    public void set_event(String event) {_event = event; }

    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) {_timeStamp = timeStamp; }

    public String getTimeStampString() {
        return SDF.format(new Date(_timeStamp));
    }

    public String getTimeStampMidString() {
        return new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS").format(new Date(_timeStamp));
    }


    public String toXML() {
        return StringUtil.wrap(_event, "eventtype") +
               StringUtil.wrap(String.valueOf(_timeStamp), "timestamp");        
    }


    public void fromXML(Element xml) {
        _id = StringUtil.strToLong(xml.getAttributeValue("key"), -1);
        _event = xml.getChildText("eventtype");
        _timeStamp = StringUtil.strToLong(xml.getChildText("timestamp"), -1);
    }
    
}
