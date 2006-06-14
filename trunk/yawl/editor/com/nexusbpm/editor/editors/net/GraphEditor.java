package com.nexusbpm.editor.editors.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphSelectionModel;
import org.jgraph.graph.Port;
import org.jgraph.layout.SugiyamaLayoutAlgorithm;
import org.jgraph.util.JGraphParallelEdgeRouter;
import org.jgraph.util.JGraphUtilities;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;

import com.nexusbpm.editor.editors.NetEditor;
import com.nexusbpm.editor.editors.net.cells.CapselaCell;
import com.nexusbpm.editor.editors.net.cells.FlowControlEdge;
import com.nexusbpm.editor.editors.net.cells.GraphEdge;
import com.nexusbpm.editor.editors.net.cells.GraphPort;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.icon.AnimatedIcon;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.worker.CapselaWorker;
import com.nexusbpm.editor.worker.GlobalEventQueue;

/**
 * A graph editor, usually contained in a tab within a flow editor.
 * 
 * @author Dean Mao
 * @created Mar 15, 2004 
 */
public class GraphEditor extends JPanel implements GraphSelectionListener, KeyListener, PropertyChangeListener {

  private static final Log LOG = LogFactory.getLog(GraphEditor.class);

  private final static ImageIcon ICON_FLOW_STATUS_OFF = ApplicationIcon.getIcon("GraphEditor.flow_status_off");

  private final static ImageIcon ICON_FLOW_STATUS_RUNNING = ApplicationIcon.getIcon("GraphEditor.flow_status_running");

  private final static ImageIcon ICON_FLOW_STATUS_PAUSED = ApplicationIcon.getIcon("GraphEditor.flow_status_paused");

  private final static ImageIcon ICON_FLOW_STATUS_STOPPED = ApplicationIcon.getIcon("GraphEditor.flow_status_stopped");

  private final static ImageIcon ICON_KILL_FLOW = ApplicationIcon.getIcon("GraphEditor.kill_flow");

  private final static ImageIcon ICON_PAUSE_FLOW = ApplicationIcon.getIcon("GraphEditor.pause_flow");

  private final static ImageIcon ICON_RUN_FLOW = ApplicationIcon.getIcon("GraphEditor.run_flow");

  private final static JGraphParallelEdgeRouter EDGE_ROUTER = JGraphParallelEdgeRouter.sharedInstance;

  private CapselaGraph _graph;

  private CapselaGraphModel _graphModel;

  private Action _edgeEditModeAction;

  private JButton _runButton;

  private EditorDataProxy _flowproxy;

  private Action _remove;
  
  private Action _openDataEditor;

  private JToolBar _toolbar;

  private JButton _statusIndicatorButton;

  private JButton _killButton;

  private JButton _pauseButton;

  private JButton _flowCheckerButton;

  private List _validationMessages;

  private boolean _isInstance = false;

  private int _edgeEditMode = CONTROL_EDGE_MODE;

  private boolean _paused = false;

  private NetEditor _flowEditor = null;


  /**
   * Creates a new graph editor to be contained in the given flow editor.
   * @param isInstance whether the graph editor is for a flow instance.
   * @param flowEditor the flow editor that is creating the graph editor.
   */
  public GraphEditor(boolean isInstance, NetEditor flowEditor) {
    super();
    _flowEditor = flowEditor;
    _isInstance = isInstance;
  }
  
  /**
   * @return the flow editor that created this graph editor.
   */
  public NetEditor getFlowEditor() {
    return _flowEditor;
  }

  /**
   * @return whether this graph editor is for a flow instance.
   */
  public boolean isInstance() {
    return _isInstance;
  }

  /**
   * @return the proxy for the flow that this editor is for.
   */
  public EditorDataProxy getProxy() {
    return _flowproxy;
  }

  /**
   * Sets the proxy for the flow that this editor is for.
   * @param flowproxy the proxy for the flow.
   */
  public void setProxy(EditorDataProxy flowproxy) {
    _flowproxy = flowproxy;
  }

