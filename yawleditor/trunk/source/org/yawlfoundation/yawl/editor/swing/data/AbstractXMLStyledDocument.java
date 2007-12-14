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
package au.edu.qut.yawl.editor.swing.data;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public abstract class AbstractXMLStyledDocument extends DefaultStyledDocument  {
  
  public static enum Validity {
    VALID,
    INVALID,
    UNCERTAIN
  }
  
  private LinkedList<AbstractXMLStyledDocumentValidityListener> subscribers 
       = new LinkedList<AbstractXMLStyledDocumentValidityListener>();
  
  private ValidityEditorPane editor;
  
  private Validity contentValid = Validity.UNCERTAIN;
  
  public AbstractXMLStyledDocument(ValidityEditorPane editor) {
    this.editor = editor;
  }

  public void insertString(int offset,
                           String text, AttributeSet style)
                           throws BadLocationException { 
    super.insertString(offset, text, style);
    publishValidity();
  } 
  
  public void remove(int offset, int length)
        throws BadLocationException {
    super.remove(offset, length);
    publishValidity();
  } 
  
  public void replace(int offset, int length, String text, AttributeSet attrs) 
         throws BadLocationException  {
    
    super.replace(offset, length, text, attrs);
    publishValidity();
  }
  
  public void publishValidity() {
    checkValidity();
    publishValidityToSubscribers();
  }
  
  private void publishValidityToSubscribers() {
    Iterator subscriberIterator = subscribers.iterator();
    while(subscriberIterator.hasNext()) {
      AbstractXMLStyledDocumentValidityListener subscriber = (AbstractXMLStyledDocumentValidityListener) 
          subscriberIterator.next();
      subscriber.documentValidityChanged(contentValid);
    }
  }
  
  public void subscribe(AbstractXMLStyledDocumentValidityListener subscriber) {
    subscribers.add(subscriber);
  }
  
  public abstract void checkValidity();
  public abstract void setPreAndPostEditorText(String preEditorText, String postEditorText);
  public abstract List getProblemList();

  public boolean isContentValidity() {
    return getContentValidity() == Validity.VALID;
  }
  
  public Validity getContentValidity() {
    return contentValid;
  }
  
  public ValidityEditorPane getEditor() {
    return this.editor;
  }
  
  public void setContentValid(Validity validity) {
    this.contentValid = validity;
  }
}
