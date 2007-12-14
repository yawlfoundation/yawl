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
import javax.swing.JFormattedTextField.AbstractFormatter;

public class JFormattedAlphaNumericField extends JFormattedSelectField {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JFormattedAlphaNumericField(int columns) {
    super();
    setFormatterFactory(new AlphaNumericFormatterFactory());
    setColumns(columns);
  }
  
  public void allowSpaces() {
    getAlphaNumericFormatter().allowSpaces();
  }
  
  public boolean spacesAllowed() {
    return getAlphaNumericFormatter().spacesAllowed();
  }
  
  public boolean xmlNameCharactersAllowed() {
    return getAlphaNumericFormatter().xmlNameCharactersAllowed();
  }
  
  /**
   * After invocation, this method will allow extra characters
   * that can go into valid XML names to be input by this 
   * widget.  Note that if {@link allowSpaces()} has also
   * been called, spaces will also be allowed, which is not
   * valid XML name convention. 
   */
  public void allowXMLNames() {
    getAlphaNumericFormatter().allowXMLNameCharacters();
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
