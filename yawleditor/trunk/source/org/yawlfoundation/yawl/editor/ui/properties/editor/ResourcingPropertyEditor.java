package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class ResourcingPropertyEditor extends DialogPropertyEditor {

    private String currentText;
    private NetTaskPair netTaskPair;

    public ResourcingPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return netTaskPair;
    }

    public void setValue(Object value) {
        netTaskPair = (NetTaskPair) value;
        currentText = netTaskPair.getSimpleText();
        ((DefaultCellRenderer) label).setValue(currentText);
    }

    protected void showDialog() {
        if (isEmptyOrMissingOrgModel()) return;

        ResourceDialog dialog = new ResourceDialog(netTaskPair.getNet(),
                    netTaskPair.getTask());
        dialog.setVisible(true);
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getNet(), null, oldPair.getTask());
        netTaskPair.setSimpleText(dialog.getInteractionString());
        firePropertyChange(oldPair, netTaskPair);
    }

    private boolean isEmptyOrMissingOrgModel() {
        if (YConnector.isResourceConnected()) {
            if (! YConnector.hasResources()) {
                JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                        "The organisational model supplied by the " +
                        "Resource Service contains no participants or roles.\n" +
                        "There are no resources available to assign to the selected task.",
                        "No Available Resources", JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }
        else {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                 "A connection to the " +
                 "Resource Service has not been established.\n" +
                 "Please connect to a running Resource Service via the Settings menu.",
                 "Service Unavailable", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;     // not missing and not empty
    }


}

