package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.MultiInstanceDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class MultiInstancePropertyEditor extends DialogPropertyEditor {

    private String currentText;
    private NetTaskPair netTaskPair;

    public MultiInstancePropertyEditor() {
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
        MultiInstanceDialog dialog = new MultiInstanceDialog(netTaskPair.getNet(),
                    netTaskPair.getTask().getID());
        dialog.setVisible(true);
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getNet(), null, oldPair.getTask());
        firePropertyChange(oldPair, netTaskPair);
    }

}

