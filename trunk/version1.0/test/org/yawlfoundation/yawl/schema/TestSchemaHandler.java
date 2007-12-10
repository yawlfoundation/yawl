package org.yawlfoundation.yawl.schema;

import junit.framework.TestCase;

/**
 * @author Mike Fowler
 *         Date: 04-Jul-2006
 */
public class TestSchemaHandler extends TestCase
{
    public void testSimpleValidSchema() throws Exception
    {
        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                        "  <xsd:simpleType name=\"title\">\n" +
                        "    <xsd:restriction base=\"xsd:string\">\n" +
                        "      <xsd:enumeration value=\"Mr.\"/>\n" +
                        "      <xsd:enumeration value=\"Mrs.\"/>\n" +
                        "      <xsd:enumeration value=\"Ms.\"/>\n" +
                        "      <xsd:enumeration value=\"Miss.\"/>\n" +
                        "    </xsd:restriction>\n" +
                        "  </xsd:simpleType>\n" +
                        "</xsd:schema>";
        SchemaHandler handler = new SchemaHandler(schema);
        assertTrue(handler.compileSchema());
    }

    public void testComplexValidSchema() throws Exception
    {
        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                        "  <xsd:complexType name=\"person\">\n" +
                        "    <xsd:sequence>\n" +
                        "      <xsd:annotation>\n" +
                        "        <xsd:documentation>This type describes a person.</xsd:documentation>\n" +
                        "      </xsd:annotation>\n" +
                        "      <xsd:element name=\"title\" type=\"title\"/>\n" +
                        "      <xsd:element name=\"forename\">\n" +
                        "        <xsd:simpleType>\n" +
                        "          <xsd:restriction base=\"xsd:string\">\n" +
                        "            <xsd:minLength value=\"2\"/>\n" +
                        "          </xsd:restriction>\n" +
                        "        </xsd:simpleType>\n" +
                        "      </xsd:element>\n" +
                        "      <xsd:element name=\"surname\" type=\"xsd:string\"/>\n" +
                        "    </xsd:sequence>\n" +
                        "  </xsd:complexType>\n" +
                        "  \n" +
                        "  <xsd:simpleType name=\"title\">\n" +
                        "    <xsd:restriction base=\"xsd:string\">\n" +
                        "      <xsd:enumeration value=\"Mr.\"/>\n" +
                        "      <xsd:enumeration value=\"Mrs.\"/>\n" +
                        "      <xsd:enumeration value=\"Ms.\"/>\n" +
                        "      <xsd:enumeration value=\"Miss.\"/>\n" +
                        "    </xsd:restriction>\n" +
                        "  </xsd:simpleType>\n" +
                        "</xsd:schema>";
        SchemaHandler handler = new SchemaHandler(schema);
        assertTrue(handler.compileSchema());
    }

    public void testSimpleInvalidSchema() throws Exception
    {
        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                        "  <xsd:simpleType name=\"title\">\n" +
                        "    <xsd:restriction base=\"xsd:string\">\n" +
                        "      <xsd:restriction value=\"Mr.\"/>\n" +
                        "      <xsd:restriction value=\"Mrs.\"/>\n" +
                        "      <xsd:restriction value=\"Ms.\"/>\n" +
                        "      <xsd:restriction value=\"Miss.\"/>\n" +
                        "    </xsd:restriction>\n" +
                        "  </xsd:simpleType>\n" +
                        "</xsd:schema>";
        SchemaHandler handler = new SchemaHandler(schema);
        assertFalse(handler.compileSchema());
    }

    public void testComplexInvalidSchema() throws Exception
    {
        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                        "  <xsd:complexType name=\"person\">\n" +
                        "    <xsd:sequence>\n" +
                        "      <xsd:annotation>\n" +
                        "        <xsd:documentation>This type describes a person.</xsd:documentation>\n" +
                        "      </xsd:annotation>\n" +
                        "      <xsd:element name=\"title\" type=\"title\"/>\n" +
                        "      <xsd:element name=\"forename\">\n" +
                        "        <xsd:simpleType>\n" +
                        "          <xsd:restriction base=\"xsd:string\">\n" +
                        "            <xsd:minLength value=\"2\"/>\n" +
                        "          </xsd:restriction>\n" +
                        "        </xsd:simpleType>\n" +
                        "      </xsd:element>\n" +
                        "      <xsd:element name=\"surname\" type=\"xsd:string\"/>\n" +
                        "    </xsd:sequence>\n" +
                        "  </xsd:complexType>\n" +
                        "</xsd:schema>";
        SchemaHandler handler = new SchemaHandler(schema);
        assertFalse(handler.compileSchema());
    }

    public void testMalformedSchema() throws Exception
    {
        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                        "  <xsd:complexType name=\"person\">\n" +
                        "    <xsd:sequence>\n" +
                        "      <xsd:annotation>\n" +
                        "        <xsd:documentation>This type describes a person.</xsd:documentation>\n" +
                        "      </xsd:annotation>\n";
        SchemaHandler handler = new SchemaHandler(schema);
        assertFalse(handler.compileSchema());
    }
}
