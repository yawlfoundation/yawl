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
import java.util.List;

import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;

public class WofYAWLProxy implements WofYAWLProxyInterface {
  
  private static final String WOF_YAWL_BINARY = "wofyawl@WofYawlReleaseNumber@.exe";
  
  private transient static final WofYAWLProxy INSTANCE 
    = new WofYAWLProxy();

  private WofYAWLProxyInterface availableImplementation;
  private WofYAWLProxyInterface unavailableImplementation;

  public static WofYAWLProxy getInstance() {
    return INSTANCE; 
  }
    
  private WofYAWLProxy() {}
  
  private WofYAWLProxyInterface getImplementation() {
    if (wofYawlAvailable()) {
      return getAvailableImplementation();
    } 
    return getUnavailableImplementation();
  }

  public static boolean wofYawlAvailable() {
    // assumption: If we can find an executable in the same user
    // directory as the editor named by WOF_YAWL_BINARY, we assume it's
    // the right utility needed to invoke to return semantic analysis results.
    
    File wofBinary = new File(
        getBinaryExecutableFilePath()
    );
    
    if (wofBinary.exists()) {
      return true;
    }
    return false;
  }
  
  private WofYAWLProxyInterface getAvailableImplementation() {
    if (availableImplementation == null) {
      availableImplementation = new AvailableWofYAWLImplementation();
    }
    return availableImplementation;
  }
  
  private WofYAWLProxyInterface getUnavailableImplementation() {
    if (unavailableImplementation == null) {
      unavailableImplementation = new UnavailableWofYAWLImplementation();
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
           WOF_YAWL_BINARY;
  }
}