  /**
   * Graph editors are property change listeners for the corresponding flow, as
   * well as for the components within that flow.
   * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent event) {
	  throw new RuntimeException("handle property change events for yawl");
//    String propertyName = event.getPropertyName();
//    int status = -1;
//    if (propertyName.equals(Component.ATTR_EXECUTION_STATUS + "." + ExecutionStatus.ATTR_STATUS)) {
//      status = ((Integer) event.getNewValue()).intValue();
//    }//if
//    else if (propertyName.equals(Component.ATTR_EXECUTION_STATUS)) {
//      ExecutionStatus executionStatus = (ExecutionStatus) event.getNewValue();
//      status = executionStatus.getStatus();
//    }//else if
//    if (status == -1) return;
//    DataProxy source = (DataProxy) event.getSource();
//    if (source == this.getProxy()) {
//      // Execution status for the flow changed.
//      LOG.debug("The execution status of the flow has changed to " + status + ".");
//      this.updateStatus(status);
//    }//else
//    else {
//      // Execution status for one of the child components changed.
//      long id = source.identifier().getId();
//      LOG.debug("Component " + id + " changed execution status to " + status + ".");
//      switch (status) {
//        case ExecutionStatus.STATUS_RUNNING:
//          startCellAnimation(source);
//          break;
//        case ExecutionStatus.STATUS_FINISHED:
//        case ExecutionStatus.STATUS_STOPPED:
//        case ExecutionStatus.STATUS_KILLED:
//        case ExecutionStatus.STATUS_ERROR:
//          stopCellAnimation(source);
//          break;
//      }//switch
//      _graph.propertyChange(event);
//      /// TODO you are doing the repaint stuff:  (for repainting specific region rectangles)
//      CellView cellView = _graph.getGraphLayoutCache().getMapping(source.getGraphCell(), false);
//      if (cellView != null) {
//      Rectangle2D bounds = cellView.getBounds();
//        this.repaint((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
//      } else {
//        this.repaint();
//      }
//    }//else
  }//propertyChange()
  
  private void stopCellAnimation(EditorDataProxy proxy) {
	  _animatedproxySet.remove(proxy);
	  proxy.clearAnimatedIcon();
  }

	private Set<EditorDataProxy> _animatedproxySet = new HashSet<EditorDataProxy>();
	public void startCellAnimation( EditorDataProxy proxy ) {
		CellView view = _graph.getGraphLayoutCache().getMapping( proxy.getGraphCell(), false );

		if( null == view ) {
			LOG.warn( "GraphEditor.startCellAnimation: Couldn't get mapping for proxy " + proxy );
		}
		else {
			_animatedproxySet.add( proxy );

			Rectangle2D rendererBounds = view.getBounds();
			AnimatedIcon animatedIcon = proxy.iconAnimated( _graph );
			int xOffset = (int) ((rendererBounds.getWidth() / 2) - (animatedIcon.getBounds().getWidth() / 2));
			int yOffset = 18;

			Rectangle bounds = _graph.getCellBounds( proxy.getGraphCell() ).getBounds();
			Point location = bounds.getLocation();

			_graph.add( animatedIcon );
			animatedIcon.setLocation( (int) location.getX() + xOffset, (int) location.getY() + yOffset );
			animatedIcon.start();
		}
	}
  
  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar() == KeyEvent.VK_DELETE && this.isEditable()) {
      this.deleteSelectedItems();
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
    if (e.isControlDown()) {
      if ('c' == e.getKeyChar()) {
    	  throw new RuntimeException("implement copy operation");
      } else if ('v' == e.getKeyChar()) {
    	  throw new RuntimeException("implement paste operation");
      } else if ('p' == e.getKeyChar()) {
    	  throw new RuntimeException("implement paste operation?  ctrl-p usually means print...  who knows?");
//        if (WorkflowEditor.isCopyAction()) {
//          Iterator iter = WorkflowEditor.getCopyOrCutNodeSet().iterator();
//          while (iter.hasNext()) {
//            try {
//              // TODO Fix this junk later: (copy and paste actions in WorkflowEditor)
//              //              SharedNode newNode = null;
//              //              newNode = CommandAction.copyAction(null, _SharedNode,
//              // (Component) ((SharedNode) iter.next()).getBean());
//              //              insert(newNode.getLocation(), newNode);
//            } catch (Exception ee) {
//              LOG.error("problem during copy", ee);
//            }
//          }
//
//        } else {
//          Iterator iter = WorkflowEditor.getCopyOrCutNodeSet().iterator();
//          while (iter.hasNext()) {
//            try {
//              // TODO Fix this junk later: (copy and paste actions in client)
//              SharedNode movingNode = (SharedNode) iter.next();
//              SharedNode oldParentNode = (SharedNode) movingNode.getParent();
//              //              CommandAction.moveAction(null, _SharedNode, oldParentNode,
//              // movingNode);
//              ComponentTree.getDraggingTreeModel().reload(oldParentNode);
//              //              insert(movingNode.getLocation(), movingNode);
//            } catch (Exception ee) {
//              LOG.error("problem during copy", ee);
//            }
//          }
//        }
//        WorkflowEditor.setCopyOrCutNodeSet(new HashSet());
      }
    }
  }

  /**
   * Empty implementation.
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    // Empty.
  }

  /**
   * @return the set of selected component nodes.
   */
  public Set getSelectedSharedNodes() {
    Set<SharedNode> SharedNodeSet = new HashSet<SharedNode>();
    if (!_graph.isSelectionEmpty()) {
      Object[] cells = _graph.getSelectionCells();
      for (int i = 0; i < cells.length; i++) {
        CapselaCell currentCell = (CapselaCell) cells[i];
        SharedNodeSet.add(currentCell.getProxy().getTreeNode());
      }
    }
    return SharedNodeSet;
  }

  /**
   * Removes all cells from the graph.
   */
  public void removeEverything() {
    Object[] roots = _graph.getRoots();
    _graph.getGraphLayoutCache().remove(roots);
  }

  /**
   * Initializes the graph editor for the flow template specified by the given
   * component node.
   * @param node the component node representing the flow template.
   * @throws EditorException not thrown in the code
   */
  public void initialize(SharedNode node) throws EditorException {
    _flowproxy = (EditorDataProxy) node.getProxy();
    _toolbar = createToolBar();

    this.setLayout(new BorderLayout());
    _graphModel = new CapselaGraphModel();
    _graph = new CapselaGraph(_graphModel, this, node);
    _graph.getSelectionModel().addGraphSelectionListener(this);
    _graph.addKeyListener(this);

    add(_toolbar, BorderLayout.NORTH);
    add(new JScrollPane(_graph), BorderLayout.CENTER);
  }

  /**
   * Adds the specified graph selection listener to the graph's listener list.
   * @param listener the listener to add.
   */
  public void addGraphSelectionListener(GraphSelectionListener listener) {
    _graph.getSelectionModel().addGraphSelectionListener(listener);
  }

	/**
	 * Removes the specified graph selection listener from the graph's listener
	 * list.
	 * @param listener the listener to remove.
	 */
	public void removeGraphSelectionListener( GraphSelectionListener listener ) {
		LOG.debug("removeGraphSelectionListener");
		if( null != _graph ) {
			GraphSelectionModel selectionModel = _graph.getSelectionModel();
			if( null != selectionModel ) {
				selectionModel.removeGraphSelectionListener( listener );
			}
			else
				LOG.debug("Selection Model is empty");
		}
		else
			LOG.debug("Graph is empty");
	}

