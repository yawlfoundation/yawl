/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.analyser;

/**
 * @author Michael Adams
 * @date 15/05/12
 */
public class YAnalyserEvent {

    private final YAnalyserEventType _eventType;
    private final String _source;
    private final String _message;

    public YAnalyserEvent(YAnalyserEventType eventType, String source, String message) {
        _eventType = eventType;
        _source = source;
        _message = message;
    }

    public String getSource() { return _source; }

    public YAnalyserEventType getEventType() { return _eventType; }

    public String getMessage() { return _message; }

}
