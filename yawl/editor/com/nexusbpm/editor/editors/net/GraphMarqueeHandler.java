/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.net;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;

import com.nexusbpm.command.CreateFlowCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.editors.net.cells.GraphPort;
import com.nexusbpm.editor.editors.net.cells.PortHighlightable;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * This marquee handler listens to the mouse handling events for connecting data/control edges and highlighting
 * the port that the mouse is hovering over.
 *
 * @author catch23
 * @created November 6, 2002
 */
public class GraphMarqueeHandler extends BasicMarqueeHandler {

	private static final transient Log LOG = LogFactory.getLog( GraphMarqueeHandler.class );

	/**
	 * Holds the Start and the Current Point
	 */
	private Point2D _start, _current;

	/**
	 * Holds the First and the Current Port
	 */
	private PortView _port, _firstPort;


	//private CellView _cell, _firstCell;

	private NexusGraph _graph;

	private GraphEditor _graphEditor;


	public GraphMarqueeHandler( NexusGraph graph, GraphEditor graphEditor ) {
		super();
		_graph = graph;
		_graphEditor = graphEditor;
	}

	/**
	 * Gets the forceMarqueeEvent attribute of the GraphMarqueeHandler object
	 * Override to Gain Control (for PopupMenu and ConnectMode)
	 *
	 * @param e Description of the Parameter
	 * @return The forceMarqueeEvent value
	 */
	public boolean isForceMarqueeEvent( MouseEvent e ) {
		// If Right Mouse Button we want to Display the PopupMenu
		if( SwingUtilities.isRightMouseButton( e ) ) {
			// Return Immediately
			return true;
		}
		// Find and Remember Port
		_port = getSourcePortAt( e.getPoint() );
		// If Port Found and in ConnectMode (=Ports Visible)
		if( _port != null && _graph.isPortsVisible() ) {
			return true;
		}
		// Else Call Superclass
		return super.isForceMarqueeEvent( e );
	}

	/**
	 * Display PopupMenu or Remember Start Location and First Port
	 *
	 * @param e Description of the Parameter
	 */
	public void mousePressed( final MouseEvent e ) {
		LOG.debug( "mouse pressed" );
		// If Right Mouse Button, then display the pop-up menu
		if( SwingUtilities.isRightMouseButton( e ) ) {
			// Find Cell in Model Coordinates
			Object cell = _graph.getFirstCellForLocation( e.getX(), e.getY() );
			// Make Cell selected
			_graph.addSelectionCell( cell );
			// Create PopupMenu for the Cell
			JPopupMenu menu = _graphEditor.createPopupMenu( cell );
			// Display PopupMenu
			if( menu != null ) {
				menu.show( _graph, e.getX(), e.getY() );
			}

			// Else if in ConnectMode and Remembered Port is Valid
		}
		else if( _port != null && !e.isConsumed() && _graph.isPortsVisible() ) {
			try {
				// If we get to this point, no ValidationFailure was thrown.
				// Remember Start Location
				_start = _graph.toScreen( _port.getLocation( null ) );
				// Remember First Port
				_firstPort = _port;
				// Consume ExecutionEvent
				e.consume();
			}
			catch( Exception f ) {
				LOG.debug( "Edge validation failed: cannot start a new connection here." );
				_graph.setCursor( Cursor.getDefaultCursor() );
			}
		}
		else {
			// Call Superclass
			super.mousePressed( e );
		}
	}