  /**
   * Initializes this graph editor to show a flow instance. The domain object
   * corresponding to the proxy passed in MUST ALREADY BE FULLY
   * INITIALIZED.
   * @param instanceproxy the proxy for the flow instance.
   * @throws EditorException if there is an error getting the local population
   *                          data for the instance.
   */
  public void initializeInstance(EditorDataProxy instanceproxy) throws EditorException {
    _isInstance = true;
    _flowproxy = instanceproxy;

//	  addPropertyChangeListener( _flowproxy, this);
//    FlowComponent flow = flowComponent;
//    for (Iterator i = flow.getComponents().iterator(); i.hasNext();) {
//      Component c = (Component) i.next();
//      DataProxy ctrl = (DataProxy) DomainObjectproxy.getproxy(c);
//
//		addPropertyChangeListener(ctrl, this);
//    }

    setLayout(new BorderLayout());
    _graphModel = new CapselaGraphModel();
    _graph = new CapselaGraph(_graphModel, this, instanceproxy);
    _graph.getSelectionModel().addGraphSelectionListener(this);

    _toolbar = createToolBar();
    this.add(_toolbar, BorderLayout.NORTH);
    this.add(new JScrollPane(_graph), BorderLayout.CENTER);
    
    throw new RuntimeException("Figure out how we do instances in yawl editor");
  }

  /**
   * Locks the graph so that nothing can be moved.
   */
  public void lockGraph() {
    _graph.lockGraph();
  }

	/**
	 * Description of the Method
	 */
	public void saveAttributes() {
		throw new RuntimeException("save all the position/bounds for all cell objects in graph");
//		if( !_isInstance ) {
			// save positions of all vertices:
//				try {
//					FlowComponent flow = (FlowComponent) _flowproxy.getPersistentDomainObject( 0 );
//					if( flow != null ) {
//						// The flow was not deleted while the graph editor was up.
//						Set vertexSet = flow.getComponents();
//						if( null == _graph ) {
//							LOG.debug( "GraphEditor.saveAttributes graph is empty" );
//						}
//						else if( vertexSet != null ) {
//							Iterator iter = vertexSet.iterator();
//							while( iter.hasNext() ) {
//								Component component = (Component) iter.next();
//								DataProxy proxy = (DataProxy) DomainObjectproxy.getproxy( component );
//								if( null != _graph ) {
//									GraphLayoutCache graphLayoutCache = _graph.getGraphLayoutCache();
//									if( null != graphLayoutCache ) {
//										CellView cellView = graphLayoutCache.getMapping( proxy.getGraphCell(), false );
//
//										if( cellView != null ) {
//											Rectangle2D bounds2D = cellView.getBounds();
//											Rectangle bounds = new Rectangle( (int) bounds2D.getX(), (int) bounds2D.getY(), (int) bounds2D
//													.getWidth(), (int) bounds2D.getHeight() );
//											if( bounds != null ) {
//												LOG.debug( "Saving flow location at: " + bounds.toString() );
//												((Component) proxy.getPersistentDomainObject()).setFlowLocation( bounds );
//											}
//										}
//										else
//											LOG.debug( "Cell View is empty" );
//									}
//									else {
//										LOG.debug( "Graph Layout Cache is empty" );
//									}
//								}
//
//							}
//						}
//					}
//				}
//				catch( Exception ee ) {
//					LOG.error( "Exception in GraphEditor.saveAttributes", ee );
//				}
//		}
	}

  /**
   * Inserts a cell for the specified domain object proxy's component at
   * the specified location.
   */
  public void insert(EditorDataProxy ctrl) throws EditorException {
    this.initializeCellAndPort(ctrl);
    CapselaCell cell = ctrl.getGraphCell();
    throw new RuntimeException("insert the object into the graph with the location properly restored");
//    Component c = (Component) ctrl.getPersistentDomainObject();
//    Map map = createComponentAttributeMap(c);
//    Hashtable<CapselaCell, Map> attributes = new Hashtable<CapselaCell, Map>();
//    attributes.put(cell, map);
//    Point location = new Point(c.getFlowLocation().getLocation());
//    _graph.snap(location);
//    _graph.getGraphLayoutCache().insert(new Object[] { cell }, attributes, null, null, null);
  }

  /**
   * Connects two cells with an edge.
   */
	public GraphEdge connect( CapselaCell source, CapselaCell target, boolean isDataEdge, EditorDataProxy edgeproxy ) {
		LOG.debug( "Connecting two cells." );
		SharedNode flowNode = _flowproxy.getTreeNode();
		SharedNode sourceNode = source.getProxy().getTreeNode();
		SharedNode targetNode = target.getProxy().getTreeNode();
		if( flowNode.isNodeChild( sourceNode ) && flowNode.isNodeChild( targetNode ) ) {
			Port sourcePort = source.getProxy().getGraphPort();
			Port targetPort = target.getProxy().getGraphPort();
			GraphEdge edge = null;  // TODO XXX implement this properly for yawl!!
			ConnectionSet cs = new ConnectionSet();
			cs.connect( edge, sourcePort, targetPort );

			Map map = createEdgeAttributeMap( isDataEdge );
			GraphConstants.setRouting( map, EDGE_ROUTER );
			GraphConstants.setLineStyle( map, GraphConstants.STYLE_BEZIER );
			Hashtable<GraphEdge, Map> attributes = new Hashtable<GraphEdge, Map>();
			attributes.put( edge, map );

			_graph.getGraphLayoutCache().insert( new Object[] { edge }, attributes, cs, null, null );
			return edge;
		}
		else {
			return null;
		}
	}

