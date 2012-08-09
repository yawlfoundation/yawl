package org.yawlfoundation.yawl.editor.ui.data;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.ArrayList;
import java.util.Collections;
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

    public static List<DataVariable> getVariablesOfType(DataVariableSet variables,
                                                        String dataType) {
        return getVariablesOfType(variables.getVariableSet(), dataType);
    }

    public static List<DataVariable> convertYParameters(List<YParameter> params) {
        if ((params == null) || params.isEmpty()) {
            return Collections.emptyList();      // no more to do
        }

        List<DataVariable> varList= mergeParameters(params);

        int index = 0;
        for (DataVariable var : varList) {
            var.setIndex(index++);
            var.setUserDefined(false);
        }
        return varList;
    }


    private static List<DataVariable> mergeParameters(List<YParameter> params) {
        List<DataVariable> varList = new ArrayList<DataVariable>();

        // map params to data variables
        for (YParameter param : params) {
            DataVariable editorVariable = new DataVariable();
            editorVariable.setDataType(param.getDataTypeName());
            editorVariable.setName(param.getName());
            editorVariable.setInitialValue(param.getInitialValue());
            editorVariable.setUserDefined(false);
            if (param.isInput()) {
                editorVariable.setUsage(DataVariable.USAGE_INPUT_ONLY);
            }
            if (param.isOutput()) {
                editorVariable.setUsage(DataVariable.USAGE_OUTPUT_ONLY);
            }
            if (param.isOptional()) {
                editorVariable.setAttribute("optional", "true");
            }

            varList.add(editorVariable);
        }

        // merge matching input & output vars into one I&O var
        List<DataVariable> inputList = new ArrayList<DataVariable>();
        List<DataVariable> outputList = new ArrayList<DataVariable>();
        for (DataVariable variable : varList) {
            if (variable.getUsage() == DataVariable.USAGE_INPUT_ONLY) {
                inputList.add(variable);
            }
            else {
                outputList.add(variable);
            }
        }
        for (DataVariable inputVar : inputList) {
            for (DataVariable outputVar : outputList) {
                if (inputVar.equalsIgnoreUsage(outputVar)) {
                    inputVar.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
                    varList.remove(outputVar);
                }
            }
        }
        return varList;
    }


}
