package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class ExtendedAttributesPropertyEditor extends DialogPropertyEditor {

    private NetTaskPair pair;

    public ExtendedAttributesPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return pair;
    }

    public void setValue(Object value) {
        pair = (NetTaskPair) value;
        ((DefaultCellRenderer) label).setValue(pair.getSimpleText());
    }


    protected void showDialog() {
        ExtendedAttributesDialog dialog =
                new ExtendedAttributesDialog(pair.getDecomposition());
        dialog.setVisible(true);
        NetTaskPair oldPair = pair;
        pair = new NetTaskPair(null, oldPair.getDecomposition(), null);
        String text = pair.getDecomposition().getAttributes().isEmpty() ? "None" :
                pair.getDecomposition().getAttributes().size() + " active";
        pair.setSimpleText(text);
        firePropertyChange(oldPair, pair);
    }

}

