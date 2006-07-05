/*
 * Created on 16/07/2004
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

public class ParameterLists implements Serializable, Cloneable {
  
  // TODO: Seal these so input params cannot be assigned to the output list and vica-versa.
  
  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();

  public ParameterLists() {
    reset();
  }

  public void setInputParameters(ParameterList inputParameters) {
    serializationProofAttributeMap.put("inputParameters",inputParameters);
  }
  
  public ParameterList getInputParameters() {
    return (ParameterList) serializationProofAttributeMap.get("inputParameters");
  }
  
  public void setOutputParameters(ParameterList outputParameters) {
    serializationProofAttributeMap.put("outputParameters",outputParameters);
  }

  public ParameterList getOutputParameters() {
    return (ParameterList) serializationProofAttributeMap.get("outputParameters");
  }
  
  public void remove(DataVariable variable) {
    getInputParameters().remove(variable);
    getOutputParameters().remove(variable);
  }
  
  public void reset() {
    setInputParameters(new ParameterList());
    setOutputParameters(new ParameterList());
  }
  
  public void changeDecompositionInQueries(String oldLabel, String newLabel) {
    getInputParameters().changeDecompositionInQueries(oldLabel, newLabel);
    getOutputParameters().changeDecompositionInQueries(oldLabel, newLabel);
  }
  
  public Object clone() {
    ParameterLists clone = new ParameterLists();
    clone.setInputParameters((ParameterList) getInputParameters().clone());
    clone.setOutputParameters((ParameterList) getOutputParameters().clone());
    return clone;
  }
}