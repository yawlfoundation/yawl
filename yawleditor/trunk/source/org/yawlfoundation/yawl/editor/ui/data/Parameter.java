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

package org.yawlfoundation.yawl.editor.ui.data;

public class Parameter {

  private ParameterList _list;
    private DataVariable _variable;
    private String _query;
  
  public Parameter() {
    setVariable(null);
    setQuery("");
  }
  
  public Parameter(DataVariable variable, String query) {
    setVariable(variable);
    setQuery(query);
  }
  
  public void setVariable(DataVariable variable) { _variable = variable; }

  public DataVariable getVariable() { return _variable; }


  public void setQuery(String query) { _query = query; }

  public String getQuery() { return _query; }


  public String getVariableName() { return getVariable().getName(); }


  public void setList(ParameterList list) { _list = list; }
  
  public ParameterList getList() { return _list; }
}