	/**
	 * Ensures that the cell and port corresponding to the specified proxy
	 * are initialiazed, and returns the cell. A component's cell and port MUST be
	 * initialized before they are added to the graph.
	 */
	private void initializeCellAndPort( EditorDataProxy ctrl ) {
		CapselaCell cell = ctrl.getGraphCell();
		GraphPort port = ctrl.getGraphPort();
		if( port == null ) {
			port = new GraphPort( ctrl );
		}
		if( cell == null ) {
			cell = new CapselaCell( ctrl );
			cell.add( port );
		}
//		addPropertyChangeListener( ctrl, _graph );
	}

//	protected void addPropertyChangeListener(DomainObjectProxy domainObjectProxy, PropertyChangeListener propertyChangeListener) {
//		if (domainObjectProxy.addPropertyChangeListener( propertyChangeListener)) {
//			DomainObjectProxy.ProxyListenerEntry proxyListenerEntry = new DomainObjectProxy.ProxyListenerEntry(domainObjectProxy, propertyChangeListener);
//			boolean ret = proxyListeners.add( proxyListenerEntry);
//			if (LOG.isDebugEnabled()) {
//				LOG.debug("GraphEditor.addPropertyChangeListener added to proxyListeners r=" + ret + ";s=" + proxyListeners.size());
//			}
//		}
//		else
//			LOG.info("GraphEditor.addPropertyChangeListener not adding to proxyListeners");
//	}
//
//	protected void removePropertyChangeListeners() {
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("GraphEditor.removePropertyChangeListeners removing " + proxyListeners.size() + " from proxyListeners");
//		}
//		DomainObjectProxy.ProxyListenerEntry proxyListenerEntry;
//		for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext(); ) {
//			proxyListenerEntry = (DomainObjectProxy.ProxyListenerEntry)iterator.next();
//			proxyListenerEntry.getDomainObjectProxy().removePropertyChangeListener( proxyListenerEntry.getPropertyChangeListener());
//
//		}
//
//		proxyListeners.clear();
//	}

  /**
   * Ensures that the graph edge corresponding to the specified proxy is
   * initialized, and returns said edge. An edge's graph edge MUST be
   * initialized before it is added to the graph.
   */
  private GraphEdge initializeGraphEdge(EditorDataProxy ctrl) {
    GraphEdge graphEdge = ctrl.getGraphEdge();
    if (graphEdge == null) {
    	YFlow componentEdge = (YFlow) ctrl.getData();
//      if (componentEdge instanceof FlowControlEdge) {
        graphEdge = new FlowControlEdge(ctrl);
        graphEdge.setUserObject(((YFlow) componentEdge).toString());
//      } else {
//        graphEdge = new FlowDataEdge(ctrl);
//      }
      ctrl.setGraphEdge(graphEdge);
    }
    return graphEdge;
  }

  /**
   * Creates the attribute map for the specified component. The attribute map is
   * required by JGraph to add the component's cell to the graph.
   */
  private Map createComponentAttributeMap(Object c) {
    Map map = new AttributeMap();
//    GraphConstants.setBounds(map, new Rectangle(c.getFlowLocation()));
    GraphConstants.setBorderColor(map, Color.black);
    GraphConstants.setBackground(map, Color.white);
    GraphConstants.setOpaque(map, true);
    return map;
  }

  /**
   * Creates the attribute map for the specified edge. The attribute map is
   * required by JGraph to add the edge to the graph.
   */
  public static Map createEdgeAttributeMap(boolean isDataEdge) {
    Map map = new AttributeMap();
    if (isDataEdge) {
      GraphConstants.setLineColor(map, new Color(150, 0, 0));
      GraphConstants.setDashPattern(map, new float[] { 8f, 4f });
      GraphConstants.setLineWidth(map, 1.0f);
    } else {
      GraphConstants.setLineColor(map, Color.BLACK);
      GraphConstants.setLineWidth(map, 1.0f);
      GraphConstants.setLabelAlongEdge(map, true);
    }
    GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
    GraphConstants.setRouting(map, EDGE_ROUTER);
    GraphConstants.setDisconnectable(map, false);
    GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
    return map;
  }

  /**
   * Returns <code>true</code> if this graph editor is in data edit mode.
   */
  public boolean isDataEditMode() {
    return _edgeEditMode == DATA_EDGE_MODE;
  }

  /**
   * Removes the specified component from the graph. Note that this method does
   * not and should not modify the flow itself (ie remove the component from the
   * flow).
   */
  public void remove(EditorDataProxy dec) {
    _graph.getGraphLayoutCache().remove(new Object[] { dec.getJGraphObject() });
	_graph.getGraphLayoutCache().removeMapping(dec.getJGraphObject());
  }

  /**
   * @see GraphSelectionListener#valueChanged(GraphSelectionEvent)
   */
  public void valueChanged(GraphSelectionEvent e) {
    boolean enabled = (!_graph.isSelectionEmpty()) && this.isEditable();
    if (_remove != null) {
      _remove.setEnabled(enabled);
    }
  }
  
  /**
   * Creates the popup menu for the specified cell.
   */
  public JPopupMenu createPopupMenu(final Object cell) {

    if (cell == null) {
      return null;
    }

    JPopupMenu menu = new JPopupMenu();

    Action editAction = new AbstractAction("Edit") {
      public void actionPerformed(ActionEvent e) {
        _graph.startEditingAtCell(cell);
      }
    };
    boolean isComponent = (cell instanceof CapselaCell);
    //boolean isDataEdge = (cell instanceof FlowDataEdge);
    menu.add(editAction);

    if (isComponent) {
      final CapselaCell capselaCell = (CapselaCell) cell;
      EditorDataProxy proxy = capselaCell.getProxy();
      final Object component = proxy.getData();
      
//      if( component != null ) {
//    	  if( component.isInstance() != true ) {
//    		  menu.addSeparator();
//    		  if( component.isCommented() != null && component.isCommented().booleanValue() == true ) {
//    			  menu.add(new AbstractAction("uncomment") {
//    				  public void actionPerformed(ActionEvent e) {
//    					  component.setCommented( Boolean.FALSE );
//    				  }
//    			  });
//    		  } else {
//    			  menu.add(new AbstractAction("comment out") {
//    				  public void actionPerformed(ActionEvent e) {
//    					  component.setCommented( Boolean.TRUE );
//    				  }
//    			  });
//    		  }
//    		  if( component.isPauseOnRun() != null && component.isPauseOnRun().booleanValue() == true ) {
//    			  menu.add(new AbstractAction("Don't pause before running") {
//    				  public void actionPerformed(ActionEvent e) {
//    					  component.setPauseOnRun( Boolean.FALSE );
//    				  }
//    			  });
//    		  } else {
//    			  menu.add(new AbstractAction("Pause before running") {
//    				  public void actionPerformed(ActionEvent e) {
//    					  component.setPauseOnRun( Boolean.TRUE );
//    				  }
//    			  });
//    		  }
//    	  } else {
//    		  if( component.getExecutionStatus().isNew() || component.getExecutionStatus().isFinished() ) {
//    			  if( component.isPauseOnRun() != null && component.isPauseOnRun().booleanValue() == true ) {
//    				  menu.addSeparator();
//    				  menu.add(new AbstractAction("Unpause") {
//    					  public void actionPerformed(ActionEvent e) {
//    						  String message = component.getPauseString();
//    						  LOG.debug( "Pressed unpause, sending JMS message '" + message + "'." );
//    						  JmsMessageProducer producer = new JmsMessageProducer( EngineBean.CONTROL_TOPIC_NAME );
//    						  producer.sendTextMessage( message, null );
//    					  }
//    				  });
//    			  }
//    		  }
//    	  }
//      }
      
      menu.addSeparator();

      menu.add(new AbstractAction("Properties") {
        public void actionPerformed(ActionEvent e) {
          _graph.startBasicEditingAtCell(capselaCell);
        }
      });
    }

    menu.addSeparator();

    Action deleteAction = new AbstractAction("Delete") {
      public void actionPerformed(ActionEvent e) {
        _remove.actionPerformed(e);
//        checkFlow();
      }
    };
    deleteAction.setEnabled(this.isEditable());
    menu.add(deleteAction);

    return menu;
  }

