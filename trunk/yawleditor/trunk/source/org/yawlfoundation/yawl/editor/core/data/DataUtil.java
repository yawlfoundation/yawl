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
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class DataUtil {

    private Document _schemaDoc;
    private final DataSchemaValidator _schemaValidator;
    private final InstanceValidator _instanceValidator;

    public static final String DEFAULT_NAMESPACE_PREFIX = "xs";
    public static final String DEFAULT_NAMESPACE_URL = "http://www.w3.org/2001/XMLSchema";
    public static final String DEFAULT_SCHEMA = "<xs:schema xmlns:" +
            DEFAULT_NAMESPACE_PREFIX + "=\"" + DEFAULT_NAMESPACE_URL + "\">\n\n</xs:schema>";


    public DataUtil(String schema) {
        _schemaValidator = new DataSchemaValidator();
        _instanceValidator = new InstanceValidator();
        setSpecificationSchema(schema);
    }


    public void setSpecificationSchema(String schema) {
        String specificationSchema = schema != null ? schema : DEFAULT_SCHEMA;
        _schemaValidator.setDataTypeSchema(specificationSchema);
        _instanceValidator.setSpecificationSchema(specificationSchema);
        _schemaDoc = JDOMUtil.stringToDocument(specificationSchema);
    }


    protected InstanceValidator getInstanceValidator() { return _instanceValidator; }


    public List<String> getBuiltInTypeNames() {
        return XSDType.getBuiltInTypeList();
    }


    public List<String> getInternalTypeNames() {
        List<String> typeList = new ArrayList<String>();
        for (YInternalType internalType : YInternalType.values()) {
            typeList.add(internalType.name());
        }
        return typeList;
    }


    public List<String> getUserDefinedTypeNames() {
        return new ArrayList<String>(_schemaValidator.getPrimarySchemaTypeNames());
    }

    public Namespace getDataSchemaNamespace() {
        return _schemaDoc != null ? _schemaDoc.getRootElement().getNamespace() :
                JDOMUtil.stringToDocument(DEFAULT_SCHEMA).getRootElement().getNamespace();
    }


    public String[] getMultiInstanceItemNameAndType(String dataType) {
        if (dataType.equals("YStringListType")) {
            return new String[] { "item", "string" };
        }
        if (! XSDType.isBuiltInType(dataType) && _schemaDoc != null) {
            Element root = _schemaDoc.getRootElement();
            Namespace ns = root.getNamespace();
            for (Element child : root.getChildren()) {
                String name = child.getAttributeValue("name");
                if (name != null && name.equals(dataType)) {
                    Element sequence = child.getChild("sequence", ns);
                    if (sequence != null) {
                        Element element = sequence.getChild("element", ns);
                        if (element != null) {
                            String maxOccurs = element.getAttributeValue("maxOccurs");
                            if (maxOccurs != null && (maxOccurs.equals("unbounded") ||
                                    ! maxOccurs.equals("1"))) {
                                String elementName = element.getAttributeValue("name");
                                String elementType = element.getAttributeValue("type");
                                return new String[] { elementName, elementType };
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public List<String> getDataTypeNames() {
        List<String> typeList = getBuiltInTypeNames();
        typeList.remove("notation");                             // non-assignable type
        typeList.addAll(getInternalTypeNames());
        typeList.addAll(getUserDefinedTypeNames());

        Collections.sort(typeList, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        return typeList;
    }


    public String getXQuerySuffix(String dataType) {
        if (XSDType.isBuiltInType(dataType)) {
            return "text()";
        }
        if (YInternalType.isType(dataType)) {
            return "*";
        }

        switch(_schemaValidator.getDataTypeComplexity(dataType)) {
            case Complex: return "*";
            case Simple:
            case Unknown:
            default: return "text()";
        }
    }

}
