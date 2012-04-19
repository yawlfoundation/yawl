package org.yawlfoundation.yawl.procletService.editor;

import javax.swing.*;


public abstract class InternalCoordinator {

  protected JFrame mainFrame = null;

  protected InternalCoordinator(JFrame aMainFrame) {
    mainFrame = aMainFrame;
  }

  public abstract JInternalFrame getInternalFrame();

  public abstract void start();

  public abstract void end();

  protected Control getControl() {
    return Control.singleton();
  }

}
