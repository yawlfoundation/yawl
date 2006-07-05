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

package au.edu.qut.yawl.editor.swing.menu;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import au.edu.qut.yawl.editor.actions.tools.ConfigureAnalysisToolsAction;
import au.edu.qut.yawl.editor.actions.tools.SetEngineDetailAction;
import au.edu.qut.yawl.editor.actions.tools.SetOrganisationDatabaseAction;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

class ToolsMenu extends JMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ToolsMenu() {
    super("Tools");
    setMnemonic(KeyEvent.VK_T);
    buildInterface();
  }   
  
  protected void buildInterface() {
    add(new YAWLMenuItem(new SetOrganisationDatabaseAction()));
    
    if (YAWLEngineProxy.engineLibrariesAvailable()) {
      addSeparator();
      add(new YAWLMenuItem(new SetEngineDetailAction()));
      addSeparator();
      add(new YAWLMenuItem(new ConfigureAnalysisToolsAction()));
    }
  }
}
