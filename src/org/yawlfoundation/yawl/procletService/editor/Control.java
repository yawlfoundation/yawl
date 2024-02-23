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

package org.yawlfoundation.yawl.procletService.editor;

import org.yawlfoundation.yawl.procletService.editor.block.BlockControl;
import org.yawlfoundation.yawl.procletService.editor.blockchoiceexception.BlockExceptionChoiceControl;
import org.yawlfoundation.yawl.procletService.editor.choiceexception.ExceptionChoiceControl;
import org.yawlfoundation.yawl.procletService.editor.extra.GraphControl;
import org.yawlfoundation.yawl.procletService.editor.model.ModelControl;
import org.yawlfoundation.yawl.procletService.editor.pconns.PConnsControl;

public class Control {

  private static Control instance = null;

  private BlockControl block;
  private ModelControl model;
  private PConnsControl pconns;
  private ExceptionChoiceControl excChoice;
  private BlockExceptionChoiceControl blockExcChoice;
  private GraphControl graphControl;

  private Control() {
    super();
    block = BlockControl.singleton();
  }

  public BlockControl getBlockControl() {
    return block;
  }
  
  public ModelControl getModelControl() {
	  return model;
  }
  
  public PConnsControl getPConnsControl() {
	  return pconns;
  }
  
  public ExceptionChoiceControl getExceptionChoiceControl () {
	  return excChoice;
  }
  
  public BlockExceptionChoiceControl getBlockExceptionChoiceControl () {
	  return blockExcChoice;
  }
  
  public GraphControl getGraphControl () {
	  return graphControl;
  }

  public static Control singleton() {
    if (instance == null) {
      instance = new Control();
    }
    return instance;
  }
}

