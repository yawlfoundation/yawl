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
import java.net.URLDecoder;
import java.util.ArrayList;
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
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphSelectionModel;
import org.jgraph.graph.Port;
import org.jgraph.layout.SugiyamaLayoutAlgorithm;
import org.jgraph.util.JGraphParallelEdgeRouter;
import org.jgraph.util.JGraphUtilities;

import au.edu.qut.yawl.elements.ExtensionListContainer;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;
import au.edu.qut.yawl.unmarshal.YMarshal;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.CompoundCommand;
import com.nexusbpm.command.RemoveFlowCommand;
import com.nexusbpm.command.RemoveNexusTaskCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.editors.NetEditor;
import com.nexusbpm.editor.editors.net.cells.FlowControlEdge;
import com.nexusbpm.editor.editors.net.cells.GraphEdge;
import com.nexusbpm.editor.editors.net.cells.GraphPort;
import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.icon.AnimatedIcon;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.util.InterfaceA;
import com.nexusbpm.editor.util.InterfaceB;
import com.nexusbpm.editor.worker.CapselaWorker;
import com.nexusbpm.editor.worker.GlobalEventQueue;

/**
 * A graph editor, usually contained in a tab within a flow editor.
 * 
 * @author Dean Mao
 * @created Mar 15, 2004 
 */
