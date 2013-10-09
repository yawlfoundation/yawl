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

package org.yawlfoundation.yawl.editor.ui.specification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

public class TestSpecificationModel extends TestCase {
  
  private NetGraphModel rootNet;
  
  private InputCondition rootNetInputCondition;
  private OutputCondition rootNetOutputCondition;
  
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
    SpecificationModel.getInstance().reset();
//    setUpRootNet();
  }
  
//  private void setUpRootNet() {
//    NetGraph rootNetGraph = new NetGraph();
//    rootNetGraph.setName("StartingNet");
//    rootNetGraph.buildNewGraphContent();
//    rootNet = rootNetGraph.getNetModel();
//    rootNet.setIsRootNet(true);
//
//    SpecificationModel.getInstance().addNet(rootNet);
//
//    for(int i = 0; i < rootNet.getRootCount(); i++) {
//      if (rootNet.getRootAt(i) instanceof InputCondition) {
//        rootNetInputCondition = (InputCondition) rootNet.getRootAt(i);
//      }
//      if (rootNet.getRootAt(i) instanceof OutputCondition) {
//        rootNetOutputCondition = (OutputCondition) rootNet.getRootAt(i);
//      }
//    }
//  }
//
//  public void testNoDataTypesDefinedInitially() {
//    assertTrue(SpecificationModel.getInstance().hasValidDataTypeDefinition());
//
//    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
//    assertEquals(dataTypes.size(),0);
//  }
//
//	public void testValidSetDataTypeDefinition() {
//    final String validSchema =
//      "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" +
//      "  <complexType name=\"quote\">\n" +
//      "    <sequence>\n" +
//      "      <element name=\"saying\" type=\"string\"/>\n" +
//      "    </sequence>\n" +
//      "    <attribute name=\"quoteid\" type=\"string\"/>\n" +
//      "  </complexType>\n" +
//      "  <complexType name=\"Nerd\">\n" +
//      "    <sequence>\n" +
//      "      <element name=\"Name\" type=\"string\"/>\n" +
//      "      <element name=\"Salary\" type=\"double\"/>\n" +
//      "    </sequence>\n" +
//      "  </complexType>\n" +
//      "</schema>";
//
//    SpecificationModel.getInstance().setDataTypeDefinition(validSchema);
//
//    assertTrue(SpecificationModel.getInstance().hasValidDataTypeDefinition());
//
//    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
//    assertEquals(dataTypes.size(),2);
//
//    Iterator typeIterator = dataTypes.iterator();
//    while(typeIterator.hasNext()) {
//      String type = (String) typeIterator.next();
//      if (!type.equals("quote")) {
//        assertEquals(type, "Nerd");
//      }
//      if (!type.equals("Nerd")) {
//        assertEquals(type, "quote");
//      }
//    }
//  }
//
//  public void testInvalidSetDataTypeDefinition() {
//    final String invalidSchema =
//      "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" +
//      "  <complexType name=\"quote\">\n" +
//      "    <sequence>\n" +
//      "      <element name=\"saying\" type=\"string\"/>\n" +
//      "    </sequence>\n" +
//      "    <attribute name=\"quoteid\" type=\"string\"/>\n" +
//      "  </complexType>\n" +
//      "  <complexType name=\"Nerd\">\n" +
//      "    <sequence>\n" +
//      "      <element name=\"Name\" type=\"string\"/>\n" +
//      "      <element name=\"Salary\" type=\"double\"/>\n" +
//      "    </sequence>\n" +
//      "  </complexType>\n" +
//      "</crapolla!>";
//
//    SpecificationModel.getInstance().setDataTypeDefinition(invalidSchema);
//
//    assertFalse(SpecificationModel.getInstance().hasValidDataTypeDefinition());
//
//    Set dataTypes = SpecificationModel.getInstance().getDataTypes();
//    assertNull(dataTypes);
//  }
//
//
//  public void testQueryRenaming() {
//    rootNet.getDecomposition().addVariable(
//        new DataVariable(
//            "booleanSwitch",
//            "boolean",
//            "",
//            DataVariable.USAGE_INPUT_AND_OUTPUT
//        )
//    );
//
//    AtomicTask initialAtomicTask = NetCellFactory.insertAtomicTask(
//        rootNet.getGraph(),
//        new Point(10,30)
//    );
//
//    LinkedList<DataVariable> paramList = new LinkedList<DataVariable>();
//    paramList.add(
//        rootNet.getDecomposition().getVariableWithName(
//            "booleanSwitch"
//        )
//    );
//
//    NetCellUtilities.creatDirectTransferDecompAndParams(
//        rootNet.getGraph(),
//        initialAtomicTask,
//        "initialTaskDecomposition",
//        paramList,
//        paramList
//    );
//
//    assertTrue(
//        initialAtomicTask.getDecomposition().getText().equals(
//            "initialTaskDecomposition"
//        )
//    );
//
//    assertTrue(
//        initialAtomicTask.getDecomposition().getVariableWithName("booleanSwitch") != null
//    );
//
//    rootNet.setSplitDecorator(
//        initialAtomicTask,
//        Decorator.XOR_TYPE,
//        YAWLTask.RIGHT
//    );
//
//    AtomicTask highRoadAtomicTask = NetCellFactory.insertAtomicTask(
//        rootNet.getGraph(),
//        new Point(10,30)
//    );
//
//    AtomicTask lowRoadAtomicTask = NetCellFactory.insertAtomicTask(
//        rootNet.getGraph(),
//        new Point(30,30)
//    );
//
//    rootNet.getGraph().connect(
//        rootNetInputCondition.getDefaultSourcePort(),
//        initialAtomicTask.getDefaultTargetPort()
//    );
//
//    rootNet.getGraph().connect(
//        initialAtomicTask.getSplitDecorator().getDefaultPort(),
//        highRoadAtomicTask.getDefaultTargetPort()
//    );
//
//    rootNet.getGraph().connect(
//        initialAtomicTask.getSplitDecorator().getDefaultPort(),
//        lowRoadAtomicTask.getDefaultTargetPort()
//    );
//
//    rootNet.getGraph().connect(
//        highRoadAtomicTask.getDefaultSourcePort(),
//        rootNetOutputCondition.getDefaultTargetPort()
//    );
//
//    rootNet.getGraph().connect(
//        lowRoadAtomicTask.getDefaultSourcePort(),
//        rootNetOutputCondition.getDefaultTargetPort()
//    );
//
//    // establish predicates on flows. high road task taken when variable
//    // content is true, low road task taken otherwise.
//
//    for(Object flowAsObject:  initialAtomicTask.getSplitDecorator().getFlows()) {
//      YAWLFlowRelation flow = (YAWLFlowRelation) flowAsObject;
//      if (flow.getTargetVertex() == highRoadAtomicTask) {
//        flow.setPredicate(
//            "boolean(" +
//            XMLUtilities.getXPathPredicateExpression(
//                rootNet.getDecomposition().getVariableWithName("booleanSwitch")
//            ) + ") eq true()"
//        );
//      }
//      if (flow.getTargetVertex() == lowRoadAtomicTask) {
//        flow.setPredicate(
//            "boolean(" +
//            XMLUtilities.getXPathPredicateExpression(
//                rootNet.getDecomposition().getVariableWithName("booleanSwitch")
//            ) + ") eq false()"
//        );
//      }
//    }
//
//    rootNet.getDecomposition().setLabel("StartingNetRenamed");
//
//    SpecificationModel.getInstance().changeDecompositionInQueries(
//        "StartingNet", rootNet.getDecomposition().getText()
//    );
//
//    assertTrue(
//      rootNet.getDecomposition().getText().equals("StartingNetRenamed")
//    );
//
//    // Cecking that task parameter queries were updated correctly with net name change.
//
////    assertTrue(
////        initialAtomicTask.getParameterLists().getInputParameters().getQueryAt(0).equals(
////            "/StartingNetRenamed/booleanSwitch/text()"
////        )
////    );
//
////    assertTrue(
////        initialAtomicTask.getParameterLists().getOutputParameters().getQueryAt(0).equals(
////            "/initialTaskDecomposition/booleanSwitch/text()"
////        )
////    );
//
//    // Cecking that flow predicate queries were updated correctly with net name change.
//
//    for(Object flowAsObject:  initialAtomicTask.getSplitDecorator().getFlows()) {
//      YAWLFlowRelation flow = (YAWLFlowRelation) flowAsObject;
//      if (flow.getTargetVertex() == highRoadAtomicTask) {
//        assertTrue(
//            flow.getPredicate().equals(
//                "boolean(/StartingNetRenamed/booleanSwitch/text()) eq true()"
//            )
//        );
//      }
//      if (flow.getTargetVertex() == lowRoadAtomicTask) {
//        assertTrue(
//            flow.getPredicate().equals(
//                "boolean(/StartingNetRenamed/booleanSwitch/text()) eq false()"
//            )
//        );
//      }
//    }
//
//    DataVariable theNetVariable = rootNet.getDecomposition().getVariableWithName(
//        "booleanSwitch"
//    );
//
//    theNetVariable.setName(
//        "renamedBooleanSwitch"
//    );
//
//    SpecificationModel.getInstance().changeVariableNameInQueries(
//        theNetVariable,
//        "booleanSwitch",
//        "renamedBooleanSwitch"
//    );
//
//    // Cecking that task parameter queries were updated correctly with net var name change.
//
////    assertTrue(
////        initialAtomicTask.getParameterLists().getInputParameters().getQueryAt(0).equals(
////            "/StartingNetRenamed/renamedBooleanSwitch/text()"
////        )
////    );
////
////    assertTrue(
////        initialAtomicTask.getParameterLists().getOutputParameters().getQueryAt(0).equals(
////            "/initialTaskDecomposition/booleanSwitch/text()"
////        )
////    );
//
//    // Cecking that flow predicate queries were updated correctly with net var name change.
//
//    for(Object flowAsObject:  initialAtomicTask.getSplitDecorator().getFlows()) {
//      YAWLFlowRelation flow = (YAWLFlowRelation) flowAsObject;
//      if (flow.getTargetVertex() == highRoadAtomicTask) {
//        assertTrue(
//            flow.getPredicate().equals(
//                "boolean(/StartingNetRenamed/renamedBooleanSwitch/text()) eq true()"
//            )
//        );
//      }
//      if (flow.getTargetVertex() == lowRoadAtomicTask) {
//        assertTrue(
//            flow.getPredicate().equals(
//                "boolean(/StartingNetRenamed/renamedBooleanSwitch/text()) eq false()"
//            )
//        );
//      }
//    }
//
//  }
//  // TODO : Expand out to cover more of SpecificationModel's interface.
}