  /**
   * Returns <code>true</code> if this graph is editable. A graph is editable
   * if it does not represent an instance and it is not locked by someone else.
   */
  private boolean isEditable() {
	  return true;
  }

  /**
   * Retrieves a list of items that should be removed from the graph if the
   * currently selected items are to be removed, including: - The
   * DomainObjectproxys for the currently selected GraphPorts, CapselaCells
   * and GraphEdges. - The DomainObjectproxys for all the GraphEdges
   * connected to the selected GraphPorts and CapselaCells.
   * 
   * Since this method returns a Set, there are not duplicate
   * DomainObjectproxys returned.
   */
  private Set getRemoveSet() {

    // Add the currently selected GraphPorts and GraphEdges to the delete list.
    HashSet<EditorDataProxy> removeSet = new HashSet<EditorDataProxy>();
    Object[] selectedArray = _graph.getSelectionCells();
    for (int i = 0; i < selectedArray.length; i++) {
      Object selected = selectedArray[i];
      if (selected instanceof GraphPort) {
        removeSet.add(((GraphPort) selected).getProxy());
      } else if (selected instanceof GraphEdge) {
        removeSet.add(((GraphEdge) selected).getProxy());
      } else if (selected instanceof CapselaCell) {
        removeSet.add(((CapselaCell) selected).getProxy());
      }
    }

    // Add the GraphEdges connected to the selected GraphPorts.
    for (int i = 0; i < selectedArray.length; i++) {
      Object selected = selectedArray[i];
      Object[] descendants = _graph.getDescendants(new Object[] { selected });
      for (int j = 0; j < descendants.length; j++) {
        Object descendant = descendants[j];
        if (descendant instanceof GraphPort) {
          GraphPort port = (GraphPort) descendant;
          for (Iterator iter = port.edges(); iter.hasNext();) {
            EditorDataProxy doc = ((GraphEdge) iter.next()).getProxy();
            removeSet.add(doc);
          }
        }
      }
    }

    // Return the compiled list.
    return removeSet;
  }

  private void deleteSelectedItems() {
//    try {
		LOG.debug("GraphEditor.deleteSelectedItems");
      Set removeSet = GraphEditor.this.getRemoveSet();
      new RuntimeException("DISPLAY ONLY implement delete operation");
//      ClientOperation.executeDeleteCommand(removeSet, GraphEditor.this.getProxy(), GraphEditor.this);
//    } catch (EditorException ce) {
//      LOG.error("Error deleting the selected items.", ce);
//    }
  }

  private void updateStatus(int status) {
      throw new RuntimeException("implement execution status stuff");
//    if (status == ExecutionStatus.STATUS_RUNNING) {
//      // The flow just started running or was unpaused.
//      _statusIndicatorButton.setDisabledIcon(ICON_FLOW_STATUS_RUNNING);
//      _killButton.setEnabled(true);
//      _pauseButton.setEnabled(true);
//      _pauseButton.setIcon(ICON_PAUSE_FLOW);
//      _pauseButton.setToolTipText("Pause Flow");
//    }
//    else if (status == ExecutionStatus.STATUS_SUSPENDED) {
//      // The flow was just paused.
//      _statusIndicatorButton.setDisabledIcon(ICON_FLOW_STATUS_PAUSED);
//      _killButton.setEnabled(true);
//      _pauseButton.setEnabled(true);
//      _pauseButton.setIcon(ICON_RUN_FLOW);
//      _pauseButton.setToolTipText("Resume Flow");
//    }
//    else if (
//      status == ExecutionStatus.STATUS_FINISHED ||
//      status == ExecutionStatus.STATUS_STOPPED ||
//      status == ExecutionStatus.STATUS_KILLED ||
//      status == ExecutionStatus.STATUS_ERROR) {
//      // The flow is done running, or has been stopped, or has been killed, or encountered an error.
//      _statusIndicatorButton.setDisabledIcon(ICON_FLOW_STATUS_STOPPED);
//      _killButton.setEnabled(false);
//      if (_killButton.isFocusOwner()) _killButton.transferFocusUpCycle();
//      _pauseButton.setEnabled(false);
//      if (_pauseButton.isFocusOwner()) _pauseButton.transferFocusUpCycle();
//    }
//    else {
//      // The flow is none of the above.
//      _statusIndicatorButton.setDisabledIcon(ICON_FLOW_STATUS_OFF);
//    }
//    // Keep track of whether or not the flow is currently paused.
//    _paused = (status == ExecutionStatus.STATUS_SUSPENDED);
//    // Refresh the screen.
//    updateUI();
  }

	private GraphChangeSummary graphChangeSummary = null;

