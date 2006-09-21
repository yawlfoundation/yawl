/*
 * Created on 16/05/2004
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

import java.awt.Color;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.border.EtchedBorder;

public class ValidityEditorPane extends JEditorPane implements AbstractXMLStyledDocumentValidityListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Color VALID_COLOR = Color.GREEN.darker().darker();
  private static final Color INVALID_COLOR = Color.RED.darker();
  
  private static final Font  COURIER             = new Font("Monospaced", Font.PLAIN, 12);
  private static final Color DISABLED_BACKGROUND = Color.LIGHT_GRAY;  
  
  private Color enabledBackground;
 
  public ValidityEditorPane() {
    setBorder(new EtchedBorder());
    setFont(COURIER);
    enabledBackground = this.getBackground();
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled) {
      this.setBackground(enabledBackground);
    } else {
      this.setBackground(DISABLED_BACKGROUND);
    }
    super.setEnabled(enabled);
  }
  
  public void setDocument(AbstractXMLStyledDocument document) {
    super.setDocument(document);
    subscribeForValidityEvents();
  }
  
  public boolean isContentValid() {
    return ((AbstractXMLStyledDocument) getDocument()).isContentValid();
  }
  
  public void validate() {
    ((AbstractXMLStyledDocument) getDocument()).publishValidity();
  }
  
  protected void subscribeForValidityEvents() {
    acceptValiditySubscription(this);
  }
  
  public void acceptValiditySubscription(AbstractXMLStyledDocumentValidityListener subscriber) {
    ((AbstractXMLStyledDocument) getDocument()).subscribe(subscriber);
  }
  
  public void setText(String text) {
    super.setText(text);
    validate();
  }
  
  public List getProblemList() {
    return ((AbstractXMLStyledDocument) this.getDocument()).getProblemList();
  }
  
  public void setTargetVariableName(String targetVariableName) {
    ((AbstractXMLStyledDocument) getDocument()).setPreAndPostEditorText(
      "<" + targetVariableName + ">",
      "</" + targetVariableName + ">"
    );
  }
  
  public void documentValidityChanged(boolean documentValid) {
    setForeground(documentValid ? VALID_COLOR : INVALID_COLOR);
  }
}