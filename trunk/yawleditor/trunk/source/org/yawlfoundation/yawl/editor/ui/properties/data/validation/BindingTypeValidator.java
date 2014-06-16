package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import net.sf.saxon.s9api.SaxonApiException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * This class validates that a variable binding from net->task or task->net is, or will
 * evaluate to, a value of the same data type as the target net or task variable. It
 * co-ops the dynamic form classes of the resource service to build a data schema and
 * then a data document filled with sample data that is correct for each element,
 * including restrictions and enumerations. The binding is first evaluated to sample
 * values where necessary by transforming XPath/XQuery expressions to literal values,
 * the compares the evaluated binding to the relevant data type.
 *
 * @author Michael Adams
 * @date 1/11/2013
 */
public class BindingTypeValidator extends TypeValueBuilder {

    private Map<String, DynFormField> _fieldMap;
    private String _dataTypeName;
    private String _rootName;
    private String _taskDecompositionID;
    private Document _dataDocument;


    /**
     * The constructor. Builds a schema from the complete set of net-level and task-level
     * variables. Next it generates a set of simple type fields corresponding to the
     * elements of the schema. Then, it builds a data document of sample values
     * corresponding to each of the simple type elements, which can be used to evaluate
     * queries and then compare data types. All of this is done only when the class is
     * first constructed, and these objects are then used in all future calls to this
     * class.
     *
     * @param varList the list of net-level variables if validating a task input
     *                binding, or the list of task-level output variables if
     *                validating a task output binding
     * @param dataTypeName the name of the target data type (the data type of the
     *                     task-level variable for input bindings, or the data type
     *                     of the net-level variable for output bindings)
     */
    public BindingTypeValidator(List<VariableRow> varList, String dataTypeName) {
        YNet net = getNet();
        _rootName = net.getRootDataElementName();
        _dataTypeName = dataTypeName;
        setTaskDecompositionID(varList);
        Map<String, FormParameter> paramMap = getParamMap(net);
        paramMap.putAll(getParamMap(varList));
        init(paramMap);
    }


    /**
     * This variant is used to validate split predicates
     * @param net the selected net
     *
     */
    public BindingTypeValidator(YNet net) {
        _rootName = net.getRootDataElementName();
        _dataTypeName = "boolean";
        init(getParamMap(net));
    }


    /**
     * Sets the target data type to validate against. Called by the binding dialogs when
     * a different target variable is chosen
     * @param dataType the data type of the variable being bound to
     */
    public void setDataType(String dataType) { _dataTypeName = dataType; }


    /**
     * Validates that the binding matches the data type of the target variable
     * @param binding the binding to validate
     * @return a list of error messages, or an empty list if the binding is valid with
     * respect to its target data type
     */
    public List<String> validate(String binding) {
        if (shouldValidate(binding)) {
            try {
                String query = evaluateQuery(maskDecompositionID(binding),
                        _dataDocument);
                if (! query.isEmpty()) {
                    List<String> errors = getDataHandler().validate(_dataTypeName, query);
                    if (! errors.isEmpty()) {
                        return Arrays.asList("Invalid value for target data type '" +
                                _dataTypeName + "'");
                    }
                }
            }
            catch (Exception e) {
                return Arrays.asList(e.getMessage());
            }
        }
        return Collections.emptyList();
    }


    /********************************************************************************/

    /**
     * Initialises the schema and data document
     * @param paramMap a map of variable names to FormParameters (an extension of
     *                 YVariable required for dynamic forms)
     */
    private void init(final Map<String, FormParameter> paramMap) {
        new Thread(new Runnable() {
            public void run() {
              _fieldMap = getFieldMap(paramMap, _rootName, getDataSchema(paramMap));
              _dataDocument = getDataDocument(
                      new ArrayList<DynFormField>(_fieldMap.values()), _rootName);
            }
        }).start();
    }


    /**
     * Checks whether validation is necessary.
     * @param binding the binding to check
     * @return true if this binding targets a net or task level variable
     */
    private boolean shouldValidate(String binding) {
        return (! (binding == null || isBuiltinPredicate(binding) ||
                _fieldMap == null || _dataTypeName == null));

    }


