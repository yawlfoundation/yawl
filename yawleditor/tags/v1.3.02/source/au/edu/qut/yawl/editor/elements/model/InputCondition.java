/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
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

package au.edu.qut.yawl.editor.elements.model;

import java.awt.geom.Point2D;
import java.util.Map;

import org.jgraph.graph.GraphConstants;

public class InputCondition extends YAWLCondition {

  public InputCondition() {
     super();
  }
  
  public InputCondition(Point2D startPoint) {
     super(startPoint);
  }

  protected void buildElement() {
    Map map = GraphConstants.createMap();
    GraphConstants.setEditable(map,false);
    changeAttributes(map);
    addPort();
  }
  
  protected void addPort() {
    addPort(GraphConstants.PERMILLE, 
            GraphConstants.PERMILLE / 2, RIGHT);
    addPort(GraphConstants.PERMILLE / 2, 0, TOP);  
    addPort(GraphConstants.PERMILLE / 2, GraphConstants.PERMILLE, BOTTOM);
  }
  
  public boolean isRemovable() {
    return false; 
  }

  public boolean isCopyable() {
    return false; 
  }
  
  public boolean acceptsIncommingFlows() {
    return false; 
  }
  
  public String getType() {
    return "Input Condition";
  }
  
  public String getEngineLabel() {
    return "InputCondition";    
  }
}
