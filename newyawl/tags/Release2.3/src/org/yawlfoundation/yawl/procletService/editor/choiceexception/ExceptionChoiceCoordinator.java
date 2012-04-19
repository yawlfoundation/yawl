package org.yawlfoundation.yawl.procletService.editor.choiceexception;


import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;

import javax.swing.*;

public class ExceptionChoiceCoordinator extends InternalCoordinator {

	  // frame for the block
	  private FrmExceptionChoice frame = null;

	  private static ExceptionChoiceCoordinator instance = null;
	  private JTextField nameTextField = null;

	  private ExceptionChoiceCoordinator(JFrame aMainFrame) {
	    super(aMainFrame);
	    frame = FrmExceptionChoice.singleton(this);
	    start();
	  }

	  public static ExceptionChoiceCoordinator singleton(JFrame aMainFrame) {
	    //if (instance == null) {
	      instance = new ExceptionChoiceCoordinator(aMainFrame);
	    //}
	    return (ExceptionChoiceCoordinator) instance;
	  }
	  
	  public static ExceptionChoiceCoordinator getInstance() {
		  return instance;
	  }

	  public static boolean exists() {
	    return instance != null;
	  }

	  public static void finish() {
	    instance = null;
	    FrmExceptionChoice.finish();
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

	  private ExceptionChoiceControl getExceptionChoiceControl() {
	    return this.getControl().getExceptionChoiceControl();
	  }
	  
	}