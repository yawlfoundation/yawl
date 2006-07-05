/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import au.edu.qut.yawl.editor.actions.net.PrintNetAction;
import au.edu.qut.yawl.editor.actions.net.RemoveNetAction;
import au.edu.qut.yawl.editor.actions.net.SetStartingNetAction;
import au.edu.qut.yawl.editor.actions.net.ExportNetToPngAction;
import au.edu.qut.yawl.editor.actions.net.NetDecompositionDetailAction;
import au.edu.qut.yawl.editor.actions.specification.CreateNetAction;

import au.edu.qut.yawl.editor.swing.YAWLMenuItem;
import au.edu.qut.yawl.editor.swing.JSplashScreen;

class NetMenu extends YAWLOpenSpecificationMenu {
    
  public NetMenu() {
    super("Net", KeyEvent.VK_N);
  }   
  
  protected void buildInterface() {
    add(new YAWLMenuItem(new CreateNetAction()));
    JSplashScreen.getInstance().updateProgressBar(22);

    add(new YAWLMenuItem(new RemoveNetAction()));
    JSplashScreen.getInstance().updateProgressBar(23);

    add(new YAWLMenuItem(new SetStartingNetAction()));
    JSplashScreen.getInstance().updateProgressBar(25);
    
    add(new YAWLMenuItem(new NetDecompositionDetailAction()));
    JSplashScreen.getInstance().updateProgressBar(26);

    addSeparator();
    add(new YAWLMenuItem(new ExportNetToPngAction()));   
    JSplashScreen.getInstance().updateProgressBar(27);
    
    addSeparator();

    add(new YAWLMenuItem(new PrintNetAction()));
    JSplashScreen.getInstance().updateProgressBar(29);
  }
}
