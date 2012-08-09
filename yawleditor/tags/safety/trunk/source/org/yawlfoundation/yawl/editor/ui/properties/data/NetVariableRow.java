package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;

/**
 * @author Michael Adams
 * @date 3/08/12
 */
class NetVariableRow {

    public enum Usage { Input, Output, InputOutput, Local }

    private Values startValues;
    private Values endValues;

    // new row added at runtime, so no starting values
    public NetVariableRow() {
        endValues = new Values();
        endValues.name = "";
        endValues.dataType = "string";
        endValues.usage = Usage.Local;
        endValues.value = "";
    }

    public NetVariableRow(YVariable variable) {
        initialise(variable, false);
    }

    public NetVariableRow(YVariable variable, boolean isInputOutput) {
        initialise(variable, isInputOutput);
    }


    public boolean isLocal() { return endValues.usage == Usage.Local; }

    public boolean isInput() { return endValues.usage == Usage.Input; }

    public boolean isOutput() { return endValues.usage == Usage.Output; }

    public boolean isInputOutput() { return endValues.usage == Usage.InputOutput; }

    public boolean mayUpdateValue() { return isLocal() || isOutput(); }

    public boolean isModified() { return ! startValues.equals(endValues); }

    public boolean isNew() { return startValues == null; }


    public String getName() { return endValues.name; }

    public String getStartingName() { return startValues.name; }

    public void setName(String name) { endValues.name = name; }

    public boolean isNameChange() { return ! startValues.name.equals(endValues.name); }


    public String getDataType() { return endValues.dataType; }

    public String getStartingDataType() { return startValues.dataType; }

    public void setDataType(String dataType) { endValues.dataType = dataType; }

    public boolean isDataTypeChange() {
        return ! startValues.dataType.equals(endValues.dataType);
    }


    public String getValue() { return endValues.value; }

    public String getStartingValue() { return startValues.value; }

    public void setValue(String value) { endValues.value = value; }

    public boolean isValueChange() {
        return (startValues.value == null && endValues.value != null) ||
               (startValues.value != null && endValues.value == null) ||
                ! startValues.value.equals(endValues.value);
    }


    public Usage getUsage() { return endValues.usage; }

    public Usage getStartingUsage() { return startValues.usage; }

    public void setUsage(Usage usage) { endValues.usage = usage; }

    public boolean isUsageChange() { return startValues.usage != endValues.usage; }


    private void initialise(YVariable variable, boolean isInputOutput) {
        startValues = new Values();
        startValues.name = variable.getName();
        startValues.dataType = variable.getDataTypeName();
        startValues.value = null;
        if (isInputOutput) {
            startValues.usage = Usage.InputOutput;
        }
        else if (variable instanceof YParameter) {
            if (((YParameter) variable).getParamType() == YParameter._INPUT_PARAM_TYPE) {
                startValues.usage = Usage.Input;
            }
            else {
                startValues.usage = Usage.Output;
                startValues.value = variable.getDefaultValue();
            }
        }
        else {
            startValues.usage = Usage.Local;
            startValues.value = variable.getInitialValue();
        }

        endValues = new Values();
        endValues.name = startValues.name;
        endValues.dataType = startValues.dataType;
        endValues.usage = startValues.usage;
        endValues.value = startValues.value;
    }


    class Values {
        String name;
        String dataType;
        Usage usage;
        String value;

        boolean equals(Values other) {
            return name != null && name.equals(other.name) &&
                    dataType.equals(other.dataType) &&
                    usage == other.usage &&
                    ((value != null && value.equals(other.value)) ||
                            (value == null && other.value == null));
        }
    }
}
