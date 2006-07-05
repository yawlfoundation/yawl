/*
 * Created on 27/02/2002
 * Donated from Lindsay's private source library
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
 *
 */

package au.edu.qut.yawl.editor.swing;

import javax.swing.JFormattedTextField;
import javax.swing.Timer;

import java.awt.Color;

import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JFormattedSelectField extends JFormattedTextField {
  
  private Color normalBackground;
  private Color normalForeground;

  public JFormattedSelectField(AbstractFormatter formatter) {
    super(formatter);
    configure();
  }
  
  public JFormattedSelectField(int columns) {
    super();
    configure();
    setColumns(columns);
  }
  
  public JFormattedSelectField() {
    super();
    configure();
  }
  
  private void configure() {
    setDisabledTextColor(Color.BLACK);

    normalBackground = getBackground();
    normalForeground = getForeground();
  }

  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if (e.getID() == FocusEvent.FOCUS_GAINED) {

      // Select the text so that the caret is at the
      // start of the text field.
      
      setCaretPosition(getText().length());
      moveCaretPosition(0);
    }
  }

  public void postActionEvent() {
    super.postActionEvent();
    selectAll();
  }
  
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (!enabled) {
      setBackground(Color.LIGHT_GRAY);
    } else {
      setBackground(normalBackground);
    }
  }
  
  public void setText(String text) {
    super.setText(text);
    setCaretPosition(0);
  }

  public void invalidEdit() {

    super.invalidEdit();

    setBackground(Color.RED);
    setForeground(Color.WHITE);
    
    final JFormattedSelectField field = this;
    
    ActionListener colorChanger = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        field.setBackground(normalBackground);
        field.setForeground(normalForeground);
      }
    };
    
    Timer timer = new Timer(75, colorChanger);
    timer.setRepeats(false);
    timer.start();
  }
}
