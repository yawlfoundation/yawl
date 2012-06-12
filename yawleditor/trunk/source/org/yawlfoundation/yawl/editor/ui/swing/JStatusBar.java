/*
 * Created on 18/10/2003
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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;

import javax.swing.*;
import java.awt.*;

public class JStatusBar extends JPanel {

  private static final long serialVersionUID = 1L;

  private static JLabel statusLabel = new JLabel();

  private static JConnectionStatus modeIndicator = new JConnectionStatus();

  public static int APPARENTLY_INSTANT_MILLISECONDS = 50;

  private JProgressBar progressBar;

  private SecondUpdateThread secondUpdateThread;

  private String previousStatusText;


  public JStatusBar() {
    super();
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
    statusLabel.setForeground(Color.DARK_GRAY);
    statusLabel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(), 
            BorderFactory.createEmptyBorder(0, 2, 0, 2)
        )
    );

    add(modeIndicator, BorderLayout.WEST);
    add(statusLabel, BorderLayout.CENTER);
    add(getProgressBar(), BorderLayout.EAST);
  }

  public String getStatusText() {
    return statusLabel.getText();
  }

  public void setStatusBarText(String message) {
    previousStatusText = getStatusText();
    statusLabel.setText(" " + message);
  }

  public void setStatusBarTextToPrevious() {
    setStatusBarText(previousStatusText);
  }

  public void setStatusMode(String component, boolean online) {
      modeIndicator.setStatusMode(component, online);
  }


  private JProgressBar getProgressBar() {
    progressBar = new JProgressBar();
    progressBar.setBorder(BorderFactory.createLoweredBevelBorder());
    progressBar.setSize(100, 10);
    progressBar.setForeground(Color.BLUE.darker().darker());
    return progressBar;
  }

  public void updateStatusBarProgress(int completionValue) {
    if (completionValue < 0 || completionValue > 100) {
      return;
    }
    progressBar.setValue(completionValue);
  }

  public void finishStatusBarProgress() {
    updateStatusBarProgress(100);
    YAWLEditor.pause(APPARENTLY_INSTANT_MILLISECONDS * 4);
    updateStatusBarProgress(0);
  }

  public void progressStatusBarOverSeconds(final int pauseSeconds) {
    secondUpdateThread = new SecondUpdateThread();
    try {
      secondUpdateThread.setPauseSeconds(pauseSeconds);
      secondUpdateThread.start();
    } catch (Exception e) {
      LogWriter.error("Error initialising statusbar", e);
      // either it works or it doesn't. 
      // Simply testing against the active status is not good enough. 
    }
  }

  public void resetStatusBarProgress() {
    secondUpdateThread.reset();
  }

  class SecondUpdateThread extends Thread {
    private int pauseSeconds = 0;

    private volatile boolean shouldReset = false;

    public void run() {
      final int secondsAsMillis = pauseSeconds * 1000;
      final int pausePasses = secondsAsMillis / APPARENTLY_INSTANT_MILLISECONDS;
      for (int i = 0; i < pausePasses; i++) {
        if (shouldReset) {
          return;
        }
        updateStatusBarProgress((i * 100) / (pausePasses));
        YAWLEditor.pause(APPARENTLY_INSTANT_MILLISECONDS);
      }
    }

    public void setPauseSeconds(int seconds) {
      this.pauseSeconds = seconds;
    }

    public void reset() {
      this.shouldReset = true;
      finishStatusBarProgress();
    }
  }
}
