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
package au.edu.qut.yawl.editor.swing;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public abstract class AbstractXMLStyledDocument extends  DefaultStyledDocument  {
  
  private static final Color VALID_COLOR = Color.GREEN.darker().darker();
  private static final Color INVALID_COLOR = Color.RED.darker();
  
  private XMLEditorPane editor;
  
  private boolean isValid = true;
  
  public AbstractXMLStyledDocument(XMLEditorPane editor) {
    this.editor = editor;
  }

  public void insertString(int offset,
                           String text, AttributeSet style)
                           throws BadLocationException { 
    super.insertString(offset, text, style);
    renderValidity();
  } 
  
  public void remove(int offset, int length)
        throws BadLocationException {
    super.remove(offset, length);
    renderValidity();
  } 
  
  public void replace(int offset, int length, String text, AttributeSet attrs) 
         throws BadLocationException  {
    
    super.replace(offset, length, text, attrs);
    renderValidity();
  }
  
  public void renderValidity() {
    checkValidity();
    renderState();
  }
  
  public abstract void checkValidity();
  public abstract void setPreAndPostEditorText(String preEditorText, String postEditorText);
  
  private void renderState() {
    editor.setForeground(isValid ? VALID_COLOR : INVALID_COLOR);
  }
  
  public boolean isValid() {
    return isValid;
  }
  
  public XMLEditorPane getEditor() {
    return this.editor;
  }
  
  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }
}
