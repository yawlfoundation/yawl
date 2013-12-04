/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.swing;

import junit.framework.*;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XMLSchemaEditorPane;

public class TestJXMLSchemaEditorPane extends TestCase {
  XMLSchemaEditorPane testDefinitionPane;
  
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
    testDefinitionPane = new XMLSchemaEditorPane();
  }

	public void testEmptyPane() {
    assertFalse(testDefinitionPane.isContentValid());
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
