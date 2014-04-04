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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.authentication.YAbstractSession;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Author: Michael Adams
 * Date: Oct 24, 2007
 * Time: 1:06:45 PM
 */

public class ServiceConnection extends YAbstractSession {

    private String _userid ;

    public ServiceConnection(String userid, long timeOutSeconds) {
        super(timeOutSeconds);
        _userid = userid ;
    }


    public String getUserID() { return _userid; }

}
