package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.elements.YDecomposition;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class ExtendedAttributesPropertyEditor extends DialogPropertyEditor {

    private YDecomposition decomposition;

    public ExtendedAttributesPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return decomposition;
    }

    public void setValue(Object value) {
        decomposition = (YDecomposition) value;
        ((DefaultCellRenderer) label).setValue(
                decomposition.getAttributes().size() + " defined");
    }


    protected void showDialog() {
        ExtendedAttributesDialog dialog = new ExtendedAttributesDialog(decomposition);
        dialog.setVisible(true);
    }

}

