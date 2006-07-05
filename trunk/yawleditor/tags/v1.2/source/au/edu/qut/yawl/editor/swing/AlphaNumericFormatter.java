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

package au.edu.qut.yawl.editor.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DocumentFilter;

public class AlphaNumericFormatter extends DefaultFormatter {
  private final AlphaNumericFilter filter = new AlphaNumericFilter();

  public AlphaNumericFormatter() {
    super();
  }
  
  protected DocumentFilter getDocumentFilter() {
    return filter;
  }
  
  public void allowSpaces() {
    filter.allowSpaces();
  }
}

class AlphaNumericFilter extends DocumentFilter {
  private String validCharacters;
  
  public AlphaNumericFilter() {
    this.validCharacters = 
      "0123456789" +
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + 
      "abcdefghijklmnopqrstuvwxyz";
  }
  
  public AlphaNumericFilter(String validCharacters) {
    this.validCharacters = validCharacters;
  }
  
  public void setValidCharacters(String validCharacters) {
    this.validCharacters = validCharacters;
  }
  
  public void allowSpaces() {
    this.validCharacters = this.validCharacters + ' ';
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
      if (validCharacters.indexOf(text.charAt(i)) == -1) {
        return false;
      }
    }
    return true;
  }
}