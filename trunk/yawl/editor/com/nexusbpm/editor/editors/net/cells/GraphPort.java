package com.nexusbpm.editor.editors.net.cells;

import org.jgraph.graph.DefaultPort;

import com.nexusbpm.editor.persistence.DataProxy;

/**
 *  Description of the Class
 *
 * @author     catch23
 * @created    October 28, 2002
 */
public class GraphPort extends DefaultPort {

  private DataProxy _proxy;

  public GraphPort(DataProxy proxy) {
  	super();
  	_proxy = proxy;
  	_proxy.setGraphPort(this);
  }

  /**
   * @return
   */
  public DataProxy getProxy() {
    return _proxy;
  }

  /**
   * @param proxy
   */
  public void setProxy(DataProxy proxy) {
    _proxy = proxy;
  }

}
