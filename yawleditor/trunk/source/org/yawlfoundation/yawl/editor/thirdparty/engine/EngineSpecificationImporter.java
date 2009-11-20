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

package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.foundations.LogWriter;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.net.CancellationSet;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.specification.*;
import org.yawlfoundation.yawl.editor.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.xml.datatype.Duration;
import java.awt.*;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.List;

public class EngineSpecificationImporter extends EngineEditorInterpretor {
  
  private static final Point DEFAULT_LOCATION = new Point(100,100);
  private static List<String> _invalidResourceReferences;
  private static int _maxEngineNumber = 0;

  public static void importEngineSpecificationFromFile(SpecificationModel editorSpec,
                                                       String fullFileName) {
    if (fullFileName == null)  return;

    String specStr = StringUtil.fileToString(fullFileName);
    if ((specStr == null) || specStr.length() == 0) return;

    _invalidResourceReferences = new ArrayList<String>();
    _maxEngineNumber = 0;

    YSpecification engineSpec = importEngineSpecificationAsEngineObjects(specStr);

    if (engineSpec == null) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Specification file falied to validate against YAWL Schema.\nDiscarding this file.\n",
          "Engine File Loading Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    editorSpec.reset();
    convertEngineSpecObjectsToEditorObjects(editorSpec, engineSpec);

    Element layout = getLayoutElement(specStr);
    if (layout != null) {
        LayoutImporter.importAndApply(editorSpec, layout) ;
    }
    else {
        removeUnnecessaryDecorators(editorSpec);
        DefaultLayoutArranger.layoutSpecification();
    }

    SpecificationModel.getInstance().setFileName(fullFileName);
    SpecificationFileModel.getInstance().incrementFileCount();
    SpecificationUndoManager.getInstance().discardAllEdits();
    SpecificationModel.getInstance().setUniqueElementNumber(_maxEngineNumber);

    if (! _invalidResourceReferences.isEmpty()) {
        showInvalidResourceReferences();
        if (! ResourcingServiceProxy.getInstance().isLiveService()) {
            if (showDisconnectedResourceServiceWarning() == JOptionPane.YES_OPTION) {
                SpecificationArchiveHandler.getInstance().processCloseRequest();
            }
        }
    }

    reset();
  }

  public static YSpecification importEngineSpecificationAsEngineObjects(String specXML) {
    try {
      List specifications = YMarshal.unmarshalSpecifications(specXML, false);
      return (YSpecification) specifications.get(0); // Engine currently only supplies a single specification per file.
    } catch (Exception e) {
        LogWriter.error("Error unmarshalling specification from XML.", e);
    }
    return null;
  }

  public static Element getLayoutElement(String specXML) {
      try {
          SAXBuilder builder = new SAXBuilder();
          Document document = builder.build(new StringReader(specXML));
          Element root = document.getRootElement();
          return root.getChild("layout", root.getNamespace());
      }
      catch (Exception e) {
          return null;
      }
  }
  
  public static void convertEngineSpecObjectsToEditorObjects(
                          SpecificationModel editorSpec, 
                          YSpecification engineSpec) {
    initialise();
    
    editorSpec.setId(
      engineSpec.getID()    
    );

    convertEngineMetaData(engineSpec);
    convertEngineDataTypeDefinition(engineSpec);

    convertRootNet(engineSpec);
    convertSubNetsAndOtherDecompositions(engineSpec);
    
    populateEditorNets(engineSpec);
    
  }
  
  private static void convertEngineMetaData(YSpecification engineSpecification) {
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

    
    SpecificationModel.getInstance().setVersionNumber(
          metaData.getVersion()
    );

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

    if (metaData.getUniqueID() != null) {
        SpecificationModel.getInstance().setUniqueID(metaData.getUniqueID());
    }
  }
  
  private static void convertEngineDataTypeDefinition(YSpecification engineSpecification) {
    SpecificationModel.getInstance().setDataTypeDefinition(
      engineSpecification.getDataValidator().getSchema().trim()
    );
  }
  
  private static void convertRootNet(YSpecification engineSpecification) {
    YNet engineRootNet = engineSpecification.getRootNet();
    
    NetGraphModel editorNetModel = convertEngineNet(engineSpecification, engineRootNet);
    
    editorNetModel.setIsStartingNet(true);

  }
  
