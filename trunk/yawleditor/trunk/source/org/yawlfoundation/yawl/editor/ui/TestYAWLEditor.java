/*
 * Created on 01/04/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui;

import junit.framework.*;
import org.yawlfoundation.yawl.editor.ui.engine.TestDataSchemaValidator;

public class TestYAWLEditor extends TestCase {

  public TestYAWLEditor(String pName) {
    super(pName);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite fullSuite = new TestSuite("All YAWLEditor Tests");

    addMicroSuite(fullSuite);

    //The Engine handler exercises the most code per test. Keep it as the last thing to do.
    addMacroSuite(fullSuite);

    return fullSuite;
  }

  private static void addMicroSuite(TestSuite parentSuite) {
    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.util.TestXMLUtilities.suite());
    
    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.net.TestNetElementSummary.suite());
    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.specification.TestSpecificationModel.suite());

    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.swing.data.TestJXMLSchemaEditorPane.suite());
  }

  private static void addMacroSuite(TestSuite parentSuite) {
    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.engine.TestSpecificationEngineHandler.suite());
    parentSuite.addTest(TestDataSchemaValidator.suite());
  }
}
