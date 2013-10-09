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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class NetBackgroundImageAction extends YAWLSelectedNetAction {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, " Set the net background image. ");
    putValue(Action.NAME, "Set Net Background Image...");
    putValue(Action.LONG_DESCRIPTION, "Set the net background image.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_I));
    putValue(Action.SMALL_ICON, getPNGIcon("picture"));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift B"));
  }

  public NetBackgroundImageAction() {}

  public void actionPerformed(ActionEvent event) {
      JFileChooser chooser = new JFileChooser("Select Background Image for Net");
      chooser.setFileFilter(new ImageFilter());
      int result = chooser.showOpenDialog(YAWLEditor.getInstance());
      if (result == JFileChooser.APPROVE_OPTION) {
          try {
              String path = chooser.getSelectedFile().getCanonicalPath();
              ImageIcon bgImage = ResourceLoader.getExternalImageAsIcon(path);
              if (bgImage != null) {
                  bgImage.setDescription(path);   // store path
                  getGraph().setBackgroundImage(bgImage);
                  SpecificationUndoManager.getInstance().setDirty(true);
              }
          }
          catch (IOException ioe) {
              // ignore
          }
      }
  }

}