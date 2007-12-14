/*
 * Created on 10/09/2004
 * YAWLEditor v1.01 
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

package au.edu.qut.yawl.editor.swing.element;

import javax.swing.JPanel;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.net.NetGraph;

public abstract class AbstractTaskDoneDialog extends AbstractVertexDoneDialog {
  
  public AbstractTaskDoneDialog(String title, boolean modality, boolean showCancelButton) {
    super(title,modality, showCancelButton);
  }

  public AbstractTaskDoneDialog(String title, 
                                boolean modality,
                                JPanel contentPanel, 
                                boolean showCancelButton) {
    super(title, modality, contentPanel, showCancelButton);
  }  
  
  
  public void setTask(YAWLTask task, NetGraph graph) {
    super.setVertex(task, graph);
  }
  
  public YAWLTask getTask() {
    return (YAWLTask) getVertex();
  }
}
