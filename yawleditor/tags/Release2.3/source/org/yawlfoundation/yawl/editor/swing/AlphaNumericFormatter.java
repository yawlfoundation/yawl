/*
 * Created on 24/01/2005
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2004-5 Queensland University of Technology
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

package org.yawlfoundation.yawl.editor.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

public class AlphaNumericFormatter extends DefaultFormatter {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final AlphaNumericFilter filter = new AlphaNumericFilter();
  
  private boolean spacesAllowed = false;
  private boolean xmlNameCharactersAllowed = false;  

  public AlphaNumericFormatter() {
    super();
  }
  
  protected DocumentFilter getDocumentFilter() {
    return filter;
  }
  
  public void allowSpaces() {
    this.spacesAllowed = true;
    filter.allowSpaces();
  }
  
  public boolean spacesAllowed() {
    return this.spacesAllowed;
  }
  
  public void allowXMLNameCharacters() {
    this.xmlNameCharactersAllowed = true;
    filter.allowXMLNameCharacters();
  }
  
  public boolean xmlNameCharactersAllowed() {
    return this.xmlNameCharactersAllowed;
  }
}

class AlphaNumericFilter extends DocumentFilter {
  
  private boolean allowingSpaces = false;
  private boolean allowingXMLNameCharacters = false;
  
  public AlphaNumericFilter() {}
  
  public void allowSpaces() {
    this.allowingSpaces = true;
  }
  
  public void allowXMLNameCharacters() {
    this.allowingXMLNameCharacters = true;
  }
  
  public void replace(DocumentFilter.FilterBypass bypass,
                      int offset,
                      int length,
                      String text,
                      AttributeSet attributes) throws BadLocationException {
    if (isValidText(text))
      super.replace(bypass,offset,length,text,attributes);
  }

  protected boolean isValidText(String text) {
    for (int i = 0; i < text.length(); i++) {
      
      if (!Character.isUpperCase(text.charAt(i)) &&
          !Character.isLowerCase(text.charAt(i)) &&
          !Character.isDigit(text.charAt(i))) {
        
        if (text.charAt(i) == ' ') {
          if (!allowingSpaces) {
            return false;
          } 
        } else if (text.charAt(i) == '_' || text.charAt(i) == '-' ||
                   text.charAt(i) == '.') {
          if (!allowingXMLNameCharacters) {
            return false;
          }
        } else {  // not uppercase, lowercase, digit, space or XML Name element.
          return false;
        }
      }
    } 
    return true;
  }
}