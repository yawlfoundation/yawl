/*
 * Created on 16/07/2004
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

import java.io.Serializable;

public class ParameterLists implements Serializable, Cloneable {
  
  // TODO: Seal these so input params cannot be assigned to the output list and vica-versa.
  
  private ParameterList inputParameters;
  private ParameterList outputParameters;
  
  public ParameterLists() {
    reset();
  }
  
  public ParameterList getInputParameters() {
    return this.inputParameters;
  }
  
  public ParameterList getOutputParameters() {
    return this.outputParameters;
  }
  
  public void setInputParameters(ParameterList inputParameters) {
    this.inputParameters = inputParameters;
  }

  public void setOutputParameters(ParameterList outputParameters) {
    this.outputParameters = outputParameters;
  }
  
  public void remove(DataVariable variable) {
    this.inputParameters.remove(variable);
    this.outputParameters.remove(variable);
  }
  
  public void reset() {
    inputParameters  = new ParameterList();
    outputParameters = new ParameterList();
  }
  
  public void changeDecompositionInQueries(String oldLabel, String newLabel) {
    inputParameters.changeDecompositionInQueries(oldLabel, newLabel);
    outputParameters.changeDecompositionInQueries(oldLabel, newLabel);
  }
  
  public Object clone() {
    ParameterLists clone = new ParameterLists();
    clone.setInputParameters((ParameterList) inputParameters.clone());
    clone.setOutputParameters((ParameterList) outputParameters.clone());
    return clone;
  }
}