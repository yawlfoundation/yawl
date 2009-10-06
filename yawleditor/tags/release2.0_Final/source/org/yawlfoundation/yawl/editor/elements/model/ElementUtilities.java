/*
 * Created on 11/02/2005
 * YAWLEditor v1.1 
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

import org.jgraph.graph.Edge;

import org.yawlfoundation.yawl.editor.net.NetGraphModel;

/**
 * @author linds
 * 
 * A library of standard utilities that return information in 
 * selected net elements. 
 */
public final class ElementUtilities {

  public static YAWLCell getTargetOf(NetGraphModel net, Edge edge) {
    return (YAWLCell) NetGraphModel.getTargetVertex(net, edge );
  }
  
  public static YAWLCell getSourceOf(NetGraphModel net, Edge edge) {
    return (YAWLCell) NetGraphModel.getSourceVertex(net, edge );
  }

}
