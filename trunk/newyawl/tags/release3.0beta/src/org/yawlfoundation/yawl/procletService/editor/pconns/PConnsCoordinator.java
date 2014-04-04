package org.yawlfoundation.yawl.procletService.editor.pconns;


import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;

import javax.swing.*;

public class PConnsCoordinator extends InternalCoordinator {
	
	  // frame for the block
	  private FrmPConns frame = null;

	  private static PConnsCoordinator instance = null;
	  private JTextField nameTextField = null;

	  private PConnsCoordinator(JFrame aMainFrame) {
	    super(aMainFrame);
	    frame = FrmPConns.singleton(this);
	    start();
	  }

	  public static PConnsCoordinator singleton(JFrame aMainFrame) {
	    //if (instance == null) {
	      instance = new PConnsCoordinator(aMainFrame);
	    //}
	    return (PConnsCoordinator) instance;
	  }
	  
	  public static PConnsCoordinator getInstance() {
		  return instance;
	  }

	  public static boolean exists() {
	    return instance != null;
	  }

	  public static void finish() {
	    instance = null;
	    FrmPConns.finish();
	  }

	  /**
	   * start
	   */
	  public void start() {
	    
	  }

	  /**
	   * getInternalFrame
	   *
	   * @return JInternalFrame
	   */
	  public JInternalFrame getInternalFrame() {
	    return frame;
	  }

	  /**
	   * end
	   */
	  public void end() {
	    try {
	      this.frame.setClosed(true);
	    }
	    catch (Exception pve) {}
	    ;
	  }

	  private PConnsControl getModelControl() {
	    return this.getControl().getPConnsControl();
	  }

}
