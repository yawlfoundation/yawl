/*
 * Created on 20/09/2004
 * YAWLEditor v1.01 
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
 */

package au.edu.qut.yawl.editor.swing.data;

import java.util.Iterator;

import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class DecompositionComboBox extends JComboBox {
  
  public DecompositionComboBox() {
    super();
    refresh();
  }
  
  public void refresh() {
    removeAllItems();
    addDecompositions();
  }
  
  private void addDecompositions() {
    Iterator typeIterator = 
      SpecificationModel.getInstance().getDecompositions().iterator();
    while (typeIterator.hasNext()) {
      Decomposition decomposition = (Decomposition) typeIterator.next();
      addItem(decomposition.getLabel());
    }
  }
}
