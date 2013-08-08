/*
 * Created on 07/01/2005
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

public class TestDataSchemaValidator extends TestCase {

    private static final String SCHEMA_HEADER =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">";
    private static final String SCHEMA_CLOSER = "</schema>";


    DataSchemaValidator validator;

  public TestDataSchemaValidator(String pName) {
    super(pName);
  }

  public static Test suite() {
    return new TestSuite(TestDataSchemaValidator.class);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }
  
  protected void setUp() {
      SpecificationModel.getInstance().reset();
      validator = SpecificationModel.getInstance().getSchemaValidator();
      validator.setDataTypeSchema(YSpecificationHandler.DEFAULT_TYPE_DEFINITION);
  }

    protected void setSchema(String innards) {
        validator.setDataTypeSchema(SCHEMA_HEADER + innards + SCHEMA_CLOSER);
    }
  
  public void testValidateSimpleSchemaForStringBaseType() {
      setSchema("<element name=\"testString\" type=\"string\"/>");
    assertEquals("", validator.validate("<testString>I am a test string</testString>"));

      //TODO: Is there a negative test for strings?
  }

  public void testValidateSimpleSchemaForLongBaseType() {
      setSchema("<element name=\"testLong\" type=\"long\"/>");
    assertEquals("", validator.validate("<testLong>1234</testLong>"));

       assertFalse(
        validator.validate("<testLong>this is not a long</testLong>").equals(""));
  }

  public void testValidateSimpleSchemaForBooleanBaseType() {
      setSchema("<element name=\"testBoolean\" type=\"boolean\"/>");
    assertEquals("", validator.validate("<testBoolean>true</testBoolean>"));

    assertEquals("", validator.validate("<testBoolean>false</testBoolean>"));
    
    assertFalse(validator.validate(
            "<testBoolean>this is not a boolean</testBoolean>").equals(""));
  }

  public void testValidateSimpleSchemaForDoubleBaseType() {
      setSchema("<element name=\"testDouble\" type=\"double\"/>");
    assertEquals("", validator.validate("<testDouble>1.234</testDouble>"));

    assertFalse(validator.validate(
            "<testDouble>this is not a double</testDouble>").equals(""));
  }

  public void testValidateSimpleSchemaForDateBaseType() {
    //TODO: Date format positive and negative tests needed.
    
    assertTrue(true);
  }
  
  public void testValidateSimpleSchemaForTimeBaseType() {
    //TODO: Time format positive and negative tests needed.
    
    assertTrue(true);
  }

  public void testValidateSimpleSchemaForEnumeratedStringVariable() {
//    SpecificationModel.getInstance().setDataTypeDefinition(
//      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">" +
//      "  <xs:simpleType name=\"TestEnumeration\">"  +
//      "    <xs:restriction base=\"xs:string\">" +
//      "      <xs:enumeration value=\"OK\"/>" +
//      "      <xs:enumeration value=\"NOK\"/>" +
//      "    </xs:restriction>" +
//      "  </xs:simpleType>" +
//      "  <xs:element name=\"testElement\" type=\"TestEnumeration\"/>" +
//      "</xs:schema>"
//    );
    
    assertEquals(
        "",
        validator.validate("testElement", "OK"));
    
    assertFalse(
        validator.validate("testElement", "KO").equals(""));
  }
  
  public void testValidateComplexSchema() {
//    SpecificationModel.getInstance().setDataTypeDefinition(
//        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">" +
//        "  <xs:complexType name='PersonList'>" +
//        "    <xs:sequence>" +
//        "      <xs:element name='PersonName' type='xs:string' maxOccurs='unbounded'/>" +
//        "    </xs:sequence>" +
//        "  </xs:complexType>" +
//        "  <xs:element name=\"testList\" type=\"PersonList\"/>" +
//        "</xs:schema>"
//    );

    assertEquals(
        "",
        validator.validate("testList", "<PersonName>Linds</PersonName>"));

    assertFalse(
        validator.validate("testList", "<NonPersonName>crap</NonPersonName>").equals(""));
  }
}
