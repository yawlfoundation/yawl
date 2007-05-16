/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford
 * 
 * 
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

package au.edu.qut.yawl.editor.thirdparty.wofyawl;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.edu.qut.yawl.editor.thirdparty.engine.AnalysisResultsParser;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class AvailableWofYAWLImplementation extends AnalysisResultsParser implements WofYAWLProxyInterface {
  
  /**
   * Returns the raw XML output from wofyawl that is generated when wofyawl is 
   * run against the input engine XML file.
   * @param engineXMLFile
   * @return the raw XML wofyawl output
   */
  
  protected String getRawResultsFromFile(String engineXMLFile) {

    boolean analysisNeeded = prefs.getBoolean(WofYAWLProxy.WOFYAWL_ANALYSIS_PREFERENCE, true);

    if (!analysisNeeded) {
      return null;
    }
    
    StringBuffer resultText = new StringBuffer();
    File engineFileHandle = new File(engineXMLFile);
    
    if (!engineFileHandle.exists()) {
      return "Unexpected Error: A temporary engine specification was not created as expected for wofyawl analysis.";
    }
    
    String[] commandArray = new String[] {
       WofYAWLProxy.getBinaryExecutableFilePath(),
       getParameterForBehaviouralAnalysis(),
       getParameterForStructuralAnalysis(),
       getParameterForExtendedCoverability(),
       engineXMLFile 
    };
    
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
      return "Unexpected Error: The wofyawl analysis tool did not return any analysis information.";
    }
  }

  private String getParameterForBehaviouralAnalysis() {
    if(prefs.getBoolean(BEHAVIOURAL_ANALYSIS_PREFERENCE, true)) {
      return "+b";
    }
    return "-b";
  }

  private String getParameterForStructuralAnalysis() {
    if(prefs.getBoolean(STRUCTURAL_ANALYSIS_PREFERENCE, true)) {
      return "+s";
    }
    return "-s";
  }

  private String getParameterForExtendedCoverability() {
    if(prefs.getBoolean(EXTENDED_COVERABILITY_PREFERENCE, true)) {
      return "+1";
    }
    return "-1";
  }
  
  protected void parseRawResultsIntoList(List resultsList, String rawAnalysisXML) {
    if (rawAnalysisXML == null) {
      return;
    }
    parseStructuralRawResultsIntoList(resultsList, rawAnalysisXML);
    parseBehaviouralRawResultsIntoList(resultsList, rawAnalysisXML);
  }
  
  protected void parseRawStructureIssuesIntoList(List resultsList, String rawStructureIssues) {
    parseRawWarningsIntoList("WofYAWL Structural Warning:  ", resultsList, rawStructureIssues);
  }
  
  
  protected void parseRawBehaviouralIssuesIntoList(List resultsList, String rawStructureIssues) {
    parseRawWarningsIntoList("WofYAWL Behavioural Warning: ", resultsList, rawStructureIssues);
  }

  private void parseStructuralRawResultsIntoList(List resultsList, String rawAnalysisXML) {
    if(!prefs.getBoolean(STRUCTURAL_ANALYSIS_PREFERENCE, true)) {
      return;
    }
    
    parseRawStructureIssuesIntoList(
        resultsList, 
        getRawResultsBetweenTagsWithName(
            "structure", 
            rawAnalysisXML
        )
    );
  }
  
  protected void parseRawWarningsIntoList(String listPrefix, List resultsList, String rawStructureIssues) {
    Pattern structureWarningPattern = 
      Pattern.compile(
          "<warning .*?>(.*?)</warning>"
    );
    Matcher structureWarningMatcher = structureWarningPattern.matcher(rawStructureIssues);
    
    while(structureWarningMatcher.find()) {
      String rawWarning = structureWarningMatcher.group();
      resultsList.add(listPrefix + XMLUtilities.stripOutermostTags(rawWarning));
    }
  }
  
  private void parseBehaviouralRawResultsIntoList(List resultsList, String rawAnalysisXML) {
    if(!prefs.getBoolean(BEHAVIOURAL_ANALYSIS_PREFERENCE, true)) {
      return;
    }
    parseRawBehaviouralIssuesIntoList(
        resultsList, 
        getRawResultsBetweenTagsWithName(
            "behavior",
            rawAnalysisXML
        )
    );
  }
  
  // ---- From this point on is where the actual XML analysis parsing takes place ----

  /**
   * Takes the raw WofYAWL output XML and converts it into a list of strings
   * that can be displayed in a table widget.
   * @param resultsList
   * @param rawAnalysisXML
   */
  
}
