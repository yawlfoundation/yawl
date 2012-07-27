package org.yawlfoundation.yawl.editor.ui.data.internal;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableUtilities;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;

import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 16/06/2008
 */
public class YTimerType {

    private static final String _schema = "\n\t<xs:complexType name=\"YTimerType\">\n" +
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

    private static final String _valElement = "<element name=\"%s\">" +
                                   "<complexType>" +
                                   "<sequence>" +
                                   "<element name=\"trigger\">" +
                                   "<simpleType>" +
                                   "<restriction base=\"string\">" +
                                   "<enumeration value=\"OnEnabled\"/>" +
                                   "<enumeration value=\"OnExecuting\"/>" +
                                   "</restriction>" +
                                   "</simpleType>" +
                                   "</element>" +
                                   "<element name=\"expiry\" type=\"string\"/>" +
                                   "</sequence>" +
                                   "</complexType>" +
                                   "</element>" ;


    public YTimerType() {}

    public static String getSchema() { return _schema; }

    public static String getValidationSchema(String name) { 
        return String.format(_valElement, name);
    }

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
        return YInternalTypeHelper.adjustSchema(specDataSchema, "YTimerType",
                                               _schema, include);
    }

}
