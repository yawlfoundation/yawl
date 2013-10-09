/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import java.awt.event.KeyEvent;

import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.CutAction;
import org.yawlfoundation.yawl.editor.ui.actions.PasteAction;
import org.yawlfoundation.yawl.editor.ui.actions.RedoAction;
import org.yawlfoundation.yawl.editor.ui.actions.UndoAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.DeleteAction;

class EditMenu extends YAWLOpenSpecificationMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public EditMenu() {
    super("Edit", KeyEvent.VK_E);
  }
  
  protected void buildInterface() {
    add(new YAWLMenuItem(UndoAction.getInstance()));
    add(new YAWLMenuItem(RedoAction.getInstance()));
    addSeparator();
    add(new YAWLMenuItem(CutAction.getInstance()));
    add(new YAWLMenuItem(CopyAction.getInstance()));
    add(new YAWLMenuItem(PasteAction.getInstance()));
    addSeparator();
    add(new YAWLMenuItem(DeleteAction.getInstance()));
  }
}
