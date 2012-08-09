package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.actions.element.CustomFormDialogPanel;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class CustomFormPropertyEditor extends DialogPropertyEditor {

    private String currentText;

    public CustomFormPropertyEditor() {
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
        String urlStr = currentText;
        if (urlStr == null || urlStr.length() == 0) urlStr = "http://";
        String newUrlText = "";
        boolean done = false;
        CustomFormDialogPanel dialogPanel = new CustomFormDialogPanel();
        dialogPanel.setURI(urlStr);

        while (! done) {
            int option = JOptionPane.showOptionDialog(editor, dialogPanel,
                    "Set Custom Form URI",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);

            if (option == JOptionPane.CANCEL_OPTION) {
                newUrlText = currentText;
                break;
            }

            newUrlText = dialogPanel.getURI();
            if ((newUrlText.length() == 0) || newUrlText.equals("http://")) {
                newUrlText = "";
                done = true;
            }
            else {                                         // uri supplied
                try {
                    new URL(newUrlText);                   // check for wellformedness
                    done = true;                           // passed the test
                }
                catch (MalformedURLException mfue) {       // not wellformed - try again
                    JOptionPane.showMessageDialog(editor,
                            "'" + newUrlText +
                            "' is not a valid absolute URL. Please correct it or cancel.",
                            "Malformed URL",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (! (newUrlText == null || newUrlText.equals(currentText))) {
            String oldUrlText = currentText;
            setValue(newUrlText);
            firePropertyChange(oldUrlText, newUrlText);
        }
    }

}

