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

package org.yawlfoundation.yawl.editor.ui.swing.element;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;

import javax.swing.*;

public abstract class AbstractVertexDoneDialog extends AbstractDoneDialog {
  
  private YAWLVertex vertex;
  protected NetGraph graph;
  
  public AbstractVertexDoneDialog(String title, boolean modality, boolean showCancelButton) {
    super(title,modality, showCancelButton);
  }

  public AbstractVertexDoneDialog(String title, boolean modality,
                                  JPanel contentPanel, boolean showCancelButton) {
    super(title, modality, contentPanel, showCancelButton);
  }  
  
  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  public void setVertex(YAWLVertex vertex, NetGraph graph) {
    this.vertex= vertex;
    this.graph = graph;
    setTitle(getTitlePrefix() + vertex.getType() + getTitleSuffix());
  }
  
  public YAWLVertex getVertex() {
    return this.vertex;
  }
  
  public NetGraph getGraph() {
    return this.graph;
  }
  
  public void setVisible(boolean state) {
    if (state) {
      JUtilities.centreWindowUnderVertex(graph, this, vertex, 10);
    }
    super.setVisible(state);
  }
  
  public String getTitlePrefix() {
    return "";
  }
  
  public String getTitleSuffix() {
    if (vertex.getLabel() != null && !vertex.getLabel().equals("")) {
      return " \"" + vertex.getLabel() + "\"";
    }
    return "";
  }
}
