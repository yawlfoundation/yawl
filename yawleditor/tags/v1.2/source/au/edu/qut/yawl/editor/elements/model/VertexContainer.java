/*
 * Created on 12/12/2003
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

import java.util.Map;
import java.awt.Rectangle;

import org.jgraph.graph.GraphConstants;

import org.jgraph.graph.DefaultGraphCell;

public class VertexContainer extends DefaultGraphCell 
                           implements YAWLCell {

  public VertexContainer() {
    initialize();
  }

  private void initialize() {
    Map map = GraphConstants.createMap();
    GraphConstants.setOpaque(map, false);
    GraphConstants.setSizeable(map, true);
    changeAttributes(map);
  }
  
  public boolean isRemovable() {
    return true;
  }
  
  public boolean isCopyable() {
    return true;
  }
  
  public boolean acceptsIncommingFlows() {
   return true; 
  }

  public boolean generatesOutgoingFlows() {
    return true;
  }
  
  
  public YAWLVertex getVertex() {
		Object[] children = this.getChildren().toArray();
		for(int i = 0; i < children.length; i++) {
			if (children[i] instanceof YAWLVertex) {
				return (YAWLVertex) children[i];		
			}
		}
    return null;
  }

  public VertexLabel getLabel() {
		Object[] children = this.getChildren().toArray();
		for(int i = 0; i < children.length; i++) {
			if (children[i] instanceof VertexLabel) {
				return (VertexLabel) children[i];		
			}
		}
		return null;
  }

  public JoinDecorator getJoinDecorator() {
  	Object[] children = this.getChildren().toArray();
  	for(int i = 0; i < children.length; i++) {
      if (children[i] instanceof JoinDecorator) {
      	return (JoinDecorator) children[i];		
      }
  	}
  	return null;
  }
  
  public SplitDecorator getSplitDecorator() {
		Object[] children = this.getChildren().toArray();
		for(int i = 0; i < children.length; i++) {
			if (children[i] instanceof SplitDecorator) {
				return (SplitDecorator) children[i];		
			}
		}
  	return null;
  }

	public void setBounds(Rectangle bounds) {
		Map map = GraphConstants.createMap();
		GraphConstants.setBounds(map, bounds);
		changeAttributes(map);
	}

	public Rectangle getBounds() {
		Map map = this.getAttributes();
		return GraphConstants.getBounds(map);
	}
}
