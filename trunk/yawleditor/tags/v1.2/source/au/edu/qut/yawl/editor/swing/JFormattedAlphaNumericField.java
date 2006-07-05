/*
 * Created on 27/02/2002
 * Donated from Lindsay's private source library
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2004 Queensland University of Technology
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
import javax.swing.JFormattedTextField.AbstractFormatter;

public class JFormattedAlphaNumericField extends JFormattedSelectField {
  
  public JFormattedAlphaNumericField(int columns) {
    super();

    setColumns(columns);
    setFormatterFactory(new AlphaNumericFormatterFactory());
  }
  
  public void allowSpaces() {
    getAlphaNumericFormatter().allowSpaces();
  }
  
  public AlphaNumericFormatter getAlphaNumericFormatter() {
    return (AlphaNumericFormatter) getFormatter();
  }
}

class AlphaNumericFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
  private final AlphaNumericFormatter FORMATTER = new AlphaNumericFormatter();
  public AbstractFormatter getFormatter(JFormattedTextField field) {
    assert field instanceof JFormattedAlphaNumericField;
    return FORMATTER;
  }
}
