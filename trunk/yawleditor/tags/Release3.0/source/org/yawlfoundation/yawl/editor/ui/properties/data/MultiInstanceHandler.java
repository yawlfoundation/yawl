package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YTask;

/**
 * @author Michael Adams
 * @date 28/11/2013
 */
public class MultiInstanceHandler {

    private final YTask _task;
    private final OutputBindings _outputBindings;
    private String _dataItemName;
    private String _netVarTarget;
    private YMultiInstanceAttributes _attributes;                  // current updates


    protected MultiInstanceHandler(YTask task, OutputBindings bindings) {
        _task = task;
        _outputBindings = bindings;
        _attributes = new YMultiInstanceAttributes(task);
    }


    protected String getFormalInputParam() {
        String param = _attributes.getMIFormalInputParam();
        return param != null ? param : getTaskAttributes().getMIFormalInputParam();
    }


    protected void setFormalInputParam(String param) {
        _attributes.setMIFormalInputParam(param);
    }


    public String getSplitQuery() {
        String query = _attributes.getMISplittingQuery();
        return query != null ? query : getTaskAttributes().getMISplittingQuery();
    }

    public void setSplitQuery(String query) {
        _attributes.setUniqueInputMISplittingQuery(query);
    }


    public String getJoinQuery() {
        String query = _attributes.getMIJoiningQuery();
        return query != null ? query : getTaskAttributes().getMIJoiningQuery();
    }

    public String getJoinQueryUnwrapped() {
        return DataUtils.unwrapBinding(getJoinQuery());
    }

    public void setJoinQuery(String query) {
        _attributes.setUniqueOutputMIJoiningQuery(query);
    }

    public void setJoinQueryUnwrapped(String query) {
        setJoinQuery(DataUtils.wrapBinding(getOutputTarget(), query));
    }

    public void setOutputTarget(String netVarName) {
        _netVarTarget = netVarName;
    }

    public void renameItem(VariableRow row, String newName) {
        setFormalInputParam(newName);
        setSplitQuery(assembleSplitQuery(row, newName));
    }

    public String getOutputTarget() {
        if (_netVarTarget != null) return _netVarTarget;
        String target = _attributes.getMIOutputAssignmentVar();
        return target != null ? target : getTaskAttributes().getMIOutputAssignmentVar();
    }

    protected String getOutputQuery() {
        String binding = _attributes.getMIFormalOutputQuery();
        return binding != null ? binding : getTaskAttributes().getMIFormalOutputQuery();
    }


    protected boolean outputQueryBindsFrom(String taskVarName) {
        String sep = "/";
        String xpath = sep + _task.getDecompositionPrototype().getID() +
                sep + taskVarName + sep;
        String binding = getOutputQuery();
        return binding != null && DataUtils.unwrapBinding(binding).startsWith(xpath);
    }


    protected boolean setupMultiInstanceRow(VariableRow row,
                                            VariableTable netTable,
                                            VariableTable taskTable) {
        clear();            // reset params
        checkValidRow(row);

        if (row.isMultiInstance()) {                // toggle multi instance
            VariableRow netVarRow = getReferencedNetVarRow(netTable, row.getName());
            unsetRowProperties(taskTable, row, netVarRow);
            return true;
        }

        String inputBinding = row.getMapping();
        String[] itemNameAndType = getItemNameAndType(row.getDataType());
        _dataItemName = itemNameAndType[0];
        _netVarTarget = _outputBindings.getTarget(row.getName());
        row.setMapping(getAdjustedInputBinding(inputBinding));

        String actualRowName = row.getName();
        setRowProperties(taskTable, row, itemNameAndType[1]);
        setAdjustedOutputBinding(actualRowName, row);
        setFormalInputParam(row.getName());
        setSplitQuery(assembleSplitQuery(row));
        setJoinQuery(assembleJoinQuery());

        return true;
    }


    protected void commit() {
        _task.setMultiInstanceInputDataMappings(getFormalInputParam(), getSplitQuery());
        _task.setMultiInstanceOutputDataMappings(getOutputBinding(), getJoinQuery());
        clear();
    }


    protected void clear() {
        _attributes = new YMultiInstanceAttributes(_task);
        _dataItemName = null;
        _netVarTarget = null;
    }


    private void setRowProperties(VariableTable table, VariableRow row, String dataType) {
        row.setName(getUniqueRowName(table, row.getName() + "_Item"));
        row.setDataType(unprefix(dataType));
        table.setMultiInstanceRow(row);
    }