    /**
     * Builds a schema from a set of parameters. The schema is composed only of
     * simple types, with complex types decomposed into their simple type equivalents.
     * @param paramMap a map of variable names to FormParameters (an extension of
     *                 YVariable required for dynamic forms)
     * @return the data schema
     */
    private String getDataSchema(Map<String, FormParameter> paramMap) {
        Map<String, Element> elementMap = assembleMap(getSpecHandler().getSchema());
        return new DataSchemaBuilder(elementMap).buildSchema(
                    _rootName, new ArrayList<FormParameter>(paramMap.values()));
    }


    /**
     * Builds a map of form parameters from a list of variable rows
     * @param varList the list of net-level variables if validating a task input
     *                binding, or the list of task-level output variables if
     *                validating a task output binding
     * @return the composed map
     */
    private Map<String, FormParameter> getParamMap(List<VariableRow> varList) {
        Map<String, FormParameter> paramMap = new HashMap<String, FormParameter>();
        for (VariableRow row : varList) {
            FormParameter param = getParameter(row);
            paramMap.put(param.getName(), param);
        }
        return paramMap;
    }


    /**
     * Builds a map of form parameters from the set of net input and local variables
     * @param net the selected net
     * @return the composed map
     */
    private Map<String, FormParameter> getParamMap(YNet net) {
        Set<YVariable> variables = new HashSet<YVariable>(
                net.getInputParameters().values());
        variables.addAll(net.getLocalVariables().values());
        Map<String, FormParameter> paramMap = new HashMap<String, FormParameter>();
        for (YVariable variable : variables) {
            FormParameter param = getParameter(variable);
            paramMap.put(param.getName(), param);
        }
        return paramMap;
    }


    /**
     * Creates a FormParameter object from the data contained in a variable
     * @param variable the variable to use
     * @return a corresponding FormParameter
     */
    private FormParameter getParameter(YVariable variable) {
        FormParameter param = new FormParameter();
        param.setInitialValue(variable.getInitialValue());
        param.setDataTypeAndName(variable.getDataTypeName(), variable.getPreferredName(),
                variable.getDataTypeNameSpace());
        param.setAttributes(variable.getAttributes());
        return param;
    }


    // return true if binding is an external data gateway, timer or cost predicate
    private boolean isBuiltinPredicate(String binding) {
        if (binding.startsWith("boolean(")) {
            binding = binding.replace("boolean(", "").replaceFirst("\\)", "");
        }
        return binding.matches("^\\s*#external:\\w+\\s*:\\w+\\s*") ||
               binding.matches("^\\s*timer\\(\\w+\\)\\s*!?=\\s*" +
                       "'(dormant|active|closed|expired)'\\s*$") ||
               binding.matches("^\\s*cost\\((\\w*|\\s*)\\)\\s*$");
    }


    /**
     * Evaluates the XPath/XQuery parts of a binding to a simple data equivalent
     * @param query the binding
     * @param dataDocument the document of sample data
     * @return the evaluated binding
     * @throws SaxonApiException if the binding is invalid
     */
    private String evaluateQuery(String query, Document dataDocument)
            throws SaxonApiException {
        boolean wrapped = ! (query.startsWith("/") || query.startsWith("bool"));
        if (wrapped) {
            query = StringUtil.wrap(query, "foo_bar");
        }
        String evaluated = SaxonUtil.evaluateQuery(query, dataDocument);
        return wrapped ? StringUtil.unwrap(evaluated) : evaluated;
    }


    /**
     * Sets the decompositionID from a list of variable rows, for task variable
     * evaluations only
     * @param varList the list of task-level variables for validation
     */
    private void setTaskDecompositionID(List<VariableRow> varList) {
        if (! (varList == null || varList.isEmpty())) {
            _taskDecompositionID = varList.get(0).getDecompositionID();
        }
    }


    /**
     * Replaces all references to a decomposition in a binding query with the name of
     * the net (since that is the root name of the constructed data document)
     * @param binding the binding to transform as required
     * @return the transformed binding
     */
    private String maskDecompositionID(String binding) {
        if (_taskDecompositionID != null) {
            return binding.replaceAll("/" + _taskDecompositionID + "/",
                    "/" + _rootName + "/").trim();
        }
        return binding.trim();
    }

}
