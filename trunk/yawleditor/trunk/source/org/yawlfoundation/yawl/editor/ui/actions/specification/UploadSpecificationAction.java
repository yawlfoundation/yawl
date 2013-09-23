package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.SpecificationUploadDialog;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 19/09/13
 */
public class UploadSpecificationAction extends YAWLOpenSpecificationAction
        implements TooltipTogglingWidget {

    {
      putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
      putValue(Action.NAME, "Upload");
      putValue(Action.LONG_DESCRIPTION, "Upload this specification.");
      putValue(Action.SMALL_ICON, getPNGIcon("arrow_up"));
      putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
      putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift U"));
    }

    public void actionPerformed(ActionEvent event) {
        new SpecificationUploadDialog(YAWLEditor.getInstance()).setVisible(true);
    }

    public String getEnabledTooltipText() {
      return " Upload this specification to YAWL Engine ";
    }

    public String getDisabledTooltipText() {
      return " You must have an open specification to upload it ";
    }

}
