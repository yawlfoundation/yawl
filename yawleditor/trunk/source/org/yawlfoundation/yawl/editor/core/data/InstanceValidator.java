package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Element;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
            schema = getXSDTypeSchema(cleansedType);
        }
        else if (YInternalType.isType(cleansedType)) {
            schema = getInternalTypeSchema(cleansedType);
        }
        else {                                        // complex type or other namespace
            schema = getAugmentedSchema(cleansedType);
        }
        if (schema != null) {
            _validator.setDataTypeSchema(schema);
            return _validator.validate(instance);
        }
        return Arrays.asList("Invalid schema") ;
    }


    private String getXSDTypeSchema(String dataType) {
        XNode baseNode = getSchemaBase();
        baseNode.addChild(buildInstanceNode("xs:" + dataType));
        return baseNode.toString();
    }


    private String getInternalTypeSchema(String dataType) {
        return insertIntoSchema(getSchemaBase().toString(),
                getInternalTypeSchemaContent(dataType));
    }


    private String getAugmentedSchema(String dataType) {
        return insertIntoSchema(_specificationSchema,
                buildInstanceNode(dataType).toString());
    }

    private XNode getSchemaBase() {
        XNode base = new XNode("xs:schema");
        base.addAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        return base;
    }


    private String getInternalTypeSchemaContent(String dataType) {
        if (_internalTypeSchemaMap == null) {
            _internalTypeSchemaMap = new Hashtable<String, String>();
        }
        String schema = _internalTypeSchemaMap.get(dataType);
        if (schema == null) {
            Element schemaFor = YInternalType.getSchemaFor(dataType, TEMP_VAR_NAME);
            schema = JDOMUtil.elementToString(schemaFor).replaceAll("yawl:", "xs:");
            _internalTypeSchemaMap.put(dataType, schema);
        }
        return schema;
    }


    // remove the default namespace (if any)
    private String cleanse(String dataType) {
        return dataType.startsWith("xs:") ? dataType.substring(3) : dataType;
    }


    private XNode buildInstanceNode(String dataType) {
        XNode elementNode = new XNode("xs:element");
        elementNode.addAttribute("name", TEMP_VAR_NAME);
        elementNode.addAttribute("type", dataType);
        return elementNode;
    }


    private String insertIntoSchema(String schema, String toInsert) {
        if (schema.endsWith("/>")) {
            schema = schema.replaceFirst("/>", "></xs:schema>");
        }
        return StringUtil.insert(schema, toInsert, schema.lastIndexOf("</xs:schema>"));
    }

}
