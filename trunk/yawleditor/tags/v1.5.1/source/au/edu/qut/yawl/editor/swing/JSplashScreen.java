/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.swing; 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;

/** 
 *  JSplashScreen is a simple window object that displays a splash screen 
 *  controllable by the calling context.
 *   
 */

public class JSplashScreen {

  private String message;
  private String imageFile;

  private JWindow splashScreen;
  private JProgressBar progressBar;


  /**
	 * Creates a new splashscreen containing an image file, progress bar and message string.
	 * @param imageFile  the filename of the image to display.
	 * @param message    the message string to display below the progress bar.
	 */
  
  public JSplashScreen() {}
  
  public void setContent(String imageFile, String message) {
    this.message = message;
    this.imageFile = imageFile;

    buildScreen();
  }

	/**
	 *    Displays the slash screen window, auto-centering on-screen and showing an 
	 *    initially 0% complete progress bar.
	 *    This method will return immediately, leaving the splash screen visibile 
	 *    until the calling context signals completion with the @see #finish() method. 
	 * */

 	public void show() {
    splashScreen.setVisible(true);
  }
  
  /**
   *   Updates the progress bar to indicate a new percentage complete value.
   *   @param completionValue  the percentage complete as an integer (e.g. - pass the value 30 to indicate 30% complete). Values not between 0 and 100 are ignored.
   */

	public void updateProgressBar(int completionValue) {
      if (progressBar == null) {
        return;
      }
      if (completionValue < 0 || completionValue > 100) {
       return;
      }
  	  progressBar.setValue(completionValue);
	}

	/**
	 *   Sets the splash screen window's progress bar to 100% complete, waits a small amount of time
	 *   and then hides the splash screen.
	 */

  public void finish() {
    updateProgressBar(100);
    pause(200);
    splashScreen.setVisible(false);
  }

  private static void pause(long milliseconds) {
    long now = System.currentTimeMillis();
    long finishTime = now + milliseconds;
    while(now < finishTime) {
      now = System.currentTimeMillis();
    }
  }

  private void buildScreen() {
    splashScreen = new JWindow();

    splashScreen.getContentPane().add(getContents(imageFile, message), 
                                      BorderLayout.CENTER);

    splashScreen.pack();

    JUtilities.centerWindow(splashScreen);
  }

  private JComponent getContents(String imageFile, String message) {
    GridBagLayout      gbl  = new GridBagLayout();
    GridBagConstraints gbc  = new GridBagConstraints();

    JPanel windowPanel = new JPanel(gbl);

    windowPanel.setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 1),
                                         BorderFactory.createEmptyBorder(1,1,1,1)));


    int yPos = 0;

    gbc.gridwidth    = 1;
    gbc.gridheight   = 1;
    gbc.gridx        = 0;
    gbc.gridy        = yPos++;

    windowPanel.add(ResourceLoader.getImageAsJLabel(imageFile),gbc);

    gbc.fill         = GridBagConstraints.HORIZONTAL;
    gbc.insets       = new Insets(3,5,2,5);
    gbc.gridy        = yPos++;

    windowPanel.add(getProgressBar(),gbc);

    gbc.anchor       = GridBagConstraints.CENTER;
    gbc.insets       = new Insets(2,0,5,0);
    gbc.gridy        = yPos++;

    windowPanel.add(new JLabel(message, JLabel.CENTER), gbc);
    
    return windowPanel;
  }

  private JProgressBar getProgressBar() {
    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setBorder(BorderFactory.createLoweredBevelBorder());
    progressBar.setForeground(Color.BLUE.darker().darker());
    return progressBar;
  }
}
