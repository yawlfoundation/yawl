/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.actions.help;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.update.UpdateChecker;
import org.yawlfoundation.yawl.editor.ui.update.UpdateDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 23/05/2014
 */
public class UpdateAction extends YAWLBaseAction implements PropertyChangeListener {

    private UpdateChecker _checker;

    {
        putValue(Action.SHORT_DESCRIPTION, "Check for Updates");
        putValue(Action.NAME, "Check for Updates...");
        putValue(Action.LONG_DESCRIPTION, "Check for Updates");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
    }

    public void actionPerformed(ActionEvent event) {
        setCursor(Cursor.WAIT_CURSOR);
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        _checker.execute();
    }


    // events from UpdateChecker process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                setCursor(Cursor.DEFAULT_CURSOR);
                if (_checker.hasError()) {
                    showError(_checker.getErrorMessage());
                }
                else if (_checker.hasUpdate()) {
                    new UpdateDialog(_checker).setVisible(true);
                }
                else showInfo("No updates available, you have the latest version.");
            }
        }
    }


    private void showInfo(String message) {
        showMessage(message, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        showMessage(message, JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                message, "Check for Updates", type);
    }

    private void setCursor(int cursor) {
        YAWLEditor.getInstance().setCursor(Cursor.getPredefinedCursor(cursor));
    }

}
