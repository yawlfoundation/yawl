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

package org.yawlfoundation.yawl.editor.ui.data.document;

import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.ValidityEditorPane;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToolBarButton;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
* @author Michael Adams
* @date 8/08/13
*/
public class XMLStyledDocument extends AbstractXMLStyledDocument {

     public XMLStyledDocument(ValidityEditorPane editor) {
        super(editor);
    }

    public List<String> getProblemList() {
        String content;
        try {
            content = new String(getEditor().getText().getBytes(), "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            content = getEditor().getText();
        }
        return new DataSchemaValidator().validateSchema(content);
    }

    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
        // deliberately does nothing.
    }

    public void checkValidity() {
        if (getEditor().getText().equals("")) {
            setContentValidity(Validity.VALID);
        }
        else if (isValidating()) {
            setContentValidity(
                    getProblemList().isEmpty() ? Validity.VALID : Validity.INVALID);
        }
        DataTypeDialogToolBarMenu menu = DataTypeDialogToolBarMenu.getInstance();
        if (menu != null) {
            YAWLToolBarButton formatBtn = menu.getButton("format");
            if (formatBtn != null) formatBtn.setEnabled(isContentValid());
        }
    }
}
