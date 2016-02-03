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
