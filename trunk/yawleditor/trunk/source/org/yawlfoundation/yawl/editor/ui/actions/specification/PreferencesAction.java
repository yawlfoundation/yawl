package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 26/09/13
 */
public class PreferencesAction extends YAWLBaseAction {

      {
        putValue(Action.SHORT_DESCRIPTION, "Set Preferences");
        putValue(Action.NAME, "Preferences...");
        putValue(Action.LONG_DESCRIPTION, "Set Preferences");
        putValue(Action.SMALL_ICON, getPNGIcon("page_white_gear"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("F"));
      }

      public void actionPerformed(ActionEvent event) {
          new PreferencesDialog().setVisible(true);
      }

}
