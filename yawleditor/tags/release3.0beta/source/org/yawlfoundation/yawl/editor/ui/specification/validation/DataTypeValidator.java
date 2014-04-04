package org.yawlfoundation.yawl.editor.ui.specification.validation;

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;

import java.util.*;

/**
 * @author Michael Adams
 * @date 12/10/13
 */

public class DataTypeValidator {

    private static Map<String, Boolean> _checkedDataTypes;
    private static List<String> _validDataTypeNames;

    public DataTypeValidator() { }


    public List<String> validate() {
        YSpecificationHandler handler = SpecificationModel.getHandler();
        _validDataTypeNames = handler.getDataHandler().getUserDefinedTypeNames();
        _checkedDataTypes = new HashMap<String, Boolean>();
        List<String> problemList = new ArrayList<String>();
        for (YNet net : handler.getControlFlowHandler().getNets()) {
            checkUserDefinedDataTypes(problemList, gatherNetVariables(net),
                    net.getName(), null);

            for (YTask task : net.getNetTasks()) {
                YDecomposition decomposition = task.getDecompositionPrototype();
                if (decomposition != null) {
                    checkUserDefinedDataTypes(problemList,
                            gatherDecompositionVariables(decomposition),
                            net.getName(), task.getID());
                }
            }
        }
        return problemList;
    }


    private Set<YVariable> gatherNetVariables(YNet net) {
        Set<YVariable> variables = new HashSet<YVariable>();
        variables.addAll(net.getLocalVariables().values());
        variables.addAll(gatherDecompositionVariables(net));
        return variables;
    }


    private Set<YVariable> gatherDecompositionVariables(YDecomposition decomposition) {
        Set<YVariable> variables = new HashSet<YVariable>();
        variables.addAll(decomposition.getInputParameters().values());
        variables.addAll(decomposition.getOutputParameters().values());
        return variables;
    }


    private void checkUserDefinedDataTypes(List<String> problemList,
                                           Set<YVariable> varSet,
                                           String netName, String taskID) {
        for (YVariable var : varSet) {
            String problem = checkUserDefinedDataType(var, netName, taskID);
            if (problem != null) problemList.add(problem);
        }
    }


    private String checkUserDefinedDataType(YVariable var, String netName, String taskID) {
        boolean valid;
        String dataType = var.getDataTypeName();
        if (! (XSDType.isBuiltInType(dataType) || YInternalType.isType(dataType))) {
            if (_checkedDataTypes.containsKey(dataType)) {
                valid = _checkedDataTypes.get(dataType);
            }
            else {
                valid = _validDataTypeNames.contains(dataType);
                _checkedDataTypes.put(dataType, valid) ;
            }

            if (! valid) {
                String taskDef = taskID == null ? "" : String.format("Task '%s', ", taskID);
                return String.format("Invalid or missing data type definition '%s' " +
                                "in Net '%s', %s Variable '%s'.",
                        dataType, netName, taskDef, var.getName());
            }
        }
        return null;
    }
}
