/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

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
