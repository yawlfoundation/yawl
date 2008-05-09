/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine;

/**
 * @author Mike Fowler
 *         Date: Apr 29, 2008
 */

public enum AnnouncementContext
{
    NORMAL,               //Announcements are being posted as part of a specification or an extra-engine request

    RECOVERING            //Announcements are being posted due to restart processing within the engine.
                          // Note: In this context, the underlying engine status may be running rather than initialising!
}
