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

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 15/06/2014
 */
public class SampleValueGenerator extends TypeValueBuilder {

    public SampleValueGenerator() { }


    public String generate(VariableRow row) {
        return generate(getParameter(row));
    }


    public String generate(YParameter parameter) {
        return generate(new FormParameter(parameter));
    }


    public Map<String, Element> getSchemaMap(String schema) {
        return assembleMap(schema);
    }


    private String generate(FormParameter parameter) {
        String rootName = "root";
        Map<String, Element> elementMap = assembleMap(
                SpecificationModel.getHandler().getSchema());
        String schema = new DataSchemaBuilder(elementMap).buildSchema(
                    "data", parameter.getName(), parameter.getDataTypeName());
        Map<String, DynFormField> fieldMap =
                getFieldMap(getParameterMap(parameter), rootName, schema);
        XNode root = getDataXNode(
                new ArrayList<DynFormField>(fieldMap.values()), rootName);
        XNode varNode = root.getChild(parameter.getName());
        return varNode != null ? StringUtil.unwrap(varNode.toPrettyString()).trim() : null;
    }


    private Map<String, FormParameter> getParameterMap(FormParameter parameter) {
        Map<String, FormParameter> map = new HashMap<String, FormParameter>();
        map.put(parameter.getName(), parameter);
        return map;
    }

}
