package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.CodeletDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class CodeletPropertyEditor extends DialogPropertyEditor {

    private String currentCodelet;
    private String simpleCodeletName;

    public CodeletPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return simpleCodeletName;
    }

    public void setValue(Object value) {
        currentCodelet = (String) value;
        ((DefaultCellRenderer) label).setValue(getSimpleName());
    }


    protected void showDialog() {
        CodeletDialog dialog = new CodeletDialog();
        dialog.setSelection(currentCodelet);
        dialog.setVisible(true);
        String newCodelet = dialog.getSelection();
        if (! (newCodelet == null || newCodelet.equals(currentCodelet))) {
            String oldCodelet = currentCodelet;
            setValue(newCodelet);
            firePropertyChange(oldCodelet, newCodelet);
        }
    }


    private String getSimpleName() {
        simpleCodeletName = null;
        if (currentCodelet != null) {
            int lastDot = currentCodelet.lastIndexOf('.');
            simpleCodeletName = lastDot > -1 ? currentCodelet.substring(lastDot + 1) :
                    currentCodelet;
        }
        return simpleCodeletName;
    }

}

