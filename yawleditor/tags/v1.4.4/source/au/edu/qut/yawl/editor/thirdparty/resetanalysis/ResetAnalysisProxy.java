/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
 *
 * @author Lindsay Bradford / Moe Wynn
 * 
 * Copyright (C) 2003 Queensland University of Technology
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
import java.util.List;

import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;

public class ResetAnalysisProxy implements ResetAnalysisProxyInterface {
  
  private static final String ANALYSIS_Jar = "YAWLResetAnalyser.jar";
  
  private transient static final ResetAnalysisProxy INSTANCE 
    = new ResetAnalysisProxy();

  private ResetAnalysisProxyInterface availableImplementation;
  private ResetAnalysisProxyInterface unavailableImplementation;

  public static ResetAnalysisProxy getInstance() {
    return INSTANCE; 
  }
    
  private ResetAnalysisProxy() {}
  
  private ResetAnalysisProxyInterface getImplementation() {
    if (resetAnalysisAvailable()) {
      return getAvailableImplementation();
    } 
    return getUnavailableImplementation();
  }

  public static boolean resetAnalysisAvailable() {
    // assumption: If we can find an executable in the same user
    // directory as the editor named by ANALYSIS_jar, we assume it's
    // the right utility needed to invoke to return semantic analysis results.
    
    File analysisBinary = new File(
        getBinaryExecutableFilePath()
    );
    
    if (analysisBinary.exists()) {
      return true;
    }
    return false;
  }
  
  private ResetAnalysisProxyInterface getAvailableImplementation() {
    if (availableImplementation == null) {
      availableImplementation = new AvailableResetAnalysisImplementation();
    }
    return availableImplementation;
  }
  
  private ResetAnalysisProxyInterface getUnavailableImplementation() {
    if (unavailableImplementation == null) {
      unavailableImplementation = new UnavailableResetAnalysisImplementation();
    }
    return unavailableImplementation;
  }
  
  public List getAnalysisResults() {
    return getImplementation().getAnalysisResults();
  }
  
  public void analyse() {
    ProblemMessagePanel.getInstance().setProblemList(
      "Problems identified in specification analysis",
      getAnalysisResults()    
    );
  }
  
  public static String getBinaryExecutableFilePath() {
    return System.getProperty("user.dir") + 
           System.getProperty("file.separator") + 
           ANALYSIS_Jar;
  }
}
