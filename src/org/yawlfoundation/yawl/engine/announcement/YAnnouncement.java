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

package org.yawlfoundation.yawl.engine.announcement;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YWorkItem;

/**
 * A container of objects that represent an engine generated event for notification to
 * observer gateways and services.
 *
 * @author Michael Adams
 * @date June 14, 2011
 */
public class YAnnouncement {

    private final YAWLServiceReference _yawlService;
    private final YWorkItem _item;
    private final AnnouncementContext _context;
    private final YEngineEvent _event;


    public YAnnouncement(YAWLServiceReference service, YWorkItem item, YEngineEvent event,
                         AnnouncementContext context) {
        _yawlService = service;
        _item = item;
        _context = context;
        _event = event;
    }

    public YAnnouncement(YAWLServiceReference service, YWorkItem item, YEngineEvent event) {
        this(service, item, event, AnnouncementContext.NORMAL);
    }


    public YAWLServiceReference getYawlService() { return _yawlService; }

    public String getScheme() { return _yawlService.getScheme(); }

    public YWorkItem getItem() { return _item; }

    public AnnouncementContext getContext() { return _context; }

    public YEngineEvent getEvent() { return _event ; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof YAnnouncement) {
            YAnnouncement other = (YAnnouncement) o;
            return _yawlService.equals(other._yawlService) && _item.equals(other._item)
                   && (_event == other._event) && (_context == other._context);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = _yawlService.hashCode();
        result = 31 * result + _item.hashCode();
        result = 31 * result + _event.hashCode();
        result = 31 * result + _context.hashCode();
        return result;
    }
}
