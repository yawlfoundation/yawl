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

package org.yawlfoundation.yawl.engine.announcement;

/**
 * @author Mike Fowler
 * @date Apr 29, 2008
 */

public enum AnnouncementContext
{
    NORMAL,               // Announcements are being posted as part of a specification or
                          // an extra-engine request

    RECOVERING            // Announcements are being posted due to restart processing
                          // within the engine. Note: In this context, the underlying
                          // engine status may be running rather than initialising!
}
