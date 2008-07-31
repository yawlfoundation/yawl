package org.yawlfoundation.yawl.editor.data;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 16/06/2008
 */
public class YTimerType {

    private static String schema = "\n\t<xs:complexType name=\"YTimerType\">\n" +
                                   "\t\t<xs:sequence>\n" +
                                   "\t\t\t<xs:element name=\"trigger\">\n" +
                                   "\t\t\t\t<xs:simpleType>\n" +
                                   "\t\t\t\t\t<xs:restriction base=\"xs:string\">\n" +
                                   "\t\t\t\t\t\t<xs:enumeration value=\"OnEnabled\"/>\n" +
                                   "\t\t\t\t\t\t<xs:enumeration value=\"OnExecuting\"/>\n" +
                                   "\t\t\t\t\t</xs:restriction>\n" +
                                   "\t\t\t\t</xs:simpleType>\n" +
                                   "\t\t\t</xs:element>\n" +
                                   "\t\t\t<xs:element name=\"expiry\" type=\"xs:string\"/>\n" +
                                   "\t\t</xs:sequence>\n" +
                                   "\t</xs:complexType>\n";

    public YTimerType() {}

    public static String getSchema() { return schema; }


    public List<DataVariable> getNetLevelTimerVariables() {
        List<DataVariable> result = null ;

        NetGraph selectedGraph = YAWLEditorDesktop.getInstance().getSelectedGraph() ;
        if (selectedGraph != null) {
            Decomposition decomp = selectedGraph.getNetModel().getDecomposition();
            if (decomp != null) {
                result = DataVariableUtilities.getVariablesOfType(decomp.getVariables(),
                                               DataVariable.YAWL_SCHEMA_TIMER_TYPE);
            }
        }
        return result ;
    }

    public static String adjustSchema(String specDataSchema, boolean include) {
        if (include) {
           return addSchemaToSpecificationDataSchema(specDataSchema);
        }
        else {
            return expungeSchemaFromSpecificationDataSchema(specDataSchema);             
        }
    }

    public static String addSchemaToSpecificationDataSchema(String specDataSchema) {
        String result = specDataSchema ;
        if ((specDataSchema != null) && (specDataSchema.indexOf("YTimerType") == -1)) {

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


    public static String expungeSchemaFromSpecificationDataSchema(String specDataSchema) {
        String result = specDataSchema ;
        if ((specDataSchema != null) && (specDataSchema.indexOf("YTimerType") > -1)) {
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
