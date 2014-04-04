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

package org.yawlfoundation.yawl.editor.ui;

import junit.framework.*;
import org.yawlfoundation.yawl.editor.ui.specification.validation.TestDataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.specification.validation.TestSpecificationEngineHandler;
import org.yawlfoundation.yawl.editor.ui.swing.TestJXMLSchemaEditorPane;

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

    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.net.TestNetElementSummary.suite());
    parentSuite.addTest(org.yawlfoundation.yawl.editor.ui.specification.TestSpecificationModel.suite());

    parentSuite.addTest(TestJXMLSchemaEditorPane.suite());
  }

  private static void addMacroSuite(TestSuite parentSuite) {
    parentSuite.addTest(TestSpecificationEngineHandler.suite());
    parentSuite.addTest(TestDataSchemaValidator.suite());
  }
}
