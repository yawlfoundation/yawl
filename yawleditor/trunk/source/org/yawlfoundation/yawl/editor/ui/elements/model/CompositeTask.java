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

import java.awt.geom.Point2D;

public class CompositeTask extends YAWLTask implements YAWLCompositeTask {


    /**
     * This constructor is to be invoked whenever we are creating a new composite task
     * from scratch. It also creates the correct ports needed for the vertex
     * as an intended side-effect.
     */
    public CompositeTask(Point2D startPoint) {
        super(startPoint);
    }

    public CompositeTask(Point2D startPoint, YTask yTask) {
        super(startPoint);
        setTask(yTask);
    }


    public String getUnfoldingNetName() {
        return (getDecomposition() != null) ? getDecomposition().getID() : "";
    }

    public void setDecomposition(YDecomposition decomposition) {
        if (getDecomposition() == null ||
                !getDecomposition().equals(decomposition)) {
            super.setDecomposition(decomposition);
        }
    }

    public String getType() {
        return "Composite Task";
    }
}
