/*
 * Created on 9/01/2004
 */
package au.edu.qut.yawl.editor.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import java.util.Map;
import java.awt.Color;

import javax.swing.JLabel;

import org.jgraph.graph.GraphConstants;

import au.edu.qut.yawl.editor.specification.SpecificationModel;

/**
 * @author bradforl
 */
public class VertexLabel extends DefaultGraphCell {

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
  }
  
  public void setLabel(String label) {
    this.label = label;
    if (label != null) {
		  setUserObject(label);
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
    Map map = GraphConstants.createMap();
    GraphConstants.setOpaque(map, false);
    GraphConstants.setAutoSize(map, true);
    GraphConstants.setForeground(map, Color.BLACK);
    GraphConstants.setHorizontalTextPosition(map,JLabel.CENTER);
    
    int size = SpecificationModel.getInstance().getFontSize();
    GraphConstants.setFont(map, GraphConstants.getFont(map).deriveFont((float) size));

    changeAttributes(map);
  }

  public YAWLVertex getVertex() {
    return vertex;
  }

  public void setVertex(YAWLVertex vertex) {
    this.vertex = vertex;
  }
}
