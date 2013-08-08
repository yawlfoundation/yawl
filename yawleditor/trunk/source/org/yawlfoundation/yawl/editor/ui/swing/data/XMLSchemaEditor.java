package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.List;

/**********************************************************************************/

class XMLSchemaEditor extends ValidityEditorPane {

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
