/*
 * Created on 28/12/2003, 17:21:49
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2003 Lindsay Bradford
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

public class JoinDecorator extends Decorator {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JoinDecorator() {
    super();
  }

  public JoinDecorator(YAWLTask task, int type, int position) {
    super(task, type, position);
  }
  
  public static int getDefaultPosition() {
    return Decorator.LEFT;
  }

  public boolean generatesOutgoingFlows() {
    return false;
  }
  
  public boolean acceptsIncommingFlows() {
    return true;
  }
  
  public String toString() {
    switch(getType()) {
      case Decorator.NO_TYPE: default: {
        return null;
      }
      case Decorator.AND_TYPE: {
        return "AND join";
      }
      case Decorator.OR_TYPE: {
        return "OR join";
      }
      case Decorator.XOR_TYPE: {
        return "XOR join";
      }
    }
  }
}
