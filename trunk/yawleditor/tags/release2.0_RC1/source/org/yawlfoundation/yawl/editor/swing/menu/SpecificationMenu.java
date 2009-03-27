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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.ExitAction;
import org.yawlfoundation.yawl.editor.actions.specification.*;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import javax.swing.*;
import java.awt.event.KeyEvent;

class SpecificationMenu extends JMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SpecificationMenu() {
    super("Specification");
    setMnemonic(KeyEvent.VK_S);
    buildInterface();
  }   
  
  protected void buildInterface() {
    addMenuItemAction(new CreateSpecificationAction());
    addMenuItemAction(new OpenSpecificationAction());
    add(OpenRecentSubMenu.getInstance());
    if (YAWLEngineProxy.engineLibrariesAvailable()) {
        addMenuItemAction(new ImportFromEngineFormatAction());        
    }

    addSeparator();
    
    addMenuItemAction(new SaveSpecificationAction());
    addMenuItemAction(new SaveSpecificationAsAction());

    YAWLEditor.updateLoadProgress(12);

    if (YAWLEngineProxy.engineLibrariesAvailable()) {
      addSeparator();
      addMenuItemAction(new ValidateSpecificationAction());
      addMenuItemAction(new AnalyseSpecificationAction());
    }

    YAWLEditor.updateLoadProgress(16);

    addSeparator();
    addMenuItemAction(new PrintSpecificationAction());
    addSeparator();

    addMenuItemAction(new UpdateSpecificationPropertiesAction());
    addMenuItemAction(new UpdateDataTypeDefinitionsAction());
    addSeparator();

    addMenuItemAction(new CloseSpecificationAction());
    addMenuItemAction(new ExitAction(this));

    YAWLEditor.updateLoadProgress(18);
  }
  
  private void addMenuItemAction(AbstractAction action) {
    add(new YAWLMenuItem(action));
  }
}
