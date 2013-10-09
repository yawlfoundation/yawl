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

import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.List;

public class XMLSchemaEditor extends ValidityEditorPane {

    private XMLSchemaEditorPane containingPane;

    public XMLSchemaEditor() {
        super();
        setDocument(new XMLStyledDocument(this));
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
        addCaretListener(new SelectionListener());
    }

    public void setContainingPane(XMLSchemaEditorPane pane) {
        containingPane = pane;
    }

    public XMLSchemaEditorPane getContainingPane() {
        return containingPane;
    }


    class SelectionListener implements CaretListener {

        public SelectionListener() {
            super();
        }

        public void caretUpdate(CaretEvent e) {
            int dot = e.getDot();
            int mark = e.getMark();
            boolean selected = (dot != mark);
            DataTypeDialogToolBarMenu.getInstance().setOnSelectionEnabled(selected);

            XMLSchemaEditorPane pane = ((XMLSchemaEditor) e.getSource()).getContainingPane();

            if (selected) {
                List<Highlighter.Highlight> toClear = new ArrayList<Highlighter.Highlight>();
                int start = Math.min(dot, mark);
                int end = Math.max(dot, mark);
                for (Highlighter.Highlight h : pane.getEditor().getHighlighter().getHighlights()) {
                    if (! ((h.getStartOffset() == start) && (h.getEndOffset() == end))) {
                        toClear.add(h);
                    }
                }
                for (Highlighter.Highlight h : toClear) {
                    pane.getEditor().getHighlighter().removeHighlight(h);
                }
            }
            else {
                pane.getEditor().getHighlighter().removeAllHighlights();
            }
        }
    }

}
