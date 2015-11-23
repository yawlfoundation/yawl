package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;

import javax.swing.*;
import java.awt.*;
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
        _mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        _checker.execute();
    }


    // events from UpdateChecker process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                _mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (_checker.hasError()) {
                    showError(_checker.getErrorMessage());
                }
                else if (_checker.getDiffer().isNewVersion()) {
                    new NewVersionDialog(_mainWindow, _checker.getDiffer()).setVisible(true);
                }
                else {
                    _mainWindow.getComponentsPanel().refresh(_checker.getDiffer());
                }
            }
        }
    }


    private void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Check for Updates Error",
                JOptionPane.ERROR_MESSAGE);
    }

}
