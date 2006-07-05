/*
 * Created on 14/11/2003
 * YAWLEditor v1.0 
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

package au.edu.qut.yawl.editor.elements.model;

import org.jgraph.graph.Edge;

import org.jgraph.graph.DefaultPort;


public class YAWLPort extends DefaultPort {
  
  private int position = YAWLTask.NOWHERE;

  public boolean acceptsIncommingFlows() {
    if (hasConditionAsParent()) {
      return true;
    }
    if (hasDecoratorAtThisPosition()) {
      return false;
    }
    if (hasJoinDecorator()) {
      return false;
    }
    return getEdges().size() == 0 ? 
      true : 
      isThisTheTargetOfFirstEdge((Edge) getEdges().iterator().next());
  }

  public boolean generatesOutgoingFlows() {
    if (hasConditionAsParent()) {
      return true;
    }
    if (hasDecoratorAtThisPosition()) {
      return false;
    }
    if (hasSplitDecorator()) {
      return false;
    }
    return getEdges().size() == 0 ? 
      true : 
      isThisTheSourceOfFirstEdge((Edge) getEdges().iterator().next());
  }
  
  private boolean isThisTheSourceOfFirstEdge(Edge edge) {
    return edge.getSource() == this ? true : false;
  }
  
  private boolean isThisTheTargetOfFirstEdge(Edge edge) {
    return edge.getTarget() == this ? true : false;
  }
  
  public void setPosition(int position) {
    this.position = position;
  }
  
  public int getPosition() {
    return position;
  }
  
  private boolean hasDecoratorAtThisPosition() {
    if (getParent() instanceof YAWLTask ) {
      YAWLTask task = (YAWLTask) getParent();
      if (task.hasDecoratorAtPosition(position)) {
        return true;
      }
    }
    return false;
  }
  
  private boolean hasSplitDecorator() {
    if (getParent() instanceof YAWLTask ) {
      YAWLTask task = (YAWLTask) getParent();
      if (task.hasSplitDecorator()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasJoinDecorator() {
    if (getParent() instanceof YAWLTask ) {
      YAWLTask task = (YAWLTask) getParent();
      if (task.hasJoinDecorator()) {
        return true;
      }
    }
    return false;
  }
  
  private boolean hasConditionAsParent() {
    if (getParent() instanceof Condition) {
      return true;
    }
    return false;
  }
  
  public YAWLTask getTask() {
    if (getParent() instanceof VertexContainer) {
      VertexContainer container = (VertexContainer) getParent();
      if (container.getVertex() instanceof YAWLTask) {
        return (YAWLTask) container.getVertex();
      }
      return null;
    } else if (getParent() instanceof YAWLTask) {
      return (YAWLTask) getParent();
    }
    return null;
  }
}
