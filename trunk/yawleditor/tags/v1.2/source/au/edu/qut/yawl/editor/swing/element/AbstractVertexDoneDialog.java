/*
 * Created on 2/07/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.swing.element;

import javax.swing.JPanel;

import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.JUtilities;

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
    if (state == true) {
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