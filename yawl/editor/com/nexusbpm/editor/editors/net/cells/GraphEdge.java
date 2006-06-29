package com.nexusbpm.editor.editors.net.cells;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;

import com.nexusbpm.editor.persistence.EditorDataProxy;


/**
 * Graph edge that has a special routing mechanism and awareness of domain object proxys
 * 
 * @author catch23
 * @created October 28, 2002 
 */
public class GraphEdge extends DefaultEdge {
  private static final Log LOG = LogFactory.getLog(GraphEdge.class);
  
  public GraphEdge(EditorDataProxy proxy) {
    super();
    _proxy = proxy;
    _proxy.setGraphEdge(this);
  }

  public EditorDataProxy _proxy;

  public EditorDataProxy getProxy() {
    return _proxy;
  }

  public void setProxy(EditorDataProxy proxy) {
    _proxy = proxy;
  }
}