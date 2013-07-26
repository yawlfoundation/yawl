package org.yawlfoundation.yawl.editor.core.data;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public enum YInternalType {

    YDocumentType(new YDocumentType()),
    YStringListType(new YStringListType()),
    YTimerType(new YTimerType());

    private YDataType _type;
    private boolean _used;

    private static final String DEFAULT_DEFINITION =
      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";


    private YInternalType(YDataType type) {
        _type = type;
    }


    public void setUsed(boolean used) { _used = used; }

    public boolean isUsed() { return _used; }


    public String getSchema() { return _type.getSchema(); }

    public static boolean isName(String name) {
        for (YInternalType type : values()) {
            if (type.name().equals(name)) return true;
        }
        return false;
    }


    public String getValidationSchema(String name) {
        return _type.getValidationSchema(name);
    }


    public String adjustSchema(String specDataSchema) {
        return _used ?
            addSchemaToSpecificationDataSchema(specDataSchema, name(), getSchema()) :
            expungeSchemaFromSpecificationDataSchema(specDataSchema, name(), getSchema());
    }


    /********************************************************************************/

    private static String addSchemaToSpecificationDataSchema(String specDataSchema,
                                                             String typeName,
                                                             String schema) {
        String result = specDataSchema ;

        if (! (specDataSchema == null || specDataSchema.contains(typeName))) {

            // if empty string, or normalised header only, get unnormalised version
            if ((specDataSchema.length()==0) || specDataSchema.endsWith("/>")) {
                specDataSchema = DEFAULT_DEFINITION;
            }

            // remove end
            int insertPoint = specDataSchema.lastIndexOf('<') ;
            if (insertPoint > 0) {
                String closer = specDataSchema.substring(insertPoint);
                String newSchema = specDataSchema.substring(0, insertPoint - 1) ;

                // insert schema
                newSchema += schema + closer ;

                String prefix = getPrefix(newSchema);
                if (! prefix.equals("xs:")) {
                    newSchema = newSchema.replaceAll("xs:", prefix) ;
                }

                result = newSchema ;
            }
        }
        return result ;
    }


    private static String expungeSchemaFromSpecificationDataSchema(String specDataSchema,
                                                                   String typeName,
                                                                   String schema) {
        String result = specDataSchema ;
        if ((specDataSchema != null) && (specDataSchema.contains(typeName))) {
            result = specDataSchema.replaceFirst(schema, "");
        }
        return result ;
    }


    private static String getPrefix(String schema) {
        String result = "";
        int end = schema.indexOf(":schema") ;
        if (end > -1) result = schema.substring(1, end + 1) ;
        return result;
    }


}
