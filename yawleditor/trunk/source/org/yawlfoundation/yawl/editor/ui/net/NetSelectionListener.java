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

package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

public class NetSelectionListener implements GraphSelectionListener {

    private GraphSelectionModel model;
  
 
    public NetSelectionListener(GraphSelectionModel model) {
        this.model = model;
    }
  
    public void valueChanged(GraphSelectionEvent event) {
        publishState(model, event);
    }
  
    public void forceActionUpdate() {
        publishState(model, null);
    }


    /**
     * This method interrogates the selected cells suppled with the <code>model</code> specified,
     * and based on the state of the selection set, will send events to subscribers that have asked for
     * notification of the selection being in a particular state of interest.
     * @param model
     */
    public void publishState(GraphSelectionModel model, GraphSelectionEvent event) {
      if (model.isSelectionEmpty()) {
          Publisher.getInstance().publishState(GraphState.NoElementSelected, event);
      }
      else {
          Publisher.getInstance().publishState(GraphState.ElementsSelected, event);
      }
      if (copyableElementsSelected(model)) {
          Publisher.getInstance().publishState(GraphState.CopyableElementSelected, event);
      }
      if (deletableElementsSelected(model)) {
          Publisher.getInstance().publishState(GraphState.DeletableElementSelected, event);
      }
      if (oneOrMoreVertexSelected(model)) {
          Publisher.getInstance().publishState(GraphState.VerticesSelected, event);
      }
      if (moreThanOneVertexSelected(model)) {
          Publisher.getInstance().publishState(GraphState.MultipleVerticesSelected, event);
      }
        if (singleElementSelected(model)) {
            Publisher.getInstance().publishState(GraphState.OneElementSelected, event);
        }
      if (singleTaskSelected(model)) {
          Publisher.getInstance().publishState(GraphState.OneTaskSelected, event);
      }
    }


    /**
     * Returns true if there exists at least 1 element within the
     * selection set supplied by <code>model</code> that is allowed to be copied,
     * false otherwise.
     * @param model
     * @return
     */

    private boolean copyableElementsSelected(GraphSelectionModel model) {
        for (Object cell : model.getSelectionCells()) {
            if (!(cell instanceof YAWLCell)) {
                return true;
            }

            if (((YAWLCell) cell).isCopyable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there exists at least 1 element within the
     * selection set supplied by <code>model</code> that is allowed to be deleted,
     * false otherwise.
     * @param model
     * @return
     */

    private boolean deletableElementsSelected(GraphSelectionModel model) {
        for (Object cell : model.getSelectionCells()) {
            if (!(cell instanceof YAWLCell)) {
                return true;
            }

            if (((YAWLCell) cell).isRemovable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there exists at least 2 vertex elements within the
     * selection set supplied by <code>model</code>, false otherwise.
     * @param model
     * @return
     */
    private boolean moreThanOneVertexSelected(GraphSelectionModel model) {
        int validElementCount = 0;
        for (Object element : model.getSelectionCells()) {
            if (element instanceof YAWLVertex || element instanceof VertexContainer) {
                validElementCount++;
            }
        }
        return validElementCount >= 2;
      }

    private boolean oneOrMoreVertexSelected(GraphSelectionModel model) {
        for (Object o : model.getSelectionCells()) {
            if ((o instanceof YAWLVertex || o instanceof VertexContainer) &&
                    (! ((o instanceof InputCondition) || (o instanceof OutputCondition)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the selection set supplied by
     * <code>model</code> contains only a single task, false otherwise.
     * @param model
     * @return
     */
    private boolean singleTaskSelected(GraphSelectionModel model) {
        Object[] elements = model.getSelectionCells();

        if (elements.length != 1) {
            return false;
        }
        if (elements[0] instanceof YAWLTask) {
            return true;
        }
        if (elements[0] instanceof VertexContainer) {
            if (((VertexContainer) elements[0]).getVertex() instanceof YAWLTask) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the selection set supplied by
     * <code>model</code> contains only a single element, false otherwise.
     * @param model
     * @return
     */
    private boolean singleElementSelected(GraphSelectionModel model) {
      Object[] elements = model.getSelectionCells();
       return elements != null && elements.length == 1;
    }

}
