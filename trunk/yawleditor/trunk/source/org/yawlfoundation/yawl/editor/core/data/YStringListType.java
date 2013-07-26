package org.yawlfoundation.yawl.editor.core.data;

/**
 * Author: Michael Adams
 * Date: 04/2009
 */
class YStringListType implements YDataType {

    private static final String _schema =
            "\n\t<xs:complexType name=\"YStringListType\">\n" +
            "\t\t<xs:sequence>\n" +
            "\t\t\t<xs:element name=\"item\" type=\"xs:string\"" +
                     " minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
            "\t\t</xs:sequence>\n" +
            "\t</xs:complexType>\n";

    private static final String _valElement =
            "<element name=\"%s\">" +
                "<complexType>" +
                    "<sequence>" +
                        "<element name=\"item\" type=\"xs:string\"" +
                                 "minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
                     "</sequence>" +
                "</complexType>" +
           "</element>" ;


    public String getSchema() { return _schema; }

    public String getValidationSchema(String name) {
        return String.format(_valElement, name);
    }

}