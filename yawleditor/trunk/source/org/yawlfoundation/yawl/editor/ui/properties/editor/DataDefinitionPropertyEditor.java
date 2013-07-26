package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.DataDefinitionDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataDefinitionPropertyEditor extends DialogPropertyEditor {

    private String currentDefinition;

    public DataDefinitionPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentDefinition;
    }

    public void setValue(Object value) {
        currentDefinition = (String) value;
        ((DefaultCellRenderer) label).setValue(currentDefinition);
    }


    protected void showDialog() {
        DataDefinitionDialog dialog = new DataDefinitionDialog();
        dialog.setContent(currentDefinition);
        dialog.setVisible(true);
        String newContent = dialog.getContent();
        if (! (newContent == null || newContent.equals(currentDefinition))) {
            String oldContent = currentDefinition;
            setValue(newContent);
            firePropertyChange(oldContent, newContent);
        }

    }

}

