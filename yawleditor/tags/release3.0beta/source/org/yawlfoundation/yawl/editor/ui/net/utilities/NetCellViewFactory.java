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

package org.yawlfoundation.yawl.editor.ui.net.utilities;

import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.elements.view.*;
import org.yawlfoundation.yawl.editor.ui.net.YPortView;

public class NetCellViewFactory extends DefaultCellViewFactory {

    public CellView createView(GraphModel model, Object cell) {
        if (cell instanceof AtomicTask) {
            return new AtomicTaskView(cell);
        }
        if (cell instanceof MultipleAtomicTask) {
            return new MultipleAtomicTaskView(cell);
        }
        if (cell instanceof CompositeTask) {
            return new CompositeTaskView(cell);
        }
        if (cell instanceof MultipleCompositeTask) {
            return new MultipleCompositeTaskView(cell);
        }
        if (cell instanceof InputCondition) {
            return new InputConditionView(cell);
        }
        if (cell instanceof OutputCondition) {
            return new OutputConditionView(cell);
        }
        if (cell instanceof Condition) {
            return new ConditionView(cell);
        }
        if (cell instanceof Decorator) {
            return new DecoratorView((Decorator) cell);
        }
        if (cell instanceof VertexContainer) {
            return new VertexContainerView(cell);
        }
        if (cell instanceof YAWLPort) {
            return new YPortView(cell);
        }
        if (cell instanceof YAWLFlowRelation) {
            return new EdgeView(cell);
        }
        return createVertexView(cell);
    }
}
