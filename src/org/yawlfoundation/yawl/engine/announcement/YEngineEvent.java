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

/**
 * An enum of the various event types that can be raised by the engine and passed to
 * observer gateways and services
 * @author Michael Adams
 * @date June 14, 2011
 */

public enum YEngineEvent {
    ITEM_ADD ("announceItemEnabled", false),
    ITEM_STATUS ("announceItemStatus", true),
    ITEM_CANCEL ("announceItemCancelled", false),
    CASE_START ("announceCaseStarted", true),
    CASE_COMPLETE ("announceCaseCompleted", true),
    CASE_CANCELLED ("announceCaseCancelled", true),
    CASE_DEADLOCKED ("announceCaseDeadlocked", true),
    CASE_SUSPENDING ("announceCaseSuspending", true),
    CASE_SUSPENDED ("announceCaseSuspended", true),
    CASE_RESUMED ("announceCaseResumed", true),
    TIMER_EXPIRED ("announceTimerExpiry", false),
    ENGINE_INIT ("announceEngineInitialised", true),
    NO_EVENT("noEvent", false);

    private final String _label;
    private final boolean _broadcast;

    YEngineEvent (String l, boolean b) {
        _label = l;
        _broadcast = b;
    }

    public String label() { return _label; }

    public boolean isBroadcast() { return _broadcast; }

    public static YEngineEvent fromString(String s) {
        for (YEngineEvent event : YEngineEvent.values()) {
            if (event.label().equals(s)) {
                return event;
            }
        }
        return NO_EVENT;
    }
}
