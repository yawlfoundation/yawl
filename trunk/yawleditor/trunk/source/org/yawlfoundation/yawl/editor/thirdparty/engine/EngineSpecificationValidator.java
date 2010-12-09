package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;

/**
 * A class whose sole responsibility is to provide engine validation results of the current specification.
 * This class should have no user-interface effects.
 * @author Lindsay Bradford
 *
 */

public class EngineSpecificationValidator {

  public static String NO_PROBLEMS_MESSAGE = "No design-time engine validation problems were found in this specification.";
  
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
    return createProblemListFrom(specification.verify());
  }
  
  private static List<String> createProblemListFrom(
                              List<YVerificationMessage> verificationList) {
    LinkedList<String> problemList = new LinkedList<String>();

      for (YVerificationMessage message : verificationList) {
          String messageString = message.getMessage();

          if (messageString.indexOf("composite task may not decompose to other than a net") != -1) {
              continue;
          }
          if (messageString.indexOf("is not registered with engine.") != -1) {
              // We have no running engine when validating, so this is not valid.
              continue;
          }

          // External db validation needs a running engine, so this is not valid.
          if (messageString.indexOf("could not be successfully parsed. External DB") != -1) {
              continue;
          }

          messageString = messageString.replaceAll("postset size", "outgoing flow number");
          messageString = messageString.replaceAll("preset size", "incoming flow number");
          messageString = messageString.replaceAll("Check the empty tasks linking from i to o.",
                  "Should all atomic tasks in the net have no decomposition?");
          messageString = messageString.replaceAll("from i to o", "between the input and output conditions");
          messageString = messageString.replaceAll("InputCondition", "Input Condition");
          messageString = messageString.replaceAll("OutputCondition", "Output Condition");

          messageString = messageString.replaceAll("ExternalCondition", "Condition");
          messageString = messageString.replaceAll("AtomicTask", "Atomic Task");
          messageString = messageString.replaceAll("CompositeTask", "Composite Task");
          messageString = messageString.replaceAll("The net \\(Net:", "The net (");
          messageString = messageString.replaceAll("composite task must contain a net", "must unfold to some net");

          problemList.add(messageString);
      }
    if (problemList.size() == 0) {
      problemList.add(NO_PROBLEMS_MESSAGE);
    }
    return problemList;
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
             datatype.equals("YTimerType") || datatype.equals("YStringListType") ||
             isXSBuiltInSimpleType(datatype))) {
          if (_checkedDataTypes.containsKey(datatype)) {
              valid = _checkedDataTypes.get(datatype);
          }
          else {
              valid = (null != YAWLEngineProxy.getInstance()
                                   .createSchemaForVariable(var.getName(), datatype));
              _checkedDataTypes.put(datatype, valid) ;
          }

          // schema will be null if datatype is invalid
          if (! valid) {
              String taskDef = taskName == null ? "" : String.format("Task '%s', ", taskName);
              return String.format(
                 "Invalid or missing datatype definition '%s' in Net '%s', %sVariable '%s'.",
                                     datatype, netName, taskDef, var.getName());
          }
     }
     return null;
  }

  private static boolean isXSBuiltInSimpleType(String datatype) {
      if (datatype.equals("anyType")) return true;
      return false;
  }

}
