/*
 * Created on 28/10/2003
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

package au.edu.qut.yawl.editor.net;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;

import au.edu.qut.yawl.editor.specification.SpecificationSelectionListener;

public class NetSelectionListener implements GraphSelectionListener {
  private GraphSelectionModel model;
  
 
  public NetSelectionListener(GraphSelectionModel model) {
    this.model = model; 
  }
  
  public void valueChanged(GraphSelectionEvent event) {
    SpecificationSelectionListener.getInstance().publishSubscriptions(model);
  }
  
  public void forceActionUpdate() {
    SpecificationSelectionListener.getInstance().publishSubscriptions(model);
  }
}
