/*
 * Created on 23/01/2005
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

package org.yawlfoundation.yawl.editor.ui.swing.data;


import org.yawlfoundation.yawl.editor.ui.swing.JFormattedAlphaNumericField;
import org.yawlfoundation.yawl.elements.YDecomposition;

public class XMLElementField extends JFormattedAlphaNumericField {
  private YDecomposition decomposition;
  
  public XMLElementField(int columns) {
    super(columns);
    getAlphaNumericFormatter().allowXMLNameCharacters();
  }
  
  public void setDecomposition(YDecomposition decomposition) {
    this.decomposition = decomposition;
    this.setText(decomposition.getID());
  }
  
  public YDecomposition getDecomposition() {
    return this.decomposition;
  }
}
