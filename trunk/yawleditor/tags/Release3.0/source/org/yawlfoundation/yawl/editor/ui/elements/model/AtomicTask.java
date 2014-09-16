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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.YTimerParameters;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;

import java.awt.geom.Point2D;

public class AtomicTask extends YAWLTask implements YAWLAtomicTask {

    /**
     * This constructor is to be invoked whenever we are creating a new
     * atomic task from scratch. It also creates the correct ports needed for
     * the task as an intended side-effect.
     */
    public AtomicTask(Point2D startPoint) {
        super(startPoint);
    }

    public AtomicTask(Point2D startPoint, YTask yTask) {
        super(startPoint);
        setTask(yTask);
    }


    /**
     * This constructor is to be invoked whenever we are creating a new
     * atomic task from scratch with an icon. It also creates the correct
     * ports needed for the task as an intended side-effect.
     */
    public AtomicTask(Point2D startPoint, String iconPath) {
        super(startPoint, iconPath);
    }

    public String getType() {
        return "Atomic Task";
    }

    public void setDecomposition(YDecomposition decomposition) {
        super.setDecomposition(decomposition);
    }

    public YDecomposition getDecomposition() {
        return super.getDecomposition();
    }

    public void setTimerParameters(YTimerParameters timerParameters) {
        if (timerParameters.getTrigger() == YWorkItemTimer.Trigger.Never) {
            timerParameters = null;
        }
        getTask().setTimerParameters(timerParameters);
    }

    public YTimerParameters getTimerParameters() {
        return getTask().getTimerParameters();
    }


    public boolean hasTimerEnabled() {
        return getTimerParameters() != null;
    }

}
