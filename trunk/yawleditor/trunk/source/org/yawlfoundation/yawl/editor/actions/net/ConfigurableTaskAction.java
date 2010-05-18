/**
 *   created on 08/12/2009
 *   author Jingxin XU
 */

package org.yawlfoundation.yawl.editor.actions.net;

import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ConfigurableTaskAction extends YAWLSelectedNetAction implements TooltipTogglingWidget{

    private static final long serialVersionUID = 1L;

    private boolean selected;
    private NetGraph net;
    private YAWLTask task;
    private JCheckBoxMenuItem checkBox = null;

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Set Task Configurable");
        putValue(Action.LONG_DESCRIPTION, "Set the task to be configurable");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        putValue(Action.SMALL_ICON, getPNGIcon("sitemap_color"));        

    }

    public ConfigurableTaskAction(YAWLTask task,NetGraph net) {
        super();
        this.net = net;
        this.task = task;
        this.selected = false;

    }


    public void actionPerformed(ActionEvent event) {
        task.setConfigurable(! task.isConfigurable());
        net.changeLineWidth(task);
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setCheckBox(JCheckBoxMenuItem checkBox) {
        this.checkBox = checkBox;
    }

    public JCheckBoxMenuItem getCheckBox(){
        return this.checkBox;
    }

    public String getDisabledTooltipText() {
        return "Configure this task";
    }


    public String getEnabledTooltipText() {
        return "Set the task to be configurable";
    }

}
