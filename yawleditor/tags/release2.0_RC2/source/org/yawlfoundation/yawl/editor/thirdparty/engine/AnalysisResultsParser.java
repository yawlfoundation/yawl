package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AnalysisResultsParser {
  // added this flag to allow the temp file to be inspected in case of error - MJF
  protected boolean errorFound = false;
  protected static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public List getAnalysisResults(SpecificationModel editorSpec) {
    
//    String tempEngineFile = getTempEngineXMLFile(editorSpec);

    String specXML = EngineSpecificationExporter.getEngineSpecificationXML(editorSpec);

    List results = getAnalysisResults(specXML);
    
    // added this check to allow the temp file to be inspected in case of error - MJF
//    if (!errorFound) removeFile(tempEngineFile);
    
    return results;
  }
  
  
  public List getAnalysisResults(String engineFile) {

    List resultsList = new LinkedList();

    parseRawResultsIntoList(
        resultsList,
        getRawResultsFromFile(engineFile)
    );
    
    return resultsList;

  }
  /**
   * Takes the currently loaded specification, and generates a temporary (randomly named) 
   * engine XML specification file for use in wofyawl analysis.
   * @return The file path of the temporary engine XML file
   */
  
  protected String getTempEngineXMLFile(SpecificationModel editorSpec) {
    
    String tempEngineFile = 
      System.getProperty("java.io.tmpdir") + 
      System.getProperty("file.separator") +
      String.valueOf(Math.random());
    
    EngineSpecificationExporter.exportEngineSpecToFile(
      editorSpec,
      tempEngineFile
    );

    return tempEngineFile;
  }

  protected void removeFile(String file) {
    (new File(file)).delete();
  }
  
  abstract protected String getRawResultsFromFile(String tempEngineFile);
  
  abstract protected void parseRawResultsIntoList(List resultsList, String rawAnalysisXML);
  
  protected String getRawResultsBetweenTagsWithName(String tagName, String rawAnalysisXML) {
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

    return rawResults;
  }

}
