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

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.data.Parameter;
import org.yawlfoundation.yawl.editor.ui.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.ui.data.internal.YDocumentType;
import org.yawlfoundation.yawl.editor.ui.data.internal.YStringListType;
import org.yawlfoundation.yawl.editor.ui.data.internal.YTimerType;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.*;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.allocators.GenericAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.PiledExecution;
import org.yawlfoundation.yawl.resourcing.constraints.SeparationOfDuties;
import org.yawlfoundation.yawl.resourcing.filters.GenericFilter;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.OfferInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.resourcing.resource.SecondaryResources;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.swing.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class EngineSpecificationExporter extends EngineEditorInterpretor {

  public static void exportEngineSpecToFile(SpecificationModel editorSpec, String fullFileName) {
      if (checkUserDefinedDataTypes(editorSpec)) {
          String specXML = getEngineSpecificationXML(editorSpec);
          if (successful(specXML)) {
              exportStringToFile(addLayoutData(specXML, editorSpec), fullFileName);
          }
      }
      reset();
  }
  
  public static boolean checkAndExportEngineSpecToFile(SpecificationModel editorSpec, String fullFileName) {
      boolean success = false;
      try {
          if (checkUserDefinedDataTypes(editorSpec)) {
              String specXML = getAndCheckEngineSpecificationXML(editorSpec);
              success = successful(specXML);
              if (success) {
                  exportStringToFile(addLayoutData(specXML, editorSpec), fullFileName);
              }
          }
      }
      catch (Exception e) {
          JOptionPane.showMessageDialog(null,
                  "The attempt to save this specification to file failed.\n " +
                  "Please see the log for details", "Save File Error",
                  JOptionPane.ERROR_MESSAGE);
          LogWriter.error("Error saving specification to file.", e);
      }
      reset();
      return success;
  }

  private static String addLayoutData(String specXML, SpecificationModel editorSpec) {
      int closingTag = specXML.lastIndexOf("</");
      return specXML.substring(0, closingTag) +
             new LayoutExporter().export(editorSpec) +
             specXML.substring(closingTag) ;
  }


  private static boolean successful(String xml) {
    if ((xml == null) || (xml.equals("null")) || (xml.startsWith("<fail"))) {
      String msg = "File save process resulted in a 'null' specification.\n File not created.\n";
      if ((xml != null) && (xml.startsWith("<fail"))) {
         int start = xml.indexOf('>') + 1;
         int len = xml.lastIndexOf('<') - start;
         msg += "\nDetail: " + xml.substring(start, len) + '\n';
      }
      JOptionPane.showMessageDialog(null, msg, "Export File Generation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }
  
  private static void exportStringToFile(String string, String fullFileName) {
      String prettyString = JDOMUtil.formatXMLString(string);
      if (prettyString == null) {
          JOptionPane.showMessageDialog(null,
               "Unable to save specification to file. Please see log output for details.",
               "Export File Generation Error", JOptionPane.ERROR_MESSAGE);
          return;
      }

    try {
        if (UserSettings.getFileBackupOnSave()) {
            FileUtilities.backup(fullFileName, fullFileName + ".bak");     // back it up
        }
        if (SpecificationModel.getInstance().isVersionChanged() &&
                UserSettings.getFileVersioningOnSave()) {
            String versionedFileName = String.format("%s.%s.yawl",
                    FileUtilities.stripFileExtension(fullFileName),
                    SpecificationModel.getInstance().getPreviousVersionNumber().toString());
            if (! new File(versionedFileName).exists()) {
                FileUtilities.backup(fullFileName, versionedFileName);
            }    
        }

      // now save it
      PrintStream outputStream =
        new PrintStream(
            new BufferedOutputStream(new FileOutputStream(fullFileName)),
            false,
            "UTF-8"
        );
      outputStream.println(prettyString);

      outputStream.close();
    } catch (IOException e) {
        LogWriter.error("IO Exception saving specification to file.", e);
     }
  }


  private static boolean checkUserDefinedDataTypes(SpecificationModel editorSpec) {
      List<String> results = new ArrayList<String>();
      results.addAll(EngineSpecificationValidator.checkUserDefinedDataTypes(editorSpec));
      if (! results.isEmpty()) {
          YAWLEditor.getInstance().showProblemList("Export Errors", results);
          JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
               "Could not export Specification due to missing or invalid user-defined " +
               "datatypes.\nPlease see the problem list below for details.",
               "Export Datatype Error", JOptionPane.ERROR_MESSAGE);
      }
      return results.isEmpty();
  }

    public static String getAndCheckEngineSpecificationXML(SpecificationModel editorSpec) {
        YSpecification engineSpec = getEngineSpecAsEngineObjects(editorSpec);
        List<String> results = new LinkedList<String>();

        if (UserSettings.getVerifyOnSave()) {
            results.addAll(EngineSpecificationValidator.getValidationResults(engineSpec));
        }
        if (UserSettings.getAnalyseOnSave()) {
            results.addAll(editorSpec.analyse());
        }
        YAWLEditor.getInstance().showProblemList("Analysis Results", results);
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
      LogWriter.error("Error marshalling specification to XML.", e);
      return null;
    }
  }

  
  public static YSpecification getEngineSpecAsEngineObjects(SpecificationModel editorSpec) {
    initialise();

    YSpecification engineSpec = new YSpecification(
        editorSpec.getId()
    );
    
    generateEngineMetaData(editorSpec,engineSpec);
    
    //important:  Engine API expects nets to be pre-generated before composite tasks reference them.
    //            We need to build the nets first, and THEN populate the nets with elements.
    
    generateRootNet(editorSpec,engineSpec);
    generateSubNets(editorSpec,engineSpec);
    populateEngineNets(editorSpec,engineSpec);
    generateEngineDataTypeDefinition(editorSpec,engineSpec);

    return engineSpec;
  }
  
  private static void generateEngineDataTypeDefinition(SpecificationModel editorSpec, 
                                                YSpecification engineSpec) {
      String schema = adjustSchemaForInternalTypes(editorSpec.getDataTypeDefinition());

      // remove any header inadvertently inserted by user
      if (schema.startsWith("<?xml")) {
          schema = schema.substring(schema.indexOf('>') + 1);
      }
      try {
          engineSpec.setSchema(schema);
      }
      catch (Exception eActual) {
          try {
              schema = adjustSchemaForInternalTypes(SpecificationModel.DEFAULT_TYPE_DEFINITION);
              engineSpec.setSchema(schema);
          }
          catch (Exception eDefault) {}
     }
  }


    private static String adjustSchemaForInternalTypes(String specDataSchema) {
        String schema = YTimerType.adjustSchema(specDataSchema,
                                             SpecificationParametersIncludeYTimerType);
        schema = YStringListType.adjustSchema(schema,
                                          SpecificationParametersIncludeYStringListType);
        return YDocumentType.adjustSchema(schema,
                                          SpecificationParametersIncludeYDocumentType);
    }


  private static void generateEngineMetaData(SpecificationModel editorSpec, 
                                             YSpecification engineSpec) {
    
    engineSpec.setVersion(YSchemaVersion.TwoPointTwo);
    
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
        List<String> authors = new ArrayList<String>();
        authors.add(XMLUtilities.quoteSpecialCharacters(editorSpec.getAuthor()));
        metaData.setCreators(authors);
    }

    YSpecVersion version = editorSpec.getVersionNumber();
    if (version.toString().equals("0.0")) {
        version.minorIncrement();
    }
    metaData.setVersion(version);

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
      if (editorSpec.getUniqueID() != null) {
          metaData.setUniqueID(editorSpec.getUniqueID());
        }

    } catch (Exception e) {
        LogWriter.error("Error parsing timestamps.", e);
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
      engineSpec.addDecomposition(engineSubNet);

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

    YLogPredicate logPredicate = new YLogPredicate();
    logPredicate.setStartPredicate(editorNet.getDecomposition().getLogPredicateStarted());
    logPredicate.setCompletionPredicate(editorNet.getDecomposition().getLogPredicateCompletion());
    engineNet.setLogPredicate(logPredicate);

    if (editorNet.isStartingNet()) {
        String gateway = editorNet.getExternalDataGateway();
        if (gateway != null) {
            engineNet.setExternalDataGateway(gateway);
        }
    }
      
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

      String initialValue = editorNetVariable.getInitialValue();
      String dataType = editorNetVariable.getDataType();
      if (dataType.equals("string")) {
          initialValue = XMLUtilities.quoteSpecialCharacters(initialValue);
      }
      else if (dataType.equals(DataVariable.YAWL_SCHEMA_TIMER_TYPE)) {
        SpecificationParametersIncludeYTimerType = true ;
      }
      else if (dataType.equals(DataVariable.YAWL_SCHEMA_STRINGLIST_TYPE)) {
        SpecificationParametersIncludeYStringListType = true ;
      }
      else if (dataType.equals(DataVariable.YAWL_SCHEMA_YDOCUMENT_TYPE)) {
        SpecificationParametersIncludeYDocumentType = true ;
      }

      engineNetVariable.setInitialValue(initialValue);
      engineNetVariable.setOrdering(editorNetVariable.getIndex());
      engineNet.setLocalVariable(engineNetVariable);
    }
    
    // There is a requirement in the engine that passing data to a net's output parameters needs
    // the net to have a local variable of the same name (and type, I assume) as the output parameter.
    // Lachlan assures me that the assignment is automatic so long as the name of the net local variable
    // and the net output parameter are the same. Highly redundant IMO, but it won't work in the engine 
    // without this intermediate variable being used for state transport.
    for (DataVariable editorNetVariable : editorNet.getDecomposition().getVariables().getOutputVariables()) {
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
    for (Object o : editorToEngineNetMap.keySet()) {
      populateEngineNetFrom(editorSpec, engineSpec, (NetGraphModel) o);
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

        if (editorCondition.hasDocumentation()) {
             engineCondition.setDocumentation(
                 XMLUtilities.quoteSpecialCharacters(
                     editorCondition.getDocumentation()
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
     if(editorTask.isConfigurable()){
    	 DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
    	 ConfigurationExporter config= new ConfigurationExporter();
    	 engineAtomicTask.setConfiguration(config.getTaskConfiguration(editorTask));
    	 engineAtomicTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
     }
     if (editorTask.hasLabel()) {
       engineAtomicTask.setName(
           XMLUtilities.quoteSpecialCharacters(
               editorTask.getLabel()
           )
       );
     }
     
     if (editorTask.hasDocumentation()) {
          engineAtomicTask.setDocumentation(
              XMLUtilities.quoteSpecialCharacters(
                  editorTask.getDocumentation()
              )
          );
     }

     generateTimeoutDetailForAtomicTask(engineAtomicTask, editorTask);

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
      setCustomFormDetail(engineAtomicTask, editorTask);
      
      engineNet.addNetElement(engineAtomicTask);
      editorToEngineElementMap.put(editorTask, engineAtomicTask);
    }
  }
  
  private static void generateTimeoutDetailForAtomicTask(YTask engineTask, YAWLTask editorTask) {
    if (!(editorTask instanceof AtomicTask)) {
      return;
    }
    
    AtomicTask editorAtomicTask = (AtomicTask) editorTask;
    
    if (editorAtomicTask.getTimeoutDetail() == null ) {
      return;
    }
    
    if (editorAtomicTask.getTimeoutDetail().getTimeoutValue() != null) {
      Duration duration = null;
      try {
        duration = DatatypeFactory.newInstance().newDuration(
            editorAtomicTask.getTimeoutDetail().getTimeoutValue()
        );
      } catch (Exception e) {}
      
      if (duration != null) {
        engineTask.setTimerParameters(
            getEngineTimerTriggerForEditorTrigger(
              editorAtomicTask.getTimeoutDetail().getTrigger()
            ),
            duration
        );
      }
    }
    
    if (editorAtomicTask.getTimeoutDetail().getTimeoutDate() != null) {
      engineTask.setTimerParameters(
          getEngineTimerTriggerForEditorTrigger(
            editorAtomicTask.getTimeoutDetail().getTrigger()
          ),
          editorAtomicTask.getTimeoutDetail().getTimeoutDate()
      );
    }
    
    if (editorAtomicTask.getTimeoutDetail().getTimeoutVariable() != null) {
      // How come I can't set a trigger for the net variable?
      // Answer: specified in the variable. must be a complex type, known by the editor.
      engineTask.setTimerParameters(
        editorAtomicTask.getTimeoutDetail().getTimeoutVariable().getName()    
      );
    }
  }
  
  private static YWorkItemTimer.Trigger getEngineTimerTriggerForEditorTrigger(int editorTrigger) {
    switch(editorTrigger) {
      case TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT: {
        return YWorkItemTimer.Trigger.OnEnabled;
      }
      case TaskTimeoutDetail.TRIGGER_ON_STARTING: {
        return YWorkItemTimer.Trigger.OnExecuting;
      }
      default: {
        return YWorkItemTimer.Trigger.OnEnabled;
      }
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
        editorDecomposition.getServiceURI(),
        engineDecomposition
      );

      engineDecomposition.setYawlService(engineService);
    }
    
    engineSpec.addDecomposition(engineDecomposition);
    
    return engineDecomposition;
  }
  
  private static void generateDecompositionParameters(YDecomposition engineDecomposition, 
                                                      Decomposition editorDecomposition) {
    if(editorDecomposition == null) {
      return;
    }
    
    //BEGIN: MLF merge extended decomposition attributes
      for (String key : editorDecomposition.getAttributes().keySet()) {
          String value = editorDecomposition.getAttribute(key);
          if (value.length() > 0) {
              engineDecomposition.setAttribute(key, XMLUtilities.quoteSpecialCharacters(value));
          }
      }
    //END: MLF
    
    generateDecompositionInputParameters(engineDecomposition, editorDecomposition);
    generateDecompositionOutputParameters(engineDecomposition, editorDecomposition);
  }
  
  private static void generateDecompositionInputParameters(YDecomposition engineDecomposition, 
                                                           Decomposition editorDecomposition) {
    Iterator inputIterator = 
      editorDecomposition.getVariables().getInputVariables().iterator();
    
//    int ordering = 0;

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
          null,  // default value for input params not possible.
          editorInputVariable.getAttributes(),
          editorInputVariable.getLogPredicateStarted(),     
          editorInputVariable.getLogPredicateCompletion(),
       //   ordering++
          editorInputVariable.getIndex()
          
      );
    }  
  }
  
  private static void generateEngineParameter(YDecomposition engineDecomposition, 
                                              int engineParameterType,
                                              String dataType, 
                                              String paramName, 
                                              String initialValue,
                                              String defaultValue,
                                              Hashtable attributes,
                                              String logPredicateStarted,
                                              String logPredicateCompletion,
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
    
    //MLF: transfer extended attributes to engine parameter
    for(Enumeration enumer = attributes.keys(); enumer.hasMoreElements();)
    {
      String key = enumer.nextElement().toString();
      String value =  attributes.get(key).toString();
      if (value.length() > 0) {
          engineParameter.addAttribute(key, XMLUtilities.quoteSpecialCharacters(value));
      }    
    }

    YLogPredicate predicate = new YLogPredicate();
    predicate.setStartPredicate(logPredicateStarted);
    predicate.setCompletionPredicate(logPredicateCompletion);
    engineParameter.setLogPredicate(predicate);
    
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

    if (engineParameterType == YParameter._OUTPUT_PARAM_TYPE) {
      if (!(defaultValue == null) && !defaultValue.equals("")) {
        engineParameter.setDefaultValue(
            XMLUtilities.quoteSpecialCharacters(
                defaultValue
            )
        );
      }
    }
    
    if (engineParameterType == YParameter._INPUT_PARAM_TYPE) {
      engineDecomposition.addInputParameter(engineParameter);
    } else {
      engineDecomposition.addOutputParameter(engineParameter);
    }

    if (dataType.equals(DataVariable.YAWL_SCHEMA_TIMER_TYPE)) {
        SpecificationParametersIncludeYTimerType = true ;
    }
      if (dataType.equals(DataVariable.YAWL_SCHEMA_STRINGLIST_TYPE)) {
          SpecificationParametersIncludeYStringListType = true ;
      }
      if (dataType.equals(DataVariable.YAWL_SCHEMA_YDOCUMENT_TYPE)) {
        SpecificationParametersIncludeYDocumentType = true ;
      }
  }
  
  private static void generateDecompositionOutputParameters(YDecomposition engineDecomposition, 
                                                            Decomposition editorDecomposition) {
    Iterator outputIterator = 
      editorDecomposition.getVariables().getOutputVariables().iterator();

//    int ordering = editorDecomposition.getVariableCount();

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
          null,  // intial value for output params not possible
          editorOutputVariable.getDefaultValue(),
          editorOutputVariable.getAttributes(),
          editorOutputVariable.getLogPredicateStarted(),
          editorOutputVariable.getLogPredicateCompletion(),     
    //      ordering++
          editorOutputVariable.getIndex()
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
      if(editorTask.isConfigurable()){
    	 DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
     	 ConfigurationExporter config= new ConfigurationExporter();
     	engineCompositeTask.setConfiguration(config.getTaskConfiguration(editorTask));
     	engineCompositeTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
      }
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

          setDataBindingForParam(engineTask, editorInputParameter, true);
        }
      }
      else {
          setDataBindingForParam(engineTask, editorInputParameter, true);
      }
    }
  }

  private static void setDataBindingForParam(YTask engineTask,
                                             Parameter editorParameter, boolean input) {
      String name = editorParameter.getVariable().getName();
      String query = editorParameter.getQuery();
      if ((query != null) && (! query.startsWith("#external:"))) {
          query = XMLUtilities.getTaggedOutputVariableWithContent(name, query);
      }
      if (input) {
          engineTask.setDataBindingForInputParam(query, name);
      }
      else {
          engineTask.setDataBindingForOutputExpression(query, name);
      }
  }

  private static void setCustomFormDetail(YTask engineTask, YAWLTask editorTask) {
    if (!(editorTask instanceof YAWLAtomicTask)) {
      return;
    }
    String urlStr = editorTask.getCustomFormURL();
    if (urlStr != null) {
        try {
            engineTask.setCustomFormURI(new URL(urlStr));
        }
        catch (MalformedURLException mue) {
            // do nothing
        }
    }
  }

  private static void populateResourceDetail(YTask engineTask, YAWLTask editorTask) {
    populateResourceMappingDetail(engineTask, editorTask);
  }
  
  private static void populateResourceMappingDetail(YTask engineTask, YAWLTask editorTask) {
    if (!(editorTask instanceof YAWLAtomicTask)) {
      return;
    }
    
    // pure routing tasks also don't have anything to do with resourcing.
    if (editorTask.getDecomposition() == null) {
      return;
    }
    
    YAWLAtomicTask atomicEditorTask = (YAWLAtomicTask) editorTask;
    
    if (!atomicEditorTask.getWSDecomposition().invokesWorklist()) {
      return;
    }
    
    // Below, we should have specified a resource mapping, but it looks as if
    // we haven't. We supply a default at this point.
    
    if (atomicEditorTask.getResourceMapping() == null) {
      atomicEditorTask.setResourceMapping(
          new ResourceMapping()
      );
    }
    

    engineTask.getDecompositionPrototype().setExternalInteraction(
            atomicEditorTask.getWSDecomposition().isManualInteraction()
    );

    engineTask.getDecompositionPrototype().setCodelet(
            atomicEditorTask.getWSDecomposition().getCodelet()
    );

    populateLogPredicates(engineTask.getDecompositionPrototype(),
            atomicEditorTask.getWSDecomposition()) ;

    ResourceMap engineResourceMapping = new ResourceMap();

    populateOfferInteractionDetail(
      atomicEditorTask.getResourceMapping(),
      engineResourceMapping
    );

    populateAllocateInteractionDetail(
      atomicEditorTask.getResourceMapping(),
      engineResourceMapping
    );
    
    populateStartInteractionDetail(
      atomicEditorTask.getResourceMapping(),
      engineResourceMapping
    );

    populateSecondaryResourcesDetail(
      atomicEditorTask.getResourceMapping(),
      engineResourceMapping
    );  

    populateTaskPrivileges(
      atomicEditorTask.getResourceMapping(),
      engineResourceMapping
    );

    engineTask.setResourcingXML(engineResourceMapping.toXML());
  }

  private static void populateOfferInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {

    engineResourceMapping.setOfferInteraction(
       new OfferInteraction(
           convertEditorInteractionToEngineInteraction(
               editorResourceMapping.getOfferInteractionPoint()
           )
       )
    );

   if (editorResourceMapping.getOfferInteractionPoint() != ResourceMapping.SYSTEM_INTERACTION_POINT) {
     return;  
   }

    //  we care only for specifying system interaction behaviour from now on.
    
    if (editorResourceMapping.getRetainFamiliarTask() != null) {
      engineResourceMapping.getOfferInteraction().setFamiliarParticipantTask(
        ((YAWLTask) editorResourceMapping.getRetainFamiliarTask()).getEngineId()   
      );
    } 
      
    populateOfferParticipants(
        editorResourceMapping,
        engineResourceMapping
    );
    
    populateOfferRoles(
        editorResourceMapping,
        engineResourceMapping
    );

    populateOfferInputParameters(
        editorResourceMapping,
        engineResourceMapping
    );

    populateOfferFilters(
        editorResourceMapping,
        engineResourceMapping
    );

    populateRuntimeConstraints(
        editorResourceMapping,
        engineResourceMapping
    );
  }

  private static void populateOfferParticipants(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    if (editorResourceMapping.getBaseUserDistributionList() == null ||
        editorResourceMapping.getBaseUserDistributionList().size() == 0) {
      return;
    }

    for(ResourcingParticipant participant : editorResourceMapping.getBaseUserDistributionList()) {
      engineResourceMapping.getOfferInteraction().addParticipantUnchecked(
          participant.getId()
      );
    }
  }
  
  private static void populateOfferRoles(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    if (editorResourceMapping.getBaseRoleDistributionList() == null ||
        editorResourceMapping.getBaseRoleDistributionList().size() == 0) {
      return;
    }

    for(ResourcingRole role : editorResourceMapping.getBaseRoleDistributionList()) {
      engineResourceMapping.getOfferInteraction().addRoleUnchecked(
          role.getId()
      );
    }
  }
  
  private static void populateOfferInputParameters(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    if (editorResourceMapping.getBaseVariableContentList() == null ||
        editorResourceMapping.getBaseVariableContentList().size() == 0) {
      return;
    }
     
    for(DataVariableContent content : editorResourceMapping.getBaseVariableContentList()) {
      if (content.getContentType() == DataVariableContent.DATA_CONTENT_TYPE) {
        continue;  // skip normal variables. We want just the role and user types to be added.
      }
      engineResourceMapping.getOfferInteraction().addInputParam(
          content.getVariable().getName(),
          convertVariableContentType(
            content.getContentType()    
          )
      );
    }
  }

  private static void populateOfferFilters(ResourceMapping editorResourceMapping,
                                           ResourceMap engineResourceMapping) {
    for(ResourcingFilter editorFilter : editorResourceMapping.getResourcingFilters()) {
      String filterName = editorFilter.getCanonicalName();
      if (filterName.startsWith("org.yawlfoundation.yawl.")) {
          filterName = filterName.substring(filterName.lastIndexOf('.') + 1);
      }
      GenericFilter engineFilter = new GenericFilter(filterName);

      // only want params with non-null values  
      Map<String, String> paramMap = editorFilter.getParameters();
      for (String name : paramMap.keySet()) {
          String value = paramMap.get(name);
          if ((value != null) && (value.length() > 0)) {
              engineFilter.addParam(name, value);
          }
      }
      engineResourceMapping.getOfferInteraction().addFilter(
          engineFilter
      );
    }
  }

  
  private static void populateRuntimeConstraints(ResourceMapping editorResourceMapping,
                                                 ResourceMap engineResourceMapping) {
    if (editorResourceMapping.isPrivilegeEnabled(ResourceMapping.CAN_PILE_PRIVILEGE)) {
       engineResourceMapping.getOfferInteraction().addConstraint(
           new PiledExecution()  
       );
    }
    
    if (editorResourceMapping.getSeparationOfDutiesTask() != null) {
      SeparationOfDuties constraint = new SeparationOfDuties();
      constraint.setKeyValue(
          "familiarTask", 
          ((YAWLTask) editorResourceMapping.getSeparationOfDutiesTask()).getEngineId()
      );
      
      engineResourceMapping.getOfferInteraction().addConstraint(
          constraint
      );
    }
  }

  private static int convertVariableContentType(int contentType) {
    switch(contentType) {
      case(DataVariableContent.PARTICIPANT_CONTENT_TYPE): {
        return OfferInteraction.USER_PARAM;
      }
      case(DataVariableContent.ROLE_CONTENT_TYPE): {
        return OfferInteraction.ROLE_PARAM;
      }
      default: {
        return OfferInteraction.USER_PARAM;
      }
    }
  }

  private static void populateAllocateInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    engineResourceMapping.setAllocateInteraction(
      new AllocateInteraction(
          convertEditorInteractionToEngineInteraction(
              editorResourceMapping.getAllocateInteractionPoint()
          )
       )
    );
    
    if (editorResourceMapping.getAllocateInteractionPoint() == ResourceMapping.SYSTEM_INTERACTION_POINT) {
        String name = editorResourceMapping.getAllocationMechanism().getCanonicalName();
        if (name.startsWith("org.yawlfoundation.yawl.")) {
            name = name.substring(name.lastIndexOf('.') + 1);
        }

        engineResourceMapping.getAllocateInteraction().setAllocator(
          new GenericAllocator(name));
    }
  }

  private static void populateStartInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    engineResourceMapping.setStartInteraction(
      new StartInteraction(
          convertEditorInteractionToEngineInteraction(
              editorResourceMapping.getStartInteractionPoint()
          )
      )
    );
  }


    private static void populateSecondaryResourcesDetail(
            ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {

        SecondaryResources sr = new SecondaryResources();
        for (Object o : editorResourceMapping.getSecondaryResourcesList()) {
            if (o instanceof ResourcingParticipant) {
                sr.getDefaultDataSet().addParticipantUnchecked(((ResourcingParticipant) o).getId());
            }
            else if (o instanceof ResourcingRole) {
                sr.getDefaultDataSet().addRoleUnchecked(((ResourcingRole) o).getId());
            }
            else if (o instanceof ResourcingAsset) {
                sr.getDefaultDataSet().addNonHumanResourceUnchecked(((ResourcingAsset) o).getId());
            }
            else if (o instanceof ResourcingCategory) {
                ResourcingCategory category = (ResourcingCategory) o;
                String subcat = category.getSubcategory();
                if (subcat != null) {
                    sr.getDefaultDataSet().addNonHumanCategoryUnchecked(category.getId(), subcat);
                }
                else sr.getDefaultDataSet().addNonHumanCategoryUnchecked(category.getId());
            }
        }
        engineResourceMapping.setSecondaryResources(sr);
    }

  private static int convertEditorInteractionToEngineInteraction(int editorInteraction)  {
    switch(editorInteraction) {
      case ResourceMapping.SYSTEM_INTERACTION_POINT: {
        return AbstractInteraction.SYSTEM_INITIATED;
      }
      case ResourceMapping.USER_INTERACTION_POINT: {
        return AbstractInteraction.USER_INITIATED;
      }
    }
    return AbstractInteraction.USER_INITIATED;
  }
  
  private static void populateTaskPrivileges(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
    TaskPrivileges enginePrivileges = new TaskPrivileges();

    for(Integer enabledPrivilege : editorResourceMapping.getEnabledPrivileges()) {
        enginePrivileges.allowAll(convertPrivilege(enabledPrivilege));      
    }

    engineResourceMapping.setTaskPrivileges(
        enginePrivileges
    );
  }
  
  private static int convertPrivilege(int editorPrivilege) {
    switch(editorPrivilege) {
      case(ResourceMapping.CAN_SUSPEND_PRIVILEGE): {
        return TaskPrivileges.CAN_SUSPEND;
      }
      case(ResourceMapping.CAN_REALLOCATE_STATELESS_PRIVILEGE): {
        return TaskPrivileges.CAN_REALLOCATE_STATELESS;
      }
      case(ResourceMapping.CAN_REALLOCATE_STATEFUL_PRIVILEGE): {
        return TaskPrivileges.CAN_REALLOCATE_STATEFUL;
      }
      case(ResourceMapping.CAN_DEALLOCATE_PRIVILEGE): {
        return TaskPrivileges.CAN_DEALLOCATE;
      }
      case(ResourceMapping.CAN_DELEGATE_PRIVILEGE): {
        return TaskPrivileges.CAN_DELEGATE;
      }
      case(ResourceMapping.CAN_SKIP_PRIVILEGE): {
        return TaskPrivileges.CAN_SKIP;
      }
      case(ResourceMapping.CAN_PILE_PRIVILEGE): {
        return TaskPrivileges.CAN_PILE;
      }
      default: {
        return TaskPrivileges.CAN_DEALLOCATE;
      }
    }
  }
  
  private static boolean taskNeedsWebServiceDetail(YAWLTask editorTask) {
    if (!(editorTask.getDecomposition() instanceof WebServiceDecomposition)) {
      return false;
    }
    
    WebServiceDecomposition decomposition = 
      (WebServiceDecomposition) editorTask.getDecomposition();

//    if (decomposition.getServiceURI() == null ||
//        decomposition.getServiceURI().trim().equals("")) {
//      return false;
//    }
//    return true;

      return ! decomposition.invokesWorklist();
  }


  private static void populateLogPredicates(YDecomposition engineDecomp,
                                            WebServiceDecomposition editorDecomp) {
      if ((editorDecomp.getLogPredicateStarted() != null) ||
          (editorDecomp.getLogPredicateCompletion() != null)) {
          YLogPredicate predicate = new YLogPredicate();
          predicate.setStartPredicate(editorDecomp.getLogPredicateStarted());
          predicate.setCompletionPredicate(editorDecomp.getLogPredicateCompletion());
          engineDecomp.setLogPredicate(predicate);
      }
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
            setDataBindingForParam(engineTask, editorOutputParameter, false);
        }
      } else {
          setDataBindingForParam(engineTask, editorOutputParameter, false);
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
//    }

    HashMap<String, String> inputMapping = new HashMap<String, String>();
    
    inputMapping.put(
        taskInstanceVariableName,
        editorMultiInstanceTask.getAccessorQuery()
    );
    
    engineTask.setDataMappingsForTaskStarting(inputMapping);
    
    engineTask.setMultiInstanceInputDataMappings(
      taskInstanceVariableName,
      editorMultiInstanceTask.getSplitterQuery()
    );
    }
    // data perspective output

    DataVariable editorNetResultVariable = 
      editorMultiInstanceTask.getResultNetVariable();
    
    String netResultVariableName = null;

    if (editorNetResultVariable != null) {
      netResultVariableName = editorNetResultVariable.getName();
//    }

    HashMap<String, String> outputMapping = new HashMap<String, String>();
    
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
        
        EngineIdentifier engineID = SpecificationModel.getInstance().getUniqueIdentifier("ImplicitCondition");
        YCondition implicitEngineCondition = new YCondition(engineID.toString(), engineNet);
        
        implicitEngineCondition.setImplicit(true);
        engineNet.addNetElement(implicitEngineCondition);

        firstEngineFlow = new YFlow(engineSource, implicitEngineCondition);

        engineSource.addPostset(firstEngineFlow);

        YFlow secondEngineFlow = new YFlow(implicitEngineCondition, engineTarget);
        
        implicitEngineCondition.addPostset(secondEngineFlow);
        addFlowConditionMapping(editorFlow, implicitEngineCondition);
        
      } else { // no need for an implicit condition. Phew!
        firstEngineFlow = new YFlow(engineSource, engineTarget);
        engineSource.addPostset(firstEngineFlow);
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
      engineTriggerTask.addRemovesTokensFrom(engineCancellationSet); 
    }
  }
  
  private static String getEngineElementID(SpecificationModel editorSpec, YAWLVertex element) {
    return element.getEngineId();
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
  

}
