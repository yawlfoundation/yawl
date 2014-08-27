package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.components.ButtonPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 26/08/2014
 */
public class BackgroundChecker implements PropertyChangeListener {

    private final UpdateChecker _checker;
    private final ButtonPanel _source;

    public BackgroundChecker(ButtonPanel source) {
        _source = source;
        _checker = new UpdateChecker();
        _checker.addPropertyChangeListener(this);
        _checker.execute();
    }


    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.DONE) {
                if (! _checker.hasError() && _checker.hasUpdates()) {
                    showMessage();
                }
            }
        }
    }


    private void showMessage() {
        int response = JOptionPane.showConfirmDialog(_source,
                "There are new YAWL updates available.\n" +
                        "Click OK to view Updates Dialog, or Cancel to ignore.",
                "Updates Available",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (response == JOptionPane.OK_OPTION) {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                    "Updates");
            _source.actionPerformed(event);
        }
    }

}
