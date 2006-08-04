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

package au.edu.qut.yawl.editor.swing.menu;

import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import au.edu.qut.yawl.editor.actions.specification.IconifyAllNetsAction;
import au.edu.qut.yawl.editor.actions.specification.ShowAllNetsAction;
import au.edu.qut.yawl.editor.actions.view.AntiAliasedToggleAction;
import au.edu.qut.yawl.editor.actions.view.FontSizeAction;
import au.edu.qut.yawl.editor.actions.view.DefaultNetBackgroundColourAction;
import au.edu.qut.yawl.editor.actions.view.ShowGridToggleAction;
import au.edu.qut.yawl.editor.actions.view.ToolTipToggleAction;


class ViewMenu extends JMenu {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ViewMenu() {
    super("View");
    setMnemonic(KeyEvent.VK_V);
    buildInterface();
  }
  
  protected void buildInterface() {
    add(buildShowToolTipsItem());
    add(buildShowGridItem());
    add(buildAntiAliasedItem());
    add(buildNetBackgroundColourItem());
    add(buildFontSizeItem());
    add(buildIconifyAllNetsItem());
    add(buildShowAllNetsItem());
    
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

  
  private JMenuItem buildIconifyAllNetsItem() {
    return new JMenuItem(new IconifyAllNetsAction());
  }

  private JMenuItem buildShowAllNetsItem() {
    return new JMenuItem(new ShowAllNetsAction());
  }
}
