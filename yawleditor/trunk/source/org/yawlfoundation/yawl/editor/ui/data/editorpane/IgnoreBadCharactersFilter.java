package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

class IgnoreBadCharactersFilter extends DocumentFilter {

    public void replace(FilterBypass bypass,
                        int offset,
                        int length,
                        String text,
                        AttributeSet attributes) throws BadLocationException {
        if (isValidText(text))
            super.replace(bypass,offset,length,text,attributes);
    }

    protected boolean isValidText(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i)== '\"' || text.charAt(i)=='&') {
                return false;
            }
        }
        return true;
    }
}
