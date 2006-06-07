package com.nexusbpm.editor.editors.net.cells;

import org.jgraph.graph.DefaultPort;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 *  Description of the Class
 *
 * @author     catch23
 * @created    October 28, 2002
 */
public class GraphPort extends DefaultPort {

  private EditorDataProxy _proxy;

  public GraphPort(EditorDataProxy proxy) {
  	super();
  	_proxy = proxy;
  	_proxy.setGraphPort(this);
  }

  /**
   * @return
   */
  public EditorDataProxy getProxy() {
    return _proxy;
  }

  /**
   * @param proxy
   */
  public void setProxy(EditorDataProxy proxy) {
    _proxy = proxy;
  }

}
