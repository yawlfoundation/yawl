/*
 * Created on 09/02/2006
 * YAWLEditor v1.4 
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

package au.edu.qut.yawl.editor.thirdparty.engine;

import java.io.File;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.JoinDecorator;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;
import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleCompositeTask;
import au.edu.qut.yawl.editor.elements.model.CompositeTask;
import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.SplitDecorator;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLPort;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;


import au.edu.qut.yawl.editor.net.CancellationSet;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

import au.edu.qut.yawl.editor.specification.SpecificationFileModel;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.specification.SpecificationUndoManager;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.LayoutManager;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;


import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.YCompositeTask;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.unmarshal.YMetaData;

public class EngineSpecificationImporter extends EngineEditorInterpretor {
  
  public void importEngineSpecificationFromFile(String fullFileName) {
    if (fullFileName == null) {
      return;
    }
    YSpecification engineSpecification = importEngineSpecificationAsEngineObjects(fullFileName);
    
    if (engineSpecification == null) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered reading YAWL engine file.\nDiscarding this file.\n",
          "Engine File Loading Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    SpecificationModel.getInstance().reset();
    
    convertEngineSpecificationObjectsToEditorObjects(engineSpecification);

    LayoutManager.layoutSpecification();
    
    SpecificationFileModel.getInstance().incrementFileCount();
    SpecificationUndoManager.getInstance().discardAllEdits();
  }

  public YSpecification importEngineSpecificationAsEngineObjects(String fullFileName) {
    try {
      List specifications = YMarshal.unmarshalSpecifications((new File(fullFileName)).getCanonicalPath());
      return (YSpecification) specifications.get(0); // Engine currently only supplies a single specification per file.
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void convertEngineSpecificationObjectsToEditorObjects(YSpecification engineSpecification) {
    initialise();
    
    SpecificationModel.getInstance().setId(
      engineSpecification.getID()    
    );

    convertEngineMetaData(engineSpecification);
    convertEngineDataTypeDefinition(engineSpecification);

    convertRootNet(engineSpecification);
    convertSubNetsAndOtherDecompositions(engineSpecification);
    
    populateEditorNets(engineSpecification);
    
    //TODO: DO Resource Perspective
    
    SpecificationModel.getInstance().syncViewToModel();
  }
  
  private void convertEngineMetaData(YSpecification engineSpecification) {
    YMetaData metaData = engineSpecification.getMetaData();
    
    if (metaData.getTitle() != null) {
      SpecificationModel.getInstance().setName(
          metaData.getTitle()    
      );
    }

    if (metaData.getTitle() != null) {
      SpecificationModel.getInstance().setName(
          metaData.getTitle()    
      );
    }

    if (metaData.getDescription() != null) {
      SpecificationModel.getInstance().setDescription(
          metaData.getDescription()
      );
    }
    
    //TODO: Current mismatch between editor and engine: engine allows several creators yet editor allows a single creator.

    if (metaData.getCreators().size() > 0) {
      SpecificationModel.getInstance().setAuthor(
          (String) metaData.getCreators().iterator().next()
      );
    }

    if (metaData.getVersion() != null) {
      SpecificationModel.getInstance().setVersionNumber(
          metaData.getVersion()
      );
    }

    if (metaData.getValidFrom() != null) {
      SpecificationModel.getInstance().setValidFromTimestamp(
          TIMESTAMP_FORMAT.format(metaData.getValidFrom())
      );
    }
    
    if (metaData.getValidUntil() != null) {
      SpecificationModel.getInstance().setValidUntilTimestamp(
          TIMESTAMP_FORMAT.format(metaData.getValidUntil())
      );
    }
  }
  
  private void convertEngineDataTypeDefinition(YSpecification engineSpecification) {
    SpecificationModel.getInstance().setDataTypeDefinition(
      engineSpecification.getToolsForYAWL().getSchemaString().trim()
    );
  }
  
  private void convertRootNet(YSpecification engineSpecification) {
    YNet engineRootNet = engineSpecification.getRootNet();
    
    NetGraphModel editorNetModel = convertEngineNet(engineSpecification, engineRootNet);
    
    editorNetModel.setIsStartingNet(true);

  }
  
  private NetGraphModel convertEngineNet(YSpecification engineSpecification, YNet engineNet) {

    NetGraph editorNet = new NetGraph();
    editorNet.setName(engineNet.getID());
    
    convertDecompositionParameters(
        engineNet, 
        editorNet.getNetModel().getDecomposition()
    );

    convertNetLocalVariables(engineNet, editorNet);

    SpecificationModel.getInstance().addNetNotUndoable(editorNet.getNetModel());

    YAWLEditorDesktop.getInstance().openNet(editorNet);
    if (!editorNet.getNetModel().isStartingNet()) {
      try {
        editorNet.getFrame().setIcon(true);
      } catch (Exception e) {}
    }

    editorToEngineNetMap.put(engineNet, editorNet.getNetModel());

    return editorNet.getNetModel();
  }

  private void convertNetLocalVariables(YNet engineNet, 
                                        NetGraph editorNet) {

    Iterator localVariableKeyIterator = engineNet.getLocalVariables().keySet().iterator();

    while(localVariableKeyIterator.hasNext()) {
      String variableKey = (String) localVariableKeyIterator.next();
      YVariable engineVariable = (YVariable) engineNet.getLocalVariables().get(variableKey);

      createEditorVariable(
          editorNet.getNetModel().getDecomposition(),
          DataVariable.USAGE_LOCAL, 
          engineVariable.getDataTypeName(),
          engineVariable.getName(),
          engineVariable.getInitialValue()
      );
    }
  }

  
  private void convertDecompositionParameters(YDecomposition engineDecomposition, 
                                              Decomposition editorDecomposition) {
    convertDecompositionInputParameters(engineDecomposition, editorDecomposition);
    convertDecompositionOutputParameters(engineDecomposition, editorDecomposition);
    
    editorDecomposition.getVariables().consolidateInputAndOutputVariables();
  }
  
  private void convertDecompositionInputParameters(YDecomposition engineDecomposition, 
                                                   Decomposition editorDecomposition) {
    
    Iterator inputIterator = 
      engineDecomposition.getInputParameters().keySet().iterator();
    
    while(inputIterator.hasNext()) {
      
      YParameter engineParameter = (YParameter) engineDecomposition.getInputParameters().get(inputIterator.next());
      
      createEditorVariable(
          editorDecomposition,
          DataVariable.USAGE_INPUT_ONLY, 
          engineParameter.getDataTypeName(),
          engineParameter.getName(),
          engineParameter.getInitialValue()
      );
      
    }
  }

  private void convertDecompositionOutputParameters(YDecomposition engineDecomposition, 
                                                    Decomposition editorDecomposition) {

    Iterator outputIterator = engineDecomposition.getOutputParameters().keySet().iterator();

    while (outputIterator.hasNext()) {

      YParameter engineParameter = (YParameter) engineDecomposition.getInputParameters().get(outputIterator.next());

      createEditorVariable(
          editorDecomposition, 
          DataVariable.USAGE_OUTPUT_ONLY,
          engineParameter.getDataTypeName(), 
          engineParameter.getName(),
          engineParameter.getInitialValue()
      );
    }
  }
  
  private void createEditorVariable(Decomposition editorDecomposition, 
                                    int editorUsage,
                                    String dataType, 
                                    String paramName, 
                                    String initialValue) {
    
    DataVariable editorVariable = new DataVariable();

    editorVariable.setName(paramName);
    editorVariable.setUsage(editorUsage);
    editorVariable.setDataType(dataType);
    editorVariable.setInitialValue(initialValue);
    editorVariable.setUserDefined(true);
    
    editorDecomposition.getVariables().add(editorVariable);
 }
  
  private void convertSubNetsAndOtherDecompositions(YSpecification engineSpecification) {
    Iterator engineDecompositions = engineSpecification.getDecompositions().iterator();
    
    while (engineDecompositions.hasNext()) {
      YDecomposition engineDecomposition = (YDecomposition) engineDecompositions.next();
      if (engineDecomposition instanceof YNet && 
          !engineDecomposition.equals(engineSpecification.getRootNet())) {
        
        YNet engineSubNet = (YNet) engineDecomposition;
        NetGraphModel editorNetModel = convertEngineNet(engineSpecification, engineSubNet);
        
        SpecificationModel.getInstance().addNet(editorNetModel);
        
        editorToEngineNetMap.put(engineSubNet, editorNetModel);
      }
      if (engineDecomposition instanceof YAWLServiceGateway) {
        YAWLServiceGateway engineGateway = (YAWLServiceGateway) engineDecomposition;
        WebServiceDecomposition editorDecomposition = new WebServiceDecomposition();

        editorDecomposition.setLabel(engineGateway.getID());
        
        if (engineGateway.getYawlService() != null) {
          editorDecomposition.setYawlServiceID(
            engineGateway.getYawlService().getURI()
          );
          editorDecomposition.setYawlServiceDescription(
              engineGateway.getYawlService().getDocumentation()
          );
        }
        
        convertDecompositionParameters(
            engineGateway, 
            editorDecomposition
        );

        SpecificationModel.getInstance().addDecomposition(editorDecomposition);
      }
    }
  }
  
  private void populateEditorNets(YSpecification engineSpecification) {
    Iterator netIterator = editorToEngineNetMap.keySet().iterator();
    while (netIterator.hasNext()) {
      YNet engineNet = (YNet) netIterator.next();
      NetGraphModel editorNetModel = (NetGraphModel) editorToEngineNetMap.get(engineNet);
      populateEditorNet(engineNet, editorNetModel);
    }
  }
  
  private void populateEditorNet(YNet engineNet, NetGraphModel editorNet) {

    EngineNetElementSummary engineNetElementSummary = new EngineNetElementSummary(engineNet);
    
    InputCondition editorInputCondition = 
      generateEditorInputCondition(
          engineNetElementSummary.getInputCondition()          
      );
      
    editorNet.getGraph().addElement(editorInputCondition);
    editorToEngineElementMap.put(
       engineNetElementSummary.getInputCondition(),        
       editorInputCondition
    );

    OutputCondition editorOutputCondition = 
      generateEditorOutputCondition(
          engineNetElementSummary.getOutputCondition()          
      );
    
    editorNet.getGraph().addElement(editorOutputCondition);
    editorToEngineElementMap.put(
        engineNetElementSummary.getOutputCondition(),        
         editorOutputCondition
     );
    
    populateElements(engineNetElementSummary, editorNet);
    populateFlows(engineNetElementSummary.getFlows(), editorNet);
    removeImplicitConditions(engineNetElementSummary.getConditions(), editorNet);
    populateCancellationSetDetail(engineNetElementSummary.getTasksWithCancellationSets(), editorNet);
    removeUnnecessaryDecorators(editorNet);
  }
  
  private InputCondition generateEditorInputCondition(YInputCondition engineInputCondition) {
    InputCondition editorInputCondition = new InputCondition();
    return editorInputCondition;
  }
  
  private OutputCondition generateEditorOutputCondition(YOutputCondition engineOutputCondition) {
    OutputCondition editorOutputCondition = new OutputCondition();
    return editorOutputCondition;
  }
  
  private void populateElements(EngineNetElementSummary engineNetSummary, NetGraphModel editorNet) {
    populateAtomicTasks(engineNetSummary.getAtomicTasks(), editorNet);
    populateCompositeTasks(engineNetSummary.getCompositeTasks(), editorNet);
    populateConditions(engineNetSummary.getConditions(), editorNet);
  }
  
  private void populateAtomicTasks(Set engineAtomicTasks, NetGraphModel editorNet) {
    Iterator atomicTaskIterator = engineAtomicTasks.iterator();
    
    while(atomicTaskIterator.hasNext()) {
      YAtomicTask engineAtomicTask = (YAtomicTask) atomicTaskIterator.next();

      YAWLAtomicTask editorAtomicTask = null;
    
      if (engineAtomicTask.getMultiInstanceAttributes() == null) {
        editorAtomicTask = generateEditorAtomicTask(engineAtomicTask, editorNet);
        editorNet.getGraph().addElement((AtomicTask) editorAtomicTask);
      } else { 
        editorAtomicTask = generateEditorMultipleAtomicTask(engineAtomicTask, editorNet);
        editorNet.getGraph().addElement((MultipleAtomicTask) editorAtomicTask);
      }
      
      setTaskDecorators(engineAtomicTask, (YAWLTask) editorAtomicTask, editorNet);
      
      editorToEngineElementMap.put(
          engineAtomicTask,        
          editorAtomicTask
      );
    }
  }
  
  private void setTaskDecorators(YTask engineTask, YAWLTask editorTask, NetGraphModel editorNet) {
    editorNet.setJoinDecorator(
        editorTask,
        engineToEditorJoin(engineTask),
        JoinDecorator.getDefaultPosition()
    );

    editorNet.setSplitDecorator(
        editorTask,
        engineToEditorSplit(engineTask),
        SplitDecorator.getDefaultPosition()
    );
  }
  
  private void convertTaskParameterQueries(YTask engineTask, YAWLTask editorTask, NetGraphModel editorNet) {
    convertTaskInputParameterQueries(engineTask, editorTask);
    convertTaskOutputParameterQueries(engineTask, editorTask, editorNet);
  }
  
  private void convertTaskInputParameterQueries(YTask engineTask, YAWLTask editorTask) {
    Iterator editorInputParamIterator = editorTask.getVariables().getInputVariables().iterator();
    while (editorInputParamIterator.hasNext()) {
      DataVariable editorVariable = (DataVariable) editorInputParamIterator.next();
      
      String engineDataBinding = engineTask.getDataBindingForInputParam(editorVariable.getName());
      
      editorTask.getParameterLists().getInputParameters().addParameterPair(
        editorVariable,
        XMLUtilities.stripOutermostTags(engineDataBinding)
      );
    }
  }

  private void convertTaskOutputParameterQueries(YTask engineTask, YAWLTask editorTask, NetGraphModel net) {
    Iterator engineOutputParams = engineTask.getParamNamesForTaskCompletion().iterator();
    
    while(engineOutputParams.hasNext()) {
      String targetParameter = (String) engineOutputParams.next();

      DataVariable editorVariable = net.getDecomposition().getVariableWithName(targetParameter);
      
      String engineDataBinding = engineTask.getDataBindingForOutputParam(targetParameter);
      
      editorTask.getParameterLists().getOutputParameters().addParameterPair(
          editorVariable,
          XMLUtilities.stripOutermostTags(engineDataBinding)
      );
    }
  }

  private AtomicTask generateEditorAtomicTask(YAtomicTask engineAtomicTask, NetGraphModel editorNet) {
    AtomicTask editorAtomicTask = new AtomicTask();
    
    WebServiceDecomposition editorDecomposition = (WebServiceDecomposition) SpecificationModel.getInstance().getDecompositionFromLabel(
      engineAtomicTask.getDecompositionPrototype().getID()    
    );
    
    editorAtomicTask.setDecomposition(editorDecomposition);

    convertTaskParameterQueries(engineAtomicTask, editorAtomicTask, editorNet);
    
    return editorAtomicTask;
  }

  private MultipleAtomicTask generateEditorMultipleAtomicTask(YAtomicTask engineAtomicTask, NetGraphModel editorNet) {
    MultipleAtomicTask editorMultipleAtomicTask = new MultipleAtomicTask();
    
    WebServiceDecomposition editorDecomposition = (WebServiceDecomposition) SpecificationModel.getInstance().getDecompositionFromLabel(
      engineAtomicTask.getDecompositionPrototype().getID()    
    );
    
    editorMultipleAtomicTask.setDecomposition(editorDecomposition);

    convertTaskParameterQueries(engineAtomicTask, editorMultipleAtomicTask, editorNet);

    setMultipleInstanceDetail(
        engineAtomicTask.getMultiInstanceAttributes(), 
        engineAtomicTask,
        editorMultipleAtomicTask
    );
    
    return editorMultipleAtomicTask;
  }
  
  private void setMultipleInstanceDetail(YMultiInstanceAttributes engineMIAttributes, 
                                         YTask                    engineTask, 
                                         YAWLMultipleInstanceTask editorTask) {
    editorTask.setMinimumInstances(
        engineMIAttributes.getMinInstances()    
    );
    editorTask.setMaximumInstances(
        engineMIAttributes.getMaxInstances()    
    );
    editorTask.setContinuationThreshold(
        engineMIAttributes.getThreshold()    
    );
    editorTask.setContinuationThreshold(
        engineMIAttributes.getThreshold()    
    );
    editorTask.setInstanceCreationType(
        engineToEditorMultiInstanceCreationMode(
            engineMIAttributes.getCreationMode()
        )
    );

    editorTask.setMultipleInstanceVariable(
      ((YAWLTask) editorTask).getVariables().getVariableWithName(
        engineMIAttributes.getMIFormalInputParam()
      )
    );
    
    editorTask.setAccessorQuery(
        engineTask.getPreSplittingMIQuery()
    );

    editorTask.setSplitterQuery(
      engineMIAttributes.getMISplittingQuery()    
    );
    
    editorTask.setInstanceQuery(
        engineMIAttributes.getMIFormalOutputQuery()    
    );

    editorTask.setAggregateQuery(
        XMLUtilities.stripOutermostTags(engineMIAttributes.getMIJoiningQuery())    
    );

    editorTask.setResultNetVariable(
        ((YAWLTask) editorTask).getParameterLists().getOutputParameters().getVariableWithName(
          engineTask.getMIOutputAssignmentVar(engineMIAttributes.getMIFormalOutputQuery())
        )
    );

    // Below we are overwriting the existing query recorded against the result net 
    // variable, because when it was establised in the generic task conversion code earleir, 
    // the outer XML element tags were stripped from it. This is valid to do in all 
    // circumstances but the instance query of a multiple-instance task.  Here, we simply 
    // force the query to be exactly what the engine specification gives us.
    
    ((YAWLTask) editorTask).getParameterLists().getOutputParameters().setQueryFor(
      editorTask.getResultNetVariable(),
      engineMIAttributes.getMIFormalOutputQuery()
    );
  }

  
  private void populateCompositeTasks(Set engineCompositeTasks, NetGraphModel editorNet) {
    Iterator compositeTaskIterator = engineCompositeTasks.iterator();
    while(compositeTaskIterator.hasNext()) {
      YCompositeTask engineCompositeTask = (YCompositeTask) compositeTaskIterator.next();
      YAWLCompositeTask editorCompositeTask = null;
      if (engineCompositeTask.getMultiInstanceAttributes() == null) {
        editorCompositeTask = generateEditorCompositeTask(engineCompositeTask, editorNet);
        editorNet.getGraph().addElement((CompositeTask) editorCompositeTask);
      } else { 
        editorCompositeTask = generateEditorMultipleCompositeTask(engineCompositeTask, editorNet);
        editorNet.getGraph().addElement((MultipleCompositeTask) editorCompositeTask);
      }
      editorToEngineElementMap.put(
          engineCompositeTask,        
          editorCompositeTask
      );
      
      setTaskDecorators(engineCompositeTask, (YAWLTask) editorCompositeTask, editorNet);
    }
  }
  
  private CompositeTask generateEditorCompositeTask(YCompositeTask engineCompositeTask, NetGraphModel editorNet) {
    CompositeTask editorCompositeTask = new CompositeTask();
    
    NetGraphModel decomposedEditorNet = SpecificationModel.getInstance().getNetModelFromName(
      engineCompositeTask.getDecompositionPrototype().getID()    
    );
    
    editorCompositeTask.setDecomposition(decomposedEditorNet.getDecomposition());

    convertTaskParameterQueries(engineCompositeTask, editorCompositeTask, editorNet);

    return editorCompositeTask;
  }
  
  private MultipleCompositeTask generateEditorMultipleCompositeTask(YCompositeTask engineCompositeTask, NetGraphModel editorNet) {
    MultipleCompositeTask editorMultipleCompositeTask = new MultipleCompositeTask();

    NetGraphModel decomposedEditorNet = SpecificationModel.getInstance().getNetModelFromName(
        engineCompositeTask.getDecompositionPrototype().getID()    
      );
      
    editorMultipleCompositeTask.setDecomposition(decomposedEditorNet.getDecomposition());

    convertTaskParameterQueries(engineCompositeTask, editorMultipleCompositeTask, editorNet);

    setMultipleInstanceDetail(
        engineCompositeTask.getMultiInstanceAttributes(), 
        engineCompositeTask,
        editorMultipleCompositeTask
    );
    
    return editorMultipleCompositeTask;
  }


  private void populateConditions(Set engineConditions, NetGraphModel editorNet) {
    Iterator conditionIterator = engineConditions.iterator();
    while(conditionIterator.hasNext()) {
      YCondition engineCondition = (YCondition) conditionIterator.next();
      Condition editorCondition = generateEditorCondition(engineCondition);

      editorNet.getGraph().addElement(editorCondition);
      editorNet.getGraph().setElementLabel(
          editorCondition,
          parseEngineIdForLabel(engineCondition.getID())
      );
      
      editorToEngineElementMap.put(
          engineCondition,        
          editorCondition
      );
    }
  }
  
  private Condition generateEditorCondition(YCondition engineCondition) {
    Condition editorCondition = new Condition();
    return editorCondition;
  }
  
  private void populateFlows(Set engineFlows, NetGraphModel editorNet) {
    Iterator flowIterator = engineFlows.iterator();
    while(flowIterator.hasNext()) {
      YFlow engineFlow = (YFlow) flowIterator.next();

      YExternalNetElement sourceEngineElement = engineFlow.getPriorElement();
      YExternalNetElement targetEngineElement = engineFlow.getNextElement();
      
      YAWLVertex sourceEditorElement = (YAWLVertex) editorToEngineElementMap.get(sourceEngineElement);
      YAWLVertex targetEditorElement = (YAWLVertex) editorToEngineElementMap.get(targetEngineElement);

      YAWLFlowRelation editorFlow = editorNet.getGraph().connect(sourceEditorElement, targetEditorElement);
      
      editorFlow.setPredicate(
        engineFlow.getXpathPredicate()    
      );
      if (engineFlow.getEvalOrdering() != null) {
        editorFlow.setPriority(
            engineFlow.getEvalOrdering().intValue()
          );
      }
    }
  }
  
  private void populateCancellationSetDetail(Set engineTasksWithCancellationSets, 
                                             NetGraphModel editorNet) {
    Iterator engineTaskIterator = engineTasksWithCancellationSets.iterator();
    while(engineTaskIterator.hasNext()) {
       YTask engineTask = (YTask) engineTaskIterator.next();
       YAWLTask editorTask = (YAWLTask) editorToEngineElementMap.get(engineTask);

       CancellationSet editorTaskCancellationSet = new CancellationSet(editorTask);   
       Iterator engineTaskCancellationSetIterator = engineTask.getRemoveSet().iterator();

       while(engineTaskCancellationSetIterator.hasNext()) {
         YExternalNetElement engineSetMember = (YExternalNetElement) engineTaskCancellationSetIterator.next();
       
         YAWLCell editorSetMember = (YAWLCell) editorToEngineElementMap.get(engineSetMember);
         
         if (editorFlowEngineConditionMap.get(engineSetMember) != null) {
           YAWLFlowRelation replacementEditorFlow = (YAWLFlowRelation) editorFlowEngineConditionMap.get(engineSetMember);
           editorSetMember = replacementEditorFlow;
         }
         editorTaskCancellationSet.addMember(editorSetMember);
       }
       editorTask.setCancellationSet(editorTaskCancellationSet);
    }
  }

  
  private void removeImplicitConditions(Set engineConditions, NetGraphModel editorNet) {
    Iterator conditionIterator = engineConditions.iterator();
    while(conditionIterator.hasNext()) {
      YCondition engineCondition = (YCondition) conditionIterator.next();
      Condition editorCondition = (Condition) editorToEngineElementMap.get(engineCondition);

      // assumption: a labelled flow, or one with more than single flow into or out of it 
      // indicates an explicit condition. It's not foolproof, but it should take away 90% 
      // of the noise implicit  conditions, leaving the designer to add back in those 
      // explicit conditions that have been compressed to flows.

      if(editorCondition.getLabel() == null || editorCondition.getLabel().trim().equals("")) {

        YAWLFlowRelation sourceFlow = editorCondition.getOnlyIncomingFlow();
        YAWLFlowRelation targetFlow = editorCondition.getOnlyOutgoingFlow();
        
        if(sourceFlow != null && targetFlow != null) {

          YAWLTask sourceTask = ((YAWLPort) sourceFlow.getSource()).getTask();
          YAWLTask targetTask = ((YAWLPort) targetFlow.getTarget()).getTask();
          
          if (sourceTask != null && targetTask != null) {

            editorNet.getGraph().removeCellsAndTheirEdges(
                new Object[] { editorCondition }    
            );

            YAWLFlowRelation editorFlow = editorNet.getGraph().connect(sourceTask, targetTask);
            
            editorFlowEngineConditionMap.put(engineCondition, editorFlow);
          }
        }
      }
    }
  }
  
  private void removeUnnecessaryDecorators(NetGraphModel editorNet) {
    Set tasks = NetUtilities.getAllTasks(editorNet);
    Iterator taskIterator = tasks.iterator();
    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();
      if (editorTask.hasJoinDecorator() && editorTask.getIncommingFlowCount() < 2) {
        editorNet.setJoinDecorator(
            editorTask,
            JoinDecorator.NO_TYPE,
            JoinDecorator.NOWHERE
        );
      }
      if (editorTask.hasSplitDecorator() && editorTask.getOutgoingFlowCount() < 2) {
        editorNet.setSplitDecorator(
            editorTask,
            SplitDecorator.NO_TYPE,
            SplitDecorator.NOWHERE
        );
      }
    }
  }

  
  private String parseEngineIdForLabel(String engineId) {
    String[] idComponents = engineId.split("_");
    if (idComponents.length == 2) {
      return idComponents[0];
    }
    return null;
  }
  
  private int engineToEditorJoin(YTask engineTask) {
    switch(engineTask.getJoinType()) {
      case YTask._AND: {
        return Decorator.AND_TYPE;
      }
      case YTask._OR: {
        return Decorator.OR_TYPE;
      }
      case YTask._XOR: {
        return Decorator.XOR_TYPE;
      }
    }
    return Decorator.XOR_TYPE;
  }

  private int engineToEditorSplit(YTask engineTask) {
    switch(engineTask.getSplitType()) {
      case YTask._AND: {
        return Decorator.AND_TYPE;
      }
      case YTask._OR: {
        return Decorator.OR_TYPE;
      }
      case YTask._XOR: {
        return Decorator.XOR_TYPE;
      }
    }
    return Decorator.AND_TYPE;
  }
  
  private int engineToEditorMultiInstanceCreationMode(String engineCreationMode) {
    if (engineCreationMode.equals(YMultiInstanceAttributes._creationModeStatic)) {
      return MultipleAtomicTask.STATIC_INSTANCE_CREATION;
    }
    if (engineCreationMode.equals(YMultiInstanceAttributes._creationModeDynamic)) {
      return MultipleAtomicTask.DYNAMIC_INSTANCE_CREATION;
    }
    return MultipleAtomicTask.STATIC_INSTANCE_CREATION;
  }
}