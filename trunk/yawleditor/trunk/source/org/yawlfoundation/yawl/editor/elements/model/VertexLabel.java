/*
 * Created on 9/01/2004
 * * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;

import java.awt.*;
import java.util.HashMap;

/**
 * @author bradforl
 */
public class VertexLabel extends DefaultGraphCell {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private YAWLVertex vertex;
  private String     label;
  
  public VertexLabel() {
    initialize(null, "");
  }

  public VertexLabel(YAWLVertex vertex) {
    initialize(vertex, "label");
  }

  public VertexLabel(YAWLVertex vertex, String label) {
    initialize(vertex, label);
    setFontSizeOffSpecification();    
  }
  
  private void setFontSizeOffSpecification() {
    HashMap map = new HashMap();

    int size = SpecificationModel.getInstance().getFontSize();
    GraphConstants.setFont(map, GraphConstants.getFont(map).deriveFont((float) size));

    getAttributes().applyMap(map);
  }
  
  public void refreshLabelView() {
      setUserObject("<html><body style=\"width:" + vertex.getBounds().getWidth() * 3 + "\" align=\"center\">" + label + "</body></html>");
  }
  
  public void setLabel(String label) {
    this.label = label;
    if (label != null && vertex != null) {
      refreshLabelView();
    } else {
      setUserObject(null);
    }
  }
  
  public String getLabel() {
    return this.label;
  }
  
  private void initialize(YAWLVertex vertex, String label) {
    buildElement();
    setVertex(vertex);
    setLabel(label);
  }

  private void buildElement() {
    HashMap map = new HashMap();

    GraphConstants.setOpaque(map, false);
    GraphConstants.setAutoSize(map, true);
    GraphConstants.setForeground(map, Color.BLACK);
    GraphConstants.setResize(map,false);
    
    getAttributes().applyMap(map);
  }

  public YAWLVertex getVertex() {
    return vertex;
  }

  public void setVertex(YAWLVertex vertex) {
    this.vertex = vertex;
  }
}
