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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;

public class YAWLMenuBar extends JMenuBar {
  
  private static final long serialVersionUID = 1L;

  public YAWLMenuBar() {
    super();
    int progress = 0;
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new SpecificationMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new NetMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new EditMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new ElementsMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new SettingsMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new ViewMenu());
    YAWLEditor.updateLoadProgress(progress+=10);
    add(new HelpMenu());
   }
}