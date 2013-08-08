/*
 * Created on 16/07/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractXMLStyledDocument extends DefaultStyledDocument {

    private List<XMLStyledDocumentValidityListener> subscribers;
    private ValidityEditorPane editor;
    private Validity contentValid;
    private boolean validating;


    public AbstractXMLStyledDocument(ValidityEditorPane editorPane) {
        editor = editorPane;
        subscribers = new ArrayList<XMLStyledDocumentValidityListener>();
        contentValid = Validity.UNCERTAIN;
        validating = true;
    }


    public ValidityEditorPane getEditor() {
        return editor;
    }

    public void insertString(int offset, String text, AttributeSet style)
            throws BadLocationException {
        super.insertString(offset, text, style);
        publishValidity();
    }

    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        publishValidity();
    }

    public void replace(int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        super.replace(offset, length, text, attrs);
        publishValidity();
    }

    public void publishValidity() {
        checkValidity();
        for (XMLStyledDocumentValidityListener subscriber : subscribers) {
            subscriber.documentValidityChanged(contentValid);
        }
    }


    public void subscribe(XMLStyledDocumentValidityListener subscriber) {
        subscribers.add(subscriber);
    }

    public boolean isContentValid() {
        return getContentValidity() == Validity.VALID;
    }

    public Validity getContentValidity() {
        return contentValid;
    }

    public void setContentValidity(Validity validity) {
        contentValid = validity;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public boolean isValidating() { return validating; }


    /******************************************************************/

    public abstract void checkValidity();

    public abstract void setPreAndPostEditorText(String preText, String postText);

    public abstract List<String> getProblemList();

}
