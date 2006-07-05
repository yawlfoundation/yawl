/*
 * Created on 15/09/2003
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

package au.edu.qut.yawl.editor.specification;

import junit.framework.*;
import java.util.Set;
import java.util.Iterator;

public class TestSpecificationModel extends TestCase {
  
  public TestSpecificationModel(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(TestSpecificationModel.class);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }
  
  protected void setUp() {
     // dunno
  }
  
  public void testNoDataTypesDefinedInitially() {
    assertTrue(SpecificationModel.getInstance().hasValidDataTypeDefinition());

    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
    assertEquals(dataTypes.size(),0);
  }

	public void testValidSetDataTypeDefinition() {
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
      "      <element name=\"Name\" type=\"string\"/>\n" +
      "      <element name=\"Salary\" type=\"double\"/>\n" +
      "    </sequence>\n" +
      "  </complexType>\n" +
      "</schema>";

    SpecificationModel.getInstance().setDataTypeDefinition(validSchema);

    assertTrue(SpecificationModel.getInstance().hasValidDataTypeDefinition());
   
    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
    assertEquals(dataTypes.size(),2);

    Iterator typeIterator = dataTypes.iterator();
    while(typeIterator.hasNext()) {
      String type = (String) typeIterator.next();
      if (!type.equals("quote")) {
        assertEquals(type, "Nerd");
      }
      if (!type.equals("Nerd")) {
        assertEquals(type, "quote");
      }
    }
	}

  public void testInvalidSetDataTypeDefinition() {
    final String invalidSchema = 
      "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" + 
      "  <complexType name=\"quote\">\n" + 
      "    <sequence>\n" +
      "      <element name=\"saying\" type=\"string\"/>\n" +
      "    </sequence>\n" +
      "    <attribute name=\"quoteid\" type=\"string\"/>\n" +
      "  </complexType>\n" +
      "  <complexType name=\"Nerd\">\n" +
      "    <sequence>\n" +
      "      <element name=\"Name\" type=\"string\"/>\n" +
      "      <element name=\"Salary\" type=\"double\"/>\n" +
      "    </sequence>\n" +
      "  </complexType>\n" +
      "</crapolla!>";

    SpecificationModel.getInstance().setDataTypeDefinition(invalidSchema);

    assertFalse(SpecificationModel.getInstance().hasValidDataTypeDefinition());
   
    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
    assertNull(dataTypes);
  }
  
  // TODO : Expand out to cover more of SpecificationModel's interface.
}
