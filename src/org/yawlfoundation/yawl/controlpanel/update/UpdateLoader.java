/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.util.CursorUtil;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateLoader implements PropertyChangeListener {

    private UpdateChecker _checker;
    private YControlPanel _mainWindow;

    public UpdateLoader(YControlPanel mainWindow) { _mainWindow = mainWindow; }


    public void execute() {
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        CursorUtil.showWaitCursor();
        _checker.execute();
    }


    // events from UpdateChecker process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                CursorUtil.showDefaultCursor();
                if (_checker.hasError()) {
                    showError(_checker.getErrorMessage());
                }
                else if (_checker.getDiffer().isNewVersion()) {
                    new NewVersionDialog(_mainWindow, _checker.getDiffer()).setVisible(true);
                }
                else {

                    // show latest version numbers
                    _mainWindow.getComponentsPane().refresh(_checker.getDiffer(), false);
                }
            }
        }
    }


    private void showError(String msg) {
        JOptionPane.showMessageDialog(_mainWindow, msg, "Check for Updates Error",
                JOptionPane.ERROR_MESSAGE);
    }

}
