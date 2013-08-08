/*
 * Created on 16/05/2004
 * YAWLEditor v1.01
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package org.yawlfoundation.yawl.editor.ui.swing.data;


import javax.swing.text.Document;

public class XQueryEditorPane extends ProblemReportingEditorPane {

    public XQueryEditorPane() {
        super(new XQueryEditor());
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

