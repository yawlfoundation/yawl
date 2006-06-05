package com.nexusbpm.editor.editors.net;

import org.jgraph.graph.CellHandle;
import org.jgraph.graph.GraphContext;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 *  Description of the Class
 *
 * @author     Dean Mao
 * @created    September 17, 2003
 */
public class GraphUI extends BasicGraphUI {

  /**
   *  Description of the Method
   *
   * @param  context  Description of the Parameter
   * @return          Description of the Return Value
   */
  public CellHandle createHandle(GraphContext context) {
    if (context != null && !context.isEmpty() && graph.isEnabled()) {
      return new CapselaRootHandle(context);
    }
    return null;
  }


  /**
   *  Description of the Class
   *
   * @author     Dean Mao
   * @created    September 17, 2003
   */
  public class CapselaRootHandle extends BasicGraphUI.RootHandle {

    /**
     *Constructor for the MyRootHandle object
     *
     * @param  ctx  Description of the Parameter
     */
    public CapselaRootHandle(GraphContext ctx) {
      super(ctx);
    }


//    public void overlay(Graphics g) { }
  }
}

