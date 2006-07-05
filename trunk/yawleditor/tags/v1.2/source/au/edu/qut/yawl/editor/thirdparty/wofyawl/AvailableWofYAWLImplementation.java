/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford
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

package au.edu.qut.yawl.editor.thirdparty.wofyawl;

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

public class AvailableWofYAWLImplementation implements WofYAWLProxyInterface {
  
  private static final String XML_ATTRIBUTE_REG_EXP = "\"[^\"]*\"";
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public List getAnalysisResults() {
    
    List resultsList = new LinkedList();
    String tempEngineFile = getTempEngineXMLFile();
    
    parseRawResultsIntoList(
        resultsList,
        getRawResultsFromWofYAWL(tempEngineFile)
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
   * Takes the raw WofYAWL output XML and converts it into a list of strings
   * that can be displayed in a table widget.
   * @param resultsList
   * @param rawAnalysisXML
   */
  
  private void parseRawResultsIntoList(List resultsList, String rawAnalysisXML) {
    
    Pattern taskIssuePattern = 
      Pattern.compile(
          "<task name=" + XML_ATTRIBUTE_REG_EXP +
               " specification=" + XML_ATTRIBUTE_REG_EXP +
               " decomposition=" + XML_ATTRIBUTE_REG_EXP + 
               " issue=" + XML_ATTRIBUTE_REG_EXP + ">" +
               ".*?</task>"
    );
    Matcher taskIssueMatcher = taskIssuePattern.matcher(rawAnalysisXML);
    
    while(taskIssueMatcher.find()) {
      String rawTaskIssue = taskIssueMatcher.group();
  /*
      System.out.println(rawTaskIssue);
      System.out.println("----");
  */    
      String resultString = 
          "In task " + 
          getTaskWithIssue(rawTaskIssue) + 
          ", " + 
          getIssueOfTask(rawTaskIssue) +
          getElementsInvolved(rawTaskIssue);
      
//      System.out.println(resultString);
      
      resultsList.add(resultString);
    }
  }
  
  /**
   * Returns the raw XML output from wofyawl that is generated when wofyawl is 
   * run against the input engine XML file.
   * @param engineXMLFile
   * @return the raw XML wofyawl output
   */
  
  private String getRawResultsFromWofYAWL(String engineXMLFile) {
    StringBuffer resultText = new StringBuffer();
    File engineFileHandle = new File(engineXMLFile);
    
    if (!engineFileHandle.exists()) {
      return "Unexpected Error: A temporary engine specification was not created as expected for wofyawl analysis.";
    }
    
    String[] commandArray = new String[] {
       WofYAWLProxy.getBinaryExecutableFilePath(),
       getParameterForRelaxedSoundness(),
       getParameterForTransitionInvariants(),
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
  
  private void removeFile(String file) {
    (new File(file)).delete();
  }
  
  private String trimQuotes(String quotedString) {
    //assumption:  The first and last characters are indeed quote characters.
    return quotedString.substring(1, quotedString.length() - 1);
  }
  
  private String getTaskWithIssue(String rawTaskIssue) {
    Pattern firstNamePattern = Pattern.compile(
        "^<task name=(" + XML_ATTRIBUTE_REG_EXP + ").*>"
    );

    Matcher firstNameMatcher = firstNamePattern.matcher(rawTaskIssue);
    firstNameMatcher.find();
    firstNameMatcher.group();
    
    return trimQuotes(firstNameMatcher.replaceAll("$1"));
  }
  
  private String getIssueOfTask(String rawTaskIssue) {
    Pattern issuePattern = Pattern.compile(
        ".* issue=(" + XML_ATTRIBUTE_REG_EXP + ").*>"
    );

    Matcher issueMatcher = issuePattern.matcher(rawTaskIssue);
    issueMatcher.find();
    
    String issue = issueMatcher.group();
    issue = trimQuotes(issueMatcher.replaceAll("$1"));
    
    // make cryptic messages pretty and shiny for the user.
    
    issue = issue.replaceAll("execution of this task with the given preconditions ","incoming flows from following elements ");
    issue = issue.replaceAll("execution of this task with the given postconditions ","outgoing flows to following elements ");

    return issue;
  }
  
  private String getElementsInvolved(String rawTaskIssue) {
    StringBuffer elementsInvolved = new StringBuffer();
    
    Pattern taskPattern = Pattern.compile(
        "^<task name=" + XML_ATTRIBUTE_REG_EXP + ".*?>(.*?)</task>$"
    );

    Matcher elementMatcher = taskPattern.matcher(rawTaskIssue);
    elementMatcher.find();
    
    String rawElements = elementMatcher.group();

    rawElements = elementMatcher.replaceAll("$1");
    
//    System.out.println(rawElements);

    Pattern elementNamePattern = Pattern.compile(
      " name=(" + XML_ATTRIBUTE_REG_EXP + ")"    
    );
    
    Matcher elementNameMatcher = elementNamePattern.matcher(rawElements);
    
    int foundNameCounter = 0;
    
    while(elementNameMatcher.find()) {
      if (elementsInvolved.length() == 0) {
        elementsInvolved.append(": ");
      }
      
      foundNameCounter++;
      
      String rawElementName = elementNameMatcher.group();

      String elementName = rawElementName.substring(7,rawElementName.length() - 1);
      
      if (foundNameCounter > 1) {
        elementsInvolved.append(", ");
      }
      
      elementsInvolved = elementsInvolved.append(elementName);
    }
    
    elementsInvolved.append(".");
      
    return elementsInvolved.toString();
  }
  
  private String getParameterForRelaxedSoundness() {
    if(prefs.getBoolean("wofYawlRelaxedSoundnessCheck", true)) {
      return "+b";
    }
    return "-b";
  }

  private String getParameterForTransitionInvariants() {
    if(prefs.getBoolean("wofYawlTransitionInvariantCheck", true)) {
      return "+s";
    }
    return "-s";
  }

  private String getParameterForExtendedCoverability() {
    if(prefs.getBoolean("wofYawlExtendedCoverabilityCheck", true)) {
      return "+1";
    }
    return "-1";
  }
}