	private static Map _cachedDomainObjects = new HashMap();
	/**
	 * Find Port under Mouse and Repaint "rubber-band" connector (control/data edge) in the XOR mode.
	 *
	 * @param e Description of the Parameter
	 */
	public void mouseDragged( MouseEvent e ) {
		// If remembered Start Point is Valid and paint the edge in preview mode
		if( _start != null && !e.isConsumed() ) {
			// Fetch Graphics from GraphEditor
			Graphics g = _graph.getGraphics();
			// Xor-Paint the old Connector (Hide old Connector)
			paintConnector( Color.black, _graph.getBackground(), g );

			// Reset Remembered Port
			_port = getTargetPortAt( e.getPoint() );
			// If Port was found then Point to Port Location. If the component
			// corresponding
			// to the port cannot accept the edge, then give the user visual feedback.
			if( _port != null ) {
				_current = _graph.toScreen( _port.getLocation( null ) );
                String error = CreateFlowCommand.validateFlow(
                        ((GraphPort) _firstPort.getCell()).getProxy().getData(),
                        ((GraphPort) _port.getCell()).getProxy().getData() );
                try {
                    if( error != null ) {
                        _graph.setCursor( Cursor.getSystemCustomCursor( "Invalid.32x32" ) );
                    }
                    else {
                        _graph.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                    }
                }
                catch( Exception exc ) {
                    LOG.error( "Error getting cursor", exc );
                }
			}
			// Else If no Port was found then Point to Mouse Location
			else {
				_graph.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
				_current = _graph.snap( e.getPoint() );
			}

			// Xor-Paint the new Connector
			paintConnector( _graph.getBackground(), Color.black, g );
			// Consume ExecutionEvent
			e.consume();
		}
		// Call Superclass
		super.mouseDragged( e );
	}

	/**
	 * Gets the source port at a particular location in the graph
	 *
	 * @param point Description of the Parameter
	 * @return The sourcePortAt value
	 */
	public PortView getSourcePortAt( Point point ) {
		// Scale from Screen to Model
		Point2D tmp = _graph.fromScreen( new Point( point ) );
		// Find a Port View in Model Coordinates and Remember
		return _graph.getPortViewAt( tmp.getX(), tmp.getY() );
	}

	/**
	 * Gets the target port at a particular location in the graph
	 *
	 * @param point Description of the Parameter
	 * @return The targetPortAt value
	 */
	protected PortView getTargetPortAt( Point point ) {
//    // Scale from Screen to Model
//    Point2D tmp = _graph.fromScreen(new Point(point));
//    // Find a Port View in Model Coordinates and Remember
//    return _graph.getPortViewAt(tmp.getX(), tmp.getY());
    
//  TODO: not sure if we should use this old code here or not.  If the above code works, then we don't need this.  
//
		// Find Cell at point (No scaling needed here)
		Object cell = _graph.getFirstCellForLocation( point.x, point.y );
		// Loop Children to find PortView
		for( int i = 0; i < _graph.getModel().getChildCount( cell ); i++ ) {
			// Get Child from Model
			Object tmp = _graph.getModel().getChild( cell, i );
			// Get View for Child using the GraphEditor's View as a Cell Mapper
			tmp = _graph.getGraphLayoutCache().getMapping( tmp, false );
			// If Child View is a Port View and not equal to First Port
			if( tmp instanceof PortView && tmp != _firstPort ) {
				// Return as PortView
				return (PortView) tmp;
			}
		}
		// No Port View found
		return getSourcePortAt( point );
	}

	/**
	 * Connect the First Port and the Current Port in the CapselaGraph or Repaint
	 *
	 * @param e Description of the Parameter
	 */
	public void mouseReleased( MouseEvent e ) {
		LOG.debug( "mouse released!" );
		_cachedDomainObjects = new HashMap();
		boolean repaint = false;
		//Rectangle2D sourceRepaintBounds = null;
		//Rectangle2D sinkRepaintBounds = null;

		// Set mouse over to false on the previous port the mouse was hovering over
		if( _firstPort != null && _firstPort.getParentView() instanceof PortHighlightable ) {
			((PortHighlightable) _firstPort.getParentView()).setMouseOverPort( false );
		}


		if( e != null && !e.isConsumed() && _port != null && _firstPort != null && _firstPort != _port ) {
            try {
                EditorDataProxy source = ((GraphPort) _firstPort.getCell()).getProxy();
                EditorDataProxy sink = ((GraphPort) _port.getCell()).getProxy();
                
                WorkflowEditor.getExecutor().executeCommand(
                        new CreateFlowCommand( source, sink,
                                (SharedNodeTreeModel) sink.getTreeNode().getTreeModel() ) );
            }
            catch( Exception exc ) {
                LOG.error( "Error creating CreateFlowCommand!", exc );
            }

			// Consume ExecutionEvent
			e.consume();
			// Else Repaint the GraphEditor
		}
		else {
			repaint = true;
		}

		if( repaint ) {
			_graph.repaint();
		}

		_graph.setCursor( Cursor.getDefaultCursor() );
		// Call Superclass
		super.mouseReleased( e );

		// Reset Global Vars
		_firstPort = _port = null;
		_start = _current = null;
	}

