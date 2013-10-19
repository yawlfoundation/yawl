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

package org.yawlfoundation.yawl.editor.ui.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

public class TestSpecificationEngineHandler extends TestCase {
  private NetGraphModel rootNet;
  
  private InputCondition rootNetInputCondition;
  private OutputCondition rootNetOutputCondition;
  
  public TestSpecificationEngineHandler(String pName) {
    super(pName);
  }

  public static Test suite() {
    return new TestSuite(TestSpecificationEngineHandler.class);
  }
  
  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }
  
//  protected void setUp() {
//    SpecificationModel.getInstance().reset();
//    setUpRootNet();
//  }
//
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
//  public void testInitialRootNet() {
//    // Input and Output conditions exist, but nothing else. Should be invald.
//    assertFalse(specificationIsValid());
//  }
//
//  //////////////////////////  Atomic Task Tests /////////////////////////////
//
//  private AtomicTask createFirstAtomicTask() {
//    return NetCellFactory.insertAtomicTask(
//        rootNet.getGraph(),
//        new Point(10,10)
//    );
//  }
//
//  public void testAtomicTaskPlacement() {
//    createFirstAtomicTask();
//    assertFalse(specificationIsValid());
//  }
//
//  private AtomicTask createFirstFlowEmbeddedAtomicTask() {
//    AtomicTask task = createFirstAtomicTask();
//
//    rootNet.getGraph().connect(
//        rootNetInputCondition.getPortAt(YAWLVertex.RIGHT),
//        task.getPortAt(YAWLVertex.LEFT)
//    );
//
//    rootNet.getGraph().connect(
//        task.getPortAt(YAWLVertex.RIGHT),
//        rootNetOutputCondition.getPortAt(YAWLVertex.LEFT)
//    );
//    return task;
//  }
//
//  public void testFlowEmbeddedAtomicTask() {
//    createFirstFlowEmbeddedAtomicTask();
//    assertFalse(specificationIsValid());
//  }
//
//  private YDecomposition createFirstTaskDecomposition() {
//    YDecomposition firstDecomposition = new YAWLServiceGateway("First",
//            SpecificationModel.getHandler().getSpecification());
//    SpecificationModel.getInstance().addWebServiceDecomposition(firstDecomposition);
//    return firstDecomposition;
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTask() {
//    AtomicTask task = createFirstFlowEmbeddedAtomicTask();
//    task.setNet(createFirstTaskDecomposition());
//    return task;
//  }
//
//  public void testFlowEmbeddedDecomposedAtomicTask() {
//    createFirstFlowEmbeddedDecomposedAtomicTask();
//    assertTrue(specificationIsValid());
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithVariable() {
//    addInOutVariable001ToRootNet();
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTask();
//      SpecificationModel.getHandler().getDataHandler().addVariable(
//              task.getDecomposition().getID(),
//            "taskVar001",
//            "string",
//            "",
//            YDataHandler.INPUT_OUTPUT, null
//        )
//    );
//
//    return task;
//  }
//
//  private void addInOutVariable001ToRootNet() {
//    rootNet.getDecomposition().addVariable(
//        new DataVariable(
//            "netVar001",
//            "string",
//            "this gives me the heebie-jeebies",
//            DataVariable.USAGE_INPUT_AND_OUTPUT
//        )
//    );
//  }
//
//  public void testFlowEmbeddedAtomicTaskWithInOutVariable() {
//    createFirstFlowEmbeddedDecomposedAtomicTaskWithVariable();
//    assertFalse(specificationIsValid());
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithParameters() {
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTaskWithVariable();
//
//    task.getParameterLists().getInputParameters().addParameterPair(
//        task.getDecomposition().getVariableWithName("taskVar001"),
//        "/StartingNet/netVar001/text()"
//    );
//    task.getParameterLists().getOutputParameters().addParameterPair(
//        rootNet.getDecomposition().getVariableWithName("netVar001"),
//        "/Firt/taskVar001/text()"
//    );
//
//    return task;
//  }
//
//  public void testFlowEmbeddedAtomicTaskPassingParameters() {
//    createFirstFlowEmbeddedDecomposedAtomicTaskWithParameters();
//    assertTrue(specificationIsValid());
//  }
//
//  private void addIn002AndOut003VariablesToRootNet() {
//    rootNet.getDecomposition().addVariable(
//        new DataVariable(
//            "netVarIn002",
//            "string",
//            "this gives me the heebie-jeebies",
//            DataVariable.USAGE_INPUT_ONLY
//        )
//    );
//    rootNet.getDecomposition().addVariable(
//        new DataVariable(
//            "netVarOut003",
//            "string",
//            "this also gives me the heebie-jeebies",
//            DataVariable.USAGE_OUTPUT_ONLY
//        )
//    );
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithParametersExtraNetVars() {
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTaskWithParameters();
//    addIn002AndOut003VariablesToRootNet();
//    return task;
//  }
//
//  public void testFlowEmbeddedAtomicTaskPassingParametersUnusedNetVariables() {
//    createFirstFlowEmbeddedDecomposedAtomicTaskWithParametersExtraNetVars();
//    assertTrue(specificationIsValid());
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithExtraTaskVars() {
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTaskWithParametersExtraNetVars();
//
//    task.getDecomposition().addVariable(
//        new DataVariable(
//            "taskVarIn002",
//            "string",
//            "this really also gives me the heebie-jeebies",
//            DataVariable.USAGE_INPUT_ONLY
//        )
//    );
//    task.getDecomposition().addVariable(
//        new DataVariable(
//            "taskVarOut003",
//            "string",
//            "this gives me the uber-heebie-jeebies",
//            DataVariable.USAGE_OUTPUT_ONLY
//        )
//    );
//
//    return task;
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithMismatchedParameters() {
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTaskWithExtraTaskVars();
//
//    task.getParameterLists().getInputParameters().addParameterPair(
//        task.getDecomposition().getVariableWithName("taskVarOut003"),
//        "/StartingNet/netVarIn002/text()"
//    );
//    task.getParameterLists().getOutputParameters().addParameterPair(
//        rootNet.getDecomposition().getVariableWithName("netVarOut003"),
//        "/Firt/taskVarIn002/text()"
//    );
//
//    return task;
//  }
//
//  public void testFlowEmbeddedAtomicTaskPassingParametersMismatched() {
//    createFirstFlowEmbeddedDecomposedAtomicTaskWithMismatchedParameters();
//    assertFalse(specificationIsValid());
//  }
//
//  private AtomicTask createFirstFlowEmbeddedDecomposedAtomicTaskWithMatchedParameters() {
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTaskWithExtraTaskVars();
//
//    task.getParameterLists().getInputParameters().addParameterPair(
//        task.getDecomposition().getVariableWithName("taskVarIn002"),
//        "/StartingNet/netVarOut003/text()"
//    );
//    task.getParameterLists().getOutputParameters().addParameterPair(
//        rootNet.getDecomposition().getVariableWithName("netVarIn002"),
//        "/Firt/taskVarOut003/text()"
//    );
//
//    return task;
//  }
//
//  public void testFlowEmbeddedAtomicTaskPassingParametersMatched() {
//    createFirstFlowEmbeddedDecomposedAtomicTaskWithMatchedParameters();
//    assertTrue(specificationIsValid());
//  }
//
//  private void addLocalVariable001ToRootNet() {
//    rootNet.getDecomposition().addVariable(
//        new DataVariable(
//            "netLocalVar001",
//            "string",
//            "this gives me the heebie-jeebies",
//            DataVariable.USAGE_LOCAL
//        )
//    );
//  }
//
//  private AtomicTask createNetWithLocalVarAndAtomicTaskWithInOutVar() {
//    addLocalVariable001ToRootNet();
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTask();
//    task.getDecomposition().addVariable(
//        new DataVariable(
//            "taskVar001",
//            "string",
//            "",
//            DataVariable.USAGE_INPUT_AND_OUTPUT
//        )
//    );
//
//    return task;
//  }
//
//  private AtomicTask createLocalNetVarTaskInOutVarMatchedParameters() {
//    AtomicTask task = createNetWithLocalVarAndAtomicTaskWithInOutVar();
//
//    task.getParameterLists().getInputParameters().addParameterPair(
//        task.getDecomposition().getVariableWithName("taskVar001"),
//        "/StartingNet/netLocalVar001/text()"
//    );
//    task.getParameterLists().getOutputParameters().addParameterPair(
//        rootNet.getDecomposition().getVariableWithName("netLocalVar001"),
//        "/Firt/taskVar001/text()"
//    );
//
//    return task;
//  }
//
//  public void testNetLocalVariablePassingMatchedParameters() {
//    createLocalNetVarTaskInOutVarMatchedParameters();
//    assertTrue(specificationIsValid());
//  }
//
//  private AtomicTask createNetWithLocalVarAndAtomicTaskWithInOnlyAndOutOnlyVar() {
//    addLocalVariable001ToRootNet();
//    AtomicTask task = createFirstFlowEmbeddedDecomposedAtomicTask();
//
//    task.getDecomposition().addVariable(
//        new DataVariable(
//            "taskVarIn002",
//            "string",
//            "this really also gives me the heebie-jeebies",
//            DataVariable.USAGE_INPUT_ONLY
//        )
//    );
//    task.getDecomposition().addVariable(
//        new DataVariable(
//            "taskVarOut003",
//            "string",
//            "this gives me the uber-heebie-jeebies",
//            DataVariable.USAGE_OUTPUT_ONLY
//        )
//    );
//
//    return task;
//  }
//
//  private AtomicTask createLocalNetVarTaskInOutVarMismatchedParameters() {
//    AtomicTask task = createNetWithLocalVarAndAtomicTaskWithInOnlyAndOutOnlyVar();
//
//    task.getParameterLists().getInputParameters().addParameterPair(
//        task.getDecomposition().getVariableWithName("taskInVar002"),
//        "/StartingNet/netLocalVar001/text()"
//    );
//    task.getParameterLists().getOutputParameters().addParameterPair(
//        rootNet.getDecomposition().getVariableWithName("netLocalVar001"),
//        "/Firt/taskOutVar003/text()"
//    );
//
//    return task;
//  }
//
//  public void testNetLocalVariablePassingMismatchedParameters() {
//    createLocalNetVarTaskInOutVarMismatchedParameters();
//    assertFalse(specificationIsValid());
//  }
//
//  //////////////////////////  Composite Task Tests /////////////////////////////
//
//  private CompositeTask createFirstCompositeTask() {
//    return NetCellFactory.insertCompositeTask(
//        rootNet.getGraph(),
//        new Point(10,10)
//    );
//  }
//
//  public void testCompositeTaskPlacement() {
//    createFirstCompositeTask();
//    assertFalse(specificationIsValid());
//  }
//
//  private CompositeTask createFirstFlowEmbeddedCompositeTask() {
//    CompositeTask task = createFirstCompositeTask();
//
//    rootNet.getGraph().connect(
//        rootNetInputCondition.getPortAt(YAWLVertex.RIGHT),
//        task.getPortAt(YAWLVertex.LEFT)
//    );
//
//    rootNet.getGraph().connect(
//        task.getPortAt(YAWLVertex.RIGHT),
//        rootNetOutputCondition.getPortAt(YAWLVertex.LEFT)
//    );
//    return task;
//  }
//
//  public void testFlowEmbeddedCompositeTask() {
//    createFirstFlowEmbeddedCompositeTask();
//    assertFalse(specificationIsValid());
//  }
//
//  public void testSelfReferencingFlowEmbeddedCompositeTask() {
//    CompositeTask task = createFirstFlowEmbeddedCompositeTask();
//    task.setNet(rootNet.getDecomposition());
//    assertFalse(specificationIsValid());
//  }
//
//  private boolean specificationIsValid() {
//    try {
//      List problemList = SpecificationValidator.getValidationResults();
//      String firstProblem = (String) problemList.get(0);
//      return  (firstProblem.startsWith("No design-time engine validation problems"));
//    } catch (Exception e) {
//      // e.printStackTrace();
//    }
//    return false;
//  }
}
