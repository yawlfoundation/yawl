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
import org.yawlfoundation.yawl.analyser.YAnalyserOptions;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.io.SpecificationWriter;
import org.yawlfoundation.yawl.editor.ui.swing.AnalysisDialog;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResultsParser {

    protected static final YAnalyser _analyser = new YAnalyser();

    private static final String WOF_YAWL_BINARY = "wofyawl0.4.exe";


    public List<String> getAnalysisResults() {
        String specXML = new SpecificationWriter().getSpecificationXML();
        return getAnalysisResults(specXML);
    }


    public List<String> getAnalysisResults(String engineSpecXML) {
        return parseRawResultsIntoList(getRawAnalysisResults(engineSpecXML));
    }


    public void cancel() {
        _analyser.cancelAnalysis();
    }


    protected String getRawAnalysisResults(String engineSpecXML) {
        AnalysisDialog messageDlg = createDialog(YAWLEditor.getInstance());
        _analyser.addEventListener(messageDlg);

        try {
            return _analyser.analyse(engineSpecXML, getAnalyserOptions(),
                    UserSettings.getAnalyserMaxMarkings());
        }
        catch (YSyntaxException yse) {
            messageDlg.setVisible(false);
            messageDlg.dispose();
            String msg = yse.getMessage().trim();
            msg = msg.substring(0, msg.indexOf(":")) + ".";
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    msg + "\nAnalysis cannot proceed until these issues are resolved.\n" +
                            "Please validate the specification for more detailed information.",
                    "Error validating specification",
                    JOptionPane.ERROR_MESSAGE);
            return "";
        }
        catch (Exception e) {
            messageDlg.setVisible(false);
            messageDlg.dispose();
            LogWriter.error("Error analysing specification.", e);
            return "<error>"+ JDOMUtil.encodeEscapes(e.getMessage()) +"</error>";
        }
        finally {
            _analyser.removeEventListener(messageDlg);
        }
    }


    private YAnalyserOptions getAnalyserOptions() {
        YAnalyserOptions options = new YAnalyserOptions();
        if (UserSettings.getResetNetAnalysis()) {
            options.enableResetWeakSoundness(UserSettings.getWeakSoundnessAnalysis());
            options.enableResetSoundness(UserSettings.getSoundnessAnalysis());
            options.enableResetCancellation(UserSettings.getCancellationAnalysis());
            options.enableResetOrJoin(UserSettings.getOrJoinAnalysis());
            options.enableResetOrjoinCycle(UserSettings.getOrJoinCycleAnalysis());
            options.enableResetReductionRules(UserSettings.getUseResetReductionRules());
            options.enableYawlReductionRules(UserSettings.getUseYawlReductionRules());
        }
        if (UserSettings.getWofyawlAnalysis()) {
            options.enableWofBehavioural(UserSettings.getBehaviouralAnalysis());
            options.enableWofStructural(UserSettings.getStructuralAnalysis());
            options.enableWofExtendedCoverabiity(UserSettings.getExtendedCoverability());
            options.setWofYawlExecutableLocation(getWofYawlExecutableFilePath());
        }
        return options;
    }


    private AnalysisDialog createDialog(JFrame owner) {
        AnalysisDialog messageDlg = new AnalysisDialog("Specification", owner);
        messageDlg.setTitle("Analyse Specification");
        messageDlg.setOwner(this);
        return messageDlg;
    }


    private static String getWofYawlExecutableFilePath() {
        String path = UserSettings.getWofyawlFilePath();
        return path != null ? path : FileUtilities.getHomeDir() + WOF_YAWL_BINARY;
    }

    public static boolean wofYawlAvailable() {
        String path = getWofYawlExecutableFilePath();
        return path.endsWith(".exe") && new File(path).exists();
    }


    protected List<String> parseRawResultsIntoList(String rawAnalysisXML) {
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
