package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Michael Adams
 * @date 3/08/12
 */
class VariableRow {

    private Values startValues;
    private Values endValues;

    private String netElement;
    private boolean outputOnlyTask;                        // used only for tasks

    // new row added at runtime, so no starting values
    public VariableRow(int scope) {
        endValues = new Values();
        endValues.name = "";
        endValues.dataType = "string";
        endValues.scope = scope;
        endValues.value = "";
    }

    public VariableRow(YVariable variable, String netElement) {
        initialise(variable, false, netElement);
    }

    public VariableRow(YVariable variable, boolean isInputOutput, String netElement) {
        initialise(variable, isInputOutput, netElement);
    }


    public boolean isLocal() { return endValues.scope == YDataHandler.LOCAL; }

    public boolean isInput() { return endValues.scope == YDataHandler.INPUT; }

    public boolean isOutput() { return endValues.scope == YDataHandler.OUTPUT; }

    public boolean isInputOutput() { return endValues.scope == YDataHandler.INPUT_OUTPUT; }

    public boolean mayUpdateValue() { return isLocal() || isOutput(); }

    public boolean isModified() { return ! (isNew() || startValues.equals(endValues)); }

    public boolean isNew() { return startValues == null; }

    public boolean isOutputOnlyTask() { return outputOnlyTask; }

    public void setOutputOnlyTask(boolean isOutputOnly) { outputOnlyTask = isOutputOnly; }


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


    public int getUsage() { return endValues.scope; }

    public int getStartingUsage() { return startValues.scope; }

    public void setUsage(int scope) { endValues.scope = scope; }

    public boolean isUsageChange() { return startValues.scope != endValues.scope; }


    public String getMapping() { return endValues.mapping; }

    public String getStartingMapping() { return startValues.mapping; }

    public String getFullMapping() {
        if (StringUtil.isNullOrEmpty(getMapping())) return null;
        StringBuilder s = new StringBuilder();
        s.append('<').append(getName()).append(">{");
        s.append(getMapping());
        s.append("}</").append(getName()).append('>');
        return s.toString();
    }


    public void setMapping(String mapping) { endValues.mapping = mapping; }

    public void setMapping(String mapping, String netVarName) {
        setMapping(mapping);
        setNetVarForOutputMapping(netVarName);
    }

    public void setInitialMapping(String mapping) {
        startValues.mapping = mapping;
        endValues.mapping = mapping;
    }

    public boolean isMappingChange() {
        if (! (startValues.mapping == null || endValues.mapping == null)) {
            return ! startValues.mapping.equals(endValues.mapping);
        }
        else if (startValues.mapping == null) {
            return endValues.mapping != null;
        }
        else if (endValues.mapping == null) {
            return startValues.mapping != null;
        }
        return false;           // both null
    }

    public void setNetVarForOutputMapping(String netVarName) {
        endValues.netVarThisMapsTo = netVarName;
    }

    public String getNetVarForOutputMapping() { return endValues.netVarThisMapsTo; }


    public void setNetElement(String name) { netElement = name; }

    public String getNetElement() { return netElement; }


    public void updatesApplied() {
        startValues = endValues.copy();
    }


    public boolean equalsNameAndType(VariableRow other) {
        return getName().equals(other.getName()) &&
                getDataType().equals(other.getDataType());
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("; ").append(getDataType()).append("; ")
                .append(YDataHandler.getScopeName(getUsage()));
        return sb.toString();
    }


    private void initialise(YVariable variable, boolean isInputOutput, String netElement) {
        startValues = new Values();
        startValues.name = variable.getName();
        startValues.dataType = variable.getDataTypeName();
        startValues.value = null;
        if (isInputOutput) {
            startValues.scope = YDataHandler.INPUT_OUTPUT;
        }
        else if (variable instanceof YParameter) {
            if (((YParameter) variable).getParamType() == YParameter._INPUT_PARAM_TYPE) {
                startValues.scope = YDataHandler.INPUT;
            }
            else {
                startValues.scope = YDataHandler.OUTPUT;
                startValues.value = variable.getDefaultValue();
            }
        }
        else {
            startValues.scope = YDataHandler.LOCAL;
            startValues.value = variable.getInitialValue();
        }

        endValues = startValues.copy();
        setNetElement(netElement);
    }


    class Values {
        String name;
        String dataType;
        int scope;
        String value;
        String mapping;
        String netVarThisMapsTo;

        public boolean equals(Object o) {
            if (! (o instanceof Values)) return false;
            Values other = (Values) o;
            return name != null && name.equals(other.name) &&
                    dataType.equals(other.dataType) &&
                    scope == other.scope &&
                    ((value != null && value.equals(other.value)) ||
                            (value == null && other.value == null)) &&
                    ((mapping != null && mapping.equals(other.mapping)) ||
                            (mapping == null && other.mapping == null));
        }

        public int hashCode() {
            return name.hashCode();    // names are unique
        }

        public Values copy() {
            Values copy = new Values();
            copy.name = name;
            copy.dataType = dataType;
            copy.scope = scope;
            copy.value = value;
            copy.mapping = mapping;
            copy.netVarThisMapsTo = netVarThisMapsTo;
            return copy;
        }

    }
}
