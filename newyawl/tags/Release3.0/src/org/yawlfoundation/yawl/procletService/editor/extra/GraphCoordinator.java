package org.yawlfoundation.yawl.procletService.editor.extra;


import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;

public class GraphCoordinator extends InternalCoordinator {

	  // frame for the block
	  private FrmGraph frame = null;

	  private static GraphCoordinator instance = null;
	  private JTextField nameTextField = null;

	  private GraphCoordinator(JFrame aMainFrame,EntityMID emid) {
	    super(aMainFrame);
	    frame = FrmGraph.singleton(this,emid);
	    start();
	  }

	  public static GraphCoordinator singleton(JFrame aMainFrame, EntityMID emid) {
	    //if (instance == null) {
	      instance = new GraphCoordinator(aMainFrame,emid);
	    //}
	    return (GraphCoordinator) instance;
	  }
	  
	  public static GraphCoordinator getInstance() {
		  return instance;
	  }

	  public static boolean exists() {
	    return instance != null;
	  }

	  public static void finish() {
	    instance = null;
	    FrmGraph.finish();
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

	  private GraphControl getGraphControl() {
	    return this.getControl().getGraphControl();
	  }
	  
	  public void setNameTextField(JTextField nameTextField) {
		  this.nameTextField = nameTextField;
	  }
	  
	  public JTextField getNameTextField() {
		  return this.nameTextField;
	  }
	  
	  public String getNameModel () {
		 return this.getNameTextField().getText();
	  }
}