    private void unsetRowProperties(VariableTable table, VariableRow row,
                                    VariableRow netVarRow) {
        row.setName(row.getName().substring(0, row.getName().lastIndexOf("_Item")));
        row.setDataType(netVarRow.getDataType());
        row.setMapping(generateBinding(netVarRow));

        if (row.isInputOutput()) {
            _outputBindings.removeBindingForTarget(_dataItemName);
            _outputBindings.setBinding(netVarRow.getName(), generateBinding(row));
        }
        table.setMultiInstanceRow(null);
        clear();
    }


    private String generateBinding(VariableRow row) {
        StringBuilder s = new StringBuilder();
        s.append('/').append(row.getElementID());
        s.append('/').append(row.getName()).append("/*");
        return s.toString();
    }


    private String getOutputBinding() {
        return _outputBindings.getBinding(getOutputTarget(), false);
    }

    private YMultiInstanceAttributes getTaskAttributes() {
        return _task.getMultiInstanceAttributes();
    }


    private void checkValidRow(VariableRow row) {
        if (row == null || row.isOutput()) {
            throw new IllegalArgumentException("Row is null or not input");
        }
    }


    private String[] getItemNameAndType(String complexType) {
        String[] itemNameAndType;
        try {
            itemNameAndType = SpecificationModel.getHandler().getDataHandler()
                    .getMultiInstanceItemNameAndType(complexType);
            if (itemNameAndType == null) {
                throw new IllegalArgumentException("Invalid MI data type");
            }
        }
        catch (YDataHandlerException ydhe) {
            throw new IllegalArgumentException(ydhe.getMessage());
        }
        return itemNameAndType;
    }



    private String getUniqueRowName(VariableTable table, String rowName) {
        String startingName = rowName;
        int suffix = 1;
        while (! isUniqueRowName(table, rowName)) {
            rowName = startingName + suffix++;
        }
        return rowName;
    }


    private boolean isUniqueRowName(VariableTable table, String rowName) {
        for (VariableRow row : table.getTableModel().getVariables()) {
            if (row.getName().equals(rowName)) {
                return false;
            }
        }
        return true;
    }


    private String getAdjustedInputBinding(String binding) {
        if (binding == null) {
            throw new IllegalArgumentException("Basic binding is null");
        }
        return binding.substring(0, binding.lastIndexOf('/'));
    }


    private String setAdjustedOutputBinding(String unsplitName, VariableRow row) {
        String oldBinding = _outputBindings.getBindingFromSource(unsplitName);
        String newBinding = oldBinding.replace(
                "/" + unsplitName + "/*",
                "/" + row.getName() + "/" + getXQuerySuffix(row));

        // need to replace binding key first, to enable the set call to succeed
        _outputBindings.replaceBinding(unsplitName, oldBinding, newBinding);
        _outputBindings.setBinding(_netVarTarget, DataUtils.wrapBinding(_dataItemName,
                        newBinding), false);
        return newBinding;
    }


    private VariableRow getReferencedNetVarRow(VariableTable netTable, String name) {
        String target = _outputBindings.getTarget(name);
        for (VariableRow row : netTable.getVariables()) {
            if (row.getName().equals(target)) {
                return row;
            }
        }
        return null;
    }


    private String assembleSplitQuery(VariableRow inputRow) {
        return assembleSplitQuery(inputRow, inputRow.getName());
    }

    private String assembleSplitQuery(VariableRow inputRow, String itemName) {
        String binding = inputRow.getMapping();
        StringBuilder s = new StringBuilder();
        s.append("for $s in ");
        s.append(binding.substring(binding.lastIndexOf('/')));
        s.append("/*\nreturn <").append(itemName);
        s.append(">{$s/").append(getXQuerySuffix(inputRow)).append("}</");
        s.append(itemName).append('>');
        return s.toString();
    }


    private String getXQuerySuffix(VariableRow row) {
        try {
            return SpecificationModel.getHandler().getDataHandler()
                    .getXQuerySuffix(row.getDataType());
        }
        catch (YDataHandlerException ydhe) {
            return "";
        }
    }


    private String assembleJoinQuery() {
        String tag = getOutputTarget();
        StringBuilder s = new StringBuilder();
        s.append('<').append(tag);
        s.append(">{for $j in /").append(_task.getID()).append("/");
        s.append(_dataItemName).append("\nreturn $j}</").append(tag).append('>');
        return s.toString();
    }


    private String unprefix(String dataType) {
        if (dataType != null) {
            int pos = dataType.indexOf(':');
            if (pos > -1) {
                return dataType.substring(pos + 1);
            }
        }
        return dataType;
    }

}
