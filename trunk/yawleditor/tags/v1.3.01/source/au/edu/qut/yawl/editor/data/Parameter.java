/*
 * Created on 13/08/2004
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

import java.io.Serializable;
import java.util.HashMap;

public class Parameter implements Serializable {
  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();

  private transient ParameterList list;
  
  public Parameter() {
    setVariable(null);
    setQuery("");
  }
  
  public Parameter(DataVariable variable, String query) {
    setVariable(variable);
    setQuery(query);
  }
  
  public void setVariable(DataVariable variable) {
    serializationProofAttributeMap.put("variable",variable);
  }

  public DataVariable getVariable() {
    return (DataVariable) serializationProofAttributeMap.get("variable");
  }
  
  public void setQuery(String query) {
    serializationProofAttributeMap.put("query",query);
  }

  public String getQuery() {
    return (String) serializationProofAttributeMap.get("query");
  }
  
  public String getVariableName() {
    return getVariable().getName();
  }
  
  public void setList(ParameterList list) {
    this.list = list;
  }
  
  public ParameterList getList() {
    return this.list;
  }
}
