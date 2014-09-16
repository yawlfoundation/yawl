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
        String rootName = "root";
        Map<String, Element> elementMap = assembleMap(
                SpecificationModel.getHandler().getSchema());
        String schema = new DataSchemaBuilder(elementMap).buildSchema(
                    "data", row.getName(), row.getDataType());
        Map<String, DynFormField> fieldMap =
                getFieldMap(getParameterMap(row), rootName, schema);
        XNode root = getDataXNode(
                new ArrayList<DynFormField>(fieldMap.values()), rootName);
        XNode varNode = root.getChild(row.getName());
        return varNode != null ? StringUtil.unwrap(varNode.toPrettyString()).trim() : null;
    }


    private Map<String, FormParameter> getParameterMap(VariableRow row) {
        Map<String, FormParameter> map = new HashMap<String, FormParameter>();
        map.put(row.getName(), getParameter(row));
        return map;
    }

}
