package org.yawlfoundation.yawl.editor.ui.data.internal;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

/**
 * Author: Michael Adams
 * Date: 04/2009
 */
public class YInternalTypeHelper {


    public static String adjustSchema(String specDataSchema, String typeName,
                                      String schema, boolean include) {
        if (include) {
           return addSchemaToSpecificationDataSchema(specDataSchema, typeName, schema);
        }
        else {
            return expungeSchemaFromSpecificationDataSchema(specDataSchema, typeName, schema);
        }
    }


    private static String addSchemaToSpecificationDataSchema(String specDataSchema,
                                                             String typeName,
                                                             String schema) {
        String result = specDataSchema ;
        
        if ((specDataSchema != null) && (specDataSchema.indexOf(typeName) == -1)) {

            // if empty string, or normalised header only, get unnormalised version
            if ((specDataSchema.length()==0) || specDataSchema.endsWith("/>")) {
                specDataSchema = SpecificationModel.DEFAULT_TYPE_DEFINITION;
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
        if ((specDataSchema != null) && (specDataSchema.indexOf(typeName) > -1)) {
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