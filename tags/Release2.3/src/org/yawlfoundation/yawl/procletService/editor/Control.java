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

