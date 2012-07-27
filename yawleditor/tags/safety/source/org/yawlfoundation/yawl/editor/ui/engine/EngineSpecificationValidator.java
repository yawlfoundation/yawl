package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.validation.Validator;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * A class whose sole responsibility is to provide engine validation results of the current specification.
 * This class should have no user-interface effects.
 * @author Lindsay Bradford
 *
 */

public class EngineSpecificationValidator {

  public static List getValidationResults() {
    return getValidationResults(SpecificationModel.getInstance());
  }

  public static List getValidationResults(SpecificationModel specification) {
    return getValidationResults(
        EngineSpecificationExporter.getEngineSpecAsEngineObjects(
            specification
        )
    );
  }
  
  public static List<String> getValidationResults(YSpecification specification) {
      return new Validator().validate(specification);
  }
  

  /**********************************************************************************/

  private static Hashtable<String, Boolean> _checkedDataTypes;

  public static List<String> checkUserDefinedDataTypes(SpecificationModel editorSpec) {
      _checkedDataTypes = new Hashtable<String, Boolean>();
      List<String> problemList = new ArrayList<String>();
      Set<NetGraphModel> nets = editorSpec.getNets();
      for (NetGraphModel net : nets) {
          DataVariableSet varSet = net.getVariableSet();
          problemList.addAll(checkUserDefinedDataTypes(varSet, net.getName(), null));
          NetElementSummary editorNetSummary = new NetElementSummary(net);
          Set tasks = editorNetSummary.getAtomicTasks();
          for (Object o : tasks) {
              YAWLAtomicTask task = (YAWLAtomicTask) o;
              Decomposition decomp = task.getWSDecomposition();
              if (decomp != null) {
                  problemList.addAll(checkUserDefinedDataTypes(decomp.getVariables(),
                                                    net.getName(), task.getLabel()));
              }
          }
          // todo : check flow predicates
//          Set flows = editorNetSummary.getFlows();
//          for (Object o : flows) {
//              String result = checkUserDefinedDataType((YAWLFlowRelation) o);
//              if (result != null) problemList.add(result);
//          }
      }
      return problemList;
  }


  private static List<String> checkUserDefinedDataTypes(DataVariableSet varSet,
                                                      String netName, String taskName) {
      List<String> problemList = new ArrayList<String>();
      for (DataVariable var : varSet.getVariableSet()) {
          String problem = checkUserDefinedDataType(var, netName, taskName);
          if (problem != null) problemList.add(problem);
      }
      return problemList;
  }

    
  private static String checkUserDefinedDataType(DataVariable var,
                                                 String netName, String taskName) {
      boolean valid;
      String datatype = var.getDataType();
      if (! (DataVariable.isBaseDataType(datatype) ||
             datatype.equals("YTimerType") ||
             datatype.equals("YStringListType") ||
             datatype.equals("YDocumentType") ||
             isXSBuiltInSimpleType(datatype))) {
          if (_checkedDataTypes.containsKey(datatype)) {
              valid = _checkedDataTypes.get(datatype);
          }
          else {
              valid = SpecificationModel.getInstance().isDefinedTypeName(datatype);
              _checkedDataTypes.put(datatype, valid) ;
          }

          if (! valid) {
              String taskDef = taskName == null ? "" : String.format("Task '%s', ", taskName);
              return String.format(
                 "Invalid or missing datatype definition '%s' in Net '%s', %s Variable '%s'.",
                                     datatype, netName, taskDef, var.getName());
          }
     }
     return null;
  }

  private static boolean isXSBuiltInSimpleType(String datatype) {
      return datatype.equals("anyType");
  }

}
