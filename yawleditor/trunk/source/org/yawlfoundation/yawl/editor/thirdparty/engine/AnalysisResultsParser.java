package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.analyser.YAnalyser;
import org.yawlfoundation.yawl.analyser.YAnalyserOptions;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.LogWriter;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.AnalysisDialog;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class AnalysisResultsParser {

    protected static final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
    protected static final YAnalyser _analyser = new YAnalyser();

    private static final String WOF_YAWL_BINARY = "wofyawl@WofYawlReleaseNumber@.exe";

    public static final String RESET_NET_ANALYSIS_PREFERENCE = "resetNetAnalysisCheck";
    public static final String SOUNDNESS_ANALYSIS_PREFERENCE = "resetSoundnessCheck";
    public static final String WEAKSOUNDNESS_ANALYSIS_PREFERENCE = "resetWeakSoundnessCheck";
    public static final String CANCELLATION_ANALYSIS_PREFERENCE = "resetCancellationCheck";
    public static final String ORJOIN_ANALYSIS_PREFERENCE = "resetOrjoinCheck";
    public static final String SHOW_OBSERVATIONS_PREFERENCE = "resetShowObservationsCheck";
    public static final String USE_YAWLREDUCTIONRULES_PREFERENCE = "yawlReductionRules";
    public static final String USE_RESETREDUCTIONRULES_PREFERENCE = "resetReductionRules";
    public static final String ORJOINCYCLE_ANALYSIS_PREFERENCE = "resetOrjoinCycleCheck";

    public static final String WOFYAWL_ANALYSIS_PREFERENCE = "wofYawlAnalysisCheck";
    public static final String STRUCTURAL_ANALYSIS_PREFERENCE = "wofYawlStructuralAnalysisCheck";
    public static final String BEHAVIOURAL_ANALYSIS_PREFERENCE = "wofYawlBehaviouralAnalysisCheck";
    public static final String EXTENDED_COVERABILITY_PREFERENCE = "wofYawlExtendedCoverabilityCheck";


    public List<String> getAnalysisResults(SpecificationModel editorSpec) {
        String specXML = EngineSpecificationExporter.getEngineSpecificationXML(editorSpec);
        return getAnalysisResults(specXML);
    }


    public List<String> getAnalysisResults(String engineSpecXML) {
        return parseRawResultsIntoList(getRawAnalysisResults(engineSpecXML));
    }


    public void cancel() {
        _analyser.cancelAnalysis();
    }


    protected String getRawAnalysisResults(String engineSpecXML) {
        AnalysisDialog messageDlg = createDialog();
        _analyser.addEventListener(messageDlg);

        try {
            return _analyser.analyse(engineSpecXML, getAnalyserOptions());
        }
        catch (YSyntaxException yse) {
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
            LogWriter.error("Error analysing specification.", e);
            return "<error>"+ XMLUtilities.quoteSpecialCharacters(e.getMessage()) +"</error>";
        }
        finally {
            _analyser.removeEventListener(messageDlg);
        }
    }


    private YAnalyserOptions getAnalyserOptions() {
        YAnalyserOptions options = new YAnalyserOptions();
        if (prefs.getBoolean(RESET_NET_ANALYSIS_PREFERENCE, true)) {
            options.enableResetWeakSoundness(
                    prefs.getBoolean(WEAKSOUNDNESS_ANALYSIS_PREFERENCE, true));
            options.enableResetSoundness(
                    prefs.getBoolean(SOUNDNESS_ANALYSIS_PREFERENCE, true));
            options.enableResetCancellation(
                    prefs.getBoolean(CANCELLATION_ANALYSIS_PREFERENCE, true));
            options.enableResetOrJoin(
                    prefs.getBoolean(ORJOIN_ANALYSIS_PREFERENCE, true));
            options.enableResetOrjoinCycle(
                    prefs.getBoolean(ORJOINCYCLE_ANALYSIS_PREFERENCE, true));
            options.enableResetReductionRules(
                    prefs.getBoolean(USE_RESETREDUCTIONRULES_PREFERENCE, true));
            options.enableYawlReductionRules(
                    prefs.getBoolean(USE_YAWLREDUCTIONRULES_PREFERENCE, true));
        }
        if (prefs.getBoolean(WOFYAWL_ANALYSIS_PREFERENCE, true)) {
            options.enableWofBehavioural(
                    prefs.getBoolean(BEHAVIOURAL_ANALYSIS_PREFERENCE, true));
            options.enableWofStructural(
                    prefs.getBoolean(STRUCTURAL_ANALYSIS_PREFERENCE, true));
            options.enableWofExtendedCoverabiity(
                    prefs.getBoolean(EXTENDED_COVERABILITY_PREFERENCE, true));
            options.setWofYawlExecutableLocation(getWofYawlExecutableFilePath());
        }
        return options;
    }


    private AnalysisDialog createDialog() {
        AnalysisDialog messageDlg = new AnalysisDialog("Specification");
        messageDlg.setTitle("Analyse Specification");
        messageDlg.setOwner(this);
        return messageDlg;
    }


    private static String getWofYawlExecutableFilePath() {
        return prefs.get("WofyawlFilePath", FileUtilities.getHomeDir() + WOF_YAWL_BINARY);
    }

    public static boolean wofYawlAvailable() {
        return new File(getWofYawlExecutableFilePath()).exists();
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
                if (prefs.getBoolean(SHOW_OBSERVATIONS_PREFERENCE, true)) {
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