public class GraphEditor extends JPanel
    implements GraphSelectionListener, KeyListener, PropertyChangeListener {
    private static final Log LOG = LogFactory.getLog( GraphEditor.class );

    private final static ImageIcon ICON_FLOW_STATUS_OFF = ApplicationIcon.getIcon( "GraphEditor.flow_status_off" );
//    private final static ImageIcon ICON_FLOW_STATUS_RUNNING = ApplicationIcon.getIcon( "GraphEditor.flow_status_running" );
//    private final static ImageIcon ICON_FLOW_STATUS_PAUSED = ApplicationIcon.getIcon( "GraphEditor.flow_status_paused" );
//    private final static ImageIcon ICON_FLOW_STATUS_STOPPED = ApplicationIcon.getIcon( "GraphEditor.flow_status_stopped" );
    private final static ImageIcon ICON_KILL_FLOW = ApplicationIcon.getIcon( "GraphEditor.kill_flow" );
    private final static ImageIcon ICON_PAUSE_FLOW = ApplicationIcon.getIcon( "GraphEditor.pause_flow" );
    private final static ImageIcon ICON_RUN_FLOW = ApplicationIcon.getIcon( "GraphEditor.run_flow" );
    private final static JGraphParallelEdgeRouter EDGE_ROUTER = JGraphParallelEdgeRouter.sharedInstance;

    private NexusGraph _graph;
    private NexusGraphModel _graphModel;

    private NetEditor _netEditor;
    private EditorDataProxy<YNet> _netProxy;

    private Action _edgeEditModeAction;
    private Action _remove;

    private JToolBar _toolbar;

    private JButton _runButton;
    private JButton _statusIndicatorButton;
    private JButton _killButton;
    private JButton _pauseButton;
    private JButton _flowCheckerButton;

    private List _validationMessages;

    private boolean _isInstance;
    private boolean _paused;

    private int _edgeEditMode = CONTROL_EDGE_MODE;

    /**
     * Creates a new graph editor to be contained in the given flow editor.
     * @param isInstance whether the graph editor is for a flow instance.
     * @param flowEditor the flow editor that is creating the graph editor.
     */
    public GraphEditor( boolean isInstance, NetEditor netEditor ) {
        super();
        _netEditor = netEditor;
        _isInstance = isInstance;
    }

    /**
     * @return the flow editor that created this graph editor.
     */
    public NetEditor getNetEditor() {
        return _netEditor;
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
        return _netProxy;
    }

    /**
     * Sets the proxy for the flow that this editor is for.
     * @param flowproxy the proxy for the flow.
     */
    public void setProxy( EditorDataProxy flowproxy ) {
        _netProxy = flowproxy;
    }
    
    protected YSpecification getParentSpecification() {
    	return _netProxy.getData().getParent();
    }
    
    private void repaintGraph() {
        Runnable repainter = new Runnable() {
            public void run() {
                _graph.invalidate();
                _graph.validate();
                _graph.repaint();
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            repainter.run();
        }
        else {
            SwingUtilities.invokeLater( repainter );
        }
    }

    /**
     * Graph editors are property change listeners for the corresponding flow, as
     * well as for the components within that flow.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange( PropertyChangeEvent event ) {
        if( event.getPropertyName().equals( DataProxyStateChangeListener.PROPERTY_NAME ) ) {
            repaintGraph();
        }
        else if( event.getPropertyName().equals( DataProxyStateChangeListener.PROPERTY_TASK_BOUNDS ) ) {
            final Map attributes = (Map) event.getNewValue();
            Runnable updater = new Runnable() {
                public void run() {
                    _graph.getGraphLayoutCache().edit(attributes, null, null, null);
                }
            };
            
            if( SwingUtilities.isEventDispatchThread() ) {
                updater.run();
            }
            else {
                SwingUtilities.invokeLater( updater );
            }
            
        }
    }//propertyChange()

    private void stopCellAnimation( EditorDataProxy proxy ) {
        _animatedproxySet.remove( proxy );
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
    public void keyTyped( KeyEvent e ) {
        if( e.getKeyChar() == KeyEvent.VK_DELETE && this.isEditable() ) {
            this.deleteSelectedItems();
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed( KeyEvent e ) {
        if( e.isControlDown() ) {
            if( 'c' == e.getKeyChar() ) {
                throw new RuntimeException( "implement copy operation" );
            }
            else if( 'v' == e.getKeyChar() ) {
                throw new RuntimeException( "implement paste operation" );
            }
            else if( 'p' == e.getKeyChar() ) {
                throw new RuntimeException( "implement paste operation?  ctrl-p usually means print...  who knows?" );
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
    public void keyReleased( KeyEvent e ) {
        // Empty.
    }

    /**
     * @return the set of selected component nodes.
     */
    public Set getSelectedSharedNodes() {
        Set<SharedNode> sharedNodeSet = new HashSet<SharedNode>();
        if( !_graph.isSelectionEmpty() ) {
            Object[] cells = _graph.getSelectionCells();
            for( int i = 0; i < cells.length; i++ ) {
                NexusCell currentCell = (NexusCell) cells[ i ];
                sharedNodeSet.add( currentCell.getProxy().getTreeNode() );
            }
        }
        return sharedNodeSet;
    }

    /**
     * Removes all cells from the graph.
     */
    public void removeEverything() {
        Runnable remover = new Runnable() {
            public void run() {
                Object[] roots = _graph.getRoots();
                _graph.getGraphLayoutCache().remove( roots );
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            remover.run();
        }
        else {
            SwingUtilities.invokeLater( remover );
        }
    }

    /**
     * Initializes the graph editor for the flow template specified by the given
     * component node.
     * @param node the component node representing the flow template.
     * @throws EditorException not thrown in the code
     */
    public void initialize( SharedNode node ) throws EditorException {
        _netProxy = node.getProxy();
        _toolbar = createToolBar();

        this.setLayout( new BorderLayout() );
        _graphModel = new NexusGraphModel();
        _graph = new NexusGraph( _graphModel, this, node );
        _graph.getSelectionModel().addGraphSelectionListener( this );
        _graph.addKeyListener( this );

        add( _toolbar, BorderLayout.NORTH );
        add( new JScrollPane( _graph ), BorderLayout.CENTER );
    }

    /**
     * Adds the specified graph selection listener to the graph's listener list.
     * @param listener the listener to add.
     */
    public void addGraphSelectionListener( GraphSelectionListener listener ) {
        _graph.getSelectionModel().addGraphSelectionListener( listener );
    }

    /**
     * Removes the specified graph selection listener from the graph's listener
     * list.
     * @param listener the listener to remove.
     */
    public void removeGraphSelectionListener( GraphSelectionListener listener ) {
        LOG.debug( "removeGraphSelectionListener" );
        if( null != _graph ) {
            GraphSelectionModel selectionModel = _graph.getSelectionModel();
            if( null != selectionModel ) {
                selectionModel.removeGraphSelectionListener( listener );
            }
            else
                LOG.debug( "Selection Model is empty" );
        }
        else
            LOG.debug( "Graph is empty" );
    }

    /**
     * Initializes this graph editor to show a flow instance. The domain object
     * corresponding to the proxy passed in MUST ALREADY BE FULLY
     * INITIALIZED.
     * @param instanceproxy the proxy for the flow instance.
     * @throws EditorException if there is an error getting the local population
     *                          data for the instance.
     */
    public void initializeInstance( EditorDataProxy instanceproxy ) throws EditorException {
        _isInstance = true;
        _netProxy = instanceproxy;
//	  addPropertyChangeListener( _flowproxy, this);
//    FlowComponent flow = flowComponent;
//    for (Iterator i = flow.getComponents().iterator(); i.hasNext();) {
//      Component c = (Component) i.next();
//      DataProxy ctrl = (DataProxy) DomainObjectproxy.getproxy(c);
//
//		addPropertyChangeListener(ctrl, this);
//    }
        setLayout( new BorderLayout() );
        _graphModel = new NexusGraphModel();
        _graph = new NexusGraph( _graphModel, this, instanceproxy );
        _graph.getSelectionModel().addGraphSelectionListener( this );

        _toolbar = createToolBar();
        this.add( _toolbar, BorderLayout.NORTH );
        this.add( new JScrollPane( _graph ), BorderLayout.CENTER );

        throw new RuntimeException( "Figure out how we do instances in yawl editor" );
    }

    /**
     * Locks the graph so that nothing can be moved.
     */
    public void lockGraph() {
        _graph.lockGraph();
    }

    /**
     * Inserts a cell for the specified domain object proxy's component at
     * the specified location.
     */
    public void insert( EditorDataProxy proxy ) {
        Runnable updater = null;
        if( proxy.getData() instanceof YExternalNetElement ) {
            final Hashtable<NexusCell, Map> cellAttributes = new Hashtable<NexusCell, Map>();
            final List<NexusCell> cells = new ArrayList<NexusCell>();
            
            NexusCell cell = proxy.getGraphCell();
            Map map = createComponentAttributeMap( proxy.getData() );
            cellAttributes.put( cell, map );
            cells.add( cell );
            
            YTaskEditorExtension editor = new YTaskEditorExtension((YExternalNetElement) proxy.getData());
            Rectangle2D rect = (Rectangle2D) editor.getBounds().clone();
            GraphConstants.setBounds(cell.getAttributes(), rect);
            
            updater = new Runnable() {
                public void run() {
                    _graph.getGraphLayoutCache().insert(cells.toArray(), cellAttributes, null, null);
                }
            };
        }
        else if( proxy.getData() instanceof YFlow ) {
            final ConnectionSet connectionSet = new ConnectionSet();
            final Hashtable<GraphEdge, Map> edgeAttributes = new Hashtable<GraphEdge, Map>();
            final List<GraphEdge> edges = new ArrayList<GraphEdge>();
            
            YFlow edge = (YFlow) proxy.getData();
            DataContext context = proxy.getContext();
            
            EditorDataProxy edgeProxy = (EditorDataProxy) context.getDataProxy( edge );
            assert edgeProxy != null : "edge proxy was null";
            EditorDataProxy sourceProxy = (EditorDataProxy) context.getDataProxy( edge.getPriorElement() );
            assert sourceProxy != null : "source proxy was null";
            EditorDataProxy sinkProxy = (EditorDataProxy) context.getDataProxy( edge.getNextElement() );
            assert sinkProxy != null : "sink proxy was null";
            
            Port sourcePort = sourceProxy.getGraphPort();
            assert sourcePort != null : "source port was null";
            Port sinkPort = sinkProxy.getGraphPort();
            assert sinkPort != null : "sink port was null";
            GraphEdge graphEdge = this.initializeGraphEdge( edgeProxy );
            assert graphEdge != null : "graph edge was null";
            
            connectionSet.connect( graphEdge, sourcePort, sinkPort );
            Map map = createEdgeAttributeMap( false );
            edgeAttributes.put( graphEdge, map );
            edges.add( graphEdge );
            
            updater = new Runnable() {
                public void run() {
                    _graph.getGraphLayoutCache().insert(
                            edges.toArray(),
                            edgeAttributes,
                            connectionSet,
                            null,
                            null );
                    _graph.setSelectionCells( new Object[] {} );
                    _graph.repaint();
                    _graph.autoSizeAll();
                }
            };
        }
        else {
            LOG.error( "Inserting invalid class:" + proxy.getData().getClass().getName(),
                    new Exception().fillInStackTrace() );
        }
        
        assert updater != null : "graph editor updater was null";
        
        if( SwingUtilities.isEventDispatchThread() ) {
            updater.run();
        }
        else {
            SwingUtilities.invokeLater( updater );
        }
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
    private GraphEdge initializeGraphEdge( EditorDataProxy ctrl ) {
        GraphEdge graphEdge = ctrl.getGraphEdge();
        if( graphEdge == null ) {
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
    private Map createComponentAttributeMap( Object c ) {
        Map map = new AttributeMap();
//        GraphConstants.setBounds(map, new Rectangle(c.getFlowLocation()));
        if (c instanceof ExtensionListContainer) {
            ExtensionListContainer container = (ExtensionListContainer) c;
//            for (Element e: container.getInternalExtensions()) {
//                e.getAttribute(getName());
//            }
        }
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBackground(map, Color.white);
        GraphConstants.setOpaque(map, true);
        return map;
    }

    /**
     * Creates the attribute map for the specified edge. The attribute map is
     * required by JGraph to add the edge to the graph.
     */
    public static Map createEdgeAttributeMap( boolean isDataEdge ) {
        Map map = new AttributeMap();
        if( isDataEdge ) {
            GraphConstants.setLineColor( map, new Color( 150, 0, 0 ) );
            GraphConstants.setDashPattern( map, new float[] { 8f, 4f } );
            GraphConstants.setLineWidth( map, 1.0f );
        }
        else {
            GraphConstants.setLineColor( map, Color.BLACK );
            GraphConstants.setLineWidth( map, 1.0f );
            GraphConstants.setLabelAlongEdge( map, true );
        }
        GraphConstants.setLineStyle( map, GraphConstants.STYLE_BEZIER );
        GraphConstants.setRouting( map, EDGE_ROUTER );
        GraphConstants.setDisconnectable( map, true );
        GraphConstants.setLineEnd( map, GraphConstants.ARROW_TECHNICAL );
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
     * not and should not modify the net itself (ie remove the component from the
     * net).
     */
    public void remove( EditorDataProxy dec ) {
        final Object obj = dec.getJGraphObject();
        Runnable remover = new Runnable() {
            public void run() {
                _graph.getGraphLayoutCache().remove( new Object[] { obj } );
                _graph.getGraphLayoutCache().removeMapping( obj );
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            remover.run();
        }
        else {
            SwingUtilities.invokeLater( remover );
        }
    }

    /**
     * @see GraphSelectionListener#valueChanged(GraphSelectionEvent)
     */
    public void valueChanged( GraphSelectionEvent e ) {
        boolean enabled = ( !_graph.isSelectionEmpty() ) && this.isEditable();
        if( _remove != null ) {
            _remove.setEnabled( enabled );
        }
    }

    protected boolean handleEditTrigger(Object cell) {
        _graph.scrollCellToVisible(cell);
        if (cell != null)
            _graph.getUI().startEditingAtCell( _graph, cell );
        return _graph.isEditing();
    }

    /**
     * Creates the popup menu for the specified cell.
     */
    public JPopupMenu createPopupMenu( final Object cell ) {

        if( cell == null ) { return null; }

        JPopupMenu menu = new JPopupMenu();

        Action editAction = new AbstractAction( "Edit" ) {
            public void actionPerformed( ActionEvent e ) {
                _graph.startEditingAtCell( cell );
            }
        };
        
        boolean editable = _graph.isCellEditable( cell );
        
        boolean isComponent = ( cell instanceof NexusCell );
        //boolean isDataEdge = (cell instanceof FlowDataEdge);
        menu.add( editAction );

        if( isComponent ) {
            final NexusCell capselaCell = (NexusCell) cell;
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
            
            if( editable ) {
                menu.add( new AbstractAction( "Rename" ) {
                    public void actionPerformed( ActionEvent e ) {
                        handleEditTrigger( cell );
                    }
                });
            }

            menu.add( new AbstractAction( "Properties" ) {
                public void actionPerformed( ActionEvent e ) {
                    _graph.startBasicEditingAtCell( capselaCell );
                }
            } );
        }

        menu.addSeparator();

        Action deleteAction = new AbstractAction( "Delete" ) {
            public void actionPerformed( ActionEvent e ) {
                _remove.actionPerformed( e );
                //        checkFlow();
            }
        };
        deleteAction.setEnabled( this.isEditable() );
        menu.add( deleteAction );

        return menu;
    }

    /**
     * Returns <code>true</code> if this graph is editable. A graph is editable
     * if it does not represent an instance and it is not locked by someone else.
     */
    private boolean isEditable() {
        return true;
    }

    private void deleteSelectedItems() {
        LOG.debug( "GraphEditor.deleteSelectedItems" );
        Object[] cells = _graph.getSelectionCells();
        List<Object> selectionCells = new ArrayList<Object>( cells.length + 1 );
        // filter out the input and output conditions first
        for( Object cell : cells ) {
            if( cell instanceof NexusCell ) {
                NexusCell ncell = (NexusCell) cell;
                Object data = ncell.getProxy().getData();
                if( ! ( data instanceof YInputCondition ||
                        data instanceof YOutputCondition ) ) {
                    selectionCells.add( cell );
                }
            }
            else {
                selectionCells.add( cell );
            }
        }
        _graph.setSelectionCells( selectionCells.toArray() );
        cells = _graph.getSelectionCells();
        
        List<Command> taskCommands = new ArrayList<Command>();
        List<Command> edgeCommands = new ArrayList<Command>();
        Set<Edge> edgeSet = new HashSet<Edge>();
        
        for( Object o : cells ) {
            if( o instanceof NexusCell ) {
                NexusCell cell = (NexusCell) o;
                taskCommands.add( new RemoveNexusTaskCommand( cell.getProxy() ) );
                
                GraphPort port = cell.getProxy().getGraphPort();
                Set<Edge> edges = new HashSet<Edge>( port.getEdges() );
                for( Edge edge : edges ) {
                    edgeSet.add( edge );
                }
            }
            else if( o instanceof FlowControlEdge ) {
                FlowControlEdge edge = (FlowControlEdge) o;
                edgeSet.add( edge );
            }
            else {
                LOG.error("not a cell or edge but instead a "
                        + o.getClass().getName() );
            }
        }
        
        for( Edge e : edgeSet ) {
            if( e instanceof FlowControlEdge ) {
                edgeCommands.add( new RemoveFlowCommand( ((FlowControlEdge) e).getProxy() ) );
            }
            else {
                LOG.warn( "deleting an edge that's not a control flow edge!" );
            }
        }
        
        if( taskCommands.size() > 0 || edgeCommands.size() > 0 ) {
            CompoundCommand taskCommand = new CompoundCommand( taskCommands );
            CompoundCommand edgeCommand = new CompoundCommand( edgeCommands );
            
            List<Command> compoundList = new ArrayList<Command>();
            compoundList.add( edgeCommand ); // remove all edges first
            compoundList.add( taskCommand ); // then remove all tasks
            CompoundCommand command = new CompoundCommand( compoundList );
            
            WorkflowEditor.getExecutor().executeCommand( command );
        }
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

	/**
	 * Redraws the entire flow graph. This causes us to remove all the vertices
	 * and redraw all vertices and edges from the domain object collection.
     * (TODO: the current code does NOT remove the current edges/vertices)
	 * <p>
	 * This method is thread-safe, ie, you may call it from a thread other than
	 * the AWT event dispatcher thread and UI updates triggered by this method
	 * will still occur on the AWT event dispatcher thread.
	 */
	public void refresh(YNet net) {
		LOG.debug( "Refreshing the flow graph." );
		// Collections to pass to the JGraph instance later on.
		final Hashtable<NexusCell, Map> cellAttributes = new Hashtable<NexusCell, Map>();
		final Hashtable<GraphEdge, Map> edgeAttributes = new Hashtable<GraphEdge, Map>();
		final ConnectionSet cs = new ConnectionSet();
		final List<NexusCell> cells = new ArrayList<NexusCell>();
		final List<GraphEdge> edges = new ArrayList<GraphEdge>();

		// Insert the vertex for each component in the flow.
//		if (net == null) {
//			net = (YNet) _netProxy.getPersistentDomainObject( 2 );
//		}
		Iterator i = net.getNetElements().iterator();
		synchronized( i ) {
			while( i.hasNext() ) {
				Object c = i.next();
				EditorDataProxy proxy = (EditorDataProxy) _netProxy.getContext().getDataProxy(c);
				NexusCell cell = proxy.getGraphCell();
				Map map = createComponentAttributeMap( c );
				cellAttributes.put( cell, map );
				cells.add( cell );
				LOG.debug( "Adding component to graph: " + c.toString() );
				try {
					if (c instanceof YExternalNetElement) {
						YTaskEditorExtension ext = new YTaskEditorExtension((YExternalNetElement) c);
						Rectangle2D rect = (Rectangle2D) ext.getBounds().clone();
						GraphConstants.setBounds(cell.getAttributes(), rect);
					}
				} catch(Exception ex) {LOG.error(ex.getMessage(), ex);}
			}
		}

		// Create the runnable that will update the graph vertices on the AWT event dispatcher thread.
		Runnable vertexUpdater = new Runnable() {
			public void run() {
				_graph.getGraphLayoutCache().insert(cells.toArray(), cellAttributes, null, null);
			}
		};

		// Add all the edges in the flow.
		i = net.getNetElements().iterator();
		synchronized( i ) {
			while( i.hasNext() ) {
				YExternalNetElement component = (YExternalNetElement) i.next();
				for( Iterator i2 = component.getPostsetFlows().iterator(); i2.hasNext(); ) {

					YFlow edge = (YFlow) i2.next();
					LOG.debug( "edge found: " + edge.toString() );
//					boolean isDataEdge = ( edge instanceof DataEdge );
					
					EditorDataProxy edgeproxy = (EditorDataProxy) _netProxy.getContext().getDataProxy(edge);
					EditorDataProxy sourceproxy = (EditorDataProxy) _netProxy.getContext().getDataProxy(edge.getPriorElement());
					EditorDataProxy sinkproxy = (EditorDataProxy) _netProxy.getContext().getDataProxy(edge.getNextElement());

//					if( isDataEdge && ( sourceproxy.equals( _flowproxy ) || sinkproxy.equals( _flowproxy ) ) ) {
						// this is likely to be a data parameter edge going from the flow to a component, or from
						// a component to the flow.
//						LOG.debug( "################## parameter edge found!" );
						// TODO remove this, but add code for parameter nodes
//					}
//					else {
						// Checking to make sure it is not an edge from the FlowComopnent to a child component
//						if( _netProxy.getTreeNode().isNodeChild( sourceproxy.getTreeNode() ) && _netProxy.getTreeNode().isNodeChild( sinkproxy.getTreeNode() ) ) {
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
//							LOG.debug( "netproxy: " + _netProxy.toString() );
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
        toolbar.setFloatable( false );

        if( _isInstance ) {

            _statusIndicatorButton = new JButton( ICON_FLOW_STATUS_OFF );
            _statusIndicatorButton.setToolTipText( "Execution Status" );
            _statusIndicatorButton.setDisabledIcon( ICON_FLOW_STATUS_OFF );
            _statusIndicatorButton.setEnabled( false );
            toolbar.add( _statusIndicatorButton );
            toolbar.addSeparator();

            _killButton = toolbar.add( new AbstractAction( "", ICON_KILL_FLOW ) {
                public void actionPerformed( ActionEvent e ) {
                    LOG.info( "Kill flow" );
                    throw new RuntimeException( "implement kill" );
//          GlobalEventQueue.add(new CapselaWorker("Kill Button Handler") {
//            public void run() throws Throwable {
//              ClientOperation.killComponent(getProxy());
//            } //run()
//          });
                } //actionPerformed()
            });
      _killButton.setToolTipText( "Kill Flow" );
            _killButton.setEnabled( false );

            _pauseButton = toolbar.add( new AbstractAction( "", ICON_PAUSE_FLOW ) {
                public void actionPerformed( ActionEvent e ) {
                    try {

                        if( !_paused ) {
                            LOG.info( "Pause" );
                            throw new RuntimeException( "implement pause" );
//                          ClientOperation.suspendComponent(getProxy());
                        } else {
                            LOG.info("Resume");
                            throw new RuntimeException("implement resume");
//                          ClientOperation.resumeComponent(getProxy());
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
                        
                        YSpecification spec = GraphEditor.this.getParentSpecification();
                        
                        // TODO do we need to save the spec?
                        
                        InterfaceA_EnvironmentBasedClient client = InterfaceA.getClient();
                        
                        LOG.info( "unloading specification...\n" + client.unloadSpecification(
                        		spec.getID(), InterfaceA.getConnectionHandle() ) );
                        String str = YMarshal.marshal( spec );
                        LOG.info( str );
                        String response = client.uploadSpecification(
                        		str, "asdf", InterfaceA.getConnectionHandle() );
                        LOG.info( response );
                        
                        if( InterfaceA.successful( response ) ) {
                        	InterfaceB_EnvironmentBasedClient clientB = InterfaceB.getClient();
                        	
                        	LOG.info( clientB.getSpecification( spec.getID(), InterfaceB.getConnectionHandle() ) );
                        }
                        else {
                        	LOG.error( "Error uploading specification!\n" + response );
                        }
                        
//                        throw new RuntimeException("implement run");
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
                    LOG.debug("Returned graph scale to normal");
                }
            }).setToolTipText("Return Zoom to normal");
            // Zoom In
            toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.zoom_in")) {
                public void actionPerformed(ActionEvent e) {
                    _graph.setScale(1.2 * _graph.getScale());
                    LOG.debug("Zooming in");
                }
            }).setToolTipText("Zoom In");
            // Zoom Out
            toolbar.add(new AbstractAction("", ApplicationIcon.getIcon("GraphEditor.zoom_out")) {
                public void actionPerformed(ActionEvent e) {
                    _graph.setScale(_graph.getScale() / 1.2);
                    LOG.debug("Zooming out");
                }
            }).setToolTipText("Zoom Out");

            toolbar.addSeparator();
            _edgeEditModeAction = new AbstractAction( "", ApplicationIcon.getIcon( "GraphEditor.select_control_edge" ) ) {
                public void actionPerformed( ActionEvent e ) {
                    try {
                        String iconKey = null;
                        if( _edgeEditMode == ALL_EDGE_MODE ) {
                            _edgeEditMode = DATA_EDGE_MODE;
//                          hideEdges(DATA_EDGE_MODE);
                            _graph.clearSelection();
                            iconKey = "GraphEditor.select_data_edge";
                            LOG.debug( "Changing to data communication edit mode, showing only data edges" );
                        }
                        else if( _edgeEditMode == DATA_EDGE_MODE ) {
                            _edgeEditMode = CONTROL_EDGE_MODE;
//                          hideEdges(CONTROL_EDGE_MODE);
                            _graph.clearSelection();
                            iconKey = "GraphEditor.select_control_edge";
                            LOG.debug( "Changing to control flow edit mode, showing only control edges" );
                        }
                        else if( _edgeEditMode == CONTROL_EDGE_MODE ) {
                            _edgeEditMode = ALL_EDGE_MODE;
//                          hideEdges(ALL_EDGE_MODE);
                            _graph.clearSelection();
                            iconKey = "GraphEditor.select_control_edge";
                            LOG.debug( "Changing to control flow edit mode, showing all edges" );
                        }
                        if( iconKey != null ) {
                            putValue( SMALL_ICON, ApplicationIcon.getIcon( iconKey ) );
                        }
                    }
                    catch( Exception ee ) {
                        LOG.error( "exception thrown in control/data line toggle", ee );
                    }
                }
            };
            toolbar.add( _edgeEditModeAction ).setToolTipText( "Toggle flow editing state" );
        }

        if( !_isInstance ) {
            // Remove button
            _remove = new AbstractAction( "Delete", ApplicationIcon.getIcon( "GraphEditor.remove" ) ) {
                public void actionPerformed( ActionEvent e ) {
                    GraphEditor.this.deleteSelectedItems();
                }
            };
            _remove.setEnabled( false );
            toolbar.add( _remove ).setToolTipText( "Remove component from whiteboard" );
        }
        toolbar.addSeparator();

        // Printing code
        toolbar.add( new AbstractAction( "", ApplicationIcon.getIcon( "GraphEditor.print" ) ) {
            public void actionPerformed( ActionEvent e ) {
                if( _graph != null ) {
                    PrinterJob printJob = PrinterJob.getPrinterJob();
                    printJob.setPrintable( _graph );

                    printJob.pageDialog( new PageFormat() );

                    if( printJob.printDialog() ) {
                        try {
                            printJob.print();
                        }
                        catch( Exception printException ) {
                            printException.printStackTrace();
                        }
                    }
                }
            }
        } );

        if( false ) {
            // this code is special [ talk to me about it :) ]

            // Ants walking animation code:
            toolbar.add( new AbstractAction( "", ApplicationIcon.getIcon( "GraphEditor.flow_checker_warning" ) ) {
                public void actionPerformed( ActionEvent e ) {
                    Object[] edges = JGraphUtilities.getEdges( _graph.getModel() );

                    AttributeMap map = new AttributeMap();
                    GraphConstants.setDashPattern( map, new float[] { 8f, 4f } );
                    GraphConstants.setDashOffset( map, 12 - Math.abs( patternPhase % 12 ) );
                    patternPhase--;
                    for( int i = 0; i < edges.length; i++ ) {
                        HashMap table = new HashMap();
                        table.put( edges[ i ], map.clone() );
                        _graph.getGraphLayoutCache().edit( table, null, null, null );
                    }
                }
            } );
        }

        if( !_isInstance ) {
            if( false ) {
                // TODO Sugiyama layout crashes capsela client for some reason so we are disabling it for now.
                // Auto layout:
                toolbar.add( new AbstractAction( "", ApplicationIcon.getIcon( "GraphEditor.layout" ) ) {
                    public void actionPerformed( ActionEvent e ) {
                        CapselaWorker w = new CapselaWorker( "Auto Layout" ) {
                            public void run() throws Throwable {
                                if( !sugiyamaApplied ) {
                                    // save cell state
                                    previousCellBounds.clear();
                                    Object[] cells = JGraphUtilities.getVertices( _graph.getModel(), DefaultGraphModel.getAll( _graph.getModel() ) );
                                    for( int i = 0; i < cells.length; i++ ) {
                                        Object cell = cells[ i ];
                                        Rectangle2D bounds = _graph.getCellBounds( cell );
                                        previousCellBounds.put( cell, bounds.clone() );
                                    }

                                    SugiyamaLayoutAlgorithm algorithm = new SugiyamaLayoutAlgorithm();
                                    algorithm.setSpacing( new Point( 150, 100 ) );
                                    JGraphUtilities.applyLayout( _graph, algorithm );
                                    _graph.autoSizeAll();
                                }
                                else {
                                    // restore cell state
                                    Hashtable table = new Hashtable();
                                    for( Iterator iter = previousCellBounds.keySet().iterator(); iter.hasNext(); ) {
                                        Object cell = iter.next();
                                        Rectangle2D bounds = (Rectangle2D) previousCellBounds.get( cell );
                                        if( bounds != null ) {
                                            bounds = (Rectangle2D) bounds.clone();
                                            AttributeMap map = new AttributeMap();
                                            GraphConstants.setBounds( map, bounds );
                                            table.put( cell, map );
                                        }
                                    }
                                    _graph.getGraphLayoutCache().edit( table, null, null, null );
                                    _graph.repaint();
                                }
                                sugiyamaApplied = !sugiyamaApplied;
                            }
                        };
                        GlobalEventQueue.add( w );
                    }
                } ).setToolTipText( "Apply Sugiyama layout" );
            }

            // Auto size:
            toolbar.add( new AbstractAction( "", ApplicationIcon.getIcon( "GraphEditor.autosize" ) ) {
                public void actionPerformed( ActionEvent e ) {
                    CapselaWorker w = new CapselaWorker( "Auto Size" ) {
                        public void run() throws Throwable {
                            _graph.autoSizeAll();
                        }
                    };
                    GlobalEventQueue.add( w );
                }
            } ).setToolTipText( "Auto size graph" );
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
        if( LOG.isDebugEnabled() ) {
            LOG.debug( "GraphEditor.clear i=" + isInstance() );
        }

        // free up the memory taken up by animated icons
        for( Iterator iter = _animatedproxySet.iterator(); iter.hasNext(); ) {
            EditorDataProxy proxy = (EditorDataProxy) iter.next();
            stopCellAnimation( proxy );
        }

        if( null != _netEditor ) {
            // TODO FIXME XXX do we really want this? Can we not close a single graph editor without closing the net editor?
            NetEditor tmp = _netEditor; // avoid stack overflow on recursive clears
            _netEditor = null;
            tmp.frameClosed();

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
        _toolbar = null;
        _statusIndicatorButton = null;
        _killButton = null;
        _pauseButton = null;
        _flowCheckerButton = null;
        _validationMessages = null;

        if( null != previousCellBounds ) {
            Map tmp = previousCellBounds;
            previousCellBounds = null;
            tmp.clear();
        }
    }
    
//    public static void main(String[] args) throws Exception {
//    	System.out.println(URLDecoder.decode("au.edu.qut.yawl.exceptions.YStateException%3A+Engine+contains+no+such+specification+with+id+%5BSpecification%5D.%0D%0A%09at+au.edu.qut.yawl.engine.AbstractEngine.unloadSpecification%28AbstractEngine.java%3A830%29%0D%0A%09at+au.edu.qut.yawl.engine.YEngine.unloadSpecification%28YEngine.java%3A646%29%0D%0A%09at+sun.reflect.GeneratedMethodAccessor235.invoke%28Unknown+Source%29%0D%0A%09at+sun.reflect.DelegatingMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+java.lang.reflect.Method.invoke%28Unknown+Source%29%0D%0A%09at+org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection%28AopUtils.java%3A334%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint%28ReflectiveMethodInvocation.java%3A181%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A148%29%0D%0A%09at+org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor.invoke%28ThrowsAdviceInterceptor.java%3A118%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke%28AfterReturningAdviceInterceptor.java%3A51%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke%28MethodBeforeAdviceInterceptor.java%3A53%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.JdkDynamicAopProxy.invoke%28JdkDynamicAopProxy.java%3A209%29%0D%0A%09at+%24Proxy42.unloadSpecification%28Unknown+Source%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.EngineGatewayImpl.unloadSpecification%28EngineGatewayImpl.java%3A738%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.processPostQuery%28InterfaceA_EngineBasedServer.java%3A246%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.doPost%28InterfaceA_EngineBasedServer.java%3A99%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A709%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A802%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.internalDoFilter%28ApplicationFilterChain.java%3A252%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.doFilter%28ApplicationFilterChain.java%3A173%29%0D%0A%09at+org.apache.catalina.core.StandardWrapperValve.invoke%28StandardWrapperValve.java%3A213%29%0D%0A%09at+org.apache.catalina.core.StandardContextValve.invoke%28StandardContextValve.java%3A178%29%0D%0A%09at+org.apache.catalina.core.StandardHostValve.invoke%28StandardHostValve.java%3A126%29%0D%0A%09at+org.apache.catalina.valves.ErrorReportValve.invoke%28ErrorReportValve.java%3A105%29%0D%0A%09at+org.apache.catalina.core.StandardEngineValve.invoke%28StandardEngineValve.java%3A107%29%0D%0A%09at+org.apache.catalina.connector.CoyoteAdapter.service%28CoyoteAdapter.java%3A148%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProcessor.process%28Http11AprProcessor.java%3A831%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProtocol%24Http11ConnectionHandler.process%28Http11AprProtocol.java%3A639%29%0D%0A%09at+org.apache.tomcat.util.net.AprEndpoint%24Worker.run%28AprEndpoint.java%3A1196%29%0D%0A%09at+java.lang.Thread.run%28Unknown+Source%29%0D%0A", "UTF-8"));
    	
//    	System.out.println("----");
    	
//    	System.out.println(URLDecoder.decode("org.jdom.input.JDOMParseException%3A+Error+on+line+2%3A+The+prefix+%22xsi%22+for+attribute+%22xsi%3Atype%22+associated+with+an+element+type+%22decomposition%22+is+not+bound.%0D%0A%09at+org.jdom.input.SAXBuilder.build%28SAXBuilder.java%3A466%29%0D%0A%09at+au.edu.qut.yawl.unmarshal.YMarshal.buildSpecificationSetDocument%28YMarshal.java%3A136%29%0D%0A%09at+au.edu.qut.yawl.unmarshal.YMarshal.unmarshalSpecifications%28YMarshal.java%3A119%29%0D%0A%09at+au.edu.qut.yawl.engine.AbstractEngine.addSpecifications%28AbstractEngine.java%3A597%29%0D%0A%09at+au.edu.qut.yawl.engine.YEngine.addSpecifications%28YEngine.java%3A617%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke0%28Native+Method%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+sun.reflect.DelegatingMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+java.lang.reflect.Method.invoke%28Unknown+Source%29%0D%0A%09at+org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection%28AopUtils.java%3A334%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint%28ReflectiveMethodInvocation.java%3A181%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A148%29%0D%0A%09at+org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor.invoke%28ThrowsAdviceInterceptor.java%3A118%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke%28AfterReturningAdviceInterceptor.java%3A51%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke%28MethodBeforeAdviceInterceptor.java%3A53%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.JdkDynamicAopProxy.invoke%28JdkDynamicAopProxy.java%3A209%29%0D%0A%09at+%24Proxy42.addSpecifications%28Unknown+Source%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.EngineGatewayImpl.loadSpecification%28EngineGatewayImpl.java%3A689%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.processPostQuery%28InterfaceA_EngineBasedServer.java%3A260%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.doPost%28InterfaceA_EngineBasedServer.java%3A99%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A709%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A802%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.internalDoFilter%28ApplicationFilterChain.java%3A252%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.doFilter%28ApplicationFilterChain.java%3A173%29%0D%0A%09at+org.apache.catalina.core.StandardWrapperValve.invoke%28StandardWrapperValve.java%3A213%29%0D%0A%09at+org.apache.catalina.core.StandardContextValve.invoke%28StandardContextValve.java%3A178%29%0D%0A%09at+org.apache.catalina.core.StandardHostValve.invoke%28StandardHostValve.java%3A126%29%0D%0A%09at+org.apache.catalina.valves.ErrorReportValve.invoke%28ErrorReportValve.java%3A105%29%0D%0A%09at+org.apache.catalina.core.StandardEngineValve.invoke%28StandardEngineValve.java%3A107%29%0D%0A%09at+org.apache.catalina.connector.CoyoteAdapter.service%28CoyoteAdapter.java%3A148%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProcessor.process%28Http11AprProcessor.java%3A831%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProtocol%24Http11ConnectionHandler.process%28Http11AprProtocol.java%3A639%29%0D%0A%09at+org.apache.tomcat.util.net.AprEndpoint%24Worker.run%28AprEndpoint.java%3A1196%29%0D%0A%09at+java.lang.Thread.run%28Unknown+Source%29%0D%0ACaused+by%3A+org.xml.sax.SAXParseException%3A+The+prefix+%22xsi%22+for+attribute+%22xsi%3Atype%22+associated+with+an+element+type+%22decomposition%22+is+not+bound.%0D%0A%09at+org.apache.xerces.util.ErrorHandlerWrapper.createSAXParseException%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.util.ErrorHandlerWrapper.fatalError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLErrorReporter.reportError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLErrorReporter.reportError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLNSDocumentScannerImpl.scanStartElement%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLDocumentFragmentScannerImpl%24FragmentContentDispatcher.dispatch%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanDocument%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XML11Configuration.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XML11Configuration.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XMLParser.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.AbstractSAXParser.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.jaxp.SAXParserImpl%24JAXPSAXParser.parse%28Unknown+Source%29%0D%0A%09at+org.jdom.input.SAXBuilder.build%28SAXBuilder.java%3A455%29%0D%0A%09...+36+more%0D%0ACaused+by%3A+org.xml.sax.SAXParseException%3A+The+prefix+%22xsi%22+for+attribute+%22xsi%3Atype%22+associated+with+an+element+type+%22decomposition%22+is+not+bound.%0D%0A%09at+org.apache.xerces.util.ErrorHandlerWrapper.createSAXParseException%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.util.ErrorHandlerWrapper.fatalError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLErrorReporter.reportError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLErrorReporter.reportError%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLNSDocumentScannerImpl.scanStartElement%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLDocumentFragmentScannerImpl%24FragmentContentDispatcher.dispatch%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanDocument%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XML11Configuration.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XML11Configuration.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.XMLParser.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.parsers.AbstractSAXParser.parse%28Unknown+Source%29%0D%0A%09at+org.apache.xerces.jaxp.SAXParserImpl%24JAXPSAXParser.parse%28Unknown+Source%29%0D%0A%09at+org.jdom.input.SAXBuilder.build%28SAXBuilder.java%3A455%29%0D%0A%09at+au.edu.qut.yawl.unmarshal.YMarshal.buildSpecificationSetDocument%28YMarshal.java%3A136%29%0D%0A%09at+au.edu.qut.yawl.unmarshal.YMarshal.unmarshalSpecifications%28YMarshal.java%3A119%29%0D%0A%09at+au.edu.qut.yawl.engine.AbstractEngine.addSpecifications%28AbstractEngine.java%3A597%29%0D%0A%09at+au.edu.qut.yawl.engine.YEngine.addSpecifications%28YEngine.java%3A617%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke0%28Native+Method%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+sun.reflect.DelegatingMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+java.lang.reflect.Method.invoke%28Unknown+Source%29%0D%0A%09at+org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection%28AopUtils.java%3A334%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint%28ReflectiveMethodInvocation.java%3A181%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A148%29%0D%0A%09at+org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor.invoke%28ThrowsAdviceInterceptor.java%3A118%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke%28AfterReturningAdviceInterceptor.java%3A51%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke%28MethodBeforeAdviceInterceptor.java%3A53%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.JdkDynamicAopProxy.invoke%28JdkDynamicAopProxy.java%3A209%29%0D%0A%09at+%24Proxy42.addSpecifications%28Unknown+Source%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.EngineGatewayImpl.loadSpecification%28EngineGatewayImpl.java%3A689%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.processPostQuery%28InterfaceA_EngineBasedServer.java%3A260%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.doPost%28InterfaceA_EngineBasedServer.java%3A99%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A709%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A802%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.internalDoFilter%28ApplicationFilterChain.java%3A252%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.doFilter%28ApplicationFilterChain.java%3A173%29%0D%0A%09at+org.apache.catalina.core.StandardWrapperValve.invoke%28StandardWrapperValve.java%3A213%29%0D%0A%09at+org.apache.catalina.core.StandardContextValve.invoke%28StandardContextValve.java%3A178%29%0D%0A%09at+org.apache.catalina.core.StandardHostValve.invoke%28StandardHostValve.java%3A126%29%0D%0A%09at+org.apache.catalina.valves.ErrorReportValve.invoke%28ErrorReportValve.java%3A105%29%0D%0A%09at+org.apache.catalina.core.StandardEngineValve.invoke%28StandardEngineValve.java%3A107%29%0D%0A%09at+org.apache.catalina.connector.CoyoteAdapter.service%28CoyoteAdapter.java%3A148%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProcessor.process%28Http11AprProcessor.java%3A831%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProtocol%24Http11ConnectionHandler.process%28Http11AprProtocol.java%3A639%29%0D%0A%09at+org.apache.tomcat.util.net.AprEndpoint%24Worker.run%28AprEndpoint.java%3A1196%29%0D%0A%09at+java.lang.Thread.run%28Unknown+Source%29%0D%0A","UTF-8"));
//    }

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug("GraphEditor.finalize");
		clear();

		// don't do this in clear cause need to to get past saveAttributes call
		// and doing this too soon will also make removeGraphSelectionListener fail
		if (null != _graph) {
			NexusGraph tmp = _graph; // avoid stack overflow on recursive clears
			_graph = null;
			GraphSelectionModel selectionModel = tmp.getSelectionModel();
			if (null != selectionModel ) {
				selectionModel.removeGraphSelectionListener(this);
			}

			tmp.clear();
		}

		// don't remove _flowproxy in clear - because it will prevent FlowEditor.removeGraphEditor from working
		// which will leave a reference to FlowEditor in GraphEditor and it won't finalize
		_netProxy = null;

		super.finalize();
	}
}