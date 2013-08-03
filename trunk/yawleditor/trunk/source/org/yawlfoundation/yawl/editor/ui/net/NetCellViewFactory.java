/*
 * Created on 21/10/2005
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.elements.view.*;

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
            return new VertexView(cell);
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
