package org.yawlfoundation.yawl.schema;

import junit.framework.TestCase;

/**
 * @author Mike Fowler
 *         Date: 04-Jul-2006
 */
public class TestSchemaHandlerValidation extends TestCase
{
    private static final String SCHEMA = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                                         "\n" +
                                         "  <xsd:element name=\"person\" type=\"typePerson\"/>\n" +
                                         "\n" +
                                         "  <xsd:complexType name=\"typePerson\">\n" +
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
                                         "      <xsd:element name=\"comments\" minOccurs=\"0\">\n" +
                                         "        <xsd:complexType>\n" +
                                         "          <xsd:sequence minOccurs=\"0\" maxOccurs=\"unbounded\">\n" +
                                         "            <xsd:any processContents=\"skip\"/>\n" +
                                         "          </xsd:sequence>\n" +
                                         "        </xsd:complexType>\n" +
                                         "      </xsd:element>\n" +
                                         "    </xsd:sequence>\n" +
                                         "  </xsd:complexType>\n" +
                                         "\n" +
                                         "  <xsd:simpleType name=\"title\">\n" +
                                         "    <xsd:restriction base=\"xsd:string\">\n" +
                                         "      <xsd:enumeration value=\"Mr.\"/>\n" +
                                         "      <xsd:enumeration value=\"Mrs.\"/>\n" +
                                         "      <xsd:enumeration value=\"Ms.\"/>\n" +
                                         "      <xsd:enumeration value=\"Miss.\"/>\n" +
                                         "    </xsd:restriction>\n" +
                                         "  </xsd:simpleType>\n" +
                                         "</xsd:schema>";

    private SchemaHandler handler;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new SchemaHandler(SCHEMA);
        assertTrue(handler.compileSchema());
    }

    public void testSimpleInstance() throws Exception
    {
        String xml = "<person>\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "</person>";
        assertTrue(handler.validate(xml));
    }

    public void testInvlaidSimpleInstance() throws Exception
    {
        String xml = "<person>\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>M</forename>\n" +
                     "  <surname>F</surname>\n" +
                     "</person>";
        assertFalse(handler.validate(xml));
    }

    public void testXMLSchemaInstance() throws Exception
    {
        String xml = "<entity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"typePerson\">\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "</entity>";
        assertTrue(handler.validate(xml));
    }

    public void testIncorrectXMLSchemaInstance() throws Exception
    {
        String xml = "<entity xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"typeBadPerson\">\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "</entity>";
        assertFalse(handler.validate(xml));
    }

    public void testXSDAny() throws Exception
    {
        String xml = "<person>\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "  <comments>\n" +
                     "    <dob>30-05-1981</dob>\n" +
                     "    <birthplace>\n" +
                     "      <city>Calgary</city>\n" +
                     "      <province>Alberta</province>\n" +
                     "      <country>Canada</country>\n" +
                     "    </birthplace>\n" +
                     "  </comments>\n" +
                     "</person>";
        assertTrue(handler.validate(xml));
    }

    public void testMalformedXSDAny() throws Exception
    {
        String xml = "<person>\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "  <comments>\n" +
                     "    <dob>30-05-1981</dob>\n" +
                     "    <birthplace>\n" +
                     "      <city>Calgary\n" +
                     "      <province>Alberta</province>\n" +
                     "      <country>Canada</country>\n" +
                     "    </birthplace>\n" +
                     "  </comments>\n" +
                     "</person>";
        assertFalse(handler.validate(xml));
    }

    public void testValidationBeforeCompilation() throws Exception
    {
        SchemaHandler handler = new SchemaHandler(SCHEMA);

        try
        {
            String xml = "<person>\n" +
                     "  <title>Mr.</title>\n" +
                     "  <forename>Mike</forename>\n" +
                     "  <surname>Fowler</surname>\n" +
                     "</person>";
            handler.validate(xml);
            fail("Handler allowed parse without schema compilation.");
        }
        catch (IllegalStateException e)
        {
            //this is what we wanted.
        }
        catch (Exception e)
        {
            fail("Hanler throw unexpection exception :" + e.getMessage());
        }
    }
}
