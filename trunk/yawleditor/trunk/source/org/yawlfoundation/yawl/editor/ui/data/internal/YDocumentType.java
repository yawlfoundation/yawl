package org.yawlfoundation.yawl.editor.ui.data.internal;

/**
 * Author: Michael Adams
 * Date: 04/2009
 */
public class YDocumentType {

    private static final String _schema =
            "\n\t<xs:complexType name=\"YDocumentType\">\n" +
            "\t\t<xs:sequence>\n" +
            "\t\t\t<xs:element name=\"id\" type=\"xs:long\" minOccurs=\"0\"/>\n" +
            "\t\t\t<xs:element name=\"name\" type=\"xs:string\"/>\n" +
            "\t\t</xs:sequence>\n" +
            "\t</xs:complexType>\n";

    private static final String _valElement =
            "<element name=\"%s\">" +
                "<complexType>" +
                    "<sequence>" +
                        "<element name=\"id\" type=\"xs:long\" minOccurs=\"0\"/>" +
                        "<element name=\"name\" type=\"xs:string\"/>" +
                     "</sequence>" +
                "</complexType>" +
           "</element>" ;


    public static String getSchema() { return _schema; }

    public static String getValidationSchema(String name) {
        return String.format(_valElement, name);
    }

    public static String adjustSchema(String specDataSchema, boolean include) {
        return YInternalTypeHelper.adjustSchema(specDataSchema, "YDocumentType",
                                               _schema, include);
    }


}