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

package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;

/**
 * @author Michael Adams
 * @date 19/11/2015
 */
public abstract class CliEngineController implements EngineStatusListener {

    protected EngineStatus _engineStatus;


    protected CliEngineController(EngineStatus initialStatus) {
        _engineStatus = initialStatus;
        new EngineMonitor();
        Publisher.addEngineStatusListener(this);
    }


    public abstract void run();


    @Override
    public void statusChanged(EngineStatus status) { _engineStatus = status; }


    protected void printError(String msg) { System.out.println(msg); }


    protected void pause(int mSecs) {
        try {
            Thread.sleep(mSecs);
        }
        catch (InterruptedException ignore) { }
    }

}
