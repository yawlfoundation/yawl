package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * @author Michael Adams
 * @date 28/11/2013
 */
public class MultiInstanceHandler {

    private YTask _task;
    private OutputBindings _outputBindings;
    private String _targetNetVar;
    private String _itemName;
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
        return unwrapQuery(getJoinQuery());
    }

    public void setJoinQuery(String query) {
        _attributes.setUniqueOutputMIJoiningQuery(query);
    }


    public void setJoinQueryUnwrapped(String query) {
        setJoinQuery(wrapQuery(getOutputTarget(), query));
    }

    protected String getOutputTarget() {
       return _targetNetVar != null ? _targetNetVar :
               _outputBindings.getTarget(getFormalInputParam());
    }

    protected void setTarget(String targetNetVar) {
        _targetNetVar = targetNetVar;
    }


    protected boolean setMultiInstanceRow(VariableRow inputRow,
                                          VariableTable inputTable,
                                          VariableTable outputTable,
                                          DataVariableDialog dialog) {
        clear();            // reset params
        checkValidRow(inputRow);
        VariableRow outputRow = getCorrespondingOutputRow(outputTable, inputRow);

        if (inputRow.isMultiInstance()) {                // already multi instance
            VariableRow netVarRow = getReferencedNetVarRow(dialog, inputRow.getName());
            unsetRowProperties(inputTable, inputRow, netVarRow);
            unsetRowProperties(outputTable, outputRow, netVarRow);
            return true;
        }

        String inputBinding = inputRow.getMapping();
        String[] itemNameAndType = getItemNameAndType(inputRow.getDataType());
        _itemName = itemNameAndType[0];
        inputRow.setMapping(getAdjustedInputBinding(inputBinding));
        setAdjustedOutputBinding(outputRow.getName());

        setRowProperties(inputTable, inputRow, itemNameAndType[1]);
        setRowProperties(outputTable, outputRow, itemNameAndType[1]);

        setFormalInputParam(inputRow.getName());
        setSplitQuery(assembleSplitQuery(inputRow));
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
        _targetNetVar = null;
        _itemName = null;
    }


    private void unsetRowProperties(VariableTable table, VariableRow row,
                                    VariableRow netVarRow) {
        table.setMultiInstanceRow(null);
        row.setName(row.getName().substring(0, row.getName().lastIndexOf("_Item")));
        row.setDataType(netVarRow.getDataType());
        if (row.isInput()) {
            row.setMapping(generateBinding(netVarRow));
        }
        else {
            _outputBindings.removeBindingForTarget(_itemName);
            _outputBindings.setBinding(netVarRow.getName(), generateBinding(row));
        }
        clear();
    }


    private String generateBinding(VariableRow row) {
        StringBuilder s = new StringBuilder();
        s.append('/').append(row.getDecompositionID());
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
        if (row == null || ! row.isInput()) {
            throw new IllegalArgumentException(
                    "Row parameter is null or not an input row");
        }
    }


    private String[] getItemNameAndType(String complexType) {
        String[] itemNameAndType = SpecificationModel.getHandler().getDataHandler()
                .getMultiInstanceItemNameAndType(complexType);
        if (itemNameAndType == null) {
            throw new IllegalArgumentException("Invalid MI data type");
        }
        return itemNameAndType;
    }


    private VariableRow getCorrespondingOutputRow(VariableTable outputTable,
                                                  VariableRow inputRow) {
        for (VariableRow outputRow : outputTable.getVariables()) {
            if (outputRow.getName().equals(inputRow.getName()) &&
                    outputRow.getDataType().equals(inputRow.getDataType())) {
                return outputRow;
            }
        }
        throw new IllegalArgumentException(
                "No corresponding output variable exists");
    }


    private void setRowProperties(VariableTable table, VariableRow row, String dataType) {
        table.setMultiInstanceRow(row);
        row.setName(row.getName() + "_Item");
        row.setDataType(unprefix(dataType));
    }


    private String getAdjustedInputBinding(String binding) {
        if (binding == null) {
            throw new IllegalArgumentException("Basic binding is null");
        }
        return binding.substring(0, binding.lastIndexOf('/'));
    }


    private String setAdjustedOutputBinding(String name) {
        String oldBinding = _outputBindings.getBindingFromSource(name);
        String newBinding = oldBinding.replace(
                "/" + name + "/*", "/" + name + "_Item/text()");
//        _outputBindings.replaceBinding(name, oldBinding, newBinding);
        _outputBindings.setBinding(name,
                StringUtil.wrap(newBinding, _itemName), false);
        return newBinding;
    }


    private VariableRow getReferencedNetVarRow(DataVariableDialog dialog, String name) {
        String target = _outputBindings.getTarget(name);
        for (VariableRow row : dialog.getNetTablePanel().getTable().getVariables()) {
            if (row.getName().equals(target)) {
                return row;
            }
        }
        return null;
    }


    private String assembleSplitQuery(VariableRow inputRow) {
        String binding = inputRow.getMapping();
        StringBuilder s = new StringBuilder();
        s.append("for $s in ");
        s.append(binding.substring(binding.lastIndexOf('/')));
        s.append("/* return <").append(inputRow.getName());
        s.append(">{$s/text()}</").append(inputRow.getName()).append('>');
        return s.toString();
    }


    private String assembleJoinQuery() {
        String tag = getOutputTarget();
        StringBuilder s = new StringBuilder();
        s.append('<').append(tag);
        s.append(">{for $j in /").append(_task.getID()).append("/");
        s.append(_itemName).append(" return $j}</").append(tag).append('>');
        return s.toString();
    }


    private String unwrapQuery(String binding) {
        return binding != null ?
                binding.replaceAll("\\{*<\\w+>\\{*|\\}*</\\w*>\\}*", "") : null;
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

    private String wrapQuery(String tagName, String mapping) {
        if (StringUtil.isNullOrEmpty(mapping)) return null;
        boolean isXPath = mapping.trim().startsWith("/");
        StringBuilder s = new StringBuilder();
        s.append('<').append(tagName).append(">");
        if (isXPath) s.append("{");
        s.append(mapping);
        if (isXPath) s.append("}");
        s.append("</").append(tagName).append('>');
        return s.toString();
    }


}
