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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResourceSet;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public abstract class AbstractResourceTabContent extends JPanel {

    private Logger _log;
    private YNet _net;
    private YAtomicTask _task;


    protected AbstractResourceTabContent(YNet net, YAtomicTask task) {
        super();
        _net = net;
        _task = task;
        _log = Logger.getLogger(this.getClass());
    }


    protected YNet getNet() { return _net; }

    protected YAtomicTask getTask() { return _task; }

    protected Logger getLog() { return _log; }


    protected TaskResourceSet getTaskResources() {
        YResourceHandler resHandler = SpecificationModel.getHandler().getResourceHandler();
        return resHandler.getOrCreateTaskResources(_net.getID(), _task.getID());
    }

    protected Set<YAtomicTask> getAllPrecedingTasks(YAtomicTask task) {
        YResourceHandler resHandler = SpecificationModel.getHandler().getResourceHandler();
        return resHandler.getAllPrecedingAtomicTasks(task);
    }


    protected JCheckBox createCheckBox(String caption, int mnemonic,
                                       ItemListener listener, ResourceDialog owner) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setMnemonic(mnemonic);
        checkBox.addItemListener(listener);
        checkBox.addItemListener(owner);
        return checkBox;
    }


    protected void enablePanelContent(JPanel panel, boolean enabled) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                enablePanelContent((JPanel) component, enabled);
            }
            component.setEnabled(enabled);
        }
    }


    public abstract void load();

    public abstract void save();

}
