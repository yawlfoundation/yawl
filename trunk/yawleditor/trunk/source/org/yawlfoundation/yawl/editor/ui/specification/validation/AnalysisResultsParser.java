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

package org.yawlfoundation.yawl.editor.ui.specification.validation;

import org.yawlfoundation.yawl.analyser.YAnalyser;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.AnalysisDialog;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnalysisResultsParser implements AnalysisCanceller {

    private YAnalyser _analyser;


    // triggered from menu or toolbar - do via swing worker
    public void showAnalysisResults() {
        final AnalysisWorker worker = new AnalysisWorker();

        worker.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getNewValue() == SwingWorker.StateValue.DONE) {
                    YAWLEditor.getInstance().showProblemList("Analysis Results",
                            new ValidationResultsParser().parse(
                                    parseRawResultsIntoList(worker.getResult())));
                }
            }
        });

        worker.execute();
    }


    // triggered on file save with analyse option - already in a swing worker
    public List<String> getAnalysisResults(String specXML) {
        AnalysisDialog messageDlg = AnalysisUtil.createDialog(this);
        _analyser = new YAnalyser();
        String result = AnalysisUtil.analyse(_analyser, messageDlg, specXML);
        return parseRawResultsIntoList(result);
    }


    public void cancel() {
        if (_analyser != null) _analyser.cancelAnalysis();
    }


    protected List<String> parseRawResultsIntoList(String rawAnalysisXML) {
        if (StringUtil.isNullOrEmpty(rawAnalysisXML)) {
            return Collections.emptyList();
        }
        if (rawAnalysisXML.startsWith("<error>") || rawAnalysisXML.startsWith("<cancelled>")) {
            return Arrays.asList(StringUtil.unwrap(rawAnalysisXML));
        }
        List<String> resultList = new ArrayList<String>();
        XNode parentNode = new XNodeParser().parse(rawAnalysisXML);
        if (parentNode != null) {
            parseResetNetResults(resultList, parentNode);
            parseWofYawlResults(resultList, parentNode);
        }
        else {
            resultList.add("Analysis Error: Malformed analysis results.");
        }
        return resultList;
    }


    protected void parseResetNetResults(List<String> resultsList, XNode parentNode) {
        XNode resetNode = parentNode.getChild("resetAnalysisResults");
        if (resetNode != null) {
            String cancelMsg = resetNode.getChildText("cancelled");
            if (cancelMsg != null) {
                resultsList.add(cancelMsg);
            }
            else {
                parseResetNetErrors(resultsList, resetNode);
                parseResetNetWarnings(resultsList, resetNode);
                if (UserSettings.getShowObservations()) {
                    parseResetNetObservations(resultsList, resetNode);
                }
            }
        }
    }


    protected void parseResetNetErrors(List<String> resultsList, XNode resetNode) {
        String prefix = "ResetNet Analysis Error: ";
        parseResultsIntoList("error", prefix, resultsList, resetNode);
    }


    protected void parseResetNetWarnings(List<String> resultsList, XNode resetNode) {
        String prefix = "ResetNet Analysis Warning: ";
        parseResultsIntoList("warning", prefix, resultsList, resetNode);
    }


    protected void parseResetNetObservations(List<String> resultsList, XNode resetNode) {
        String prefix = "ResetNet Analysis Observation: ";
        parseResultsIntoList("observation", prefix, resultsList, resetNode);
    }


    protected void parseWofYawlResults(List<String> resultsList, XNode parentNode) {
        XNode wofYawlNode = parentNode.getChild("wofYawlAnalysisResults");
        if (wofYawlNode != null) {
            parseWofYawlStructuralWarnings(resultsList, wofYawlNode);
            parseWofYawlBehaviouralWarnings(resultsList, wofYawlNode);
        }
    }


    protected void parseWofYawlStructuralWarnings(List<String> resultsList,
                                                  XNode wofYawlNode) {
        String prefix = "WofYAWL Structural Warning: ";
        parseResultsIntoList("structure", prefix, resultsList, wofYawlNode);
    }


    protected void parseWofYawlBehaviouralWarnings(List<String> resultsList,
                                                   XNode wofYawlNode) {
        String prefix = "WofYAWL Behavioural Warning: ";
        parseResultsIntoList("behavior", prefix, resultsList, wofYawlNode);
    }


    private void parseResultsIntoList(String childName, String prefix,
                                      List<String> resultsList, XNode node) {
        for (XNode childNode : node.getChildren(childName)) {
            String msg = childNode.getText();
            if (msg != null) resultsList.add(prefix + msg);
        }
    }

}
