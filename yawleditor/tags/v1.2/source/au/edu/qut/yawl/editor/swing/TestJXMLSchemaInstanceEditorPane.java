/*
 * Created on 01/04/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.swing;

import junit.framework.*;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class TestJXMLSchemaInstanceEditorPane extends TestCase {
  JXMLSchemaInstanceEditorPane testSimpleInstancePane;
  JXMLSchemaInstanceEditorPane testComplexInstancePane;
  
  private String validComplexTypeName;
  
  public TestJXMLSchemaInstanceEditorPane(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(TestJXMLSchemaInstanceEditorPane.class);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }
  
  protected void setUp() {
    setupSchema();
    setupSimpleInstancePane();
    setupComplexInstancePane();
  }
  
  private void setupSchema() {
    final String validSchema = 
      "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" + 
      "  <complexType name=\"quote\">\n" + 
      "    <sequence>\n" +
      "      <element name=\"saying\" type=\"string\"/>\n" +
      "    </sequence>\n" +
      "    <attribute name=\"quoteid\" type=\"string\"/>\n" +
      "  </complexType>\n" +
      "  <complexType name=\"Nerd\">\n" +
      "    <sequence>\n" +
      "      <element name=\"Name\"  type=\"string\"/>\n" +
      "      <element name=\"Salary\" type=\"double\"/>\n" +
      "    </sequence>\n" +
      "  </complexType>\n" +
      "</schema>";
    
    SpecificationModel.getInstance().setDataTypeDefinition(validSchema);
  }
  
  private void setupSimpleInstancePane() {
    testSimpleInstancePane = new JXMLSchemaInstanceEditorPane();
  }

  private void setupComplexInstancePane() {
    testComplexInstancePane = new JXMLSchemaInstanceEditorPane();
    validComplexTypeName = "Nerd";
  }

	public void testEmptyPanes() {
    assertTrue(testSimpleInstancePane.isValid());
    assertTrue(testComplexInstancePane.isValid());
  }

  public void testHTMLisInvalid() {
    
    final String html = 
      "<html><body>This isn't XML Schema!  *phhht* </body></html>";

    testSimpleInstancePane.setVariableName("htmlVariable");
    testSimpleInstancePane.setVariableType("string");
    
    testSimpleInstancePane.setText(html);
    assertFalse(testSimpleInstancePane.isValid());
    
    testComplexInstancePane.setVariableName("LindsNerd");
    testComplexInstancePane.setVariableType(validComplexTypeName);
    
    testComplexInstancePane.setText(html);
    assertFalse(testComplexInstancePane.isValid());
  }

  public void testValidSimpleTypeInstance() {
    testSimpleInstancePane.setVariableName("validDouble");
    testSimpleInstancePane.setVariableType("double");

    final String validInstance = "12.5"; 
    
    testSimpleInstancePane.setText(validInstance);
    assertTrue(testSimpleInstancePane.isValid());
  }

  public void testInvalidSimpleTypeInstance() {
    testSimpleInstancePane.setVariableName("invalidDouble");
    testSimpleInstancePane.setVariableType("double");

    final String invalidInstance = "text isn't a number\n";
    
    testSimpleInstancePane.setText(invalidInstance);
    assertFalse(testSimpleInstancePane.isValid());
  }

  public void testValidComplexTypeInstance() {

    testComplexInstancePane.setVariableName("LindsNerd");
    testComplexInstancePane.setVariableType(validComplexTypeName);
    
    final String validInstance = 
      "<LindsNerd>\n" +
      "  <Name>Linds</Name>\n" +
      "  <Salary>12.05</Salary>\n" +
      "</LindsNerd>";
    
    testComplexInstancePane.setText(validInstance);
    assertTrue(testComplexInstancePane.isValid());
  }

  public void testInvalidComplexTypeInstance() {

    testComplexInstancePane.setVariableName("LindsNerd");
    testComplexInstancePane.setVariableType(validComplexTypeName);
    
    final String invalidInstance = 
      "<LindsNerd>\n" +
      "  <Name>Linds</Name>\n" +
      "  <Salary>text isn't a number</Salary>\n" +
      "</LindsNerd>";

    testComplexInstancePane.setText(invalidInstance);
    assertFalse(testComplexInstancePane.isValid());
  }
}
