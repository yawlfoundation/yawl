/*
 * Created on 22/09/2004
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

package au.edu.qut.yawl.editor.data;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;


public class Decomposition implements Serializable {
  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();
  
  public static final Decomposition DEFAULT = new Decomposition();
  
  public Decomposition() {
    setLabel("");
    setDescription("The default (empty) decomposition");
    setVariables(new DataVariableSet());
  }

  public void setLabel(String label) {
    serializationProofAttributeMap.put("label",label);
  }
  
  public String getLabel() {
    return (String) serializationProofAttributeMap.get("label");
  }

  public String getLabelAsElementName() {
    return XMLUtilities.toValidXMLName(getLabel());
  }
  
  public void setDescription(String description) {
    serializationProofAttributeMap.put("description",description);
  }
  
  public String getDescription() {
    return (String) serializationProofAttributeMap.get("description");
  }
  
  public DataVariableSet getVariables() {
    return (DataVariableSet) serializationProofAttributeMap.get("variables");
  }

  public void setVariables(DataVariableSet variables) {
    if (variables != null) {
      serializationProofAttributeMap.put("variables",variables);
      variables.setDecomposition(this);
    } 
  }
  
  public void addVariable(DataVariable variable) {
    getVariables().add(variable);
  }
  
  public void removeVariable(DataVariable variable) {
    getVariables().remove(variable);
  }
  
  public DataVariable getVariableWithName(String name) {
    return getVariables().getVariableWithName(name);
  }
  
  public boolean hasVariableEqualTo(DataVariable variable) {
    Iterator variableIterator = getVariables().getAllVariables().iterator();
    while(variableIterator.hasNext()) {
      DataVariable myVariable = (DataVariable) variableIterator.next();
      if (myVariable.equals(variable)) {
        return true;
      }
    }
    return false;
  }
  
  public DataVariable getVariableAt(int position) {
    return getVariables().getVariableAt(position);
  }
  
  public int getVariableCount() {
    return getVariables().size();
  }
  
  public boolean invokesWorklist() {
    return false;
  }
}