	/**
	 * Redraws the entire flow graph. This causes us to remove all the vertices
	 * and redraw all vertices and edges from the domain object collection.
	 * <p>
	 * This method is thread-safe, ie, you may call it from a thread other than
	 * the AWT event dispatcher thread and UI updates triggered by this method
	 * will still occur on the AWT event dispatcher thread.
	 */
	public void refresh(YNet flow) throws EditorException {
		LOG.debug( "Refreshing the flow graph." );
		// Collections to pass to the JGraph instance later on.
		final Hashtable<CapselaCell, Map> cellAttributes = new Hashtable<CapselaCell, Map>();
		final Hashtable<GraphEdge, Map> edgeAttributes = new Hashtable<GraphEdge, Map>();
		final ConnectionSet cs = new ConnectionSet();
		final List<CapselaCell> cells = new ArrayList<CapselaCell>();
		final List<GraphEdge> edges = new ArrayList<GraphEdge>();

		// Insert the vertex for each component in the flow.
//		if (flow == null) {
//			flow = (YNet) _flowproxy.getPersistentDomainObject( 2 );
//		}
		Iterator i = flow.getNetElementsDB().iterator();
		synchronized( i ) {
			while( i.hasNext() ) {
				Object c = i.next();
//				Rectangle bounds = c.getFlowLocation();
//				if( bounds == null ) throw new EditorException( "Null flow location: " + c );
				EditorDataProxy proxy = (EditorDataProxy) _flowproxy.getContext().getDataProxy(c, null );
				this.initializeCellAndPort( proxy );
				CapselaCell cell = proxy.getGraphCell();
				Map map = createComponentAttributeMap( c );
				cellAttributes.put( cell, map );
				cells.add( cell );
				LOG.error( "Adding component to graph: " + c.toString() );
//				LOG.error( "View: " + proxy.getGraphCell().get);
				
			}
		}

		// Create the runnable that will update the graph vertices on the AWT event dispatcher thread.
		Runnable vertexUpdater = new Runnable() {
			public void run() {
				System.out.println("arra: " + cells);
				System.out.println("attr: " + cellAttributes);
//				_graph.getGraphLayoutCache().insert( null, null, null, null, null );
				_graph.getGraphLayoutCache().insert(cells.toArray(), cellAttributes, null, null);
				if( graphChangeSummary != null ) {
					_graph.getModel().removeGraphModelListener( graphChangeSummary );
					graphChangeSummary = null;
				}
				graphChangeSummary = new GraphChangeSummary( _graph, GraphEditor.this );
				_graph.getModel().addGraphModelListener( graphChangeSummary );
			}
		};

		// Add all the edges in the flow.
		i = flow.getNetElementsDB().iterator();
		synchronized( i ) {
			while( i.hasNext() ) {
				YExternalNetElement component = (YExternalNetElement) i.next();
				for( Iterator i2 = component.getPostsetFlows().iterator(); i2.hasNext(); ) {

					YFlow edge = (YFlow) i2.next();
					LOG.error( "edge found: " + edge.toString() );
//					boolean isDataEdge = ( edge instanceof DataEdge );
					
					EditorDataProxy edgeproxy = (EditorDataProxy) _flowproxy.getContext().getDataProxy(edge, null );
					EditorDataProxy sourceproxy = (EditorDataProxy) _flowproxy.getContext().getDataProxy(edge.getPriorElement(), null );
					EditorDataProxy sinkproxy = (EditorDataProxy) _flowproxy.getContext().getDataProxy(edge.getNextElement(), null );

//					if( isDataEdge && ( sourceproxy.equals( _flowproxy ) || sinkproxy.equals( _flowproxy ) ) ) {
						// this is likely to be a data parameter edge going from the flow to a component, or from
						// a component to the flow.
//						LOG.debug( "################## parameter edge found!" );
						// TODO remove this, but add code for parameter nodes
//					}
//					else {
						// Checking to make sure it is not an edge from the FlowComopnent to a child component
//						if( _flowproxy.getTreeNode().isNodeChild( sourceproxy.getTreeNode() ) && _flowproxy.getTreeNode().isNodeChild( sinkproxy.getTreeNode() ) ) {
							Port sourcePort = sourceproxy.getGraphPort();
							Port sinkPort = sinkproxy.getGraphPort();
							GraphEdge graphEdge = this.initializeGraphEdge( edgeproxy );
							cs.connect( graphEdge, sourcePort, sinkPort );
							Map map = createEdgeAttributeMap( false );
							edgeAttributes.put( graphEdge, map );
							edges.add( graphEdge );
//						}
//						else {
//							LOG.debug( "NOT DRAWING EDGE! " );
//							LOG.debug( "sourceproxy: " + sourceproxy.toString() );
//							LOG.debug( "sinkproxy: " + sinkproxy.toString() );
//							LOG.debug( "flowproxy: " + _flowproxy.toString() );
//						}
//					}
//					addPropertyChangeListener(edgeproxy, _edgePropertyChangeListener);
				}
			}
		}

		// Create the runnable that will update the graph edges on the AWT event dispatcher thread.
		Runnable edgeUpdater = new Runnable() {
			public void run() {
				_graph.getGraphLayoutCache().insert( edges.toArray(), edgeAttributes, cs, null, null );
				_graph.setSelectionCells( new Object[] {} );
				_graph.repaint();
				_graph.autoSizeAll();
			}
		};

		// Update the vertices and edges on the AWT event dispatcher thread.
		// http://www.iam.ubc.ca/guides/javatut99/uiswing/overview/threads.html
		if( SwingUtilities.isEventDispatchThread() ) {
			vertexUpdater.run();
			edgeUpdater.run();
		}
		else {
			SwingUtilities.invokeLater( vertexUpdater );
			SwingUtilities.invokeLater( edgeUpdater );
		}

//		hideEdges(CONTROL_EDGE_MODE);
		
		LOG.debug( "Finished refreshing the flow graph." );
	}

