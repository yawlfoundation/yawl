/*
 * Created on 13/08/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

public class Parameter implements Serializable {
  private DataVariable variable = null;
  private String query = "";
  
  private transient ParameterList list;
  
  public Parameter() {
    variable = null;
    query    = "";
  }
  
  public Parameter(DataVariable variable, String query) {
    setVariable(variable);
    setQuery(query);
  }
  
  public void setVariable(DataVariable variable) {
    this.variable = variable;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public DataVariable getVariable() {
    return this.variable;
  }
  
  public String getVariableName() {
    return this.variable.getName();
  }
  
  public String getQuery() {
    return this.query;
  }
  
  public String getEngineReadyQuery() {
    return XMLUtilities.quoteSpecialCharacters(getQuery());
  }
  
  public void setList(ParameterList list) {
    this.list = list;
  }
  
  public ParameterList getList() {
    return this.list;
  }
}
