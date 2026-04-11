package org.yawlfoundation.yawl.schema.internal;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Adams
 * @date 29/1/2026
 */
public class YInternalTypeUtil {

    private static final String XS_NS = "http://www.w3.org/2001/XMLSchema";
    private static final String YAWL_NS = "http://www.yawlfoundation.org/yawlschema";
    private static final String ELEM_FORM_DEFAULT = "qualified";
    private static final String XS_PREFIX = "xs";
    private static final String YAWL_PREFIX = "yawl";
    private static final String TARGET_PREFIX = "targetNamespace";
    private static final String ELEM_PREFIX = "elementFormDefault";

    
    public static String injectTypeSchema(String dtdSchema) {
        return injectSchema(dtdSchema, false);
    }


    public static String injectInstanceSchema(String dataTypeSchema) {
        return injectSchema(dataTypeSchema, true);
    }


    public static String injectSchema(String dataTypeSchema, boolean hasParamElement) {

        // check that schema is not null and is well-structured
        XNode schema = parseSchema(dataTypeSchema);
        if (schema == null) {
            return dataTypeSchema;
        }

        // check that schema references at least one internal type
        Set<String> referencedInternalTypes = getReferencedInternalTypes(dataTypeSchema);
        if (referencedInternalTypes.isEmpty()) {
            return dataTypeSchema;
        }

        // check that the schema is not already injected
        String targetNS = schema.getAttributeValue("targetNamespace");
        if (targetNS != null && targetNS.equals(YAWL_NS)) {
            return dataTypeSchema;
        }

        // checks passed, inject
        addSchemaAttributes(schema);
        if (hasParamElement) {
            addPrefixToElement(schema);
        }
        addReferencedSchemas(schema, referencedInternalTypes);

        return schema.toPrettyString();
    }


    public static String annotateDataNS(String schema, String dataXML) {
        XNode dataNode = parseSchema(dataXML);
        if (! (dataNode == null || getReferencedInternalTypes(schema).isEmpty())) {
            dataNode.addAttribute("xmlns", YAWL_NS);
            return dataNode.toPrettyString();
        }
        return dataXML;
    }


    public static boolean isGeoType(YInternalType type) {
        return type.ordinal() >= YInternalType.YGeoLatLongType.ordinal();
    }


    public static boolean isGeoListType(YInternalType type) {
        return type.ordinal() >=  YInternalType.YGeoLatLongListType.ordinal();
    }

    
    public static boolean isGeoTypeName(String typeName) {
        if (YInternalType.isType(typeName)) {
            return isGeoType(YInternalType.valueOf(typeName));
        }
        return false;
    }
    

    public static boolean isGeoListTypeName(String typeName) {
        if (YInternalType.isType(typeName)) {
            return isGeoListType(YInternalType.valueOf(typeName));
        }
        return false;
    }


    public static void addSchemaAttributes(XNode schema) {
        schema.addAttribute("xmlns:" + XS_PREFIX, XS_NS);
        schema.addAttribute("xmlns:" + YAWL_PREFIX, YAWL_NS);
        schema.addAttribute(TARGET_PREFIX, YAWL_NS);
        schema.addAttribute(ELEM_PREFIX, ELEM_FORM_DEFAULT);
    }


    public static List<XNode> getChildNodes(String dataTypeSchema) {
        XNode root = parseSchema(dataTypeSchema);
        return root != null ? root.getChildren() :  Collections.emptyList();
    }


    public static Set<String> getReferencedInternalTypes(String dataTypeSchema) {
        Pattern p = Pattern.compile("\\byawl:([A-Za-z_][A-Za-z0-9._-]*)");
        Matcher m = p.matcher(dataTypeSchema);

        Set<String> types = new HashSet<>();
        while (m.find()) {
            types.add(m.group(1));   // <-- no prefix
        }
        return types;
    }


    public static String getSchemaString(String internalTypeName) {
        if (YInternalType.isType(internalTypeName)) {
            return YInternalType.valueOf(internalTypeName).getSchemaString();
        }
        return null;
    }

    public static void addReferencedSchemas(XNode schema, Set<String> referencedInternalTypes) {
        for (String internalTypeName : referencedInternalTypes) {
            String referencedSchema = getSchemaString(internalTypeName);

            // if null, the schema validator will handle the error later
            if (referencedSchema != null) {
                schema.addContent(referencedSchema);
            }
        }
    }

    public static void addPrefixToElement(XNode schema) {
        for (XNode child : schema.getChildren()) {
            if (child.getName().endsWith("element")) {
                Map<String, String> attrMap = child.getAttributes();
                String type = attrMap.get("type");
                if (! (type == null || type.contains(":"))) {
                    attrMap.put("type", YAWL_PREFIX + ":" + type);
                }
            }
        }
    }


    public static void dereferenceTypeAttributes(XNode node) {
        for (XNode child : node.getChildren()) {
            Map<String, String> attrMap = child.getAttributes();
            String typeValue = attrMap.get("type");
            if (typeValue != null && typeValue.startsWith(YAWL_PREFIX + ":")) {
                attrMap.put("type", typeValue.substring(typeValue.indexOf(':') + 1));
            }
            if (child.hasChildren()) {
                dereferenceTypeAttributes(child);
            }
        }
    }


    private static XNode parseSchema(String schema) {
        try {
            XNodeParser parser = new XNodeParser(true);
            parser.suppressMessages(true);
            return parser.parse(schema);
        }
        catch (Exception e) {    // ok to safely ignore parsing exceptions
            return null;
        }
    }

}
