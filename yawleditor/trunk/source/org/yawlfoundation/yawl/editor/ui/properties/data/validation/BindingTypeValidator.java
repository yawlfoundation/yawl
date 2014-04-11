package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import net.sf.saxon.s9api.SaxonApiException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormException;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFieldAssembler;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * This class validates that a variable binding from net-task or task-net is, or will
 * evaluate to, a value of the same data type as the target net or task variable. It
 * co-ops the dynamic form classes of the resource service to build a data schema and
 * then a data document filled with sample data that is correct for each element,
 * including restrictions and enumerations. The binding is first evaluated to sample
 * values where necessary by converted XPath/XQuery expressions to values, then
 * compares the evaluated binding to the relevant data type.
 *
 * @author Michael Adams
 * @date 1/11/2013
 */
public class BindingTypeValidator {

    private String _schema;
    private Map<String, DynFormField> _fieldMap;
    private String _dataTypeName;
    private String _rootName;
    private Document _dataDocument;


    /**
     * The constructor. Builds a schema from the complete set of net-level vars (for
     * and input variable) or of task-level vars (for an output variable). Next it
     * generates a set of simple type fields corresponding to the elements of the schema.
     * Then, it build a data document of sample values corresponding to each of the
     * simple type element, which can be used to evaluate queries and then compare
     * data types. All of this is done only when the class is first constructed, and
     * these objects are then used in all future calls to this class.
     *
     * @param varList the list of net-level variables if validating a task input
     *                binding, or the list of task-level output variables if
     *                validating a task output binding
     * @param row the row representing the input task variable being bound to, or
     *            the output task variable being bound from
     * @param dataTypeName the name of the target data type (the data type of the
     *                     task-level variable for input bindings, or the data type
     *                     of the net-level variable for output bindings
     */
    public BindingTypeValidator(List<VariableRow> varList, VariableRow row,
                                String dataTypeName) {
        _rootName = getRootElementName(row);
        _dataTypeName = dataTypeName;
        init(getParamMap(varList));
    }


    /**
     * This variant is used to validate split predicates
     * @param net the selected net
     *
     */
    public BindingTypeValidator(YNet net) {
        _rootName = "foo_bar";
        _dataTypeName = "boolean";
        init(getParamMap(net));
    }


    /**
     * Sets the target data type to validate against. Used for output bindings when
     * a different target net-level variable is chosen
     * @param dataType the data type of the variable being bound to
     */
    public void setDataType(String dataType) { _dataTypeName = dataType; }


    public void setRootElementName(String rootName) { _rootName = rootName; }


