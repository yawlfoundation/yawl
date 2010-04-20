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

import org.yawlfoundation.yawl.editor.actions.tools.ConfigureAnalysisToolsAction;
import org.yawlfoundation.yawl.editor.actions.tools.SetEngineDetailAction;
import org.yawlfoundation.yawl.editor.actions.tools.SetExtendedAttributeFilePathAction;
import org.yawlfoundation.yawl.editor.actions.tools.SetResourcingServiceAction;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

import javax.swing.*;
import java.awt.event.KeyEvent;

class ToolsMenu extends JMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ToolsMenu() {
    super("Settings");
    setMnemonic(KeyEvent.VK_T);
    buildInterface();
  }   
  
  protected void buildInterface() {

    if (YAWLEngineProxy.engineLibrariesAvailable())
      add(new YAWLMenuItem(new SetEngineDetailAction()));
    
    add(new YAWLMenuItem(new SetResourcingServiceAction()));

    if (YAWLEngineProxy.engineLibrariesAvailable())
      add(new YAWLMenuItem(new ConfigureAnalysisToolsAction()));

      add(new YAWLMenuItem(new SetExtendedAttributeFilePathAction()));      
  }
  
  public static boolean needsToBeAddedToMenus() {
      return YAWLEngineProxy.engineLibrariesAvailable() || WofYAWLProxy.wofYawlAvailable();
  }
}
