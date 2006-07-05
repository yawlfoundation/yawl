/*
 * Created on 18/03/2004
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
 */

package au.edu.qut.yawl.editor.thirdparty.engine;

import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.NetElementSummary;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.JoinDecorator;
import au.edu.qut.yawl.editor.elements.model.SplitDecorator;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.FileChooserFactory;
import au.edu.qut.yawl.editor.swing.JStatusBar;
import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.data.YParameter;

import au.edu.qut.yawl.unmarshal.YMarshal;

import au.edu.qut.yawl.util.YVerificationMessage;

import java.io.File;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.YAWLEditor;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SpecificationEngineHandler {
  private long uniqueLabelNumber = 0;
  private HashMap editorToEngineElementMap;
  private HashMap editorToEngineNetMap;
	private HashMap editorFlowEngineConditionMap;

  private YSpecification engineSpecification;
  
  private static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
  
  private static final String SPECIFICATION_FILE_TYPE = "xml";

  private static final JFileChooser EXPORT_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        "YAWL Engine Specification",
        "Export specification to engine ",
        " format"
    );

  private transient static final SpecificationEngineHandler INSTANCE 
    = new SpecificationEngineHandler();

  public static SpecificationEngineHandler getInstance() {
    return INSTANCE; 
  }

  private SpecificationEngineHandler() {
    reset();  
  }
  
  public void reset() {
    editorToEngineElementMap = new HashMap();
    editorToEngineNetMap = new HashMap();
    editorFlowEngineConditionMap = new HashMap();
    engineSpecification = null;
    uniqueLabelNumber = 0;
  }

  public void export() {
    reset();
    promptForAndSetSaveFileName();
    saveSpecificationToFile(SpecificationModel.getInstance().getEngineFileName());
  }
  
   public void validate() {
    reset();
    
    JStatusBar.getInstance().updateProgressOverSeconds(2);
    
    try {
      ProblemMessagePanel.getInstance().setProblemList(
          "Problems identified in engine specification validation",
          createProblemListFrom(nonGUIvalidate())
      );
      JStatusBar.getInstance().resetProgress();

    } catch (Exception e) {
      e.printStackTrace();
      JStatusBar.getInstance().resetProgress();
    }
  }
   
   public List nonGUIvalidate() throws Exception {
     YSpecification specification = generateEngineSpecification();
     return specification.verify();
   }
  
  private void promptForAndSetSaveFileName() {

    if (JFileChooser.CANCEL_OPTION == 
        EXPORT_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
      return;
    }

    File file = EXPORT_FILE_CHOOSER.getSelectedFile();

    if (file.exists() && 
        !getFullNameFromFile(file).equals(SpecificationModel.getInstance().getEngineFileName())) {
      if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(YAWLEditor.getInstance(),
              "You have chosen an existing engine specification file.\n" +
              "If you save to this file, you will overwrite the file's contents.\n\n" +
              "Are you absolutely certain you want to export your specification to this file?\n",
              "Existing Specification File Selected",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE)) {
        return;   
      }
    }
		SpecificationModel.getInstance().setEngineFileName(getFullNameFromFile(file));
  }

  public void saveSpecificationToFile(String fullFileName) {
    if (fullFileName.equals("")) {
      return;
    }

    JStatusBar.getInstance().updateProgressOverSeconds(2);

    saveSpecificationToFileNoGUI(fullFileName);
    
    JStatusBar.getInstance().resetProgress();
  }
  
  public void saveSpecificationToFileNoGUI(String fullFileName) {
    try {
      PrintStream outputStream = 
        new PrintStream(new BufferedOutputStream(new FileOutputStream(fullFileName)));
      outputStream.println(getSpecificationAsXML());
      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private String getSpecificationAsXML() {
    try {
      YSpecification specification = generateEngineSpecification();
      return YMarshal.marshal(specification,false);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private YSpecification generateEngineSpecification() {
    
    engineSpecification = new YSpecification(
        justFileName(SpecificationModel.getInstance().getFileName())
    );

    try {
      engineSpecification.setSchema(
          SpecificationModel.getInstance().getDataTypeDefinition()
      );
    } catch (Exception eActual) {
      try {
        engineSpecification.setSchema(
            SpecificationModel.DEFAULT_TYPE_DEFINITION
        );      
      } catch (Exception eDefault) {}
    }
    
    //important:  Engine API expects nets to be pre-generated before we specifiy decompositions
    //            between.  We need to build the nets first, and THEN populate the nets with elements.

    generateRootNet(engineSpecification);
    generateSubNets(engineSpecification);
    
    populateEngineNets(engineSpecification);     
      
    return engineSpecification;
  }
  
  private void generateRootNet(YSpecification engineSpecification) {
    YNet rootEngineNet = 
      generateEngineNet(engineSpecification, 
                        SpecificationModel.getInstance().getStartingNet());

    engineSpecification.setRootNet(rootEngineNet);
    editorToEngineNetMap.put(SpecificationModel.getInstance().getStartingNet(), rootEngineNet);
  }
  
  private void generateSubNets(YSpecification engineSpecification) {
    Iterator subNetIterator = SpecificationModel.getInstance().getSubNets().iterator();
    while (subNetIterator.hasNext()) {
      NetGraphModel editorNet = (NetGraphModel) subNetIterator.next();
      
      YNet engineSubNet = 
        generateEngineNet(engineSpecification, editorNet);
      engineSpecification.setDecomposition(engineSubNet);

      editorToEngineNetMap.put(editorNet, engineSubNet);
    }
  }
  
  private YNet generateEngineNet(YSpecification engineSpecification, NetGraphModel editorNet) {
    YNet engineNet = new YNet(XMLUtilities.toValidElementName(editorNet.getName()),
                              engineSpecification);

    generateDecompositionParameters(
        engineNet, 
        editorNet.getDecomposition()
    );
    
    establishEngineLocalVariables(engineNet, editorNet);
    
    return engineNet;
  }

  private void establishEngineLocalVariables(YNet engineNet, NetGraphModel editorNet) {
    Iterator i = editorNet.getDecomposition().getVariables().getLocalVariables().iterator();
    
    while(i.hasNext()) {
      DataVariable editorNetVariable = (DataVariable) i.next();
      
      YVariable engineNetVariable = new YVariable(engineNet);
      engineNetVariable.setDataTypeAndName(
          editorNetVariable.getDataType(),
          editorNetVariable.getName(),
          XML_SCHEMA_URI
      );

      engineNetVariable.setInitialValue(
          XMLUtilities.quoteSpecialCharacters(
              editorNetVariable.getInitialValue()
          )
      );
 
      engineNet.setLocalVariable(engineNetVariable);
    }
  }
  
  private void populateEngineNets(YSpecification engineSpecification) {
    Iterator netIterator = editorToEngineNetMap.keySet().iterator();
    while (netIterator.hasNext()) {
      NetGraphModel editorNet = (NetGraphModel) netIterator.next();
      populateEngineNetFrom(engineSpecification, editorNet);
    }
  }

  private void populateEngineNetFrom(YSpecification engineSpecification, NetGraphModel editorNet)  {
    YNet engineNet = (YNet) editorToEngineNetMap.get(editorNet);
    NetElementSummary editorNetSummary = new NetElementSummary(editorNet);

    engineNet.setInputCondition(generateInputCondition(engineNet, editorNetSummary));
    engineNet.setOutputCondition(generateOutputCondition(engineNet, editorNetSummary));
    
    setElements(engineSpecification, engineNet, editorNetSummary);
    setFlows(engineNet, editorNetSummary);
    setCancellationSetDetail(editorNetSummary);
  }
  
  private YInputCondition generateInputCondition(YNet engineNet, 
                                                 NetElementSummary editorNetSummary) {
    YInputCondition engineInputCondition = 
      new YInputCondition(
          getNewUniqueElementID(editorNetSummary.getInputCondition()),
          engineNet);
    
    editorToEngineElementMap.put(
        editorNetSummary.getInputCondition(), 
        engineInputCondition);

    return engineInputCondition;
  }

  private YOutputCondition generateOutputCondition(YNet engineNet, 
                                                  NetElementSummary editorNetSummary) {
    YOutputCondition engineOutputCondition = 
      new YOutputCondition(
          getNewUniqueElementID(editorNetSummary.getOutputCondition()),
          engineNet);

    editorToEngineElementMap.put(
        editorNetSummary.getOutputCondition(), 
        engineOutputCondition);

    return engineOutputCondition;
  }
  
  private void setElements(YSpecification engineSpecification, 
                           YNet engineNet, 
                           NetElementSummary editorNetSummary) {
    setConditions(engineNet, editorNetSummary);
    setAtomicTasks(engineSpecification, engineNet, editorNetSummary);
    setCompositeTasks(engineNet, editorNetSummary);
  }
  
  private void setConditions(YNet engineNet, NetElementSummary editorNetSummary) {
    
    Iterator conditionIterator = 
      editorNetSummary.getConditions().iterator();
    
    while(conditionIterator.hasNext()) {
      Condition editorCondition = (Condition) conditionIterator.next();
      
      YCondition engineCondition = 
        new YCondition(
          getNewUniqueElementID(editorCondition),
          engineNet);
      
      engineNet.addNetElement(engineCondition);

      editorToEngineElementMap.put(editorCondition, engineCondition);
    }
  }
  
  private void setAtomicTasks(YSpecification engineSpecification, 
                              YNet engineNet, 
                              NetElementSummary editorNetSummary) {
    
    Iterator taskIterator = editorNetSummary.getAtomicTasks().iterator();
    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();

      YAtomicTask engineAtomicTask = 
        new YAtomicTask(
          getNewUniqueElementID(editorTask),
          editorToEngineJoin(editorTask),
          editorToEngineSplit(editorTask),   
          engineNet
        );

      if (editorTask.getDecomposition() != null) {
        YAWLServiceGateway engineDecomposition = 
          (YAWLServiceGateway) engineSpecification.getDecomposition(
              editorTask.getDecomposition().getLabelAsElementName()
          );
        if (engineDecomposition == null) {
          engineDecomposition = 
            generateAtomicDecompositionFor(
                engineSpecification,
                editorTask 
            );
        }
        engineAtomicTask.setDecompositionPrototype(engineDecomposition);
      }  

      generateDecompositionParameters(
          engineAtomicTask.getDecompositionPrototype(), 
          editorTask.getDecomposition()
      );
      
      populateTaskParameterQueries(engineAtomicTask, editorTask);

      populateMultipleInstanceDetail(engineAtomicTask, editorTask);
      engineNet.addNetElement(engineAtomicTask);
      editorToEngineElementMap.put(editorTask, engineAtomicTask);
    }
  }
  
  private YAWLServiceGateway generateAtomicDecompositionFor(YSpecification engineSpecification, 
                                                            YAWLTask editorTask) {

    WebServiceDecomposition editorDecomposition = 
      ((YAWLAtomicTask)editorTask).getWSDecomposition();
    
    YAWLServiceGateway engineDecomposition = 
      new YAWLServiceGateway(
          editorDecomposition.getLabelAsElementName(), 
          engineSpecification
      );
    
    if (editorDecomposition.getYawlServiceID() != null &&
        !editorDecomposition.getYawlServiceID().trim().equals("")) {
      YAWLServiceReference engineService = new YAWLServiceReference(
        editorDecomposition.getYawlServiceID(),
        engineDecomposition
      );

      if (editorDecomposition.getServiceDescriptionURI() != null &&
          !editorDecomposition.getServiceDescriptionURI().trim().equals("")) {
        engineService.setWsdlLocation(editorDecomposition.getServiceDescriptionURI());
      }
      if (editorDecomposition.getServiceOperation() != null && 
          !editorDecomposition.getServiceOperation().trim().equals("")) {
        engineService.setOperationName(editorDecomposition.getServiceOperation());
      }
        
      engineDecomposition.setYawlService(engineService);
    }
    
    engineSpecification.setDecomposition(engineDecomposition);
    
    return engineDecomposition;
  }
  
  private void generateDecompositionParameters(YDecomposition engineDecomposition, 
                                               Decomposition editorDecomposition) {
    if(editorDecomposition == null) {
      return;
    }
    
    generateDecompositionInputParameters(engineDecomposition, editorDecomposition);
    generateDecompositionOutputParameters(engineDecomposition, editorDecomposition);
  }
  
  private void generateDecompositionInputParameters(YDecomposition engineDecomposition, 
                                                    Decomposition editorDecomposition) {
    Iterator inputIterator = 
      editorDecomposition.getVariables().getInputVariables().iterator();
    
    while(inputIterator.hasNext()) {
      DataVariable editorInputVariable = 
        (DataVariable) inputIterator.next();
      
      // Don't need to create parameters for local variables.
      if(editorInputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
        continue;
      }

      YParameter engineInputParameter = 
        new YParameter(engineDecomposition, 
                       true); //input
      
      engineInputParameter.setDataTypeAndName(
        editorInputVariable.getDataType(),
        editorInputVariable.getName(),
        XML_SCHEMA_URI
      );
      
      if (!(editorInputVariable.getInitialValue() == null) && 
          !editorInputVariable.getInitialValue().equals("")) {
        engineInputParameter.setInitialValue(
            XMLUtilities.quoteSpecialCharacters(
                editorInputVariable.getInitialValue()
            )
        );
      }
      
      engineDecomposition.setInputParam(engineInputParameter);
    }  
  }
  
  private void generateDecompositionOutputParameters(YDecomposition engineDecomposition, 
                                                    Decomposition editorDecomposition) {
    Iterator outputIterator = 
      editorDecomposition.getVariables().getOutputVariables().iterator();
    
    while(outputIterator.hasNext()) {
      DataVariable editorOutputVariable = 
        (DataVariable) outputIterator.next();
      
      // Don't need to create parameters for local variables.
      if(editorOutputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
        continue;
      }

      YParameter engineOutputParameter = 
        new YParameter(engineDecomposition, 
                       false);  // output
      
      engineOutputParameter.setDataTypeAndName(
        editorOutputVariable.getDataType(),
        editorOutputVariable.getName(),
        XML_SCHEMA_URI
      );
      
      /*  Engine BETA 3/4 doesn't like this at run-time.
      if (!(editorOutputVariable.getInitialValue() == null) && 
          !editorOutputVariable.getInitialValue().equals("")) {
        engineOutputParameter.setInitialValue(
            XMLUtilities.quoteSpecialCharacters(
                editorOutputVariable.getInitialValue()
            )
        );
      }*/

      engineDecomposition.setOutputParameter(engineOutputParameter);
    }
  }

  private void setCompositeTasks(YNet engineNet, NetElementSummary editorNetSummary) {
    Iterator taskIterator = editorNetSummary.getCompositeTasks().iterator();

    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();
      
      YCompositeTask engineCompositeTask = 
        new YCompositeTask(
          getNewUniqueElementID(editorTask),
          editorToEngineJoin(editorTask),
          editorToEngineSplit(editorTask),   
        engineNet
      );
      
      if (editorTask.getDecomposition() != null) {
        NetGraphModel editorUnfoldingNet = 
          SpecificationModel.getInstance().getNetModelFromName(
              editorTask.getDecomposition().getLabel()
          );
        
        engineCompositeTask.setDecompositionPrototype(
            (YDecomposition) editorToEngineNetMap.get(editorUnfoldingNet)
        );
      }

      /* Already done via the net decomposition creation.
      generateDecompositionParameters(
          engineCompositeTask.getDecompositionPrototype(), 
          editorTask.getDecomposition()
      );*/
      
      populateTaskParameterQueries(engineCompositeTask, editorTask);
      populateMultipleInstanceDetail(engineCompositeTask, editorTask);
      engineNet.addNetElement(engineCompositeTask);
      editorToEngineElementMap.put(editorTask, engineCompositeTask);
    }
  }
  private void populateTaskParameterQueries(YTask engineTask, YAWLTask editorTask) {
    populateTaskInputParameterQueries(engineTask, editorTask);
    populateTaskOutputParameterQueries(engineTask, editorTask);
  }
  
  private void populateTaskInputParameterQueries(YTask engineTask, 
                                                 YAWLTask editorTask) {
    Iterator inputIterator = 
      editorTask.getParameterLists().getInputParameters().getParameters().iterator();
    
    while(inputIterator.hasNext()) {
      Parameter editorInputParameter = 
        (Parameter) inputIterator.next();

      if (editorTask instanceof YAWLMultipleInstanceTask) {
        YAWLMultipleInstanceTask multiInstanceTask = (YAWLMultipleInstanceTask) editorTask;
        if (multiInstanceTask.getMultipleInstanceVariable() != null && 
            !multiInstanceTask.getMultipleInstanceVariable().equals(editorInputParameter.getVariable())) {
          engineTask.setDataBindingForInputParam(
               XMLUtilities.getTaggedOutputVariableQuery(
                   editorInputParameter.getVariable(), 
                   editorInputParameter.getEngineReadyQuery()
               ), 
               editorInputParameter.getVariable().getName()
          );
        }
      } else {
        engineTask.setDataBindingForInputParam(
            XMLUtilities.getTaggedOutputVariableQuery(
                editorInputParameter.getVariable(), 
                editorInputParameter.getEngineReadyQuery()
            ), 
            editorInputParameter.getVariable().getName()
        );
      }
    }
  }
  
  private void populateTaskOutputParameterQueries(YTask engineTask, 
                                                  YAWLTask editorTask) {
    
    Iterator outputIterator = 
      editorTask.getParameterLists().getOutputParameters().getParameters().iterator();

    while(outputIterator.hasNext()) {
      Parameter editorOutputParameter = 
        (Parameter) outputIterator.next();

      if (editorTask instanceof YAWLMultipleInstanceTask) {
        YAWLMultipleInstanceTask multiInstanceTask = (YAWLMultipleInstanceTask) editorTask;
        if (multiInstanceTask.getResultNetVariable() != null &&
            !multiInstanceTask.getResultNetVariable().equals(editorOutputParameter.getVariable())) {
          System.out.println("non-net variable Param for " + editorOutputParameter.getVariableName());
          engineTask.setDataBindingForOutputExpression(
              XMLUtilities.getTaggedOutputVariableQuery(
                  editorOutputParameter.getVariable(), 
                  editorOutputParameter.getEngineReadyQuery()
              ), 
              editorOutputParameter.getVariable().getName()
          );
        }
      } else {
        engineTask.setDataBindingForOutputExpression(
            XMLUtilities.getTaggedOutputVariableQuery(
                editorOutputParameter.getVariable(), 
                editorOutputParameter.getEngineReadyQuery()
            ), 
            editorOutputParameter.getVariable().getName()
        );
      }
    }
  }
  
  private void populateMultipleInstanceDetail(YTask engineTask, 
                                              YAWLTask editorTask) {
    
    if (!(editorTask instanceof YAWLMultipleInstanceTask)) {
      return;
    }
    
    YAWLMultipleInstanceTask editorMultiInstanceTask = 
      (YAWLMultipleInstanceTask) editorTask;

    engineTask.setUpMultipleInstanceAttributes(
      String.valueOf(editorMultiInstanceTask.getMinimumInstances()),
      String.valueOf(editorMultiInstanceTask.getMaximumInstances()),
      String.valueOf(editorMultiInstanceTask.getContinuationThreshold()),
      editorToEngineMultiInstanceMode(editorMultiInstanceTask)
    ); 

    // data perspective input
    
    DataVariable editorTaskInstanceVariable = 
      editorMultiInstanceTask.getMultipleInstanceVariable();
    
    String taskInstanceVariableName = null;

    if (editorTaskInstanceVariable != null) {
      taskInstanceVariableName = editorTaskInstanceVariable.getName();
    } 

    HashMap inputMapping = new HashMap();
    
    inputMapping.put(
        taskInstanceVariableName,
        editorMultiInstanceTask.getAccessorQuery()
    );
    
    engineTask.setDataMappingsForTaskStarting(inputMapping);
    
    engineTask.setMultiInstanceInputDataMappings(
      taskInstanceVariableName,
      editorMultiInstanceTask.getSplitterQuery()
    );

    // data perspective output

    DataVariable editorNetResultVariable = 
      editorMultiInstanceTask.getResultNetVariable();
    
    String netResultVariableName = null;

    if (editorNetResultVariable != null) {
      netResultVariableName = editorNetResultVariable.getName();
    }

    HashMap outputMapping = new HashMap();
    
    outputMapping.put(
        editorMultiInstanceTask.getInstanceQuery(),
        netResultVariableName
    );
    
    engineTask.setDataMappingsForTaskCompletion(
        outputMapping
    );

    // Whatever I pass to the engine below must be exactly the
    // same text as the ..DataMappings... calls above.

    engineTask.setMultiInstanceOutputDataMappings(
        editorMultiInstanceTask.getInstanceQuery(), 
        XMLUtilities.getTaggedOutputVariableQuery(
            editorNetResultVariable,
            editorMultiInstanceTask.getAggregateQuery()
        )
    );
  }
  
  private void setFlows(YNet engineNet, NetElementSummary editorNetSummary) {
    Iterator flowIterator = editorNetSummary.getFlows().iterator();
    while(flowIterator.hasNext()) {
      YAWLFlowRelation editorFlow = (YAWLFlowRelation) flowIterator.next(); 
      
      Object editorFlowSource = 
        NetGraphModel.getSourceVertex(editorNetSummary.getModel(), editorFlow);
      
      if (editorFlowSource instanceof VertexContainer) {
        editorFlowSource = ((VertexContainer) editorFlowSource).getVertex();
      }
      if (editorFlowSource instanceof Decorator) {
        editorFlowSource = ((Decorator) editorFlowSource).getTask();                  
      }
      
      Object editorFlowTarget = 
        NetGraphModel.getTargetVertex(editorNetSummary.getModel(), editorFlow);
      
      if (editorFlowTarget instanceof VertexContainer) {
        editorFlowTarget = ((VertexContainer) editorFlowTarget).getVertex();
      }
      if (editorFlowTarget instanceof Decorator) {
        editorFlowTarget = ((Decorator) editorFlowTarget).getTask();                  
      }

      YExternalNetElement engineSource = 
        (YExternalNetElement) editorToEngineElementMap.get(editorFlowSource);
      
      YExternalNetElement engineTarget = 
        (YExternalNetElement) editorToEngineElementMap.get(editorFlowTarget);

      YFlow firstEngineFlow = null; 
      
      if (editorFlowSource instanceof YAWLTask && 
          editorFlowTarget instanceof YAWLTask) {
        
        YCondition implicitEngineCondition = 
          new YCondition(
            getNewUniqueID() + "_ImplicitCondition",
            engineNet
          );
        
        implicitEngineCondition.setImplicit(true);
        engineNet.addNetElement(implicitEngineCondition);

        firstEngineFlow = new YFlow(engineSource, implicitEngineCondition);

        engineSource.setPostset(firstEngineFlow);

        YFlow secondEngineFlow = new YFlow(implicitEngineCondition, engineTarget);
        implicitEngineCondition.setPostset(secondEngineFlow);
        addFlowConditionMapping(editorFlow, implicitEngineCondition);
        
      } else { // no need for an implicit condition. Phew!
        firstEngineFlow = new YFlow(engineSource, engineTarget);
        engineSource.setPostset(firstEngineFlow);
      }
      
      // Pass in data perspective data.
      
      if (editorFlow.hasXorSplitAsSource() && !editorFlow.isDefaultFlow()) {
        firstEngineFlow.setEvalOrdering(new Integer(editorFlow.getPriority()));
        firstEngineFlow.setXpathPredicate(editorFlow.getPredicate());
        // The Engine is quoting these
        //firstEngineFlow.setXpathPredicate(editorFlow.getEngineReadyPredicate());
      }
      
      if (editorFlow.hasXorSplitAsSource() || editorFlow.hasOrSplitAsSource()) {
        firstEngineFlow.setIsDefaultFlow(editorFlow.isDefaultFlow());
        if (editorFlow.hasOrSplitAsSource()) {
          firstEngineFlow.setXpathPredicate(editorFlow.getPredicate());
          // The Engine is quoting these
          //firstEngineFlow.setXpathPredicate(editorFlow.getEngineReadyPredicate());
        }
      }

      editorToEngineElementMap.put(editorFlow, firstEngineFlow);
    }
  }
  
  private void setCancellationSetDetail(NetElementSummary editorNetSummary) {
    Iterator taskIterator = editorNetSummary.getTasksWithCancellationSets().iterator();
    while(taskIterator.hasNext()) {
      YAWLTask editorTriggerTask = (YAWLTask) taskIterator.next();
      
      Iterator cancellationSetIterator = 
        editorTriggerTask.getCancellationSet().getSetMembers().iterator();
      
      LinkedList engineCancellationSet = new LinkedList();
      while(cancellationSetIterator.hasNext()) {
        Object editorSetMember = cancellationSetIterator.next();
        
        if (editorSetMember instanceof YAWLFlowRelation) {
          engineCancellationSet.add(
              getConditionForFlow((YAWLFlowRelation) editorSetMember)
          );
        } else {
          engineCancellationSet.add(
              editorToEngineElementMap.get(editorSetMember)
          );
        }
      }
      
      YTask engineTriggerTask = 
        (YTask) editorToEngineElementMap.get(editorTriggerTask);
      engineTriggerTask.setRemovesTokensFrom(engineCancellationSet); 
    }
  }
  
  private String justFileName(String fullFilePath) {
    File file = new File(fullFilePath);

    return file.getName().trim().equals("") ? 
        "unnamed.ywl" : 
        XMLUtilities.toValidElementName(file.getName());
  }
  
  private String getNewUniqueID() {
    return Long.toString(this.uniqueLabelNumber++);  
  }
  
  private String getNewUniqueElementID(YAWLVertex element) {
    if (element instanceof InputCondition) {
      return getNewUniqueID() + "_InputCondition";
    }
    if (element instanceof OutputCondition) {
      return getNewUniqueID() + "_OutputCondition";
    }
    if (element.getLabel() != null) {
      return getNewUniqueID() + "_" + XMLUtilities.toValidElementName(element.getLabel());
    }
    return getNewUniqueID();   
  }
  
  private int editorToEngineJoin(YAWLTask task) {
    if (task.hasJoinDecorator()) {
      JoinDecorator decorator = task.getJoinDecorator();
      switch (decorator.getType()) {
         case Decorator.AND_TYPE: {
           return YTask._AND;
         }
         case Decorator.OR_TYPE: {
           return YTask._OR;
         }
         case Decorator.XOR_TYPE: {
           return YTask._XOR;
         }
         default: {
           return YTask._XOR;
         }
      }
    }
    return YTask._XOR;
  }

  private int editorToEngineSplit(YAWLTask task) {
    if (task.hasSplitDecorator()) {
      SplitDecorator decorator = task.getSplitDecorator();
      switch (decorator.getType()) {
         case Decorator.AND_TYPE: {
           return YTask._AND;
         }
         case Decorator.OR_TYPE: {
           return YTask._OR;
         }
         case Decorator.XOR_TYPE: {
           return YTask._XOR;
         }
         default: {
           return YTask._AND;
         }
      }
    }
    return YTask._AND;
  }

  private String editorToEngineMultiInstanceMode(YAWLMultipleInstanceTask task) {
    switch(task.getInstanceCreationType()) {
      case YAWLMultipleInstanceTask.STATIC_INSTANCE_CREATION: {
        return YMultiInstanceAttributes._creationModeStatic;
      }
      case YAWLMultipleInstanceTask.DYNAMIC_INSTANCE_CREATION: {
        return YMultiInstanceAttributes._creationModeDynamic;
      }
      default: {
        return YMultiInstanceAttributes._creationModeStatic;
      }
    } 
  }

	private void addFlowConditionMapping(YAWLFlowRelation editorFlow, 
                                      YCondition engineCondition) {
		editorFlowEngineConditionMap.put(editorFlow, engineCondition);
	} 
  
	private YCondition getConditionForFlow(YAWLFlowRelation editorFlow) {
		return (YCondition) editorFlowEngineConditionMap.get(editorFlow);
	}
  
  private String getFullNameFromFile(File file) {
    if (file == null) {
      return "";
    }
    String fullFileName = file.getAbsolutePath();
    if (!fullFileName.toLowerCase().endsWith(SPECIFICATION_FILE_TYPE)) {
      fullFileName += "." + SPECIFICATION_FILE_TYPE;
    }
    return fullFileName;
  }
  
  private List createProblemListFrom(List verificationList) {
    LinkedList problemList = new LinkedList();
    
    for(int i = 0; i < verificationList.size(); i++) {
      YVerificationMessage message = (YVerificationMessage) verificationList.get(i);
      String messageString = message.getMessage();
      
      if (messageString.indexOf("composite task may not decompose to other than a net") != -1) {
        continue;
      }
      if (messageString.indexOf("is not registered with engine.") != -1) {
        // We have no running engine when validating, so this is not valid.
        continue;
      }
      
      messageString = messageString.replaceAll("postset size","outgoing flow number");
      messageString = messageString.replaceAll("preset size","incomming flow number");
      messageString = messageString.replaceAll("Check the empty tasks linking from i to o.",
                                               "Should all atomic tasks in the net be unlabelled?");
      messageString = messageString.replaceAll("from i to o","between the input and output conditions");
      messageString = messageString.replaceAll("InputCondition","Input Condition");
      messageString = messageString.replaceAll("OutputCondition","Output Condition");

      messageString = messageString.replaceAll("ExternalCondition","Condition");
      messageString = messageString.replaceAll("AtomicTask","Atomic Task");
      messageString = messageString.replaceAll("CompositeTask","Composite Task");
      messageString = messageString.replaceAll("The net \\(Net:","The net (");
      messageString = messageString.replaceAll("composite task must contain a net","must unfold to some net");
      
      problemList.add(messageString);
    }
    if (problemList.size() == 0) {
      problemList.add("No design-time engine problems were found in this specification.");
    }
    return problemList;
  }
}

