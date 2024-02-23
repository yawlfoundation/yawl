/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
