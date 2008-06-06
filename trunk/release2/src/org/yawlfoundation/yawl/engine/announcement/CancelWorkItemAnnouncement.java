/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.announcement;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YWorkItem;

/**
 * @author Mike Fowler
 *         Date: May 14, 2008
 */
public class CancelWorkItemAnnouncement implements Announcement
{
    private YAWLServiceReference yawlService;
    private YWorkItem item;

    public CancelWorkItemAnnouncement(YAWLServiceReference yawlService, YWorkItem item)
    {
        this.yawlService = yawlService;
        this.item = item;
    }

    public YAWLServiceReference getYawlService()
    {
        return yawlService;
    }

    public YWorkItem getItem()
    {
        return item;
    }
}
