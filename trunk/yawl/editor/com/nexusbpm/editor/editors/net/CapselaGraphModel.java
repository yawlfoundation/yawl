package com.nexusbpm.editor.editors.net;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;

/**
 * A custom graph model that does not allow edges with the same source and sink.
 * 
 * @author     Dean Mao
 * @created    January 21, 2003
 */
public class CapselaGraphModel extends DefaultGraphModel {

  private static final Log LOG = LogFactory.getLog(CapselaGraphModel.class);
  /**
   * Returns <code>true</code> only if the specified source port is not
   * equal to the edge's target port.
   * 
   * @see DefaultGraphModel#acceptsSource(Object, Object)
   */
  public boolean acceptsSource(Object edge, Object port) {
    return (((Edge) edge).getTarget() != port);
  }

  /**
   * Returns <code>true</code> only if the specified target port is not
   * equal to the edge's source port.
   * 
   * @see DefaultGraphModel#acceptsTarget(Object, Object)
   */
  public boolean acceptsTarget(Object edge, Object port) {
    return (((Edge) edge).getSource() != port);
  }

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug("CapselaGraphModel.finalize");
		super.finalize();
	}
}
