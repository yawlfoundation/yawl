package au.edu.qut.yawl.editor.data;

import java.util.LinkedList;
import java.util.List;

public class DataVariableUtilities {

  /**
   * Returns only those variables from the supplied list that matches the given 
   * data type.
   * @param variables
   * @param dataType
   * @return
   */
  
  public static List<DataVariable> getVariablesOfType(List<DataVariable> variables, String dataType) {
    LinkedList<DataVariable> filteredList = new LinkedList<DataVariable>();
    
    for(DataVariable variable : variables) {
      if (variable.getDataType().equals(dataType)) {
        filteredList.add(variable);
      }
    }
    
    return filteredList;
  }
}
