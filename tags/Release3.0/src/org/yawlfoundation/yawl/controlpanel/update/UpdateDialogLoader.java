package org.yawlfoundation.yawl.controlpanel.update;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateDialogLoader implements PropertyChangeListener {

    private UpdateChecker _checker;
    private UpdateDialog _updateDialog;
    private JFrame _main;

    public UpdateDialogLoader(JFrame main) { _main = main; }


    public void execute() {
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        _main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        _checker.execute();
    }


    public boolean isDialogActive() {
        return _updateDialog != null && _updateDialog.isVisible();
    }


    public void toFront() { if (_updateDialog != null) _updateDialog.toFront(); }


    // events from UpdateChecker process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                _main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (_checker.hasError()) {
                    showError(_checker.getErrorMessage());
                }
                else {
                    _updateDialog = new UpdateDialog(_main, _checker.getDiffer());
                    _updateDialog.setVisible(true);
                    _updateDialog.toFront();
                }
            }
        }
    }


    private void showError(String msg) {
            JOptionPane.showMessageDialog(null, msg, "Check for Updates Error",
                    JOptionPane.ERROR_MESSAGE);
    }


}
