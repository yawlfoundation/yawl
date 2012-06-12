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
import org.yawlfoundation.yawl.editor.ui.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

public class TestDataSchemaValidator extends TestCase {

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
      validator.setDataTypeSchema(SpecificationModel.DEFAULT_TYPE_DEFINITION);
  }
  
  
  public void testValidateSimpleSchemaForStringBaseType() {
    assertEquals(
        "",
        validator.validateBaseDataTypeInstance(
            "<element name=\"testString\" type=\"string\"/>", 
            "<testString>I am a test string</testString>")
    );
    
    //TODO: Is there a negative test for strings?
  }

  public void testValidateSimpleSchemaForLongBaseType() {
    assertEquals(
        "",
        validator.validateBaseDataTypeInstance(
            "<element name=\"testLong\" type=\"long\"/>", 
            "<testLong>1234</testLong>")
    );
    
    assertFalse(
        validator.validateBaseDataTypeInstance(
            "<element name=\"testLong\" type=\"long\"/>", 
            "<testLong>this is not a long</testLong>").equals("")
    );
  }

  public void testValidateSimpleSchemaForBooleanBaseType() {
    assertEquals(
        "",
        validator.validateBaseDataTypeInstance(
            "<element name=\"testBoolean\" type=\"boolean\"/>", 
            "<testBoolean>true</testBoolean>")
    );

    assertEquals(
        "",
        validator.validateBaseDataTypeInstance(
            "<element name=\"testBoolean\" type=\"boolean\"/>", 
            "<testBoolean>false</testBoolean>")
    );
    
    assertFalse(
        validator.validateBaseDataTypeInstance(
            "<element name=\"testBoolean\" type=\"boolean\"/>", 
            "<testBoolean>this is not a boolean</testBoolean>").equals("")
    );
  }

  public void testValidateSimpleSchemaForDoubleBaseType() {
    assertEquals(
        "",
        validator.validateBaseDataTypeInstance(
            "<element name=\"testDouble\" type=\"double\"/>", 
            "<testDouble>1.234</testDouble>")
    );

    assertFalse(
        validator.validateBaseDataTypeInstance(
            "<element name=\"testDouble\" type=\"double\"/>", 
            "<testDouble>this is not a double</testDouble>").equals("")
    );
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
    SpecificationModel.getInstance().setDataTypeDefinition(
      "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">" +
      "  <xs:simpleType name=\"TestEnumeration\">"  +
      "    <xs:restriction base=\"xs:string\">" +
      "      <xs:enumeration value=\"OK\"/>" +
      "      <xs:enumeration value=\"NOK\"/>" +
      "    </xs:restriction>" +   
      "  </xs:simpleType>" +
      "</xs:schema>"         
    );
    
    assertEquals(
        "",
        validator.validateUserSuppliedDataTypeInstance(
            "testElement",
            "TestEnumeration", 
            "OK")
    );
    
    assertFalse(
        validator.validateUserSuppliedDataTypeInstance(
            "testElement", 
            "TestEnumeration",
            "KO").equals("")
    );
  }
  
  public void testValidateComplexSchema() {
    SpecificationModel.getInstance().setDataTypeDefinition(
        "<schema xmlns='http://www.w3.org/2001/XMLSchema'>" +
        "  <complexType name='PersonList'>" +
        "    <sequence>" +
        "      <element name='PersonName' type='string' maxOccurs='unbounded'/>" +
        "    </sequence>" +
        "  </complexType>" +
        "</schema>"
    );

    assertEquals(
        "",
        validator.validateUserSuppliedDataTypeInstance(
            "testList",
            "PersonList",
            "<PersonName>Linds</PersonName>")
    );

    assertFalse(
        validator.validateUserSuppliedDataTypeInstance(
            "testList", 
            "PersonList",
            "<NonPersonName>crap</NonPersonName>").equals("")
    );
  }
}
