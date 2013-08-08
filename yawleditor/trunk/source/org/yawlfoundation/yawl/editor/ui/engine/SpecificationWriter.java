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

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SpecificationWriter extends EngineEditorInterpretor {

    private static YSpecificationHandler _handler = SpecificationModel.getHandler();

    // todo - make sure editor saveas matches core saveas
    public static boolean checkAndExportEngineSpecToFile(SpecificationModel model,
                                                         String fullFileName) {
        boolean success = false;
        try {
            if (checkUserDefinedDataTypes(model)) {
                YLayout layout = new LayoutExporter().parse(model);
                populateSpecification(model);
                analyseIfNeeded(model);
                if (fullFileName != null) {
                    _handler.saveAs(fullFileName, layout, UserSettings.getFileSaveOptions());
                }
                else _handler.save(layout, UserSettings.getFileSaveOptions());
                success = true;
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


    private static boolean checkUserDefinedDataTypes(SpecificationModel editorSpec) {
        List<String> results = new ArrayList<String>();
        results.addAll(new EngineSpecificationValidator().checkUserDefinedDataTypes(editorSpec));
        if (! results.isEmpty()) {
            YAWLEditor.getInstance().showProblemList("Export Errors", results);
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Could not export Specification due to missing or invalid user-defined " +
                            "datatypes.\nPlease see the problem list below for details.",
                    "Export Datatype Error", JOptionPane.ERROR_MESSAGE);
        }
        return results.isEmpty();
    }


    private static void analyseIfNeeded(SpecificationModel model) {
        List<String> results = new ArrayList<String>();

        if (UserSettings.getVerifyOnSave()) {
            results.addAll(new EngineSpecificationValidator().getValidationResults(
                    _handler.getSpecification()));
        }
        if (UserSettings.getAnalyseOnSave()) {
            try {
                results.addAll(new AnalysisResultsParser().getAnalysisResults(
                    _handler.getSpecificationXML()));
            }
            catch (Exception e) {
                // analysis failed
            }
        }
        YAWLEditor.getInstance().showProblemList("Analysis Results", results);
    }


    public static String getEngineSpecificationXML(SpecificationModel editorSpec) {
        return getEngineSpecificationXML(populateSpecification(editorSpec));
    }


    public static String getEngineSpecificationXML(YSpecification engineSpec) {
        try {
            return YMarshal.marshal(engineSpec);
        } catch (Exception e) {
            LogWriter.error("Error marshalling specification to XML.", e);
            return null;
        }
    }


    public static YSpecification populateSpecification(SpecificationModel model) {
        YSpecification spec = _handler.getSpecification();
        initialise();
        generateEngineMetaData(model);

        // Important:  Engine API expects nets to be pre-generated before composite tasks reference them.
        //            We need to build the nets first, and THEN populate the nets with elements.
        generateRootNet(model);
        generateSubNets(model);
        populateEngineNets(spec);
        generateEngineDataTypeDefinition();

        return spec;
    }

    private static void generateEngineDataTypeDefinition() {
        String originalSchema = _handler.getSchema();
        String updatedSchema = adjustSchemaForInternalTypes(originalSchema);

        // remove any header inadvertently inserted by user
        if (updatedSchema.startsWith("<?xml")) {
            updatedSchema = updatedSchema.substring(updatedSchema.indexOf('>') + 1);
        }
        if (! updatedSchema.equals(originalSchema)) {
            try {
                _handler.setSchema(updatedSchema);
            }
            catch (Exception eActual) {
                try {
                    originalSchema = adjustSchemaForInternalTypes(
                            YSpecificationHandler.DEFAULT_TYPE_DEFINITION);
                    _handler.setSchema(originalSchema);
                }
                catch (Exception eDefault) {}
            }
        }
    }


    private static String adjustSchemaForInternalTypes(String specDataSchema) {
        for (YInternalType type : YInternalType.values()) {
            specDataSchema = type.adjustSchema(specDataSchema);
        }
        return specDataSchema;
    }


    private static void generateEngineMetaData(SpecificationModel model) {
        _handler.setVersion(model.getVersionNumber());
    }

    private static void generateRootNet(SpecificationModel model) {
        YNet rootEngineNet = generateEngineNet(model.getNets().getRootNet());
        editorToEngineNetMap.put(model.getNets().getRootNet(), rootEngineNet);
    }


    private static void generateSubNets(SpecificationModel model) {
        for (NetGraphModel editorNet : model.getNets().getSubNets()) {
            YNet engineSubNet = generateEngineNet(editorNet);
            editorToEngineNetMap.put(editorNet, engineSubNet);
        }
    }


    private static YNet generateEngineNet(NetGraphModel editorNet) {
        YNet engineNet = (YNet) editorNet.getDecomposition();
        engineNet.setID(XMLUtilities.toValidXMLName(engineNet.getID()));
        return engineNet;
    }


     private static void populateEngineNets(YSpecification spec) {
        for (NetGraphModel netModel : editorToEngineNetMap.keySet()) {
            populateEngineNetFrom(spec, netModel);
        }
    }


    private static void populateEngineNetFrom(YSpecification spec,
                                              NetGraphModel netModel)  {
        NetElementSummary editorNetSummary = new NetElementSummary(netModel);
        setElements(editorNetSummary);
        setFlows(editorNetSummary);
        setCancellationSetDetail(editorNetSummary);
    }



    private static void setElements(NetElementSummary editorNetSummary) {

        // temp
        editorToEngineElementMap.put(editorNetSummary.getInputCondition(),
                editorNetSummary.getInputCondition().getYCondition());
        editorToEngineElementMap.put(editorNetSummary.getOutputCondition(),
                editorNetSummary.getOutputCondition().getYCondition());

        setAtomicTasks(editorNetSummary);
        setCompositeTasks(editorNetSummary);
    }


    private static void setAtomicTasks(NetElementSummary editorNetSummary) {

        for (YAWLAtomicTask yawlAtomicTask : editorNetSummary.getAtomicTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlAtomicTask;

            YTask engineAtomicTask = editorTask.getTask();

            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineAtomicTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineAtomicTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }

            editorToEngineElementMap.put(editorTask, engineAtomicTask);
        }
    }


    private static void setCompositeTasks(NetElementSummary editorNetSummary) {

        for (YAWLCompositeTask yawlCompositeTask : editorNetSummary.getCompositeTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlCompositeTask;

            YTask engineCompositeTask = editorTask.getTask();

            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineCompositeTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineCompositeTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }
            editorToEngineElementMap.put(editorTask, engineCompositeTask);
        }
    }


    private static void setFlows(NetElementSummary editorNetSummary) {
        for (YAWLFlowRelation editorFlow : editorNetSummary.getFlows()) {
            YCompoundFlow engineFlow = editorFlow.getYFlow();
            if (engineFlow.hasSourceSplitType(YTask._XOR)) {
                if (engineFlow.isOnlySourceFlow()) {
                    engineFlow.setIsDefaultFlow(true);
                }
            }

            editorToEngineElementMap.put(editorFlow, engineFlow);
        }
    }

    private static void setCancellationSetDetail(NetElementSummary editorNetSummary) {
        for (YAWLTask editorTriggerTask : editorNetSummary.getTasksWithCancellationSets()) {
           List<YExternalNetElement> cancellationSet = new ArrayList<YExternalNetElement>();
            for (YAWLCell element : editorTriggerTask.getCancellationSet().getSetMembers()) {
                if (element instanceof YAWLFlowRelation) {
                    cancellationSet.add(getConditionForFlow((YAWLFlowRelation) element));
                }
                else {
                    cancellationSet.add((YExternalNetElement)
                            editorToEngineElementMap.get(element)
                    );
                }
            }


            YTask engineTriggerTask = (YTask) editorToEngineElementMap.get(editorTriggerTask);
            engineTriggerTask.addRemovesTokensFrom(cancellationSet);
        }
    }


    private static YCondition getConditionForFlow(YAWLFlowRelation editorFlow) {
        return editorFlow.getYFlow().getImplicitCondition();
    }


}
