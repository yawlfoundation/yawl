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

package org.yawlfoundation.yawl.editor.ui.update;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 23/05/2014
 */
public class BackgroundUpdateChecker implements PropertyChangeListener {

    private final UpdateChecker _checker;

    public BackgroundUpdateChecker() {
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        _checker.execute();
    }

    public void execute() {
        _checker.execute();
    }


    // events from UpdateChecker process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                if (_checker.hasError()) {
                    showMessage("Error " + _checker.getErrorMessage());
                }
                else if (_checker.hasUpdate()) {
                    new UpdateDialog(_checker.getDiffer()).setVisible(true);
                }
                else showMessage("you have the latest version");
            }
        }
    }


    private void showMessage(String message) {
        YAWLEditor.getStatusBar().setText("Check for Updates: " + message);
    }

}
