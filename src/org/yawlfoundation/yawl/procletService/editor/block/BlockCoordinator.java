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

package org.yawlfoundation.yawl.procletService.editor.block;


import org.yawlfoundation.yawl.procletService.connect.Receiver;
import org.yawlfoundation.yawl.procletService.editor.InternalCoordinator;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;

public class BlockCoordinator
    extends InternalCoordinator {

  // frame for the block
  private FrmBlock frame = null;
  
  private Receiver receive = null;
  
  private InteractionNode node = null;
  
  private EntityMID emidSelected = null;
  
  private String option = "";

  private static BlockCoordinator instance = null;

  private BlockCoordinator(JFrame aMainFrame,String option) {
    super(aMainFrame);
    this.option = option;
    frame = FrmBlock.singleton(this);
    start();
  }

  public static BlockCoordinator singleton(JFrame aMainFrame,String option) {
    //if (instance == null) {
      instance = new BlockCoordinator(aMainFrame,option);
    //}
    return (BlockCoordinator) instance;
  }

  public static boolean exists() {
    return instance != null;
  }

  public static void finish() {
	  FrmBlock.finish();
	  instance = null;
  }
  
  public void setCP() {
	  option = "CP";
  }
  
  public boolean isCP() {
	  if (option.equals("CP")) {
		  return true;
	  }
	  return false;
  }
  
  public void setCaseException() {
	  option = "CaseException";
  }
  
  public boolean isCaseException () {
	  if (option.equals("CaseException")) {
		  return true;
	  }
	  return false;
  }
  
  public void setBlockException () {
	  option = "BlockException";
  }
  
  public boolean isBlockException () {
	  if (option.equals("BlockException")) {
		  return true;
	  }
	  return false;
  }
  
  public void setInteractionNode(InteractionNode node) {
	  this.node = node;
  }
  
  public void setSelectedEmid (EntityMID emid) {
	  this.emidSelected = emid;
  }
  
  public EntityMID getSelectedEmid () {
	  return this.emidSelected;
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
  
  public void initiateReceiver () {
	// make a connect
	  try {
		  Thread.sleep(500);
	  }
	  catch (Exception e) {
		  e.printStackTrace();
	  }
	  this.receive = new Receiver();
	  receive.initiate();
  }
  
  public Receiver getReceiver() {
	  return this.receive;
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

  private BlockControl getBlockControl() {
    return this.getControl().getBlockControl();
  }
}