	/**
	 * Show Special Cursor if Over Port
	 *
	 * @param e Description of the Parameter
	 */
	public void mouseMoved( MouseEvent e ) {
		if( e != null ) {
			Point2D screenPoint = _graph.toScreen( e.getPoint() );
			PortView port = getSourcePortAt( (Point) screenPoint );
			if( port != _port ) {
				if( _port != null && _port.getParentView() instanceof PortHighlightable ) {
					// Set mouse over to false on the previous port the mouse was hovering over
					((PortHighlightable) _port.getParentView()).setMouseOverPort( false );
					Rectangle2D bounds = (Rectangle2D) (_port.getParentView().getBounds().clone());
					_graph.toScreen( bounds );
					_graph.repaint( bounds.getBounds() );
				}
				_port = port;

				if( port != null && _port.getParentView() instanceof PortHighlightable ) {
					_graph.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
					// Set mouse over to true on the current port so that it will be "highlighted"
					((PortHighlightable) _port.getParentView()).setMouseOverPort( true );
					Rectangle2D bounds = (Rectangle2D) (_port.getParentView().getBounds().clone());
					_graph.toScreen( bounds );
					_graph.repaint( bounds.getBounds() );
				}
				else {
					_graph.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
				}
			}

			// Consume ExecutionEvent
			e.consume();
		}
		// Call Superclass
		super.mouseMoved( e );
	}

	/**
	 * Use Xor-Mode on Graphics to Paint Connector
	 */
	protected void paintConnector( Color fg, Color bg, Graphics g ) {
		// Set Foreground
		g.setColor( fg );
		// Set Xor-Mode Color
		g.setXORMode( bg );
		// Highlight the Current Port
		paintPort( _graph.getGraphics() );
		// If Valid First Port, Start and Current Point
		if( _firstPort != null && _start != null && _current != null ) {
			// Then Draw A Line From Start to Current Point
			g.drawLine( (int) _start.getX(), (int) _start.getY(), (int) _current.getX(), (int) _current.getY() );
		}
	}

	/**
	 * Use the Preview Flag to Draw a Highlighted Port
	 *
	 * @param g Description of the Parameter
	 */
	protected void paintPort( Graphics g ) {
		// If Current Port is Valid
		if( _port != null ) {
			// If Not Floating Port...
			boolean o = (GraphConstants.getOffset( _port.getAttributes() ) != null);
			// ...Then use Parent's Bounds
			Rectangle2D r = (o) ? _port.getBounds() : _port.getParentView().getBounds();
			// Scale from Model to Screen
			r = _graph.toScreen( new Rectangle2D.Double( r.getX(), r.getY(), r.getWidth(), r.getHeight() ) );
			// Add Space For the Highlight Border
			r = new Rectangle2D.Double( r.getX() - 3, r.getY() - 3, r.getWidth() + 6, r.getHeight() + 6 );
			// Paint Port in Preview (=Highlight) Mode
			_graph.getUI().paintCell( g, _port, r, true );
		}
	}

	public void finalize() throws Throwable {
		LOG.debug( "GraphMarqueeHandler.finalize" );

		// not strictly necessary but should make finding what's left in profiler easier - mjf 2005-05-17 16:27
		_start = null;
		_current = null;
		_port = null;
		_firstPort = null;

		_graph = null;
		_graphEditor = null;

		super.finalize();

	}
}