    /**
     * Validates that the binding matches the data type of the target variable
     * @param binding the binding to validate
     * @return a list of error messages, or an empty list if the binding is valid in
     * respect to its target data type
     */
    public List<String> validate(String binding) {
        if (shouldValidate(binding)) {
            try {
                String query = evaluateQuery(binding.trim(), _dataDocument);
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
              _schema = getDataSchema(paramMap);
              _fieldMap = getFieldMap(paramMap);
              _dataDocument = getDataDocument(new ArrayList<DynFormField>(_fieldMap.values()));
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
     * Builds a map of dynamic form fields from a set of variables, each field
     * representing a simple type variable or a hierarchy of simple type elements
     * of a complex type variable.
     * @param paramMap a map of variable names to FormParameters (an extension of
     *                 YVariable required for dynamic forms)
     * @return the composed map of field names to fields
     */
    private Map<String, DynFormField> getFieldMap(Map<String, FormParameter> paramMap) {
        String data = "<" + _rootName + "/>";
        try {
            DynFormFieldAssembler fieldAssembler = new DynFormFieldAssembler(
                   _schema, data, paramMap);
            return buildFieldMap(fieldAssembler.getFieldList());
        }
        catch (DynFormException dfe) {
            // fall through;
        }
        return Collections.emptyMap();
    }


    /**
     * Builds a map of dynamic form fields from a list of them
     * @param fieldList a list of dynamic form fields
     * @return the composed map of field names to fields
     */
    private Map<String, DynFormField> buildFieldMap(List<DynFormField> fieldList) {
        Map<String, DynFormField> fieldMap = new HashMap<String, DynFormField>();
        for (DynFormField field : fieldList) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
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
     * Gets the name for the root of the data schema and document.
     * @param row the row representing the input task variable being bound to, or
     *            the output task variable being bound from
     * @return the name of the net for input bindings, or of the task for output
     *         bindings
     */
    private String getRootElementName(VariableRow row) {
        if (row == null) return "foo_bar";
        return row.isInput() ? getNet().getRootDataElementName() :
               row.getDecompositionID();
    }


    /**
     * Builds a map of complex data types and their names from the specification schema,
     * required for the build of the data schema
     * @param schema the data definition schema of the specification
     * @return a map of name to data type definition
     */
    private Map<String, Element> assembleMap(String schema) {
        Map<String, Element> map = new HashMap<String, Element>();
        if (schema != null) {
            Element dataSchema = JDOMUtil.stringToElement(schema);
            for (Element child : dataSchema.getChildren()) {
                String name = child.getAttributeValue("name");
                if (name != null) {
                    map.put(name, child);
                }
            }
        }
        return map;
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
     * Creates a FormParameter object from the data contained in a variable row
     * @param row the row to use
     * @return a corresponding FormParameter
     */
    private FormParameter getParameter(VariableRow row) {
        try {

            // exception here if no current specification (should never occur)
            String ns = getDataHandler().getDataSchemaNamespace().toString();

            FormParameter param = new FormParameter();
            param.setInitialValue(row.getValue());
            param.setDataTypeAndName(row.getDataType(), row.getName(), ns);
            param.setAttributes(row.getAttributes());
            return param;
        }
        catch (YDataHandlerException ydhe) {
            return null;
        }
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
     * Creates a data document filled with sample values from a list of dynamic
     * form fields.
     * @param fieldList a list of dynamic form fields
     * @return a corresponding data document
     */
    private Document getDataDocument(List<DynFormField> fieldList) {
        XNode root = new XNode(_rootName);
        for (DynFormField field : fieldList) {
            root.addChild(expandField(field));
        }
        return root.toDocument();
    }


    /**
     * Recursively builds a hierarchical data document for a field, until its simple
     * type leaf is reached, which is added with a sample value for its data type
     * inserted
     * @param field the field to build from
     * @return a node containing the field breakdown
     */
    private XNode expandField(DynFormField field) {
        XNode fieldNode = new XNode(field.getName());
        if (field.isSimpleField()) {
            fieldNode.setText(getSampleValue(field), true);
        }
        else {
            for (DynFormField subField : field.getSubFieldList()) {
                fieldNode.addChild(expandField(subField));
                if (subField.isChoiceField()) break;              // only want one choice
            }
        }
        return fieldNode;
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
     * Gets the sample value based on the data type of the field
     * @param field the field to get a value for
     * @return the sample data value
     */
    private String getSampleValue(DynFormField field) {
        if (field.isSimpleField()) {
            if (field.hasEnumeratedValues()) {
                return field.getEnumeratedValues().get(0);
            }
            if (field.hasRestriction()) {
                return new RestrictionSampleValueGenerator(field.getRestriction())
                        .generateValue();
            }
            return XSDType.getSampleValue(field.getDataTypeUnprefixed());
        }
        return "";
    }


    // gets the currently selected net
    private YNet getNet() {
        return YAWLEditor.getNetsPane().getSelectedYNet();
    }


    // gets the core data handler
    private YDataHandler getDataHandler() {
        return getSpecHandler().getDataHandler();
    }


    // gets the core specification handler
    private YSpecificationHandler getSpecHandler() {
        return SpecificationModel.getHandler();
    }

}
