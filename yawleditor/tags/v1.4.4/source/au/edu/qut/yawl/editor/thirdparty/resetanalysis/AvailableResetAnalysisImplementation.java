/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford / Moe Wynn
 * 
 * Copyright (C) 2005 Queensland University of Technology
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
 */

package au.edu.qut.yawl.editor.thirdparty.resetanalysis;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.thirdparty.engine.SpecificationEngineHandler;

public class AvailableResetAnalysisImplementation implements ResetAnalysisProxyInterface {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public List getAnalysisResults() {
    
    List resultsList = new LinkedList();
    String tempEngineFile = getTempEngineXMLFile();
    
    parseRawResultsIntoList(
        resultsList,
        getRawResultsFromResetAnalysis(tempEngineFile)
    );
    
    if (resultsList.size() == 0) {
      resultsList.add("No problems were discovered in the analysis of this specification.");
    }
    
    removeFile(tempEngineFile);
    return resultsList;
  }

  /**
   * Takes the currently loaded specification, and generates a temporary (randomly named) 
   * engine XML specification file for use in wofyawl analysis.
   * @return The file path of the temporary engine XML file
   */
  
  private String getTempEngineXMLFile() {
    
    String tempEngineFile = 
      System.getProperty("java.io.tmpdir") + 
      System.getProperty("file.separator") +
      String.valueOf(Math.random());
    
    SpecificationEngineHandler.getInstance().saveSpecificationToFileNoGUI(
      tempEngineFile
    );

    return tempEngineFile;
  }
  
  /**
   * Returns the raw XML output from wofyawl that is generated when wofyawl is 
   * run against the input engine XML file.
   * @param engineXMLFile
   * @return the raw XML wofyawl output
   */
  
  private String getRawResultsFromResetAnalysis(String engineXMLFile) {
    StringBuffer resultText = new StringBuffer();
    File engineFileHandle = new File(engineXMLFile);
    
    if (!engineFileHandle.exists()) {
      return "Unexpected Error: A temporary engine specification was not created as expected for wofyawl analysis.";
    }
    
/*    String[] commandArray = new String[] {
       "java -jar",
       ResetAnalysisProxy.getBinaryExecutableFilePath(),
       engineXMLFile,
       getOptionParameters()
    };
 */   
    String commandArray = "java -jar " + ResetAnalysisProxy.getBinaryExecutableFilePath();
    commandArray += " " + engineXMLFile;
    commandArray += " " + getOptionParameters(); 
     
    try {
      Process resultProcess = Runtime.getRuntime().exec(commandArray);

      BufferedReader in = new BufferedReader(
          new InputStreamReader(resultProcess.getInputStream()));
      String line = null;
      while ((line = in.readLine()) != null) {
        resultText.append(line);
      }
      return resultText.toString();

    } catch (Exception e) {
      e.printStackTrace();
      return "Unexpected Error: The reset analysis tool did not return any analysis information.";
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
    if(prefs.getBoolean(WEAKSOUNDNESS_ANALYSIS_PREFERENCE, true)) {
      return "w";
    }
    return "";
  }

  private String getParameterForSoundnessAnalysis() {
    if(prefs.getBoolean(SOUNDNESS_ANALYSIS_PREFERENCE, true)) {
      return "s";
    }
    return "";
  }

  private String getParameterForCancellationAnalysis() {
    if(prefs.getBoolean(CANCELLATION_ANALYSIS_PREFERENCE, true)) {
      return "c";
    }
    return "";
  }


 private String getParameterForOrjoinAnalysis() {
    if(prefs.getBoolean(ORJOIN_ANALYSIS_PREFERENCE, true)) {
      return "o";
    }
    return "";
  }
  private void removeFile(String file) {
    (new File(file)).delete();
  }
  
  // ---- From this point on is where the actual XML analysis parsing takes place ----

  /**
   * Takes the raw ResetAnalysis output XML and converts it into a list of strings
   * that can be displayed in a table widget.
   * @param resultsList
   * @param rawAnalysisXML
   */
  
  private void parseRawResultsIntoList(List resultsList, String rawAnalysisXML) {
  	
      System.out.println(rawAnalysisXML);
      parseBehaviouralRawResultsIntoList(resultsList, rawAnalysisXML);
  }
 
  private String getRawResultsBetweenTagsWithName(String tagName, String rawAnalysisXML) {
    Pattern resultsPattern = 
      Pattern.compile(
          ".*?<" + tagName + ">(.*?)</" + tagName + ">.*"
    );
    Matcher resultsMatcher = resultsPattern.matcher(rawAnalysisXML);

    String rawResults = null;
    
    if (resultsMatcher.find()) {
      resultsMatcher.group();
      rawResults = resultsMatcher.replaceAll("$1");
    }
/*
    System.out.println("Raw " + tagName + " results:\n----\n" + rawResults);
    System.out.println("----");
*/  
    return rawResults;
  }
 
  private void parseBehaviouralRawResultsIntoList(List resultsList, String rawAnalysisXML) {
   parseRawBehaviouralIssuesIntoList(
        resultsList, 
        getRawResultsBetweenTagsWithName(
            "behavior",
            rawAnalysisXML
        )
    );
  }
  
  private void parseRawBehaviouralIssuesIntoList(List resultsList, String rawStructureIssues) {
    parseRawWarningsIntoList("ResetAnalysis Message: ", resultsList, rawStructureIssues);
  }
  
  private void parseRawWarningsIntoList(String listPrefix, List resultsList, String rawStructureIssues) {
    Pattern structureWarningPattern = 
      Pattern.compile(
          "<warning .*?>(.*?)</warning>"
    );
    Matcher structureWarningMatcher = structureWarningPattern.matcher(rawStructureIssues);
    
    while(structureWarningMatcher.find()) {
      String rawWarning = structureWarningMatcher.group();
      resultsList.add(listPrefix + stripOutermostTags(rawWarning));
    }
  }

  /**
   * Simple regular-expression based method to strip the outermost tags from a fragment of XML.
   * Assumes that the fragment begins and ends with tags.
   * @param xmlFragment
   * @return the xmlFragment string with the outermost tags removed.
   */
  
  private String stripOutermostTags(String xmlFragment) {
    Pattern tagContainingPattern = 
      Pattern.compile(
          "^<.*?>(.*?)</.*?>$"
    );

    Matcher tagContainingMatcher = tagContainingPattern.matcher(xmlFragment);

    if (tagContainingMatcher.find()) {
      tagContainingMatcher.group();
      return tagContainingMatcher.replaceAll("$1");
    }
    return xmlFragment;  // *shrug* no tags in the fragment, apparently.
  }
}
