package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataVariablesPropertyEditor extends DialogPropertyEditor {

    private String currentText;
    private NetTaskPair netTaskPair;

    public DataVariablesPropertyEditor() {
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
        new DataVariableDialog(netTaskPair.getNet(), editor).showDialog();
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getNet());
        firePropertyChange(oldPair, netTaskPair);
    }

}

