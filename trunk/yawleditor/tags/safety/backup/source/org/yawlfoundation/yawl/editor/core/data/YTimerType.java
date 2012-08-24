package org.yawlfoundation.yawl.editor.core.data;

/**
 * Author: Michael Adams
 * Creation Date: 16/06/2008
 */
class YTimerType implements YDataType {

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


    YTimerType() {}

    public String getSchema() { return _schema; }

    public String getValidationSchema(String name) {
        return String.format(_valElement, name);
    }

}
