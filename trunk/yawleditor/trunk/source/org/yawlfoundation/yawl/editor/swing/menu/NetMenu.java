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

import java.awt.event.KeyEvent;

import org.yawlfoundation.yawl.editor.actions.net.PrintNetAction;
import org.yawlfoundation.yawl.editor.actions.net.NetBackgroundColourAction;
import org.yawlfoundation.yawl.editor.actions.net.RemoveNetAction;
import org.yawlfoundation.yawl.editor.actions.net.SetStartingNetAction;
import org.yawlfoundation.yawl.editor.actions.net.ExportNetToPngAction;
import org.yawlfoundation.yawl.editor.actions.net.NetDecompositionDetailAction;

import org.yawlfoundation.yawl.editor.actions.specification.CreateNetAction;

import org.yawlfoundation.yawl.editor.YAWLEditor;

class NetMenu extends YAWLOpenSpecificationMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public NetMenu() {
    super("Net", KeyEvent.VK_N);
  }   
  
  protected void buildInterface() {
    add(new YAWLMenuItem(new CreateNetAction()));
    YAWLEditor.updateLoadProgress(22);

    add(new YAWLMenuItem(new RemoveNetAction()));
    YAWLEditor.updateLoadProgress(24);

    add(new YAWLMenuItem(new SetStartingNetAction()));
    YAWLEditor.updateLoadProgress(26);

    add(new YAWLMenuItem(new NetDecompositionDetailAction()));
    YAWLEditor.updateLoadProgress(27);

    addSeparator();
    add(new YAWLMenuItem(new ExportNetToPngAction()));   
    YAWLEditor.updateLoadProgress(28);

    addSeparator();

    add(new YAWLMenuItem(new NetBackgroundColourAction()));
    
    addSeparator();
    
    add(new YAWLMenuItem(new PrintNetAction()));
    YAWLEditor.updateLoadProgress(29);
  }
}
