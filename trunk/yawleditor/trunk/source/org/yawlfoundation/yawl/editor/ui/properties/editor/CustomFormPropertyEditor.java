/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

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
            int option = JOptionPane.showOptionDialog(YAWLEditor.getInstance(), dialogPanel,
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

