/*
 * Created on 03/09/2003
 * YAWLEditor v1.1 
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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.view.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Set;


class ViewMenu extends JMenu implements SpecificationStateListener {

  private static final SpecificationModel specificationModel =  
    SpecificationModel.getInstance(); 
  
  private static final int NET_LIST_START_INDEX = 6;
  
  private boolean noNetList = true;
  
  private java.util.List<Component> netListMenuItems = new LinkedList<Component>();
  
  public ViewMenu() {
    super("View");
    setMnemonic(KeyEvent.VK_V);
    buildInterface();
      Publisher.getInstance().subscribe(this);
  }
  
  protected void buildInterface() {
    add(buildShowToolTipsItem());
    add(buildShowGridItem());
    add(buildAntiAliasedItem());
    add(buildNetBackgroundColourItem());
    add(buildElementBackgroundColourItem());  
    add(buildFontSizeItem());
  }
  
  private JMenuItem buildShowToolTipsItem() {
    ToolTipToggleAction action = new ToolTipToggleAction();
    JCheckBoxMenuItem tooltipItem = 
      new JCheckBoxMenuItem(action);
    tooltipItem.setSelected(action.isSelected());
    tooltipItem.setToolTipText(null);
    return tooltipItem;
  }

  private JMenuItem buildShowGridItem() {
    ShowGridToggleAction action = new ShowGridToggleAction();
    JCheckBoxMenuItem showGridItem = 
      new JCheckBoxMenuItem(action);
    showGridItem.setSelected(action.isSelected());
    showGridItem.setToolTipText(null);
    return showGridItem;
  }
  
  private JMenuItem buildAntiAliasedItem() {
    AntiAliasedToggleAction action = new AntiAliasedToggleAction();
    JCheckBoxMenuItem antiAliasedItem = 
      new JCheckBoxMenuItem(action);
    antiAliasedItem.setSelected(action.isSelected());
    antiAliasedItem.setToolTipText(null);
    return antiAliasedItem;
  }
  
  private JMenuItem buildFontSizeItem() {
    JMenuItem fontSizeItem = new JMenuItem(new FontSizeAction());
    return fontSizeItem;
  }
  
  private JMenuItem buildNetBackgroundColourItem() {
    return new JMenuItem(new DefaultNetBackgroundColourAction());
  }

    private JMenuItem buildElementBackgroundColourItem() {
      return new JMenuItem(new DefaultElementBackgroundColourAction());
    }

  public void specificationStateChange(SpecificationState state) {
    switch (state) {
      case NoNetsExist: removeNetList(); break;
      case NetDetailChanged: rebuildNetList(); break;
    }
  }
    
  private void removeNetList() {
    if (noNetList) {
      return;
    }
    
    for(Component component : netListMenuItems) {
      this.remove(component);
    }
    
    netListMenuItems.clear();
    noNetList = true;
  }
    
  private void rebuildNetList() {
    removeNetList();

    Set<NetGraphModel> nets = specificationModel.getSortedNets();
    if (nets == null) {
      return;
    }

    noNetList = false;
    int position = NET_LIST_START_INDEX;

    JPopupMenu.Separator separator = new JPopupMenu.Separator();
    add(separator, position++);
    netListMenuItems.add(separator);

    for (NetGraphModel net : nets) {          // do root net first
        if (net.isStartingNet()) {
            addNetListMenuItem(net, position++);
            break;
        }
    }
      
    for (NetGraphModel net : nets) {
        if (! net.isStartingNet()) addNetListMenuItem(net, position++);
    }
  }

  private void addNetListMenuItem(NetGraphModel net, int position) {
      JMenuItem newItem = new JMenuItem(new ViewNetAction(net));
      add(newItem, position);
      netListMenuItems.add(newItem);
  }
}

class ViewNetAction extends AbstractAction {
  private NetGraphModel net;
  
  public ViewNetAction(NetGraphModel net) {
    super();
    this.net = net;
    putValue(Action.SHORT_DESCRIPTION, net.getName());
    putValue(Action.NAME, net.getName());
    putValue(
        Action.SMALL_ICON,
        NetUtilities.getIconForNetModel(net)
    );
    putValue(Action.LONG_DESCRIPTION, net.getName());
  }

  public void actionPerformed(ActionEvent event) {
      YAWLEditorNetPanel frame = net.getGraph().getFrame();
      if (frame != null) {
          ((YAWLEditorDesktop) frame.getParent()).setSelectedComponent(frame);
      }
  }
}
