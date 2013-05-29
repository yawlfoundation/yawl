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

    private String decompositionID;
    private boolean outputOnlyTask;                        // used only for tasks

    private boolean multiInstance;

    // new row added at runtime, so no starting values
    public VariableRow(int scope) {
        endValues = new Values();
        endValues.name = "";
        endValues.dataType = "string";
        endValues.scope = scope;
        endValues.value = "";
    }

    public VariableRow(YVariable variable, String decompositionID) {
        initialise(variable, false, decompositionID);
    }

    public VariableRow(YVariable variable, boolean isInputOutput, String decompositionID) {
        initialise(variable, isInputOutput, decompositionID);
    }


    public boolean isLocal() { return endValues.scope == YDataHandler.LOCAL; }

    public boolean isInput() { return endValues.scope == YDataHandler.INPUT; }

    public boolean isOutput() { return endValues.scope == YDataHandler.OUTPUT; }

    public boolean isInputOutput() { return endValues.scope == YDataHandler.INPUT_OUTPUT; }

    public boolean mayUpdateValue() { return isLocal() || isOutput(); }

    public boolean isModified() { return ! (isNew() || startValues.equals(endValues)); }

    public boolean isNew() { return startValues == null; }

    public boolean isMultiInstance() { return multiInstance; }

    public boolean isOutputOnlyTask() { return outputOnlyTask; }


    public void setMultiInstance(boolean isMultiInstance) {
        multiInstance = isMultiInstance;
    }

    public void setOutputOnlyTask(boolean isOutputOnly) { outputOnlyTask = isOutputOnly; }


    public String getName() { return endValues.name; }

    public String getStartingName() { return startValues.name; }

    public void setName(String name) { endValues.name = name; }

    public boolean isNameChange() {
        return ! startValues.equals(startValues.name, endValues.name);
    }


    public String getDataType() { return endValues.dataType; }

    public String getStartingDataType() { return startValues.dataType; }

    public void setDataType(String dataType) { endValues.dataType = dataType; }

    public boolean isDataTypeChange() {
        return ! startValues.equals(startValues.dataType, endValues.dataType);
    }


    public String getValue() { return endValues.value; }

    public String getStartingValue() { return startValues.value; }

    public void setValue(String value) { endValues.value = value; }

    public boolean isValueChange() {
        return ! startValues.equals(startValues.value, endValues.value);
    }


    public int getUsage() { return endValues.scope; }

    public int getStartingUsage() { return startValues.scope; }

    public void setUsage(int scope) { endValues.scope = scope; }

    public boolean isUsageChange() { return startValues.scope != endValues.scope; }


    public String getMapping() { return endValues.mapping; }

    public String getStartingMapping() { return startValues.mapping; }

    public String getFullMapping() {
        if (isInput()) {
            if (isMultiInstance()) return getMapping();
            return getWrappedMapping(getName(), getMapping());
        }
        return getWrappedMapping(getNetVarForOutputMapping(), getMapping());
    }


    public void setMapping(String mapping) { endValues.mapping = mapping; }

    public void setMapping(String mapping, String netVarName) {
        setMapping(mapping);
        setNetVarForOutputMapping(netVarName);
    }

    public void initMapping(String mapping) {
        startValues.mapping = mapping;
        endValues.mapping = mapping;
    }

    public boolean isMappingChange() {
        return ! startValues.equals(startValues.mapping, endValues.mapping);
    }


    public void initNetVarForOutputMapping(String netVarName) {
        startValues.netVarThisMapsTo = netVarName;
        endValues.netVarThisMapsTo = netVarName;
    }

    public void setNetVarForOutputMapping(String netVarName) {
        endValues.netVarThisMapsTo = netVarName;
    }

    public String getNetVarForOutputMapping() { return endValues.netVarThisMapsTo; }


    public String getMIQuery() {
        return endValues.miQuery;
    }

    public String getFullMIQuery() {
        if (isInput()) {
            return getMIQuery();
        }
        return getWrappedMapping(getNetVarForOutputMapping(), getMapping());
    }


    public void setMIQuery(String query) { endValues.miQuery = query; }

    public void initMIQuery(String query) {
        startValues.miQuery = query;
        endValues.miQuery = query;
    }

    public boolean isMIQueryChange() {
        return ! startValues.equals(startValues.miQuery, endValues.miQuery);
    }


    public void setDecompositionID(String name) { decompositionID = name; }

    public String getDecompositionID() { return decompositionID; }


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
        setDecompositionID(netElement);
    }


    private String getWrappedMapping(String tagName, String mapping) {
        if (StringUtil.isNullOrEmpty(mapping)) return null;
        StringBuilder s = new StringBuilder();
        s.append('<').append(tagName).append(">{");
        s.append(mapping);
        s.append("}</").append(tagName).append('>');
        return s.toString();
    }



    class Values {
        String name;
        String dataType;
        int scope;
        String value;
        String mapping;
        String netVarThisMapsTo;       // output params only
        String miQuery;               // mi params only

        public Values copy() {
            Values copy = new Values();
            copy.name = name;
            copy.dataType = dataType;
            copy.scope = scope;
            copy.value = value;
            copy.mapping = mapping;
            copy.netVarThisMapsTo = netVarThisMapsTo;
            copy.miQuery = miQuery;
            return copy;
        }

        public boolean equals(Object o) {
            if (! (o instanceof Values)) return false;
            Values other = (Values) o;
            return scope == other.scope && equals(name, other.name) &&
                    equals(dataType, other.dataType) && equals(value, other.value) &&
                    equals(mapping, other.mapping) &&
                    equals(netVarThisMapsTo, other.netVarThisMapsTo) &&
                    equals(miQuery, other.miQuery);
        }

        public int hashCode() {
            return name.hashCode();    // names are unique
        }

        public boolean equals(String s1, String s2) {
           return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
       }

    }

}
