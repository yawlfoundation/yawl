/*
 * Created on 07/10/2003
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

import javax.swing.JMenuBar;

import au.edu.qut.yawl.editor.swing.JSplashScreen;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

public class YAWLMenuBar extends JMenuBar {
  
  private int progress = 0;
  
  public YAWLMenuBar() {
    super();
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    add(new SpecificationMenu());
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    add(new NetMenu());
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    add(new EditMenu());
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    add(new ElementsMenu());
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    if (YAWLEngineProxy.engineLibrariesAvailable() || WofYAWLProxy.wofYawlAvailable()) {
      add(new ToolsMenu());
      JSplashScreen.getInstance().updateProgressBar(progress+=10);
    } else {
      progress+=10;
    }
    add(new ViewMenu());
    JSplashScreen.getInstance().updateProgressBar(progress+=10);
    add(new HelpMenu());
   }
}