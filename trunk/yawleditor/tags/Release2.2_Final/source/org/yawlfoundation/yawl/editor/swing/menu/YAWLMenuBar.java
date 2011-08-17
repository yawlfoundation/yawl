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

package org.yawlfoundation.yawl.editor.swing.menu;

import javax.swing.JMenuBar;

import org.yawlfoundation.yawl.editor.YAWLEditor;

public class YAWLMenuBar extends JMenuBar {
  
  private static final long serialVersionUID = 1L;
  private int progress = 0;
  
  public YAWLMenuBar() {
    super();
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new SpecificationMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new NetMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new EditMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new ElementsMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    if (SettingsMenu.needsToBeAddedToMenus()) {
      add(new SettingsMenu());
      YAWLEditor.updateLoadProgress(progress+=10);
    } else {
      progress+=10;
    }
    add(new ViewMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new HelpMenu());
   }
}