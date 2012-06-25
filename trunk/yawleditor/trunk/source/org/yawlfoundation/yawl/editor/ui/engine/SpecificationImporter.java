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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.core.YEditorSpecification;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.specification.*;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.ui.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.xml.datatype.Duration;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class SpecificationImporter extends EngineEditorInterpretor {
  
  private static final Point DEFAULT_LOCATION = new Point(100,100);
  private static List<String> _invalidResourceReferences;


    public static void importSpecificationFromFile(SpecificationModel editorSpec,
                                                   String fullFileName) {
        String errorMsg = "";
        try {
            editorSpec.loadFromFile(fullFileName);
        }
        catch (IOException ioe) {
            errorMsg = ioe.getMessage();
        }

        if (SpecificationModel.getSpec() == null || errorMsg.length() > 0) {
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Failed to load specification.\n" +
                            (errorMsg.length() > 0 ? "Reason: " + errorMsg : ""),
                    "Specification File Load Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        _invalidResourceReferences = new ArrayList<String>();
        convertEngineSpecObjectsToEditorObjects(editorSpec);

          if (SpecificationModel.getSpec().getLayoutXML() != null) {
              try {
                  LayoutImporter.importAndApply(SpecificationModel.getInstance());
              }
              catch (YLayoutParseException ylpe) {
                 removeUnnecessaryDecorators(editorSpec);
                 DefaultLayoutArranger.layoutSpecification();
              }
        }
        else {
            removeUnnecessaryDecorators(editorSpec);
            DefaultLayoutArranger.layoutSpecification();
        }

        SpecificationModel.getInstance().setFileName(fullFileName);
        SpecificationFileModel.getInstance().incrementFileCount();
        SpecificationUndoManager.getInstance().discardAllEdits();

        if (! _invalidResourceReferences.isEmpty()) {
            showInvalidResourceReferences();
            if (! YConnector.isResourceConnected()) {
                if (showDisconnectedResourceServiceWarning() == JOptionPane.YES_OPTION) {
                    SpecificationArchiveHandler.getInstance().processCloseRequest();
                }
            }
        }

        ConfigurationImporter.ApplyConfiguration();
        reset();
    }


  public static Element getLayoutElement(YEditorSpecification spec) {
      return JDOMUtil.stringToElement(spec.getLayoutXML());
  }
  
  public static void convertEngineSpecObjectsToEditorObjects(
                          SpecificationModel editorSpec) {
    initialise();
      YSpecification engineSpec = SpecificationModel.getSpec().getSpecification();
    editorSpec.setId(engineSpec.getURI());
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
          metaData.getCreators().iterator().next()
      );
    }

    
    SpecificationModel.getInstance().setVersionNumber(
          metaData.getVersion()
    );

    // reset version change for file open
    SpecificationModel.getInstance().setVersionChanged(false);  

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
    
    NetGraphModel editorNetModel = convertEngineNet(engineRootNet, true);
    
    editorNetModel.setIsStartingNet(true);
    String gateway = engineRootNet.getExternalDataGateway();
    if (gateway != null) {
        editorNetModel.setExternalDataGateway(gateway);
    }

  }
  
  private static NetGraphModel convertEngineNet(YNet engineNet, boolean isRootNet) {

    NetGraph editorNet = new NetGraph(isRootNet);
    editorNet.setName(engineNet.getID());
    
    convertDecompositionParameters(
        engineNet, 
        editorNet.getNetModel().getDecomposition()
    );

    convertNetLocalVariables(engineNet, editorNet);
    convertLogPredicates(engineNet, editorNet.getNetModel().getDecomposition());
    SpecificationModel.getInstance().addNetNotUndoable(editorNet.getNetModel());

    YAWLEditorDesktop.getInstance().openNet(editorNet);

    editorToEngineNetMap.put(engineNet, editorNet.getNetModel());

    return editorNet.getNetModel();
  }

  private static void convertNetLocalVariables(YNet engineNet, 
                                               NetGraph editorNet) {

    List<YVariable> localVarList =
            new ArrayList<YVariable>(engineNet.getLocalVariables().values()) ;
    Collections.sort(localVarList);

    for (YVariable engineVariable : localVarList) {  
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
          engineVariable.getOrdering(),
          dataType,
          engineVariable.getName(),
          initialValue,
          engineVariable.getDefaultValue(),
          new YLogPredicate(),   // local vars don't have log predicates
          new Hashtable()      // local vars don't have extended attributes
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
                    engineParameter.getOrdering(),
                    engineParameter.getDataTypeName(),
                    engineParameter.getName(),
                    engineParameter.getInitialValue(),
                    engineParameter.getDefaultValue(),
                    engineParameter.getLogPredicate(),
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
                    engineParameter.getOrdering(),
                    engineParameter.getDataTypeName(),
                    engineParameter.getName(),
                    engineParameter.getInitialValue(),
                    JDOMUtil.decodeEscapes(engineParameter.getDefaultValue()),
                    engineParameter.getLogPredicate(),
                    engineParameter.getAttributes()
            );
        }
    }
  
  private static void createEditorVariable(Decomposition editorDecomposition, 
                                           int editorUsage,
                                           int index,
                                           String dataType, 
                                           String paramName, 
                                           String initialValue,
                                           String defaultValue,
                                           YLogPredicate logPredicate,
                                           Hashtable attributes) {
    
    DataVariable editorVariable = new DataVariable();

    editorVariable.setName(paramName);
    editorVariable.setIndex(index);
    editorVariable.setUsage(editorUsage);
    editorVariable.setDataType(dataType);
    editorVariable.setInitialValue(initialValue);
    editorVariable.setDefaultValue(defaultValue);
    editorVariable.setUserDefined(true);
    editorVariable.setAttributes(attributes);

    if (logPredicate != null) {
        editorVariable.setLogPredicateStarted(logPredicate.getStartPredicate());
        editorVariable.setLogPredicateCompletion(logPredicate.getCompletionPredicate());
    }

    editorDecomposition.getVariables().add(editorVariable);
 }
  
  private static void convertSubNetsAndOtherDecompositions(YSpecification engineSpecification) {
      for (YDecomposition engineDecomposition : engineSpecification.getDecompositions()) {
          if (engineDecomposition instanceof YNet &&
                  !engineDecomposition.equals(engineSpecification.getRootNet())) {

              YNet engineSubNet = (YNet) engineDecomposition;
              NetGraphModel editorNetModel = convertEngineNet(engineSubNet, false);

              SpecificationModel.getInstance().addNet(editorNetModel);

              editorToEngineNetMap.put(engineSubNet, editorNetModel);
          }
          if (engineDecomposition instanceof YAWLServiceGateway) {
              YAWLServiceGateway engineGateway = (YAWLServiceGateway) engineDecomposition;
              WebServiceDecomposition editorDecomposition = new WebServiceDecomposition();

              editorDecomposition.setLabel(engineGateway.getID());

              if (engineGateway.getYawlService() != null) {
                  YAWLServiceReference service;
                  try {
                      service = YConnector.getService(
                              engineGateway.getYawlService().getURI());
                  }
                  catch (IOException ioe) {
                      service = null;
                  }
                  if (service != null) {
                      editorDecomposition.setService(service);
                  }
                  else editorDecomposition.setUnresolvedURI(engineGateway.getYawlService().getURI());
              }

              convertDecompositionParameters(
                      engineGateway,
                      editorDecomposition
              );

              convertInteractionSettings(engineDecomposition, editorDecomposition);
              convertExtendedAttributes(engineDecomposition, editorDecomposition);
              convertLogPredicates(engineDecomposition, editorDecomposition);

              SpecificationModel.getInstance().addWebServiceDecomposition(editorDecomposition);
          }
      }
  }


  private static void convertInteractionSettings(YDecomposition engineDecomposition,
                                                 WebServiceDecomposition editorDecomposition) {
      editorDecomposition.setCodelet(engineDecomposition.getCodelet());
      editorDecomposition.setManualInteraction(engineDecomposition.requiresResourcingDecisions());
  }


    private static void convertLogPredicates(YDecomposition engineDecomposition,
                                             Decomposition editorDecomposition) {
        YLogPredicate predicate = engineDecomposition.getLogPredicate();
        if (predicate != null) {
            editorDecomposition.setLogPredicateStarted(predicate.getStartPredicate());
            editorDecomposition.setLogPredicateCompletion(predicate.getCompletionPredicate());
        }
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
    setConditionDocumentation(editorInputCondition, engineInputCondition);
    return editorInputCondition;
  }
  
  private static OutputCondition generateEditorOutputCondition(YOutputCondition engineOutputCondition) {
    OutputCondition editorOutputCondition = new OutputCondition(DEFAULT_LOCATION);
    mapUniqueElementID(editorOutputCondition, engineOutputCondition.getID()) ;
    setConditionDocumentation(editorOutputCondition, engineOutputCondition);
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
      setEditorTaskDocumentation((YAWLTask) editorAtomicTask, engineAtomicTask);

      setTaskDecorators(engineAtomicTask, (YAWLTask) editorAtomicTask, editorNet);
      setTaskResources(engineAtomicTask, editorAtomicTask, editorNet) ;
      setTaskTimers(engineAtomicTask, editorAtomicTask, editorNet);
      setTaskCustomForm(engineAtomicTask, editorAtomicTask);
     
     if(engineAtomicTask.getConfigurationElement() != null){
    	 ConfigurationImporter.CTaskList.add((YAWLTask) editorAtomicTask);
    	 ConfigurationImporter.map.put(editorAtomicTask,engineAtomicTask.getConfigurationElement() );
    	 ConfigurationImporter.NetTaskMap.put(editorAtomicTask,editorNet );

     }


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
        EngineIdentifier id = editorTask.getEngineIdentifier();
        if (! (id == null || id.getName().equals(ElementIdentifiers.DEFAULT_ELEMENT_NAME))) {
            editorNet.setElementLabel(editorTask, engineTask.getName());
        }
    }
  }

    private static void setEditorTaskDocumentation(YAWLTask editorTask, YTask engineTask) {
      String doco = engineTask.getDocumentation();
      if (doco != null) {
          editorTask.setDocumentation(doco);
      }
    }

    private static void setConditionDocumentation(YAWLCondition editorCondition, YCondition engineCondition) {
      String doco = engineCondition.getDocumentation();
      if (doco != null) {
          editorCondition.setDocumentation(doco);
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
        for (String targetParameter : engineTask.getParamNamesForTaskCompletion()) {
            String engineDataBinding = engineTask.getDataBindingForOutputParam(targetParameter);

            DataVariable editorVariable;
            if ((targetParameter != null) && (targetParameter.length() == 0) &&
                    engineDataBinding.startsWith("#external:")) {
                editorVariable = new DataVariable();
            }
            else {
                editorVariable = net.getDecomposition().getVariableWithName(targetParameter);
            }

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
      setEditorTaskDocumentation((YAWLTask) editorCompositeTask, engineCompositeTask);

      editorToEngineElementMap.put(
          engineCompositeTask,        
          editorCompositeTask
      );
      if(engineCompositeTask.getConfigurationElement() != null){
     	 ConfigurationImporter.CTaskList.add((YAWLTask) editorCompositeTask);
     	 ConfigurationImporter.map.put((YAWLTask) editorCompositeTask,engineCompositeTask.getConfigurationElement() );
     	 ConfigurationImporter.NetTaskMap.put(editorCompositeTask,editorNet );

      }
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

      setConditionDocumentation(editorCondition, engineCondition);    
      
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
        vertex.setEngineID(parseElementID(engineID));
    }
    
    private static EngineIdentifier parseElementID(String engineID) {
        int pos = engineID.lastIndexOf("_");
        if (pos < 0) return new EngineIdentifier(engineID);
        String suffixStr = engineID.substring(pos + 1);
        if (StringUtil.isIntegerString(suffixStr)) {
            return new EngineIdentifier(engineID.substring(0, pos),
                    StringUtil.strToInt(suffixStr, 0));
        }
        return new EngineIdentifier(engineID);
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