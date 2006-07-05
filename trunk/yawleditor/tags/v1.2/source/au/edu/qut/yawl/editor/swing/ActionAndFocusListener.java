/*
 * Created on 28/05/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public abstract class ActionAndFocusListener
  implements ActionListener, FocusListener {
  
  public ActionAndFocusListener(JComponent component) {
    
    // there doesn't seem to be a common interface that JComponents 
    // implement if they supply action events to external listeneres
    // I need to list each type directly as a result.
    
    if (component instanceof JComboBox) {
      ((JComboBox)component).addActionListener(this);
    }
    if (component instanceof JTextField) {
      ((JTextField)component).addActionListener(this);
    }
    component.addFocusListener(this);
  }

  public void actionPerformed(ActionEvent event) {
    process(event.getSource());
  }

  public void focusGained(FocusEvent event) {
    process(event.getSource());
  }

  public void focusLost(FocusEvent event) {
    process(event.getSource());
  }

  protected abstract void process(Object eventSource);
}