	private static final int CONTROL_EDGE_MODE = 0;
	private static final int DATA_EDGE_MODE = 1;
	private static final int ALL_EDGE_MODE = 2;
	
  /**
   * @throws EditorException not thrown in the code.
   */
  private JToolBar createToolBar() throws EditorException {

    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    if (_isInstance) {

      _statusIndicatorButton = new JButton(ICON_FLOW_STATUS_OFF);
      _statusIndicatorButton.setToolTipText("Execution Status");
      _statusIndicatorButton.setDisabledIcon(ICON_FLOW_STATUS_OFF);
      _statusIndicatorButton.setEnabled(false);
      toolbar.add(_statusIndicatorButton);
      toolbar.addSeparator();

      _killButton = toolbar.add(new AbstractAction("", ICON_KILL_FLOW) {
        public void actionPerformed(ActionEvent e) {
			LOG.info("Kill flow");
			throw new RuntimeException("implement kill");
//          GlobalEventQueue.add(new CapselaWorker("Kill Button Handler") {
//            public void run() throws Throwable {
//              ClientOperation.killComponent(getProxy());
//            } //run()
//          });
        } //actionPerformed()
      });
      _killButton.setToolTipText("Kill Flow");
      _killButton.setEnabled(false);

      _pauseButton = toolbar.add(new AbstractAction("", ICON_PAUSE_FLOW) {
        public void actionPerformed(ActionEvent e) {
          try {

            if (!_paused) {
				LOG.info("Pause");
				throw new RuntimeException("implement pause");
//              ClientOperation.suspendComponent(getProxy());
            } else {
				LOG.info("Resume");
				throw new RuntimeException("implement resume");
//              ClientOperation.resumeComponent(getProxy());
            }
          } catch (Exception ex) {
            LOG.error(ex);
          }
        } //actionPerformed()
      });
      _pauseButton.setToolTipText("Pause Flow");
      _pauseButton.setEnabled(false);
      
// TODO XXX
//      Component c = (Component) getProxy().getPersistentDomainObject();
//      ExecutionStatus status = c.getExecutionStatus();
//      updateStatus(status.getStatus());

    } //if
    else {

      _runButton = toolbar.add(new AbstractAction("", ICON_RUN_FLOW) {
        public void actionPerformed(ActionEvent e) {
          try {
			  LOG.info("Run flow");

				throw new RuntimeException("implement run");
//            GlobalEventQueue.add(new CapselaWorker("Run Button Handler") {
//              public void run() throws Throwable {
//                if (_flowEditor.isDirty()) {
//                  // Save component locations before creating the instance.
//                  GraphEditor ge = _flowEditor.getFlowGraphEditor();
//                  ge.saveAttributes();
//                }
//                DataProxy ctrl = GraphEditor.this.getProxy();
//                DataProxy instance = ClientOperation.createInstance(ctrl);
//                _flowEditor.addGraphEditor(instance, true);
//                ClientOperation.runInstance(instance);
//              } //run()
//            });
          } //try
          catch (Exception ee) {
            LOG.error("exception", ee);
          } //catch
        } //actionPerformed()
      });
      _runButton.setToolTipText("Execute flow");

    } //else

    if (!_isInstance) {
    // Zoom Std
    toolbar.addSeparator();
    toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.zoom_reset")) {
      public void actionPerformed(ActionEvent e) {
        _graph.setScale(1.0);
        LOG.info("Returned graph scale to normal");
      }
    }).setToolTipText("Return Zoom to normal");
    // Zoom In
    toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.zoom_in")) {
      public void actionPerformed(ActionEvent e) {
        _graph.setScale(1.2 * _graph.getScale());
        LOG.info("Zooming in");
      }
    }).setToolTipText("Zoom In");
    // Zoom Out
    toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.zoom_out")) {
      public void actionPerformed(ActionEvent e) {
        _graph.setScale(_graph.getScale() / 1.2);
        LOG.info("Zooming out");
      }
    }).setToolTipText("Zoom Out");

      toolbar.addSeparator();
      _edgeEditModeAction = new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.select_control_edge")) {
        public void actionPerformed(ActionEvent e) {
          try {
            String iconKey = null;
            if (_edgeEditMode == ALL_EDGE_MODE) {
            	_edgeEditMode = DATA_EDGE_MODE;
//                hideEdges(DATA_EDGE_MODE);
              _graph.clearSelection();
              iconKey = "GraphEditor.select_data_edge";
              LOG.info("Changing to data communication edit mode, showing only data edges");
            } else if (_edgeEditMode == DATA_EDGE_MODE){
            	_edgeEditMode = CONTROL_EDGE_MODE;
//                hideEdges(CONTROL_EDGE_MODE);
              _graph.clearSelection();
              iconKey = "GraphEditor.select_control_edge";
              LOG.info("Changing to control flow edit mode, showing only control edges");
            } else if (_edgeEditMode == CONTROL_EDGE_MODE){
            	_edgeEditMode = ALL_EDGE_MODE;
//                hideEdges(ALL_EDGE_MODE);
              _graph.clearSelection();
              iconKey = "GraphEditor.select_control_edge";
              LOG.info("Changing to control flow edit mode, showing all edges");
            }
            if (iconKey != null) {
            	putValue(SMALL_ICON, ApplicationIcon.getIcon(iconKey));
            }
          } catch (Exception ee) {
            LOG.error("exception thrown in control/data line toggle", ee);
          }
        }
      };
      toolbar.add(_edgeEditModeAction).setToolTipText("Toggle flow editing state");
    }
    
    
    if (!_isInstance) {
      // Remove button
      _remove = new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.remove")) {
        public void actionPerformed(ActionEvent e) {
          GraphEditor.this.deleteSelectedItems();
        }
      };
      _remove.setEnabled(false);
      toolbar.add(_remove).setToolTipText("Remove component from whiteboard");
    }
    toolbar.addSeparator();

    // Printing code
    toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.print")) {
      public void actionPerformed(ActionEvent e) {
        if (_graph != null) {
          PrinterJob printJob = PrinterJob.getPrinterJob();
          printJob.setPrintable(_graph);

          printJob.pageDialog(new PageFormat());

          if (printJob.printDialog()) {
            try {
              printJob.print();
            } catch (Exception printException) {
              printException.printStackTrace();
            }
          }
        }
      }
    });

    if (false) {
      // this code is special [ talk to me about it :) ]

      // Ants walking animation code:
      toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.flow_checker_warning")) {
        public void actionPerformed(ActionEvent e) {
          Object[] edges = JGraphUtilities.getEdges(_graph.getModel());

          AttributeMap map = new AttributeMap();
          GraphConstants.setDashPattern(map, new float[] { 8f, 4f });
          GraphConstants.setDashOffset(map, 12 - Math.abs(patternPhase % 12));
          patternPhase--;
          for (int i = 0; i < edges.length; i++) {
            HashMap table = new HashMap();
            table.put(edges[i], map.clone());
            _graph.getGraphLayoutCache().edit(table, null, null, null);
          }
        }
      });
    }

    if (!_isInstance) {
      if (false) { 
        // TODO Sugiyama layout crashes capsela client for some reason so we are disabling it for now.
      // Auto layout:
      toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.layout")) {
        public void actionPerformed(ActionEvent e) {
          CapselaWorker w = new CapselaWorker("Auto Layout") {
            public void run() throws Throwable {
              if (!sugiyamaApplied) {
                // save cell state
                previousCellBounds.clear();
                Object[] cells = JGraphUtilities.getVertices(_graph.getModel(), DefaultGraphModel.getAll(_graph.getModel()));
                for (int i = 0; i < cells.length; i++) {
                  Object cell = cells[i];
                  Rectangle2D bounds = _graph.getCellBounds(cell);
                  previousCellBounds.put(cell, bounds.clone());
                }

                SugiyamaLayoutAlgorithm algorithm = new SugiyamaLayoutAlgorithm();
                algorithm.setSpacing(new Point(150, 100));
                JGraphUtilities.applyLayout(_graph, algorithm);
                _graph.autoSizeAll();
              } else {
                // restore cell state
                Hashtable table = new Hashtable();
                for (Iterator iter = previousCellBounds.keySet().iterator(); iter.hasNext();) {
                  Object cell = iter.next();
                  Rectangle2D bounds = (Rectangle2D) previousCellBounds.get(cell);
                  if (bounds != null) {
                    bounds = (Rectangle2D) bounds.clone();
                    AttributeMap map = new AttributeMap();
                    GraphConstants.setBounds(map, bounds);
                    table.put(cell, map);
                  }
                }
                _graph.getGraphLayoutCache().edit(table, null, null, null);
                _graph.repaint();
              }
              sugiyamaApplied = !sugiyamaApplied;
            }
          };
          GlobalEventQueue.add(w);
        }
      }).setToolTipText("Apply Sugiyama layout");
      }
      
      // Auto size:
      toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.autosize")) {
        public void actionPerformed(ActionEvent e) {
          CapselaWorker w = new CapselaWorker("Auto Size") {
            public void run() throws Throwable {
              _graph.autoSizeAll();
            }
          };
          GlobalEventQueue.add(w);
        }
      }).setToolTipText("Auto size graph");
    }

    // Converting to image
    //    JGraphUtilities.toImage();

    return toolbar;
  }

  private Map previousCellBounds = new HashMap();

  private boolean sugiyamaApplied = false;

  //private static final ImageIcon ANIMATED_ICON = ApplicationIcon.getIcon("GraphEditor.animated_icon");

  // pattern phase:
  private float patternPhase = 0;

	public void clear() throws Throwable {
		if (LOG.isDebugEnabled()) {
			LOG.debug("GraphEditor.clear i=" + isInstance());
		}
		
		// free up the memory taken up by animated icons
		for(Iterator iter=_animatedproxySet.iterator(); iter.hasNext();) {
			EditorDataProxy proxy = (EditorDataProxy) iter.next();
			stopCellAnimation(proxy);
		}

		if (null != graphChangeSummary) {
			GraphChangeSummary tmp = graphChangeSummary;  // avoid stack overflow on recursive clears
			graphChangeSummary = null;

			if (null != _graph) {
				GraphModel model = _graph.getModel();
				if (null != model ) {
					model.removeGraphModelListener( tmp);
				}
			}
		}

		if (null != _flowEditor) {
			NetEditor tmp = _flowEditor; // avoid stack overflow on recursive clears
			_flowEditor = null;
			tmp.clear();

		}

		// don't remove _flowproxy in clear - because it will prevent FlowEditor.removeGraphEditor from working
		// which will leave a reference to FlowEditor in GraphEditor and it won't finalize
		// _flowproxy = null;

//		removePropertyChangeListeners();

		_graphModel = null;

		// may not be strictly necessary but makes it a little easier to see in profile what is left
		_edgeEditModeAction = null;
		_runButton = null;
		_remove = null;
		_openDataEditor = null;
		_toolbar = null;
		_statusIndicatorButton = null;
		_killButton = null;
		_pauseButton = null;
		_flowCheckerButton = null;
		_validationMessages = null;

		if (null != previousCellBounds) {
			Map tmp = previousCellBounds;
			previousCellBounds = null;
			tmp.clear();
		}
	}

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug("GraphEditor.finalize");
		clear();

		// don't do this in clear cause need to to get past saveAttributes call
		// and doing this too soon will also make removeGraphSelectionListener fail
		if (null != _graph) {
			CapselaGraph tmp = _graph; // avoid stack overflow on recursive clears
			_graph = null;
			GraphSelectionModel selectionModel = tmp.getSelectionModel();
			if (null != selectionModel ) {
				selectionModel.removeGraphSelectionListener(this);
			}

			tmp.clear();
		}

		// don't remove _flowproxy in clear - because it will prevent FlowEditor.removeGraphEditor from working
		// which will leave a reference to FlowEditor in GraphEditor and it won't finalize
		_flowproxy = null;

		super.finalize();
	}

}