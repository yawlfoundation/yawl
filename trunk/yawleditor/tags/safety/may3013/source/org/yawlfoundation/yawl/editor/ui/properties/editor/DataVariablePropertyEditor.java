package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;
import org.yawlfoundation.yawl.elements.YDecomposition;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataVariablePropertyEditor extends DialogPropertyEditor {

    private String currentText;
    private NetTaskPair netTaskPair;

    public DataVariablePropertyEditor() {
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
        DataVariableDialog dialog;
        YDecomposition decomposition = netTaskPair.getDecomposition();
        if (decomposition == null) {
            dialog = new DataVariableDialog(netTaskPair.getNet());
        }
        else {
            dialog = new DataVariableDialog(netTaskPair.getNet(), decomposition,
                    netTaskPair.getTask());
        }
        dialog.setVisible(true);
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getNet(), oldPair.getDecomposition(),
                oldPair.getTask());
        firePropertyChange(oldPair, netTaskPair);
    }

}

