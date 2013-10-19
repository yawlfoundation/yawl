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

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SpecificationWriter {

    private YSpecificationHandler _handler = SpecificationModel.getHandler();

    public boolean writeToFile(SpecificationModel model, String fileName) {
        boolean success = false;
        try {
            if (checkUserDefinedDataTypes()) {
                YLayout layout = new LayoutExporter().parse(model);
                cleanSpecification(model);
                checkSpecification();
                if (! fileName.equals(_handler.getFileName())) {
                    _handler.saveAs(fileName, layout, UserSettings.getFileSaveOptions());
                }
                else _handler.save(layout, UserSettings.getFileSaveOptions());
                success = true;
            }
        }
        catch (Exception e) {
            showError("The attempt to save this specification to file failed.\n " +
                    "Please see the log for details", "Save File Error");
            LogWriter.error("Error saving specification to file.", e);
        }
        return success;
    }


    public String getSpecificationXML(SpecificationModel model) {
        cleanSpecification(model);
        try {
            return _handler.getSpecificationXML();
        }
        catch (Exception e) {
            LogWriter.error("Error marshalling specification to XML.", e);
            return null;
        }
    }


    public YSpecification cleanSpecification(SpecificationModel model) {
        _handler.getControlFlowHandler().removeOrphanTaskDecompositions();
        removeUndoneElements();
        configureTasks(model);
        return _handler.getSpecification();
    }


    /***************************************************************************/

    private boolean checkUserDefinedDataTypes() {
        List<String> results = new DataTypeValidator().validate();
        if (! results.isEmpty()) {
            YAWLEditor.getInstance().showProblemList("Export Errors",
                    new ValidationResultsParser().parse(results));
            showError("Could not export Specification due to missing or invalid user-" +
                    "defined data types.\nPlease see the problem list below for details.",
                    "Data type Error");
        }
        return results.isEmpty();
    }


    private void checkSpecification() {
        List<String> results = new ArrayList<String>();
        String title = "Validation";
        if (UserSettings.getVerifyOnSave()) {
            results.addAll(new SpecificationValidator().getValidationResults(
                    _handler.getSpecification()));
        }
        if (UserSettings.getAnalyseOnSave()) {
            title = "Analysis";
            try {
                results.addAll(new AnalysisResultsParser().getAnalysisResults(
                    _handler.getSpecificationXML()));
            }
            catch (Exception e) {
                // analysis failed
            }
        }
        YAWLEditor.getInstance().showProblemList(title + " Results",
                new ValidationResultsParser().parse(results));
    }


     private void configureTasks(SpecificationModel model) {
        for (NetGraphModel netModel : model.getNets()) {
            for (YAWLTask task : NetUtilities.getAllTasks(netModel)) {
                if (task.isConfigurable()) {
                   YTask yTask = (YTask) task.getYAWLElement();
                   yTask.setConfiguration(
                           new ConfigurationExporter().getTaskConfiguration(task));
                   yTask.setDefaultConfiguration(
                           new DefaultConfigurationExporter()
                                   .getTaskDefaultConfiguration(task));
               }
            }
        }
    }


    private void removeUndoneElements() {
        for (YExternalNetElement netElement :
                 SpecificationUndoManager.getInstance().getRemovedYNetElements()) {
            _handler.getControlFlowHandler().removeNetElement(netElement);
        }
        SpecificationUndoManager.getInstance().clearYNetElementSet();
    }


    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message, title,
                JOptionPane.ERROR_MESSAGE);
    }

}
