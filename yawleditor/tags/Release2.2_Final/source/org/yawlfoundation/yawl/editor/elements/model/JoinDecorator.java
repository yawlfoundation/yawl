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

package org.yawlfoundation.yawl.editor.elements.model;

public class JoinDecorator extends Decorator {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * This constructor is ONLY to be invoked when we are reconstructing a decorator
   * from saved state. Ports will not be created with this constructor, as they
   * are already part of the JGraph state-space.
   */

  public JoinDecorator() {
    super();
  }

  /**
   * This constructor is to be invoked whenever we are creating a new decorator
   * from scratch. It also creates the correct ports needed for the decorator
   * as an intended side-effect.
   */

  public JoinDecorator(YAWLTask task, int type, int position) {
    super(task, type, position);
  }
  
  public static int getDefaultPosition() {
    return Decorator.LEFT;
  }

  public boolean generatesOutgoingFlows() {
    return false;
  }
  
  public boolean acceptsIncomingFlows() {
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
