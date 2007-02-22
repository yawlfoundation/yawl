package au.edu.qut.yawl.editor.thirdparty.engine;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.edu.qut.yawl.editor.YAWLEditor;

public abstract class AnalysisResultsParser {
  protected static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public List getAnalysisResults() {
    
    List resultsList = new LinkedList();
    String tempEngineFile = getTempEngineXMLFile();
    
    parseRawResultsIntoList(
        resultsList,
        getRawResultsFromFile(tempEngineFile)
    );
    
    
    removeFile(tempEngineFile);
    return resultsList;
  }

  /**
   * Takes the currently loaded specification, and generates a temporary (randomly named) 
   * engine XML specification file for use in wofyawl analysis.
   * @return The file path of the temporary engine XML file
   */
  
  protected String getTempEngineXMLFile() {
    
    String tempEngineFile = 
      System.getProperty("java.io.tmpdir") + 
      System.getProperty("file.separator") +
      String.valueOf(Math.random());
    
    (new EngineSpecificationExporter()).exportEngineSpecificationToFile(
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
/*
    System.out.println("Raw " + tagName + " results:\n----\n" + rawResults);
    System.out.println("----");
*/  
    return rawResults;
  }

}
