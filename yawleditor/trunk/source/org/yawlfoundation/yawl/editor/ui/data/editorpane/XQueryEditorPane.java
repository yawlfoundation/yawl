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


package org.yawlfoundation.yawl.editor.ui.data.editorpane;


import org.yawlfoundation.yawl.editor.ui.data.editor.XQueryEditor;

import javax.swing.text.Document;

public class XQueryEditorPane extends ProblemReportingEditorPane {

    public XQueryEditorPane() {
        super(new XQueryEditor());
    }

    public XQueryEditorPane(ValidityEditorPane editor) {
        super(editor);
    }

    public XQueryEditorPane(String extraParseText) {
        super(new XQueryEditor(extraParseText));
    }

    public void setTargetVariableName(String targetVariableName) {
        getEditor().setTargetVariableName(targetVariableName);
    }

    public Document getDocument() {
        return getEditor().getDocument();
    }

    public int getCaretPosition() {
        return getEditor().getCaretPosition();
    }

    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
        getXQueryEditor().setPreAndPostEditorText(preEditorText, postEditorText);
    }

    public XQueryEditor getXQueryEditor() {
        return (XQueryEditor) getEditor();
    }

    public void setValidating(boolean validating) {
        getXQueryEditor().setValidating(validating);
    }
}

