package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.net.ApplyProcessConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.CheckProcessCorrectness;
import org.yawlfoundation.yawl.editor.ui.actions.net.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModelListener;

import javax.swing.*;

/**
 * Author: Michael Adams
 * Creation Date: 13/05/2010
 */
public class ProcessConfigurationMenu extends JMenu
              implements ProcessConfigurationModelListener {

    public ProcessConfigurationMenu() {
        super("Process Configuration");
        add(new YAWLPopupMenuCheckBoxItem(PreviewConfigurationProcessAction.getInstance()));
        add(new YAWLPopupMenuCheckBoxItem(ApplyProcessConfigurationAction.getInstance()));
        add(new YAWLMenuItem(new CheckProcessCorrectness()));
        setIcon(ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/wrench.png"));
        ProcessConfigurationModel.getInstance().subscribe(this);
    }

    
    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        YAWLPopupMenuCheckBoxItem previewMenu = (YAWLPopupMenuCheckBoxItem) getItem(0);
        previewMenu.setSelected(previewState != ProcessConfigurationModel.PreviewState.OFF);

        YAWLPopupMenuCheckBoxItem applyMenu = (YAWLPopupMenuCheckBoxItem) getItem(1);
        applyMenu.setSelected(applyState == ProcessConfigurationModel.ApplyState.ON);
    }
}
