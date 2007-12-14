/*
 * Created on 01/04/2003
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

package org.yawlfoundation.yawl.editor.swing.data;

import junit.framework.*;

public class TestJXMLSchemaEditorPane extends TestCase {
  JXMLSchemaEditorPane testDefinitionPane;
  
  public TestJXMLSchemaEditorPane(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(TestJXMLSchemaEditorPane.class);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }
  
  protected void setUp() {
    testDefinitionPane = new JXMLSchemaEditorPane();
  }

	public void testEmptyPane() {
    assertTrue(testDefinitionPane.isContentValid());
	}

	public void testValidSchema() {
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
      
    testDefinitionPane.setText(validSchema);
    assertTrue(testDefinitionPane.isContentValid());
	}

  public void testInvalidSchema() {
    final String invalidSchema = 
      "<complexType name=\"BrokenNerd\">\n" +
      "</complexT>";
    
    testDefinitionPane.setText(invalidSchema);
    assertFalse(testDefinitionPane.isContentValid());
  }

  public void testHTMLisInvalid() {
    final String html = 
      "<html><body>This isn't XML Schema!  *phhht* </body></html>";
    testDefinitionPane.setText(html);
    assertFalse(testDefinitionPane.isContentValid());
  }
}
