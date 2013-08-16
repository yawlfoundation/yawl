package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TextAreaDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class TextPropertyEditor extends DialogPropertyEditor {

    private String currentText;

    public TextPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentText;
    }

    public void setValue(Object value) {
        currentText = (String) value;
        ((DefaultCellRenderer) label).setValue(currentText);
    }

    protected void showDialog() {
        String newText = new TextAreaDialog(YAWLEditor.getInstance(),
                "Update text", currentText).showDialog();
        if (! (newText == null || newText.equals(currentText))) {
            String oldText = currentText;
            setValue(newText);
            firePropertyChange(oldText, newText);
        }
    }

}

