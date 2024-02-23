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

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Checks for updates in the background. Informs user only if updates are available
 * @author Michael Adams
 * @date 26/08/2014
 */
public class BackgroundChecker implements PropertyChangeListener {

    private final UpdateChecker _checker;
    private final YControlPanel _mainWindow;


    public BackgroundChecker(YControlPanel mainWindow) {
        _mainWindow = mainWindow;
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        _checker.execute();
    }


    // state events from UpdateChecker (SwingWorker)
    public void propertyChange(PropertyChangeEvent event) {

        // if checker has completed without errors and has updates, show the updates
        if (event.getPropertyName().equals("state") &&
                event.getNewValue() == SwingWorker.StateValue.DONE &&
                ! _checker.hasError() && _checker.hasUpdates()) {
            showMessage();
            _mainWindow.showComponentsPane();
            _mainWindow.getComponentsPane().refresh(_checker.getDiffer(), false);
        }
    }


    private void showMessage() {
        JOptionPane.showMessageDialog(_mainWindow, "There are new YAWL updates available.",
                "Updates Available", JOptionPane.INFORMATION_MESSAGE);
    }

}
