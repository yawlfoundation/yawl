/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class InstanceValidator {

    private String _specificationSchema;
    private DataSchemaValidator _validator;

    // a lookup map for internal type schemas
    private Map<String, String> _internalTypeSchemaMap;

    private static final String TEMP_VAR_NAME = "foo_bar";


    protected InstanceValidator() {
        _validator = new DataSchemaValidator();
    }


    protected void setSpecificationSchema(String schema) {
        _specificationSchema = schema != null ? schema : DataUtil.DEFAULT_SCHEMA;
    }


    protected List<String> validate(String dataType, String value) {
        String instance = StringUtil.wrap(value, TEMP_VAR_NAME);
        String cleansedType = cleanse(dataType);
        String schema;
        if (XSDType.isBuiltInType(cleansedType)) {
            if (XSDType.getString(XSDType.STRING).equals(cleansedType)) {
                return Collections.emptyList();                 // strings are always OK
            }
            schema = getXSDTypeSchema(cleansedType);
        }
        else if (YInternalType.isType(cleansedType)) {
            schema = getInternalTypeSchema(cleansedType);
        }
        else {                                        // complex type or other namespace
            schema = getAugmentedSchema(dataType);
        }
        if (schema != null) {
            _validator.setDataTypeSchema(schema);
            return _validator.validate(instance);
        }
        return Arrays.asList("Invalid schema") ;
    }


    private String extractNamespace() {
        Document doc = JDOMUtil.stringToDocument(_specificationSchema);
        return doc != null ? doc.getRootElement().getNamespace().getPrefix() + ":" : "";
    }


    private String getXSDTypeSchema(String dataType) {
        XNode baseNode = getSchemaBase();
        baseNode.addChild(buildInstanceNode(dataType));
        return baseNode.toString();
    }


    private String getInternalTypeSchema(String dataType) {
        return insertIntoSchema(getSchemaBase().toString(),
                DataUtil.DEFAULT_NAMESPACE_PREFIX + ":",
                getInternalTypeSchemaContent(dataType));
    }


    private String getAugmentedSchema(String dataType) {
        String nsPrefix = extractNamespace();
        return insertIntoSchema(_specificationSchema, nsPrefix,
                buildInstanceNode(nsPrefix, dataType).toString());
    }

    private XNode getSchemaBase() {
        String prefix = DataUtil.DEFAULT_NAMESPACE_PREFIX;
        XNode base = new XNode(prefix + ":schema");
        base.addAttribute("xmlns:" + prefix, DataUtil.DEFAULT_NAMESPACE_URL);
        return base;
    }


    private String getInternalTypeSchemaContent(String dataType) {
        if (_internalTypeSchemaMap == null) {
            _internalTypeSchemaMap = new HashMap<String, String>();
        }
        String schema = _internalTypeSchemaMap.get(dataType);
        if (schema == null) {
            Element schemaFor = YInternalType.getSchemaFor(dataType, TEMP_VAR_NAME);
            schema = JDOMUtil.elementToString(schemaFor)
                    .replaceAll("yawl:", DataUtil.DEFAULT_NAMESPACE_PREFIX + ":");
            _internalTypeSchemaMap.put(dataType, schema);
        }
        return schema;
    }


    // remove the default namespace (if any)
    private String cleanse(String dataType) {
        int pos = dataType.indexOf(':');
        return pos > -1 ? dataType.substring(pos + 1) : dataType;
    }


    // for default xs types
    private XNode buildInstanceNode(String dataType) {
        XNode elementNode = new XNode("xs:element");
        elementNode.addAttribute("name", TEMP_VAR_NAME);
        elementNode.addAttribute("type", "xs:" + dataType);
        return elementNode;
    }

    // for external types
    private XNode buildInstanceNode(String prefix, String dataType) {
        XNode elementNode = new XNode(prefix + "element");
        elementNode.addAttribute("name", TEMP_VAR_NAME);
        elementNode.addAttribute("type", dataType);
        return elementNode;
    }

    private String insertIntoSchema(String schema, String prefix, String toInsert) {
        String endTag = "</" + prefix + "schema>";
        if (schema.endsWith("/>")) {
            schema = schema.replaceFirst("/>", ">" + endTag);
        }
        return StringUtil.insert(schema, toInsert, schema.lastIndexOf(endTag));
    }

}
