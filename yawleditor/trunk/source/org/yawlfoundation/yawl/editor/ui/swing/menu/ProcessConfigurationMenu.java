package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.element.CancellationRegionConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.element.InputPortConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.element.MultipleInstanceConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.element.OutputPortConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.ApplyProcessConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.CheckProcessCorrectness;
import org.yawlfoundation.yawl.editor.ui.actions.net.ConfigurableTaskAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.ui.actions.tools.ConfigurationSettingsAction;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModelListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;

/**
 * Author: Michael Adams
 * Creation Date: 13/05/2010
 */
public class ProcessConfigurationMenu extends JMenu
        implements ProcessConfigurationModelListener, FileStateListener {

    JMenu _netMenu;
    JMenu _taskMenu;

    public ProcessConfigurationMenu() {
        super("Process Configuration");
        add(getNetMenu());
        add(getTaskMenu());
        add(new YAWLMenuItem(new ConfigurationSettingsAction()));

        Publisher.getInstance().subscribe(this);
        ProcessConfigurationModel.getInstance().subscribe(this);
        setIcon(ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/wrench.png"));
    }

    public void specificationFileStateChange(FileState state) {
        setEnabled(state == FileState.Open);
    }

    private JMenu getNetMenu() {
        _netMenu = new JMenu("Net");
        _netMenu.setMnemonic('N');
        _netMenu.add(new YAWLPopupMenuCheckBoxItem(
                PreviewConfigurationProcessAction.getInstance()));
        _netMenu.add(new YAWLPopupMenuCheckBoxItem(
                ApplyProcessConfigurationAction.getInstance()));
        _netMenu.add(new YAWLMenuItem(new CheckProcessCorrectness()));

        return _netMenu;
    }


    private JMenu getTaskMenu() {
        _taskMenu = new JMenu("Task");
         _taskMenu.setMnemonic('T');
        _taskMenu.add(buildConfigurableTaskItem());
        _taskMenu.add(new YAWLPopupMenuItem(new InputPortConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new OutputPortConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new MultipleInstanceConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new CancellationRegionConfigurationAction()));
        return _taskMenu;
    }


    private YAWLPopupMenuCheckBoxItem buildConfigurableTaskItem() {
        ConfigurableTaskAction action = new ConfigurableTaskAction();
        YAWLPopupMenuCheckBoxItem configurableTaskItem =
                new YAWLPopupMenuCheckBoxItem(action);
        action.setCheckBox(configurableTaskItem);
        return configurableTaskItem;
    }



    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        YAWLPopupMenuCheckBoxItem previewMenu =
                (YAWLPopupMenuCheckBoxItem) _netMenu.getItem(0);
        previewMenu.setSelected(previewState != ProcessConfigurationModel.PreviewState.OFF);

        YAWLPopupMenuCheckBoxItem applyMenu =
                (YAWLPopupMenuCheckBoxItem) _netMenu.getItem(1);
        applyMenu.setSelected(applyState == ProcessConfigurationModel.ApplyState.ON);
    }
}