  private static NetGraphModel convertEngineNet(YSpecification engineSpecification, YNet engineNet) {

    NetGraph editorNet = new NetGraph();
    editorNet.setName(engineNet.getID());
    
    convertDecompositionParameters(
        engineNet, 
        editorNet.getNetModel().getDecomposition()
    );

    convertNetLocalVariables(engineNet, editorNet);

    SpecificationModel.getInstance().addNetNotUndoable(editorNet.getNetModel());

    YAWLEditorDesktop.getInstance().openNet(editorNet);

    editorToEngineNetMap.put(engineNet, editorNet.getNetModel());

    return editorNet.getNetModel();
  }

  private static void convertNetLocalVariables(YNet engineNet, 
                                               NetGraph editorNet) {

    Iterator localVariableKeyIterator = engineNet.getLocalVariables().keySet().iterator();

    while(localVariableKeyIterator.hasNext()) {
      String variableKey = (String) localVariableKeyIterator.next();
      YVariable engineVariable = (YVariable) engineNet.getLocalVariables().get(variableKey);
      DataVariableSet varSet = editorNet.getNetModel().getDecomposition().getVariables();

      if (! localVarForOutputOnlyVar(varSet, engineVariable)) {
        String dataType = engineVariable.getDataTypeName();
        String initialValue = engineVariable.getInitialValue();
        if (dataType.equals("string")) {
            initialValue =  JDOMUtil.decodeEscapes(initialValue);
        }
        createEditorVariable(
          editorNet.getNetModel().getDecomposition(),
          DataVariable.USAGE_LOCAL, 
          dataType,
          engineVariable.getName(),
          initialValue,
          engineVariable.getDefaultValue(),
          new Hashtable()  // LWB: engine local variables do not have extended attributes, apparently.
        );
      }
    }
  }


  /** output-only net level vars are required to have a local var of the same name to be
   * exported as well (see EngineSpecificationExporter.establishEngineLocalVariables()
   * for more info). When importing form xml, we want to prevent a duplicate netvar
   * from being created in this case.
   *
   * @return true if localVar has the same name and datatype as an output-only
   *          editor var
   */
  private static boolean localVarForOutputOnlyVar(DataVariableSet varSet, YVariable localVar) {
      DataVariable editorVar = varSet.getVariableWithName(localVar.getName()) ;
      return (editorVar != null) &&
              editorVar.getDataType().equals(localVar.getDataTypeName()) &&
              (editorVar.getUsage() == DataVariable.USAGE_OUTPUT_ONLY);
  }
  
  private static void convertDecompositionParameters(YDecomposition engineDecomposition, 
                                                     Decomposition editorDecomposition) {
    convertDecompositionInputParameters(engineDecomposition, editorDecomposition);
    convertDecompositionOutputParameters(engineDecomposition, editorDecomposition);
    
    editorDecomposition.getVariables().consolidateInputAndOutputVariables();
  }

    private static void convertDecompositionInputParameters(YDecomposition engineDecomposition,
                                                            Decomposition editorDecomposition) {

        Vector<YParameter> inputParams =
                new Vector<YParameter>(engineDecomposition.getInputParameters().values());
        Collections.sort(inputParams);   // sort on ordering

        for (YParameter engineParameter : inputParams) {
            createEditorVariable(
                    editorDecomposition,
                    DataVariable.USAGE_INPUT_ONLY,
                    engineParameter.getDataTypeName(),
                    engineParameter.getName(),
                    engineParameter.getInitialValue(),
                    engineParameter.getInitialValue(),
                    engineParameter.getAttributes()
            );
        }
    }

    private static void convertDecompositionOutputParameters(YDecomposition engineDecomposition,
                                                             Decomposition editorDecomposition) {

        Vector<YParameter> outputParams =
                new Vector<YParameter>(engineDecomposition.getOutputParameters().values());
        Collections.sort(outputParams);   // sort on ordering

        for (YParameter engineParameter : outputParams) {
            createEditorVariable(
                    editorDecomposition,
                    DataVariable.USAGE_OUTPUT_ONLY,
                    engineParameter.getDataTypeName(),
                    engineParameter.getName(),
                    engineParameter.getInitialValue(),
                    engineParameter.getDefaultValue(),
                    engineParameter.getAttributes()
            );
        }
    }
  
