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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;

import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.JoinDecorator;
import au.edu.qut.yawl.editor.elements.model.SplitDecorator;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import au.edu.qut.yawl.editor.net.NetElementSummary;
import au.edu.qut.yawl.editor.net.NetGraphModel;

import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.specification.SpecificationUtilities;

import au.edu.qut.yawl.editor.thirdparty.orgdatabase.OrganisationDatabaseProxy;
import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.unmarshal.YMetaData;

public class EngineSpecificationExporter extends EngineEditorInterpretor {
  
  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);

  public static String VERIFICATION_WITH_EXPORT_PREFERENCE = "verifyWithExportCheck";
  public static String ANALYSIS_WITH_EXPORT_PREFERENCE = "analyseWithExportCheck";
  
  public static void exportEngineSpecToFile(SpecificationModel editorSpec, String fullFileName) {
     exportStringToFile(
         getEngineSpecificationXML(
             editorSpec
         ),
         fullFileName
     );
  }
  
  public static void checkAndExportEngineSpecToFile(SpecificationModel editorSpec, String fullFileName) {
    exportStringToFile(
        getAndCheckEngineSpecificationXML(
            editorSpec
        ),
        fullFileName
    );
  }

  
  private static void exportStringToFile(String string, String fullFileName) {
    try {
      PrintStream outputStream = 
        new PrintStream(
            new BufferedOutputStream(new FileOutputStream(fullFileName)),
            false,
            "UTF-8"
        );
      outputStream.println(string);

      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  
  public static String getAndCheckEngineSpecificationXML(SpecificationModel editorSpec) {
    boolean verificationNeeded = prefs.getBoolean(VERIFICATION_WITH_EXPORT_PREFERENCE, true);
    boolean analysisNeeded = prefs.getBoolean(ANALYSIS_WITH_EXPORT_PREFERENCE, true);

    YSpecification engineSpec = getEngineSpecAsEngineObjects(editorSpec);   
    
    List<String> results = new LinkedList<String>();
    if (verificationNeeded) {
      results.addAll(EngineSpecificationValidator.getValidationResults(engineSpec));
    }
    
    if (analysisNeeded) {
      results.addAll(
        YAWLEngineProxy.getInstance().getAnalysisResults(editorSpec)
      );
    }

    YAWLEditor.getInstance().showProblemList(
        editorSpec, 
        "Export problems", 
        "Checking exported file...", 
        results
    );
    
    return getEngineSpecificationXML(engineSpec);
  }
  
  public static String getEngineSpecificationXML(SpecificationModel editorSpec) {
    return getEngineSpecificationXML(
       getEngineSpecAsEngineObjects(
           editorSpec
       )    
    ); 
  }
  
  public static String getEngineSpecificationXML(YSpecification engineSpec) {
    try {
      return YMarshal.marshal(engineSpec);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  
  public static YSpecification getEngineSpecAsEngineObjects(SpecificationModel editorSpec) {
    initialise();

    YSpecification engineSpec = new YSpecification(
        editorSpec.getId()
    );
    
    generateEngineMetaData(editorSpec,engineSpec);
    
    generateEngineDataTypeDefinition(editorSpec,engineSpec);

    //important:  Engine API expects nets to be pre-generated before composite tasks reference them.
    //            We need to build the nets first, and THEN populate the nets with elements.
    
    generateRootNet(editorSpec,engineSpec);
    generateSubNets(editorSpec,engineSpec);
    
    populateEngineNets(editorSpec,engineSpec);
      
    return engineSpec;
  }
  
  private static void generateEngineDataTypeDefinition(SpecificationModel editorSpec, 
                                                YSpecification engineSpec) {
    try {
      engineSpec.setSchema(
          editorSpec.getDataTypeDefinition()
      );
    } catch (Exception eActual) {
      try {
        engineSpec.setSchema(
            SpecificationModel.DEFAULT_TYPE_DEFINITION
        );      
      } catch (Exception eDefault) {}
    }
  }
  
  private static void generateEngineMetaData(SpecificationModel editorSpec, 
                                             YSpecification engineSpec) {
    
    engineSpec.setBetaVersion(YSpecification._Beta6);
    
    YMetaData metaData = new YMetaData();

    if (editorSpec.getName() != null && 
        !editorSpec.getName().trim().equals("")) {
      metaData.setTitle(
          XMLUtilities.quoteSpecialCharacters(
            editorSpec.getName()
          )
      );
    }
    if (editorSpec.getDescription() != null &&
        !editorSpec.getDescription().trim().equals("")) {
      metaData.setDescription(
          XMLUtilities.quoteSpecialCharacters(
            editorSpec.getDescription()
          )
      );
    }
    if (editorSpec.getAuthor() != null &&
        !editorSpec.getAuthor().trim().equals("")) {
      metaData.setCreator(
          XMLUtilities.quoteSpecialCharacters(
            editorSpec.getAuthor()
          )
      );
    }
    if (editorSpec.getVersionNumber() != null &&
        !editorSpec.getVersionNumber().trim().equals("")) {
      metaData.setVersion(
          XMLUtilities.quoteSpecialCharacters(
            editorSpec.getVersionNumber()
          )
      );
    }
    try {
      if (editorSpec.getValidFromTimestamp() != null &&
          !editorSpec.getValidFromTimestamp().trim().equals("")) {
        metaData.setValidFrom(
            TIMESTAMP_FORMAT.parse(
                editorSpec.getValidFromTimestamp()
            )
        );
      }
      if (editorSpec.getValidUntilTimestamp() != null &&
          !editorSpec.getValidUntilTimestamp().trim().equals("")) {
        metaData.setValidUntil(
            TIMESTAMP_FORMAT.parse(
                editorSpec.getValidUntilTimestamp()
            )
        );
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    engineSpec.setMetaData(metaData);
  }
  
  private static void generateRootNet(SpecificationModel editorSpec, 
                                      YSpecification engineSpec) {
    YNet rootEngineNet = 
      generateEngineNet(
          engineSpec, 
          editorSpec.getStartingNet()
      );

    engineSpec.setRootNet(rootEngineNet);
    editorToEngineNetMap.put(
        editorSpec.getStartingNet(), 
        rootEngineNet
    );
  }
  
  private static void generateSubNets(SpecificationModel editorSpec, 
                                      YSpecification engineSpec) {
    Iterator subNetIterator = editorSpec.getSubNets().iterator();
    while (subNetIterator.hasNext()) {
      NetGraphModel editorNet = (NetGraphModel) subNetIterator.next();
      
      YNet engineSubNet = 
        generateEngineNet(engineSpec, editorNet);
      engineSpec.setDecomposition(engineSubNet);

      editorToEngineNetMap.put(editorNet, engineSubNet);
    }
  }
  
  private static YNet generateEngineNet(YSpecification engineSpec, NetGraphModel editorNet) {
    YNet engineNet = new YNet(XMLUtilities.toValidXMLName(editorNet.getName()),
                              engineSpec);

    generateDecompositionParameters(
        engineNet, 
        editorNet.getDecomposition()
    );
    
    establishEngineLocalVariables(engineNet, editorNet);
    
    return engineNet;
  }

  private static void establishEngineLocalVariables(YNet engineNet, NetGraphModel editorNet) {
    Iterator localVarIterator = editorNet.getDecomposition().getVariables().getLocalVariables().iterator();
    
    while(localVarIterator.hasNext()) {
      DataVariable editorNetVariable = (DataVariable) localVarIterator.next();
      
      YVariable engineNetVariable = new YVariable(engineNet);
      engineNetVariable.setDataTypeAndName(
          editorNetVariable.getDataType(),
          editorNetVariable.getName(),
          XML_SCHEMA_URI
      );

      engineNetVariable.setInitialValue(
        editorNetVariable.getInitialValue()
      );
      
      engineNet.setLocalVariable(engineNetVariable);
    }
    
    // There is a requirement in the engine that passing data to a net's output parameters needs
    // the net to have a local variable of the same name (and type, I assume) as the output parameter.
    // Lachlan assures me that the assignment is automatic so long as the name of the net local variable
    // and the net output parameter are the same. Highly redundant IMO, but it won't work in the engine 
    // without this intermediate variable being used for state transport.
    
    Iterator outputVarIterator = editorNet.getDecomposition().getVariables().getOutputVariables().iterator();
    while (outputVarIterator.hasNext()) {
      DataVariable editorNetVariable = (DataVariable) outputVarIterator.next();
      if (editorNetVariable.getUsage() != DataVariable.USAGE_OUTPUT_ONLY) {
        continue;
      }

      YVariable engineNetVariable = new YVariable(engineNet);
      engineNetVariable.setDataTypeAndName(
          editorNetVariable.getDataType(),
          editorNetVariable.getName(),
          XML_SCHEMA_URI
      );

      engineNet.setLocalVariable(engineNetVariable);
    }
  }
  
  private static void populateEngineNets(SpecificationModel editorSpec, YSpecification engineSpec) {
    Iterator netIterator = editorToEngineNetMap.keySet().iterator();
    while (netIterator.hasNext()) {
      NetGraphModel editorNet = (NetGraphModel) netIterator.next();
      populateEngineNetFrom(editorSpec,engineSpec, editorNet);
    }
  }

  private static void populateEngineNetFrom(SpecificationModel editorSpec, 
                                     YSpecification engineSpec, 
                                     NetGraphModel editorNet)  {
    YNet engineNet = (YNet) editorToEngineNetMap.get(editorNet);
    NetElementSummary editorNetSummary = new NetElementSummary(editorNet);

    engineNet.setInputCondition(generateInputCondition(editorSpec, engineNet, editorNetSummary));
    engineNet.setOutputCondition(generateOutputCondition(editorSpec, engineNet, editorNetSummary));
    
    setElements(editorSpec,engineSpec, engineNet, editorNetSummary);
    setFlows(editorSpec, engineNet, editorNetSummary);
    setCancellationSetDetail(editorNetSummary);
  }
  
  private static YInputCondition generateInputCondition(SpecificationModel editorSpec, 
                                                        YNet engineNet, 
                                                        NetElementSummary editorNetSummary) {
    YInputCondition engineInputCondition = 
      new YInputCondition(
          getEngineElementID(
              editorSpec, 
              editorNetSummary.getInputCondition()
          ),
          engineNet
      );
    
    if (editorNetSummary.getInputCondition().hasLabel()) {
      engineInputCondition.setName(
          XMLUtilities.quoteSpecialCharacters(
              editorNetSummary.getInputCondition().getLabel()
          )
      );
    }

    editorToEngineElementMap.put(
        editorNetSummary.getInputCondition(), 
        engineInputCondition);

    return engineInputCondition;
  }

  private static YOutputCondition generateOutputCondition(SpecificationModel editorSpec, 
                                                          YNet engineNet, 
                                                          NetElementSummary editorNetSummary) {
    YOutputCondition engineOutputCondition = 
      new YOutputCondition(
          getEngineElementID(
              editorSpec, 
              editorNetSummary.getOutputCondition()
          ),
          engineNet
      );
    
    
    if (editorNetSummary.getOutputCondition().hasLabel()) {
      engineOutputCondition.setName(
          XMLUtilities.quoteSpecialCharacters(
              editorNetSummary.getOutputCondition().getLabel()
          )
      );
    }

    editorToEngineElementMap.put(
        editorNetSummary.getOutputCondition(), 
        engineOutputCondition);

    return engineOutputCondition;
  }
  
  private static void setElements(SpecificationModel editorSpec,
                                  YSpecification engineSpec, 
                                  YNet engineNet, 
                                  NetElementSummary editorNetSummary) {
    setConditions(editorSpec, engineNet, editorNetSummary);
    setAtomicTasks(editorSpec, engineSpec, engineNet, editorNetSummary);
    setCompositeTasks(editorSpec,engineNet, editorNetSummary);
  }
  
  private static void setConditions(SpecificationModel editorSpec, 
                                    YNet engineNet, 
                                    NetElementSummary editorNetSummary) {
    
    Iterator conditionIterator = 
      editorNetSummary.getConditions().iterator();
    
    while(conditionIterator.hasNext()) {
      Condition editorCondition = (Condition) conditionIterator.next();
      
      YCondition engineCondition = 
        new YCondition(
          getEngineElementID(editorSpec, editorCondition),
          engineNet);
      
      if (editorCondition.hasLabel()) {
        engineCondition.setName(
            XMLUtilities.quoteSpecialCharacters(
                editorCondition.getLabel()
            )
        );
      }
      
      engineNet.addNetElement(engineCondition);

      editorToEngineElementMap.put(editorCondition, engineCondition);
    }
  }
  
  private static void setAtomicTasks(SpecificationModel editorSpec,
                                     YSpecification engineSpec, 
                                     YNet engineNet, 
                                     NetElementSummary editorNetSummary) {
    
    Iterator taskIterator = editorNetSummary.getAtomicTasks().iterator();
    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();

      YAtomicTask engineAtomicTask = 
        new YAtomicTask(
          getEngineElementID(editorSpec, editorTask),
          editorToEngineJoin(editorTask),
          editorToEngineSplit(editorTask),   
          engineNet
        );
      
     if (editorTask.hasLabel()) {
       engineAtomicTask.setName(
           XMLUtilities.quoteSpecialCharacters(
               editorTask.getLabel()
           )
       );
     }

      if (editorTask.getDecomposition() != null) {
        YAWLServiceGateway engineDecomposition = 
          (YAWLServiceGateway) engineSpec.getDecomposition(
              editorTask.getDecomposition().getLabelAsElementName()
          );
        if (engineDecomposition == null) {
          engineDecomposition = 
            generateAtomicDecompositionFor(
                engineSpec,
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
      populateResourceDetail(engineAtomicTask, editorTask);
      
      engineNet.addNetElement(engineAtomicTask);
      editorToEngineElementMap.put(editorTask, engineAtomicTask);
    }
  }
  
  private static YAWLServiceGateway generateAtomicDecompositionFor(YSpecification engineSpec, 
                                                                   YAWLTask editorTask) {

    WebServiceDecomposition editorDecomposition = 
      ((YAWLAtomicTask)editorTask).getWSDecomposition();
    
    YAWLServiceGateway engineDecomposition = 
      new YAWLServiceGateway(
          editorDecomposition.getLabelAsElementName(), 
          engineSpec
      );
    
    if (taskNeedsWebServiceDetail(editorTask)) {
    
      YAWLServiceReference engineService = new YAWLServiceReference(
        editorDecomposition.getYawlServiceID(),
        engineDecomposition
      );

      engineDecomposition.setYawlService(engineService);
    }
    
    engineSpec.setDecomposition(engineDecomposition);
    
    return engineDecomposition;
  }
  
  private static void generateDecompositionParameters(YDecomposition engineDecomposition, 
                                                      Decomposition editorDecomposition) {
    if(editorDecomposition == null) {
      return;
    }
    
    generateDecompositionInputParameters(engineDecomposition, editorDecomposition);
    generateDecompositionOutputParameters(engineDecomposition, editorDecomposition);
  }
  
  private static void generateDecompositionInputParameters(YDecomposition engineDecomposition, 
                                                           Decomposition editorDecomposition) {
    Iterator inputIterator = 
      editorDecomposition.getVariables().getInputVariables().iterator();
    
    int ordering = 0;

    while(inputIterator.hasNext()) {
      DataVariable editorInputVariable = 
        (DataVariable) inputIterator.next();
      
      // Don't need to create parameters for local variables.
      if(editorInputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
        continue;
      }
      
      generateEngineParameter(
          engineDecomposition,
          YParameter._INPUT_PARAM_TYPE, 
          editorInputVariable.getDataType(),
          editorInputVariable.getName(),
          editorInputVariable.getInitialValue(),
          ordering++
      );
    }  
  }
  
  private static void generateEngineParameter(YDecomposition engineDecomposition, 
                                              int engineParameterType,
                                              String dataType, 
                                              String paramName, 
                                              String initialValue,
                                              int ordering) {

    YParameter engineParameter = 
      new YParameter(
          engineDecomposition, 
          engineParameterType
    ); 
    
    engineParameter.setDataTypeAndName(
      dataType,
      paramName,
      XML_SCHEMA_URI
    );
    
    engineParameter.setOrdering(ordering);
    
    /*  Engine BETA 3/4 doesn't like initial values for output parameters. */
    if (engineParameterType == YParameter._INPUT_PARAM_TYPE) {
      if (!(initialValue == null) && !initialValue.equals("")) {
        engineParameter.setInitialValue(
            XMLUtilities.quoteSpecialCharacters(
                initialValue
            )
        );
      }
    }
    
    // TODO: need somehow to tell the decomposition about enablement params as well.

    if (engineParameterType == YParameter._INPUT_PARAM_TYPE) {
      engineDecomposition.setInputParam(engineParameter);
    } else {
      engineDecomposition.setOutputParameter(engineParameter);
    }
  }
  
  private static void generateDecompositionOutputParameters(YDecomposition engineDecomposition, 
                                                            Decomposition editorDecomposition) {
    Iterator outputIterator = 
      editorDecomposition.getVariables().getOutputVariables().iterator();

    int ordering = editorDecomposition.getVariableCount();

    while(outputIterator.hasNext()) {
      DataVariable editorOutputVariable = 
        (DataVariable) outputIterator.next();
      
      // Don't need to create parameters for local variables.
      if(editorOutputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
        continue;
      }
      
      generateEngineParameter(
          engineDecomposition,
          YParameter._OUTPUT_PARAM_TYPE,
          editorOutputVariable.getDataType(),
          editorOutputVariable.getName(),
          editorOutputVariable.getInitialValue(),
          ordering++
      );
    }
  }

  private static void setCompositeTasks(SpecificationModel editorSpec, YNet engineNet, NetElementSummary editorNetSummary) {
    Iterator taskIterator = editorNetSummary.getCompositeTasks().iterator();

    while(taskIterator.hasNext()) {
      YAWLTask editorTask = (YAWLTask) taskIterator.next();
      
      YCompositeTask engineCompositeTask = 
        new YCompositeTask(
          getEngineElementID(editorSpec, editorTask),
          editorToEngineJoin(editorTask),
          editorToEngineSplit(editorTask),   
        engineNet
      );
      
      if (editorTask.hasLabel()) {
        engineCompositeTask.setName(
            XMLUtilities.quoteSpecialCharacters(
                editorTask.getLabel()
            )
        );
      }
      
      if (editorTask.getDecomposition() != null) {
        NetGraphModel editorUnfoldingNet = 
          SpecificationUtilities.getNetModelFromName(
              editorSpec,
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
  private static void populateTaskParameterQueries(YTask engineTask, YAWLTask editorTask) {
    populateTaskInputParameterQueries(engineTask, editorTask);
    populateTaskOutputParameterQueries(engineTask, editorTask);
  }
  
  private static void populateTaskInputParameterQueries(YTask engineTask, 
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
               XMLUtilities.getTaggedOutputVariableWithContent(
                   editorInputParameter.getVariable().getName(), 
                   editorInputParameter.getQuery()
               ), 
               editorInputParameter.getVariable().getName()
          );
        }
      } else {
        engineTask.setDataBindingForInputParam(
            XMLUtilities.getTaggedOutputVariableWithContent(
                editorInputParameter.getVariable().getName(), // NPE happening here
                editorInputParameter.getQuery()
            ), 
            editorInputParameter.getVariable().getName()
        );
      }
    }
  }
  
  private static void populateResourceDetail(YTask engineTask, YAWLTask editorTask) {
    populateResourceAllocationDetail(engineTask, editorTask);
    populateResourceAuthorisationDetail(engineTask, editorTask);
  }
  
  private static void populateResourceAllocationDetail(YTask engineTask, YAWLTask editorTask) {
    if (editorTask.getAllocationResourceMapping() == null) {
      return;
    }
    
    if (editorTask.getAllocationResourceMapping().getMappingType() == 
        ResourceMapping.ALLOCATE_TO_ANYONE) {
      return;
    }
    
    generateEngineParameter(
        engineTask.getDecompositionPrototype(),
        YParameter._ENABLEMENT_PARAM_TYPE,
        "string",
        ENGINE_RESOURCE_ALLOCATION_PARAMETER,
        null,
        0
    );

    engineTask.setDataBindingForEnablementParam(
        XMLUtilities.getTaggedOutputVariableWithContent(
            ENGINE_RESOURCE_ALLOCATION_PARAMETER,
            quoteSQLQueryForEngine(
                OrganisationDatabaseProxy.getInstance().getQueryFromResourceMapping(
                    editorTask.getAllocationResourceMapping()                
                )
            )
        ), 
        ENGINE_RESOURCE_ALLOCATION_PARAMETER
    );
  }

  private static void populateResourceAuthorisationDetail(YTask engineTask, YAWLTask editorTask) {
    if (editorTask.getAuthorisationResourceMapping() == null) {
      return;
    }

    if (editorTask.getAuthorisationResourceMapping().getMappingType() == 
        ResourceMapping.AUTHORISATION_UNNECESSARY) {
      return;
    }
    
    generateEngineParameter(
        engineTask.getDecompositionPrototype(),
        YParameter._ENABLEMENT_PARAM_TYPE,
        "string",
        ENGINE_RESOURCE_AUTHORISATION_PARAMETER,
        null,
        1
    );

    engineTask.setDataBindingForEnablementParam(
        XMLUtilities.getTaggedOutputVariableWithContent(
            ENGINE_RESOURCE_AUTHORISATION_PARAMETER,
            quoteSQLQueryForEngine(
                OrganisationDatabaseProxy.getInstance().getQueryFromResourceMapping(
                    editorTask.getAuthorisationResourceMapping()                
                )
            )
        ), 
        ENGINE_RESOURCE_AUTHORISATION_PARAMETER
    );
  }

  private static boolean taskNeedsWebServiceDetail(YAWLTask editorTask) {
    if (!(editorTask.getDecomposition() instanceof WebServiceDecomposition)) {
      return false;
    }
    
    WebServiceDecomposition decomposition = 
      (WebServiceDecomposition) editorTask.getDecomposition();

    if (decomposition.getYawlServiceID() == null ||
        decomposition.getYawlServiceID().trim().equals("")) {
      return false;
    }
    return true;
  }
  
  private static void populateTaskOutputParameterQueries(YTask engineTask, 
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
          engineTask.setDataBindingForOutputExpression(
              XMLUtilities.getTaggedOutputVariableWithContent(
                  editorOutputParameter.getVariable().getName(), 
                  editorOutputParameter.getQuery()
              ), 
              editorOutputParameter.getVariable().getName()
          );
        }
      } else {
        engineTask.setDataBindingForOutputExpression(
            XMLUtilities.getTaggedOutputVariableWithContent(
                editorOutputParameter.getVariable().getName(), 
                editorOutputParameter.getQuery()
            ), 
            editorOutputParameter.getVariable().getName()
        );
      }
    }
  }
  
  private static void populateMultipleInstanceDetail(YTask engineTask, 
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

    if (editorNetResultVariable != null) {
      engineTask.setMultiInstanceOutputDataMappings(
          editorMultiInstanceTask.getInstanceQuery(), 
          XMLUtilities.getTaggedOutputVariableWithContent(
              editorNetResultVariable.getName(),
              editorMultiInstanceTask.getAggregateQuery()
          )
      );
    }
  }
  
  private static void setFlows(SpecificationModel editorSpec, YNet engineNet, NetElementSummary editorNetSummary) {
    Iterator flowIterator = editorNetSummary.getFlows().iterator();
    while(flowIterator.hasNext()) {
      YAWLFlowRelation editorFlow = (YAWLFlowRelation) flowIterator.next(); 
      
      Object editorFlowSource = 
        NetGraphModel.getSourceVertex(editorNetSummary.getModel(), editorFlow);
      
      if (editorFlowSource instanceof VertexContainer) {
        editorFlowSource = ((VertexContainer) editorFlowSource).getVertex();
      }
      if (editorFlowSource instanceof Decorator) {
        VertexContainer container = (VertexContainer) ((Decorator) editorFlowSource).getParent();
        editorFlowSource = container.getVertex();                  
      }
      
      Object editorFlowTarget = 
        NetGraphModel.getTargetVertex(editorNetSummary.getModel(), editorFlow);
      
      if (editorFlowTarget instanceof VertexContainer) {
        editorFlowTarget = ((VertexContainer) editorFlowTarget).getVertex();
      }
      if (editorFlowTarget instanceof Decorator) {
        VertexContainer container = (VertexContainer) ((Decorator) editorFlowTarget).getParent();
        editorFlowTarget = container.getVertex();                  
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
            getNewUniqueEngineIDNumber(editorSpec) + "_ImplicitCondition",
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
  
  private static void setCancellationSetDetail(NetElementSummary editorNetSummary) {
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
  
  private static String getEngineElementID(SpecificationModel editorSpec, YAWLVertex element) {
    if (element.getEngineIdNumber() == null || element.getEngineIdNumber().equals("")) {
      element.setEngineIdNumber(
          getNewUniqueEngineIDNumber(editorSpec)
      );
    }
    return element.getEngineId();
  }
  
  private static String getNewUniqueEngineIDNumber(SpecificationModel editorSpec) {
    editorSpec.setUniqueElementNumber(
        editorSpec.getUniqueElementNumber() + 1
    );
    
    return Long.toString(
        editorSpec.getUniqueElementNumber()
    );  
  }

  private static int editorToEngineJoin(YAWLTask task) {
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

  private static int editorToEngineSplit(YAWLTask task) {
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

  private static String editorToEngineMultiInstanceMode(YAWLMultipleInstanceTask task) {
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

  private static void addFlowConditionMapping(YAWLFlowRelation editorFlow, 
                                      YCondition engineCondition) {
    editorFlowEngineConditionMap.put(editorFlow, engineCondition);
  } 
  
  private static YCondition getConditionForFlow(YAWLFlowRelation editorFlow) {
    return (YCondition) editorFlowEngineConditionMap.get(editorFlow);
  }
  
  
  /**
   * This method is a special request from Lachlan to supply the engine with all (') characters
   * converted to ($apos;) strings. There is a problem in the engine support libraries with 
   * processing the XQuery string that is really an SQL statement that he won't be investigating 
   * just yet. This method is a workaround until that issue is resolved.
   */
  
  private static String quoteSQLQueryForEngine(String sqlQuery) {
    return sqlQuery.replaceAll("\'","\\$apos;");
  }

}
