/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormException;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFieldAssembler;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 15/06/2014
 */
public abstract class TypeValueBuilder {

    /**
     * Builds a map of complex data types and their names from the specification schema,
     * required for the building of the data schema
     * @param schema the data definition schema of the specification
     * @return a map of name to data type definition
     */
    protected Map<String, Element> assembleMap(String schema) {
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
     * Creates a FormParameter object from the data contained in a variable row
     * @param row the row to use
     * @return a corresponding FormParameter
     */
    protected FormParameter getParameter(VariableRow row) {
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
     * Builds a map of dynamic form fields from a set of variables, each field
     * representing a simple type variable or a hierarchy of simple type elements
     * of a complex type variable.
     * @param paramMap a map of variable names to FormParameters (an extension of
     *                 YVariable required for dynamic forms)
     * @return the composed map of field names to fields
     */
    protected Map<String, DynFormField> getFieldMap(Map<String, FormParameter> paramMap,
                                                  String rootName, String schema) {
        String data = "<" + rootName + "/>";
        try {
            DynFormFieldAssembler fieldAssembler = new DynFormFieldAssembler(
                   schema, data, paramMap);
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
     * Creates a data document filled with sample values from a list of dynamic
     * form fields.
     * @param fieldList a list of dynamic form fields
     * @return a corresponding data document
     */
    protected Document getDataDocument(List<DynFormField> fieldList, String rootName) {
        return getDataXNode(fieldList, rootName).toDocument();
    }


    protected XNode getDataXNode(List<DynFormField> fieldList, String rootName) {
        XNode root = new XNode(rootName);
        for (DynFormField field : fieldList) {
            root.addChild(expandField(field));
        }
        return root;
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
    protected YNet getNet() {
        return YAWLEditor.getNetsPane().getSelectedYNet();
    }


    // gets the core data handler
    protected YDataHandler getDataHandler() {
        return getSpecHandler().getDataHandler();
    }


    // gets the core specification handler
    protected YSpecificationHandler getSpecHandler() {
        return SpecificationModel.getHandler();
    }

}