  private static void createEditorVariable(Decomposition editorDecomposition, 
                                           int editorUsage,
                                           String dataType, 
                                           String paramName, 
                                           String initialValue,
                                           String defaultValue,
                                           Hashtable attributes) {
    
    DataVariable editorVariable = new DataVariable();

    editorVariable.setName(paramName);
    editorVariable.setUsage(editorUsage);
    editorVariable.setDataType(dataType);
    editorVariable.setInitialValue(initialValue);
    editorVariable.setUserDefined(true);
    editorVariable.setAttributes(attributes);
    
    editorDecomposition.getVariables().add(editorVariable);
 }
  
  private static void convertSubNetsAndOtherDecompositions(YSpecification engineSpecification) {
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

        convertInteractionSettings(engineDecomposition, editorDecomposition);
        convertExtendedAttributes(engineDecomposition, editorDecomposition);

        SpecificationModel.getInstance().addWebServiceDecomposition(editorDecomposition);
      }
    }
  }


  private static void convertInteractionSettings(YDecomposition engineDecomposition,
                                                 WebServiceDecomposition editorDecomposition) {
      editorDecomposition.setCodelet(engineDecomposition.getCodelet());
      editorDecomposition.setManualInteraction(engineDecomposition.requiresResourcingDecisions());
  }


    private static void convertExtendedAttributes(YDecomposition engineDecomposition,
                                                   WebServiceDecomposition editorDecomposition) {
      editorDecomposition.setAttributes(engineDecomposition.getAttributes());
    }

  
  private static void populateEditorNets(YSpecification engineSpecification) {
    Iterator netIterator = editorToEngineNetMap.keySet().iterator();
    while (netIterator.hasNext()) {
      YNet engineNet = (YNet) netIterator.next();
      NetGraphModel editorNetModel = (NetGraphModel) editorToEngineNetMap.get(engineNet);
      populateEditorNet(engineNet, editorNetModel);
    }
  }
  
  private static void populateEditorNet(YNet engineNet, NetGraphModel editorNet) {

    EngineNetElementSummary engineNetElementSummary = new EngineNetElementSummary(engineNet);
    
    InputCondition editorInputCondition = 
      generateEditorInputCondition(
          engineNetElementSummary.getInputCondition()          
      );
      
    editorNet.getGraph().addElement(editorInputCondition);
    editorNet.getGraph().setElementLabel(editorInputCondition,
          engineNetElementSummary.getInputCondition().getName());

    editorToEngineElementMap.put(
       engineNetElementSummary.getInputCondition(),        
       editorInputCondition
    );

    OutputCondition editorOutputCondition = 
      generateEditorOutputCondition(
          engineNetElementSummary.getOutputCondition()          
      );
    
    editorNet.getGraph().addElement(editorOutputCondition);
    editorNet.getGraph().setElementLabel(editorOutputCondition,
            engineNetElementSummary.getOutputCondition().getName());

    editorToEngineElementMap.put(
        engineNetElementSummary.getOutputCondition(),        
         editorOutputCondition
     );
    
    populateElements(engineNetElementSummary, editorNet);
    populateFlows(engineNetElementSummary.getFlows(), editorNet);
    removeImplicitConditions(engineNetElementSummary.getConditions(), editorNet);
    populateCancellationSetDetail(engineNetElementSummary.getTasksWithCancellationSets(), editorNet);
  }

  private static InputCondition generateEditorInputCondition(YInputCondition engineInputCondition) {
    InputCondition editorInputCondition = new InputCondition(DEFAULT_LOCATION);
    mapUniqueElementID(editorInputCondition, engineInputCondition.getID()) ;
    return editorInputCondition;
  }
  
  private static OutputCondition generateEditorOutputCondition(YOutputCondition engineOutputCondition) {
    OutputCondition editorOutputCondition = new OutputCondition(DEFAULT_LOCATION);
    mapUniqueElementID(editorOutputCondition, engineOutputCondition.getID()) ;
    return editorOutputCondition;
  }
  
  private static void populateElements(EngineNetElementSummary engineNetSummary, NetGraphModel editorNet) {
    populateAtomicTasks(engineNetSummary.getAtomicTasks(), editorNet);
    populateCompositeTasks(engineNetSummary.getCompositeTasks(), editorNet);
    populateConditions(engineNetSummary.getConditions(), editorNet);
  }
  
  private static void populateAtomicTasks(Set engineAtomicTasks, NetGraphModel editorNet) {
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
      setEditorTaskLabel(editorNet.getGraph(), (YAWLTask) editorAtomicTask, engineAtomicTask);
      
      setTaskDecorators(engineAtomicTask, (YAWLTask) editorAtomicTask, editorNet);
      setTaskResources(engineAtomicTask, editorAtomicTask, editorNet) ;
      setTaskTimers(engineAtomicTask, editorAtomicTask, editorNet);
      setTaskCustomForm(engineAtomicTask, editorAtomicTask);
     
      editorToEngineElementMap.put(
          engineAtomicTask,        
          editorAtomicTask
      );
    }
    finaliseRetainFamiliarMappings(editorNet);
  }
  
  private static void setEditorTaskLabel(NetGraph editorNet, YAWLTask editorTask, YTask engineTask) {
    if (engineTask.getName() == null && engineTask.getDecompositionPrototype() != null) {
      editorNet.setElementLabel(editorTask, engineTask.getDecompositionPrototype().getID());
    }
    else {
      mapUniqueElementID(editorTask, engineTask.getID()) ;
      editorNet.setElementLabel(editorTask, engineTask.getName());
    }
  }

  
  private static void setTaskDecorators(YTask engineTask, YAWLTask editorTask, NetGraphModel editorNet) {
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
  
  private static void convertTaskParameterQueries(YTask engineTask, YAWLTask editorTask, NetGraphModel editorNet) {
    convertTaskInputParameterQueries(engineTask, editorTask);
    convertTaskOutputParameterQueries(engineTask, editorTask, editorNet);
  }
  
  private static void convertTaskInputParameterQueries(YTask engineTask, YAWLTask editorTask) {
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

  private static void convertTaskOutputParameterQueries(YTask engineTask, YAWLTask editorTask, NetGraphModel net) {
    Iterator engineOutputParams = engineTask.getParamNamesForTaskCompletion().iterator();
    
    while(engineOutputParams.hasNext()) {
      String targetParameter = (String) engineOutputParams.next();
      
      DataVariable editorVariable = net.getDecomposition().getVariableWithName(targetParameter);
      
      String engineDataBinding = engineTask.getDataBindingForOutputParam(targetParameter);

        if ((editorVariable != null) && (engineDataBinding != null)) {
            editorTask.getParameterLists().getOutputParameters().addParameterPair(
                editorVariable,
                XMLUtilities.stripOutermostTags(engineDataBinding)
            );
        }    
    }
  }

  private static AtomicTask generateEditorAtomicTask(YAtomicTask engineAtomicTask, NetGraphModel editorNet) {
    AtomicTask editorAtomicTask = new AtomicTask(DEFAULT_LOCATION);
    mapUniqueElementID(editorAtomicTask, engineAtomicTask.getID()) ;
    if (engineAtomicTask.getDecompositionPrototype() == null) {
      return editorAtomicTask;
    }
    
    WebServiceDecomposition editorDecomposition = (WebServiceDecomposition) SpecificationModel.getInstance().getDecompositionFromLabel(
      engineAtomicTask.getDecompositionPrototype().getID()    
    );
    
    editorAtomicTask.setDecomposition(editorDecomposition);
    convertTaskParameterQueries(engineAtomicTask, editorAtomicTask, editorNet);
    
    return editorAtomicTask;
  }

  private static MultipleAtomicTask generateEditorMultipleAtomicTask(YAtomicTask engineAtomicTask, NetGraphModel editorNet) {
    MultipleAtomicTask editorMultipleAtomicTask = new MultipleAtomicTask(DEFAULT_LOCATION);
    mapUniqueElementID(editorMultipleAtomicTask, engineAtomicTask.getID()) ;
    if (engineAtomicTask.getDecompositionPrototype() == null) {
      return editorMultipleAtomicTask;
    }
    
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
  
  private static void setMultipleInstanceDetail(YMultiInstanceAttributes engineMIAttributes, 
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

  
  private static void populateCompositeTasks(Set engineCompositeTasks, NetGraphModel editorNet) {
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

      setEditorTaskLabel(editorNet.getGraph(), (YAWLTask) editorCompositeTask, engineCompositeTask);
      
      editorToEngineElementMap.put(
          engineCompositeTask,        
          editorCompositeTask
      );
      
      setTaskDecorators(engineCompositeTask, (YAWLTask) editorCompositeTask, editorNet);
    }
  }
  
  private static CompositeTask generateEditorCompositeTask(YCompositeTask engineCompositeTask, NetGraphModel editorNet) {
    CompositeTask editorCompositeTask = new CompositeTask(DEFAULT_LOCATION);
    mapUniqueElementID(editorCompositeTask, engineCompositeTask.getID()) ;

    NetGraphModel decomposedEditorNet = SpecificationUtilities.getNetModelFromName(
      SpecificationModel.getInstance(),
      engineCompositeTask.getDecompositionPrototype().getID()    
    );
    
    editorCompositeTask.setDecomposition(decomposedEditorNet.getDecomposition());

    convertTaskParameterQueries(engineCompositeTask, editorCompositeTask, editorNet);

    return editorCompositeTask;
  }
  
  private static MultipleCompositeTask generateEditorMultipleCompositeTask(YCompositeTask engineCompositeTask, NetGraphModel editorNet) {
    MultipleCompositeTask editorMultipleCompositeTask = new MultipleCompositeTask(DEFAULT_LOCATION);
    mapUniqueElementID(editorMultipleCompositeTask, engineCompositeTask.getID()) ;

    NetGraphModel decomposedEditorNet = SpecificationUtilities.getNetModelFromName(
        SpecificationModel.getInstance(),
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


  private static void setTaskTimers(YAtomicTask engineTask, YAWLAtomicTask editorTask,
                                    NetGraphModel containingNet) {
      Map timeParams = engineTask.getTimeParameters();
      if (timeParams != null) {
          TaskTimeoutDetail timeoutDetail = new TaskTimeoutDetail();
          String netParam = (String) timeParams.get("netparam");
          if (netParam != null) {
              DataVariable netVar = containingNet.getVariableSet().getVariableWithName(netParam);
              timeoutDetail.setTimeoutVariable(netVar);
          }
          else {
              YWorkItemTimer.Trigger trigger = (YWorkItemTimer.Trigger) timeParams.get("trigger");
              if (trigger == YWorkItemTimer.Trigger.OnEnabled) {
                  timeoutDetail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT);
              }
              else {
                  timeoutDetail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_STARTING);
              }
              Date expiry = (Date) timeParams.get("expiry");
              if (expiry != null) {
                  timeoutDetail.setTimeoutDate(expiry);
              }
              else {
                  Duration duration = (Duration) timeParams.get("duration");
                  if (duration != null) {
                      timeoutDetail.setTimeoutValue(duration.toString());
                  }
              }
          }

          ((AtomicTask) editorTask).setTimeoutDetail(timeoutDetail);
      }
  }


  private static void setTaskResources(YAtomicTask engineTask, YAWLAtomicTask editorTask,
                                       NetGraphModel editorNet) {
      Element rawResourceElement = engineTask.getResourcingSpecs();
      if (rawResourceElement != null) {
          ResourceMapping resourceMap = new ResourceMapping(editorTask, true);
          boolean badRef = resourceMap.parse(rawResourceElement, editorNet);
          if (badRef) {
              _invalidResourceReferences.add(editorNet.getName() + "::" + editorTask.getLabel());
          }
          editorTask.setResourceMapping(resourceMap);
      }
  }


  private static void finaliseRetainFamiliarMappings(NetGraphModel editorNet) {
      Set<YAWLAtomicTask> taskSet = NetUtilities.getAtomicTasks(editorNet);
      for (YAWLAtomicTask task : taskSet) {
          ResourceMapping rMap = task.getResourceMapping();
          if (rMap != null) rMap.finaliseRetainFamiliarTasks(taskSet);
      }
  }


  private static void setTaskCustomForm(YAtomicTask engineTask, YAWLAtomicTask editorTask) {
      URL formURL = engineTask.getCustomFormURL();
      if (formURL != null) ((YAWLTask) editorTask).setCustomFormURL(formURL.toString());
  }


  private static void showInvalidResourceReferences() {
      List<String> msgList = new ArrayList<String>();
      String template = "An invalid resource reference in Task '%s' of Net '%s' has been removed.";
      for (String ref : _invalidResourceReferences) {
          String[] split = ref.split("::");
          msgList.add(String.format(template, split[1], split[0]));
      }
      ProblemMessagePanel.getInstance().setProblemList("Invalid Resource References", msgList);
  }


  private static void populateConditions(Set engineConditions, NetGraphModel editorNet) {
    Iterator conditionIterator = engineConditions.iterator();
    while(conditionIterator.hasNext()) {
      YCondition engineCondition = (YCondition) conditionIterator.next();
      Condition editorCondition = generateEditorCondition(engineCondition);

      editorNet.getGraph().addElement(editorCondition);
      mapUniqueElementID(editorCondition, engineCondition.getID()) ;

      editorNet.getGraph().setElementLabel(
          editorCondition,
          engineCondition.getName()
      );
      
      editorToEngineElementMap.put(
          engineCondition,        
          editorCondition
      );
    }
  }
  
  private static Condition generateEditorCondition(YCondition engineCondition) {
    Condition editorCondition = new Condition(DEFAULT_LOCATION);
    return editorCondition;
  }
  
  private static void populateFlows(Set engineFlows, NetGraphModel editorNet) {

    Iterator flowIterator = engineFlows.iterator();
    while(flowIterator.hasNext()) {
      YFlow engineFlow = (YFlow) flowIterator.next();
      YAWLVertex sourceEditorElement = (YAWLVertex) editorToEngineElementMap.get(
              engineFlow.getPriorElement());
      YAWLVertex targetEditorElement = (YAWLVertex) editorToEngineElementMap.get(
              engineFlow.getNextElement());
      YAWLFlowRelation editorFlow = editorNet.getGraph().connect(sourceEditorElement,
              targetEditorElement);
      
      editorFlow.setPredicate(engineFlow.getXpathPredicate());
      if (engineFlow.getEvalOrdering() != null) {
        editorFlow.setPriority(engineFlow.getEvalOrdering());
      }

      // when a default flow is exported, it has no predicate or ordering recorded
      // (because it is the _default_ flow) - so when importing from that xml,
      // a default predicate and ordering need to be reinstated.
      if (engineFlow.isDefaultFlow()) {
          if (editorFlow.getPredicate() == null) editorFlow.setPredicate("true()");
          editorFlow.setPriority(10000);        // ensure it's ordered last
      }
    }
  }
  
  private static void populateCancellationSetDetail(Set engineTasksWithCancellationSets, 
                                                    NetGraphModel editorNet) {
    Iterator engineTaskIterator = engineTasksWithCancellationSets.iterator();
    while (engineTaskIterator.hasNext()) {
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

  private static void removeImplicitConditions(Set engineConditions, NetGraphModel editorNet) {
      for (Object o : engineConditions) {
          YCondition engineCondition = (YCondition) o;
          if (engineCondition.isImplicit()) {
              Condition editorCondition = (Condition)
                      editorToEngineElementMap.get(engineCondition);

              YAWLFlowRelation sourceFlow = editorCondition.getOnlyIncomingFlow();
              YAWLFlowRelation targetFlow = editorCondition.getOnlyOutgoingFlow();
              if(sourceFlow != null && targetFlow != null) {
                  YAWLTask sourceTask = ((YAWLPort) sourceFlow.getSource()).getTask();
                  YAWLTask targetTask = ((YAWLPort) targetFlow.getTarget()).getTask();
                  if (sourceTask != null && targetTask != null) {
                      editorNet.getGraph().removeCellsAndTheirEdges(
                              new Object[] { editorCondition });

                      YAWLFlowRelation editorFlow =
                          editorNet.getGraph().connect(sourceTask, targetTask);

                      // map predicate & priority from removed condition to new flow
                      editorFlow.setPredicate(sourceFlow.getPredicate());
                      editorFlow.setPriority(sourceFlow.getPriority());

                      editorFlowEngineConditionMap.put(engineCondition, editorFlow);
                  }
              }
          }
      }
  }

 // I'm leaving the below method as a monument to the grief it caused me //
//  private static void removeImplicitConditions(Set engineConditions, NetGraphModel editorNet) {
//    Iterator conditionIterator = engineConditions.iterator();
//    while(conditionIterator.hasNext()) {
//      YCondition engineCondition = (YCondition) conditionIterator.next();
//      Condition editorCondition = (Condition) editorToEngineElementMap.get(engineCondition);
//
//      // assumption: a labelled flow, or one with more than single flow into or out of it
//      // indicates an explicit condition. It's not foolproof, but it should take away 90%
//      // of the noise implicit  conditions, leaving the designer to add back in those
//      // explicit conditions that have been compressed to flows.
//
//      if(editorCondition.getLabel() == null || editorCondition.getLabel().trim().equals("")) {
//
//        YAWLFlowRelation sourceFlow = editorCondition.getOnlyIncomingFlow();
//        YAWLFlowRelation targetFlow = editorCondition.getOnlyOutgoingFlow();
//
//        if(sourceFlow != null && targetFlow != null) {
//
//          YAWLTask sourceTask = ((YAWLPort) sourceFlow.getSource()).getTask();
//          YAWLTask targetTask = ((YAWLPort) targetFlow.getTarget()).getTask();
//
//          if (sourceTask != null && targetTask != null) {
//
//            editorNet.getGraph().removeCellsAndTheirEdges(
//                new Object[] { editorCondition }
//            );
//
//            YAWLFlowRelation editorFlow = editorNet.getGraph().connect(sourceTask, targetTask);
//
//            // map predicate & priority from removed condition to new flow
//            editorFlow.setPredicate(sourceFlow.getPredicate());
//            editorFlow.setPriority(sourceFlow.getPriority());
//
//            editorFlowEngineConditionMap.put(engineCondition, editorFlow);
//          }
//        }
//      }
//    }
//  }
  
    private static void removeUnnecessaryDecorators(SpecificationModel editorSpec) {
        for (NetGraphModel net : editorSpec.getNets())
            removeUnnecessaryDecorators(net);
    }


  private static void removeUnnecessaryDecorators(NetGraphModel editorNet) {
    Set tasks = NetUtilities.getAllTasks(editorNet);
    Iterator taskIterator = tasks.iterator();
    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();
      if (editorTask.hasJoinDecorator() && editorTask.getIncomingFlowCount() < 2) {
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
  
  private static int engineToEditorJoin(YTask engineTask) {
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

  private static int engineToEditorSplit(YTask engineTask) {
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
  
  private static int engineToEditorMultiInstanceCreationMode(String engineCreationMode) {
    if (engineCreationMode.equals(YMultiInstanceAttributes._creationModeStatic)) {
      return MultipleAtomicTask.STATIC_INSTANCE_CREATION;
    }
    if (engineCreationMode.equals(YMultiInstanceAttributes._creationModeDynamic)) {
      return MultipleAtomicTask.DYNAMIC_INSTANCE_CREATION;
    }
    return MultipleAtomicTask.STATIC_INSTANCE_CREATION;
  }

    private static void mapUniqueElementID(YAWLVertex vertex, String engineID) {
      int engNbr = vertex.setActualEngineID(engineID);
      if (engNbr > -1) {
          vertex.setEngineIdNumber(String.valueOf(engNbr));
          updateMaxEngineNumber(engNbr);
      }
    }

    
    private static void updateMaxEngineNumber(int nbr) {
      if (nbr > _maxEngineNumber) _maxEngineNumber = nbr;
    }


    private static int showDisconnectedResourceServiceWarning() {
        Object[] buttonText = {"Close", "Continue"};
        return JOptionPane.showOptionDialog(
                YAWLEditor.getInstance(),
                "The loaded specification contains resource settings, but the resource\n " +
                "service is currently offline. This means that the settings cannot be\n "+
                "validated and will be LOST if the specification is saved. It is\n " +
                "suggested that the specification be closed, a valid connection to\n " +
                "the resource service is established (via the Tools menu), then\n " +
                "the specification be reloaded.\n\n" +
                "Click the 'Close' button to close the loaded file (recommended)\n " +
                "or the 'Continue' button to keep it loaded, but with resourcing\n " +
                "settings stripped.",
                "Warning - read carefully",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                buttonText,
                buttonText[0]);
    }    

}