package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.validation.Validator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;

import java.util.*;

/**
 * A class whose sole responsibility is to provide engine validation results of the
 * current specification.
 * This class should have no user-interface effects.
 * @author Lindsay Bradford
 *
 */

public class EngineSpecificationValidator {

    private static Hashtable<String, Boolean> _checkedDataTypes;
    private static List<String> _validDataTypeNames;


    public List<String> getValidationResults() {
        return getValidationResults(Validator.ALL_MESSAGES);
    }

    public List<String> getValidationResults(int msgType) {
        return getValidationResults(SpecificationModel.getInstance(), msgType);
    }

    public List<String> getValidationResults(SpecificationModel model) {
        return getValidationResults(model, Validator.ALL_MESSAGES);
    }

    public List<String> getValidationResults(SpecificationModel model, int msgType) {
        return getValidationResults(
                SpecificationWriter.populateSpecification(model), msgType);
    }

    public List<String> getValidationResults(YSpecification specification) {
        return getValidationResults(specification, Validator.ALL_MESSAGES);
    }

    public List<String> getValidationResults(YSpecification specification, int msgType) {
        return new Validator().validate(specification, msgType);
    }


    /**********************************************************************************/

    public List<String> checkUserDefinedDataTypes(SpecificationModel model) {
        _validDataTypeNames = SpecificationModel.getHandler().getDataHandler().getUserDefinedTypeNames();
        _checkedDataTypes = new Hashtable<String, Boolean>();
        List<String> problemList = new ArrayList<String>();
        Set<NetGraphModel> nets = model.getNets();
        for (NetGraphModel net : nets) {
            YNet netDecomp = (YNet) net.getDecomposition();
            Set<YVariable> variables = new HashSet<YVariable>();
            variables.addAll(netDecomp.getLocalVariables().values());
            variables.addAll(netDecomp.getInputParameters().values());
            variables.addAll(netDecomp.getOutputParameters().values());
            problemList.addAll(checkUserDefinedDataTypes(variables, net.getName(), null));
            NetElementSummary editorNetSummary = new NetElementSummary(net);
            Set tasks = editorNetSummary.getAtomicTasks();
            for (Object o : tasks) {
                YAWLAtomicTask task = (YAWLAtomicTask) o;
                YDecomposition decomp = task.getDecomposition();
                if (decomp != null) {
                    Set<YVariable> taskVars = new HashSet<YVariable>();
                    taskVars.addAll(decomp.getInputParameters().values());
                    taskVars.addAll(decomp.getOutputParameters().values());
                    problemList.addAll(checkUserDefinedDataTypes(taskVars,
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


    private List<String> checkUserDefinedDataTypes(Set<YVariable> varSet,
                                                          String netName, String taskName) {
        List<String> problemList = new ArrayList<String>();
        for (YVariable var : varSet) {
            String problem = checkUserDefinedDataType(var, netName, taskName);
            if (problem != null) problemList.add(problem);
        }
        return problemList;
    }


    private String checkUserDefinedDataType(YVariable var,
                                                   String netName, String taskName) {
        boolean valid;
        String datatype = var.getDataTypeName();
        if (! (XSDType.isBuiltInType(datatype) || YInternalType.isType(datatype))) {
            if (_checkedDataTypes.containsKey(datatype)) {
                valid = _checkedDataTypes.get(datatype);
            }
            else {
                valid = _validDataTypeNames.contains(datatype);
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

}
