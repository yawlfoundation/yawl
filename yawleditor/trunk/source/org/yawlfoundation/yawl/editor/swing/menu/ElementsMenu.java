/*
 * Created on 05/10/2003
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

package org.yawlfoundation.yawl.editor.swing.menu;

import org.yawlfoundation.yawl.editor.actions.net.*;
import org.yawlfoundation.yawl.editor.api.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.api.plugin.YPluginLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;


public class ElementsMenu extends YAWLOpenSpecificationMenu {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JMenu vertexMenu;

  public ElementsMenu() {
    super("Elements",KeyEvent.VK_L);
  }
  
  protected void buildInterface() {
    add(new YAWLMenuItem(new SetSelectedElementsFillColourAction()));  
    add(getAlignmentMenu());
    addSeparator();
		add(new YAWLMenuItem(IncreaseSizeAction.getInstance()));
		add(new YAWLMenuItem(DecreaseSizeAction.getInstance()));
		addSeparator();
    add(new YAWLMenuItem(AddToVisibleCancellationSetAction.getInstance()));
    add(new YAWLMenuItem(RemoveFromVisibleCancellationSetAction.getInstance()));
      addPlugins();
  }
  
  private JMenu getAlignmentMenu() {
    JMenu alignmentMenu = new JMenu("Alignment");
    alignmentMenu.setMnemonic(KeyEvent.VK_L);
    
    alignmentMenu.add(new YAWLMenuItem(AlignTopAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignMiddleAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignBottomAction.getInstance()));
    alignmentMenu.addSeparator();
    alignmentMenu.add(new YAWLMenuItem(AlignLeftAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignCentreAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignRightAction.getInstance()));

    return alignmentMenu;
  }

    private int addPlugins() {
        int addedItemCount = 0;
        for (YEditorPlugin plugin : YPluginLoader.getInstance().getPlugins()) {
            AbstractAction action = plugin.getElementsMenuAction();
            if (action != null) {
                if (addedItemCount == 0) addSeparator();
                add(new YAWLMenuItem(action));
                addedItemCount++;
            }
        }
        return addedItemCount;
    }



  private JMenu getVertexMenu() {
      vertexMenu = new JMenu("Element");
      vertexMenu.setMnemonic(KeyEvent.VK_E);
      vertexMenu.setEnabled(false);
      return vertexMenu;
  }

  public void addVertexMenu(VertexPopupMenu menu) {
      vertexMenu.removeAll();
      Component[] components = menu.getComponents();
      for (Component component : components) {
          vertexMenu.add(component);
      }
      vertexMenu.setEnabled(true);
  }

  public void removeVertexMenu() {
      vertexMenu.removeAll();
      vertexMenu.setEnabled(false);
  }
}
