/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
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

package au.edu.qut.yawl.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;

public abstract class YAWLBaseAction extends AbstractAction {

  protected ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/menuicons/" 
           + iconName + "16.gif");
  }

  public void actionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                                  "The action labelled \"" +
                                  getValue(Action.NAME) + 
                                  "\" is not yet implemented.\n\n" +
                                  "We apologise for the inconvenience.",
                                  "Oopsies!",
                                  JOptionPane.INFORMATION_MESSAGE);
  }

  public NetGraph getGraph() {
    return YAWLEditorDesktop.getInstance().getSelectedGraph(); 
  }
}
