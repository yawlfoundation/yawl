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

import au.edu.qut.yawl.editor.actions.ExitAction;
import au.edu.qut.yawl.editor.actions.specification.AnalyseSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.CloseSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.CreateSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.OpenSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.SaveSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.SaveSpecificationAsAction;
import au.edu.qut.yawl.editor.actions.specification.ExportToEngineFormatAction;
import au.edu.qut.yawl.editor.actions.specification.UpdateDataTypeDefinitionsAction;
import au.edu.qut.yawl.editor.actions.specification.UpdateSpecificationPropertiesAction;
import au.edu.qut.yawl.editor.actions.specification.ValidateSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.PrintSpecificationAction;

import au.edu.qut.yawl.editor.swing.JSplashScreen;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

class SpecificationMenu extends JMenu {
    
  public SpecificationMenu() {
    super("Specification");
    setMnemonic(KeyEvent.VK_S);
    buildInterface();
  }   
  
  protected void buildInterface() {
    add(new YAWLMenuItem(new CreateSpecificationAction()));
    add(new YAWLMenuItem(new OpenSpecificationAction()));
    addSeparator();
    add(new YAWLMenuItem(new SaveSpecificationAction()));
    add(new YAWLMenuItem(new SaveSpecificationAsAction()));

    JSplashScreen.getInstance().updateProgressBar(12);

    if (YAWLEngineProxy.engineLibrariesAvailable()) {
      addSeparator();
      add(new YAWLMenuItem(new ValidateSpecificationAction()));
      if (WofYAWLProxy.wofYawlAvailable()) {
        add(new YAWLMenuItem(new AnalyseSpecificationAction()));
      }
      add(new YAWLMenuItem(new ExportToEngineFormatAction()));
    }

    JSplashScreen.getInstance().updateProgressBar(16);

    addSeparator();

    add(new YAWLMenuItem(new PrintSpecificationAction()));
    
    addSeparator();
    add(new YAWLMenuItem(new UpdateSpecificationPropertiesAction()));
    add(new YAWLMenuItem(new UpdateDataTypeDefinitionsAction()));
    addSeparator();

    add(new YAWLMenuItem(new CloseSpecificationAction()));
    add(new YAWLMenuItem(new ExitAction(this)));

    JSplashScreen.getInstance().updateProgressBar(18);
  }
}
