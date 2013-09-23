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
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SpecificationWriter {

    private static YSpecificationHandler _handler = SpecificationModel.getHandler();

    // todo - make sure editor saveas matches core saveas
    public static boolean checkAndExportEngineSpecToFile(SpecificationModel model,
                                                         String fullFileName) {
        boolean success = false;
        try {
            if (checkUserDefinedDataTypes(model)) {
                YLayout layout = new LayoutExporter().parse(model);
                populateSpecification(model);
                analyseIfNeeded();
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
        return success;
    }


    private static boolean checkUserDefinedDataTypes(SpecificationModel model) {
        List<String> results = new ArrayList<String>();
        results.addAll(new EngineSpecificationValidator().checkUserDefinedDataTypes(model));
        if (! results.isEmpty()) {
            YAWLEditor.getInstance().showProblemList("Export Errors", results);
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Could not export Specification due to missing or invalid user-defined " +
                            "datatypes.\nPlease see the problem list below for details.",
                    "Export Datatype Error", JOptionPane.ERROR_MESSAGE);
        }
        return results.isEmpty();
    }


    private static void analyseIfNeeded() {
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


    public static String getSpecificationXML(SpecificationModel model) {
        return getSpecificationXML(populateSpecification(model));
    }


    public static String getSpecificationXML(YSpecification engineSpec) {
        try {
            return YMarshal.marshal(engineSpec);
        } catch (Exception e) {
            LogWriter.error("Error marshalling specification to XML.", e);
            return null;
        }
    }


    public static YSpecification populateSpecification(SpecificationModel model) {
        _handler.getControlFlowHandler().removeOrphanTaskDecompositions();
        removeUndoneElements();
        finaliseEngineNets(model);
        return _handler.getSpecification();
    }


     private static void finaliseEngineNets(SpecificationModel model) {
        for (NetGraphModel netModel : model.getNets()) {
            checkNetIdIsValidXML((YNet) netModel.getDecomposition());
            NetElementSummary editorNetSummary = new NetElementSummary(netModel);
            configureTasks(editorNetSummary);
            setFlows(editorNetSummary);
            setCancellationSetDetail(editorNetSummary);
        }
    }

    private static void checkNetIdIsValidXML(YNet net) {
        String checkedID = XMLUtilities.toValidXMLName(net.getID());
        if (! net.getID().equals(checkedID)) {
            net.setID(checkedID);
        }
    }


    private static void configureTasks(NetElementSummary editorNetSummary) {
        for (YAWLTask task : editorNetSummary.getTasks()) {
            configureTask(task);
        }
    }

    private static void setFlows(NetElementSummary editorNetSummary) {
        for (YAWLFlowRelation editorFlow : editorNetSummary.getFlows()) {
            YCompoundFlow engineFlow = editorFlow.getYFlow();
            if (engineFlow.hasSourceSplitType(YTask._XOR)) {
                if (engineFlow.isOnlySourceFlow()) {
                    engineFlow.setPredicate(null);
                    engineFlow.setIsDefaultFlow(true);
                }
            }
        }
    }

    private static void setCancellationSetDetail(NetElementSummary editorNetSummary) {
        for (YAWLTask editorTriggerTask : editorNetSummary.getTasksWithCancellationSets()) {
           List<YExternalNetElement> cancellationSet = new ArrayList<YExternalNetElement>();
            for (YAWLCell element : editorTriggerTask.getCancellationSet().getSetMembers()) {
                if (element instanceof YAWLFlowRelation) {
                    cancellationSet.add(((YAWLFlowRelation)
                            element).getYFlow().getImplicitCondition());
                }
                else {
                    cancellationSet.add(((YAWLVertex) element).getYAWLElement());
                }
            }

            YTask engineTriggerTask = (YTask) editorTriggerTask.getYAWLElement();
            engineTriggerTask.addRemovesTokensFrom(cancellationSet);
        }
    }


    private static void configureTask(YAWLTask task) {
        if (task.isConfigurable()) {
            YTask yTask = (YTask) task.getYAWLElement();
            DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
            ConfigurationExporter config = new ConfigurationExporter();
            yTask.setConfiguration(config.getTaskConfiguration(task));
            yTask.setDefaultConfiguration(
                    defaultConfig.getTaskDefaultConfiguration(task));
        }
    }


    private static void removeUndoneElements() {
        for (YExternalNetElement netElement :
                 SpecificationUndoManager.getInstance().getRemovedYNetElements()) {
            _handler.getControlFlowHandler().removeNetElement(netElement);
        }
        SpecificationUndoManager.getInstance().clearYNetElementSet();
    }

}
