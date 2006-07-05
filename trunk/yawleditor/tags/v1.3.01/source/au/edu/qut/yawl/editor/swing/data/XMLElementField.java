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

package au.edu.qut.yawl.editor.swing.data;


import au.edu.qut.yawl.editor.swing.JFormattedAlphaNumericField;
import au.edu.qut.yawl.editor.data.Decomposition;

public class XMLElementField extends JFormattedAlphaNumericField {
  
  private Decomposition decomposition;
  
  public XMLElementField(int columns) {
    super(columns);
    getAlphaNumericFormatter().allowXMLNameCharacters();
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition;
    this.setText(decomposition.getLabel());
  }
  
  public Decomposition getDecomposition() {
    return this.decomposition;
  }
}
