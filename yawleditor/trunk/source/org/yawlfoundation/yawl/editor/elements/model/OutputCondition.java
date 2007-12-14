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

package org.yawlfoundation.yawl.editor.elements.model;

import java.awt.geom.Point2D;

import java.util.HashMap;

import org.jgraph.graph.GraphConstants;

public class OutputCondition extends YAWLCondition {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This constructor is ONLY to be invoked when we are reconstructing an
   * output condition from saved state. Ports will not be created with this 
   * constructor, as they are already part of the JGraph state-space.
   */

  public OutputCondition() {
    super();
  }

  
  /**
   * This constructor is to be invoked whenever we are creating a new 
   * output condition from scratch. It also creates the correct ports needed 
   * for the vertex as an intended side-effect.
   */

  public OutputCondition(Point2D startPoint) {
    super(startPoint);
  }
  
  protected void buildElement() {
    HashMap map = new HashMap();
    GraphConstants.setEditable(map,false);
    getAttributes().applyMap(map);

    addDefaultPorts();
  }
  
  public boolean isRemovable() {
    return false; 
  }

  public boolean isCopyable() {
    return false; 
  }

  public boolean generatesOutgoingFlows() {
    return false;
  }
  
  public String getType() {
    return "Output Condition";
  }
  
  public String getEngineLabel() {
    return "OutputCondition";    
  }
}
