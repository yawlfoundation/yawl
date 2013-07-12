package org.yawlfoundation.yawl.procletService.editor.blockchoiceexception;

import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;

import javax.swing.*;

public class BlockExceptionChoiceCoordinator extends InternalCoordinator {
	
	  // frame for the block
	  private FrmBlockExceptionChoice frame = null;

	  private static BlockExceptionChoiceCoordinator instance = null;
	  private JTextField nameTextField = null;

	  private BlockExceptionChoiceCoordinator(JFrame aMainFrame) {
	    super(aMainFrame);
	    frame = FrmBlockExceptionChoice.singleton(this);
	    start();
	  }

	  public static BlockExceptionChoiceCoordinator singleton(JFrame aMainFrame) {
	    //if (instance == null) {
	      instance = new BlockExceptionChoiceCoordinator(aMainFrame);
	    //}
	    return (BlockExceptionChoiceCoordinator) instance;
	  }
	  
	  public static BlockExceptionChoiceCoordinator getInstance() {
		  return instance;
	  }

	  public static boolean exists() {
	    return instance != null;
	  }

	  public static void finish() {
//		if (instance != null) {
//			instance.frame = null;
//		}
	    instance = null;
	    FrmBlockExceptionChoice.finish();
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

	  private BlockExceptionChoiceControl getBlockExceptionChoiceControl() {
	    return this.getControl().getBlockExceptionChoiceControl();
	  }
	
}
