/*
 * Created on 17/03/2006
 * YAWLEditor v1.4.1
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.swing;

import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

/**
 * This class is an extension of {@link JFormattedSelectField} that allows
 * users to only enter characters that are not XML special characters.
 * 
 * @author Lindsay Bradford
 */

public class JFormattedSafeXMLCharacterField extends JFormattedSelectField {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JFormattedSafeXMLCharacterField(int columns) {
    super();
    setFormatterFactory(new SafeXMLFormatterFactory());
    setColumns(columns);
    setMinimumSize(getPreferredSize());  
  }
}

class SafeXMLFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
  private final SafeXMLFormatter FORMATTER = new SafeXMLFormatter();
  public AbstractFormatter getFormatter(JFormattedTextField field) {
    assert field instanceof JFormattedSafeXMLCharacterField;
    return FORMATTER;
  }
}

class SafeXMLFormatter extends DefaultFormatter {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final SafeXMLCharacterFilter filter = new SafeXMLCharacterFilter();
  

  public SafeXMLFormatter() {
    super();
  }
  
  protected DocumentFilter getDocumentFilter() {
    return filter;
  }
}

class SafeXMLCharacterFilter extends DocumentFilter {
  
  public SafeXMLCharacterFilter() {}
  
  public void replace(DocumentFilter.FilterBypass bypass,
                      int offset,
                      int length,
                      String text,
                      AttributeSet attributes) throws BadLocationException {
    if (isValidText(text))
      super.replace(bypass,offset,length,text,attributes);
  }

  protected boolean isValidText(String text) {
    if (text == null) {
      return true;
    }
    for (int i = 0; i < text.length(); i++) {
      
      if (XMLUtilities.isSpecialXMLCharacter(text.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
