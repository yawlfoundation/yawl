/**
 * Created by Jingxin XU on 20/01/2010
 */

package org.yawlfoundation.yawl.editor.elements.model;

import java.awt.Color;
import java.util.HashMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;




public class ConfigureInformation extends DefaultGraphCell {

  
  private static final long serialVersionUID = 1L;
  private YAWLVertex vertex;
  private String     configureInfor;
  
  public ConfigureInformation() {
    initialize(null, "");
  }


  public ConfigureInformation(YAWLVertex vertex, String label) {
    initialize(vertex, label);
    setFontSizeOffSpecification();    
  }
  
  private void setFontSizeOffSpecification() {
    HashMap map = new HashMap();

    int size = SpecificationModel.getInstance().getFontSize();
    GraphConstants.setFont(map, GraphConstants.getFont(map).deriveFont((float) size));

    getAttributes().applyMap(map);
  }
  
  //should make sure whether it affectes the generated XML files or not
  public void refreshLabelView() {
      setUserObject("<html><body style=\"width:" + vertex.getBounds().getWidth() * 3 + "\" align=\"center\">" + configureInfor + "</body></html>");
  }
  
  public void setLabel(String label) {
    this.configureInfor = label;
    if (label != null && vertex != null) {
      refreshLabelView();
    } else {
      setUserObject(null);
    }
  }
  
  public String getLabel() {
    return this.configureInfor;
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