/*
 * Created on 23/10/2003
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

public class Condition extends YAWLCondition {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This constructor is ONLY to be invoked when we are reconstructing a condition
   * from saved state. Ports will not be created with this constructor, as they
   * are already part of the JGraph state-space.
   */

  public Condition() {
    super();
  }
  
  /**
   * This constructor is to be invoked whenever we are creating a new condition
   * from scratch. It also creates the correct ports needed for the vertex
   * as an intended side-effect.
   */

  public Condition(Point2D startPoint) {
     super(startPoint);
  }
  
  public String getType() {
    return "Condition";
  }

}
