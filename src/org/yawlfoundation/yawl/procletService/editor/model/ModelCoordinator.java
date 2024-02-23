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

package org.yawlfoundation.yawl.procletService.editor.model;

import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;

import javax.swing.*;

public class ModelCoordinator extends InternalCoordinator {

  // frame for the block
  private FrmModel frame = null;

  private static ModelCoordinator instance = null;
  private JTextField nameTextField = null;

  private ModelCoordinator(JFrame aMainFrame) {
    super(aMainFrame);
    frame = FrmModel.singleton(this);
    start();
  }

  public static ModelCoordinator singleton(JFrame aMainFrame) {
    //if (instance == null) {
      instance = new ModelCoordinator(aMainFrame);
    //}
    return (ModelCoordinator) instance;
  }
  
  public static ModelCoordinator getInstance() {
	  return instance;
  }

  public static boolean exists() {
    return instance != null;
  }

  public static void finish() {
	FrmModel.finish();
    instance = null;
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

  private ModelControl getModelControl() {
    return this.getControl().getModelControl();
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


