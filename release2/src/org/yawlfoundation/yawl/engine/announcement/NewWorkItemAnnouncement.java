/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
 * @author Mike Fowler
 *         Date: May 14, 2008
 */
public class NewWorkItemAnnouncement implements Announcement
{
    private YAWLServiceReference yawlService;
    private YWorkItem item;
    private AnnouncementContext context;

    public NewWorkItemAnnouncement(YAWLServiceReference yawlService, YWorkItem item,
                                   AnnouncementContext context)
    {
        this.yawlService = yawlService;
        this.item = item;
        this.context = context;
    }

    public YAWLServiceReference getYawlService()
    {
        return yawlService;
    }

    public YWorkItem getItem()
    {
        return item;
    }

    public AnnouncementContext getContext()
    {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewWorkItemAnnouncement)) return false;

        NewWorkItemAnnouncement other = (NewWorkItemAnnouncement) o;
        return (context == other.context) &&
               (item != null ? item.equals(other.item) : other.item == null) &&
               (yawlService != null ? yawlService.equals(other.yawlService) :
                       other.yawlService == null);
    }

    @Override
    public int hashCode() {
        int result = yawlService != null ? yawlService.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
