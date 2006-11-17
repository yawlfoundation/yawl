package au.edu.qut.yawl.editor.thirdparty.engine;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.edu.qut.yawl.editor.analyser.YAWLResetAnalyser;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class ResetNetAnalysisResultsParser extends AnalysisResultsParser {
  private static final YAWLResetAnalyser ANALYSER = new YAWLResetAnalyser();
  
  protected String getRawResultsFromFile(String tempEngineFile) {
    boolean analysisNeeded = prefs.getBoolean(YAWLResetAnalyser.RESET_NET_ANALYSIS_PREFERENCE, true);

    if (!analysisNeeded) {
      return null;
    }
    
    try {
      return ANALYSER.analyse(tempEngineFile, getOptionParameters(),getParameterForYAWLReductionRules(),getParameterForResetReductionRules());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private String getOptionParameters()
  {
    String Options = "";
    Options = getParameterForWeakSoundnessAnalysis() +
              getParameterForSoundnessAnalysis()+
              getParameterForCancellationAnalysis()+
              getParameterForOrjoinAnalysis();
    return Options;
    
  }
  private String getParameterForWeakSoundnessAnalysis() {
    if(prefs.getBoolean(YAWLResetAnalyser.WEAKSOUNDNESS_ANALYSIS_PREFERENCE, true)) {
      return "w";
    }
    return "";
  }

  private String getParameterForSoundnessAnalysis() {
    if(prefs.getBoolean(YAWLResetAnalyser.SOUNDNESS_ANALYSIS_PREFERENCE, true)) {
      return "s";
    }
    return "";
  }

  private String getParameterForCancellationAnalysis() {
    if(prefs.getBoolean(YAWLResetAnalyser.CANCELLATION_ANALYSIS_PREFERENCE, true)) {
      return "c";
    }
    return "";
  }

 private String getParameterForOrjoinAnalysis() {
    if(prefs.getBoolean(YAWLResetAnalyser.ORJOIN_ANALYSIS_PREFERENCE, true)) {
      return "o";
    }
    return "";
  }

 private boolean getParameterForYAWLReductionRules() {
    
    return prefs.getBoolean(YAWLResetAnalyser.USE_YAWLREDUCTIONRULES_PREFERENCE, true);
     
 }
 
 private boolean getParameterForResetReductionRules() {
    
    return prefs.getBoolean(YAWLResetAnalyser.USE_RESETREDUCTIONRULES_PREFERENCE, true);
     
 }
  
  protected void parseRawResultsIntoList(List resultsList, String rawAnalysisXML) {
   parseResetNetAnalysisResultsIntoList(resultsList, rawAnalysisXML);
  }
  
  protected void parseResetNetAnalysisResultsIntoList(List resultsList, String rawStructureIssues) {
    parseRawResetNetAnalysisWarningsIntoList("ResetNet Analysis Warning: ", resultsList, rawStructureIssues);
    parseRawResetNetAnalysisObservationsIntoList("ResetNet Analysis Observation: ", resultsList, rawStructureIssues);
  }

  protected void parseRawResetNetAnalysisWarningsIntoList(String listPrefix, List resultsList, String rawStructureIssues) {
    parseResultsIntoList("warning", listPrefix, resultsList, rawStructureIssues);
  }

  protected void parseRawResetNetAnalysisObservationsIntoList(String listPrefix, List resultsList, String rawStructureIssues) {
    if (prefs.getBoolean(YAWLResetAnalyser.SHOW_OBSERVATIONS_PREFERENCE, true)) {
      parseResultsIntoList("observation", listPrefix, resultsList, rawStructureIssues);
    }
  }
  
  private void parseResultsIntoList(String tagLabel, String listPrefix, List resultsList, String rawStructureIssues) {
    if (rawStructureIssues == null) {
      return;
    }
    Pattern structurePattern = 
      Pattern.compile(
          "<" + tagLabel + ".*?>(.*?)</" + tagLabel+ ">"
    );
    Matcher structureMatcher = structurePattern.matcher(rawStructureIssues);
    
    while(structureMatcher.find()) {
      String rawMessage = structureMatcher.group();
      resultsList.add(listPrefix + XMLUtilities.stripOutermostTags(rawMessage));
    }
  }
}
