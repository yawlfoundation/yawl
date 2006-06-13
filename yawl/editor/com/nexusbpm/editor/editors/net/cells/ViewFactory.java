package com.nexusbpm.editor.editors.net.cells;

import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;

import com.nexusbpm.editor.editors.net.renderer.DefaultRenderer;

/**
 * @author catch23
 */
public class ViewFactory implements CellViewFactory {

	public ViewFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.jgraph.graph.CellViewFactory#createView(org.jgraph.JGraph, org.jgraph.graph.CellMapper, java.lang.Object)
	 */
	public CellView createView( GraphModel model, Object c ) {
		CellView view = null;
		if( c instanceof CapselaCell ) {
			CapselaCell cell = (CapselaCell) c;
//			Class rendererClass = DomainObjectProperties.flowRendererForClass( cell.getController().identifier().className() );
			Class rendererClass = DefaultRenderer.class;
			// TODO XXX implement this properly later!!!
			
			if( rendererClass == DefaultRenderer.class ) {
				view = new DefaultView( cell );
			}
		}
		else if( c instanceof GraphPort ) {
			view = new InvisiblePortView( c );
		}
		else if( c instanceof GraphEdge ) {
			view = new EdgeView( c );
		}
		else if( model.isPort( c ) )
			view = createPortView( c );
		else if( model.isEdge( c ) )
			view = createEdgeView( c );
		else if (c instanceof DefaultGraphCell) {
			view = new DefaultView(c);
		}
		else {
			throw new RuntimeException( "Can't find a proper view for: " + c.getClass().getName() );
		}
		if (view == null) {
			new RuntimeException( "Can't find a proper view for: " + c.getClass().getName() ).printStackTrace();
			view = new DefaultView(c);
		}
		return view;
	}

	/**
	 * Constructs a PortView view for the specified object.
	 *
	 * lifted from newer jgraph library as part of list selector migration
	 */
	protected PortView createPortView( Object cell ) {
		if( cell instanceof Port )
			return createPortView( (Port) cell );
		else
			return new PortView( cell );
	}

	/**
	 * Constructs a PortView view for the specified object.
	 *
	 * @deprecated replaced by {@link #createPortView(Object)}since
	 *             JGraph no longer exposes dependecies on GraphCell subclasses
	 *             (Port, Edge)
	 *
	 *             lifted from newer jgraph library as part of list selector migration
	 */
	protected PortView createPortView( Port cell ) {
		return new PortView( cell );
	}


	/**
	 * Constructs an EdgeView view for the specified object.
	 *
	 * @deprecated replaced by {@link #createEdgeView(Object)}since
	 *             JGraph no longer exposes dependecies on GraphCell subclasses
	 *             (Port, Edge)
	 *
	 *             lifted from newer jgraph library as part of list selector migration
	 */
	protected EdgeView createEdgeView( Edge cell ) {
		return new EdgeView( cell );
	}

	/**
	 * Constructs an EdgeView view for the specified object.
	 *
	 * lifted from newer jgraph library as part of list selector migration
	 */
	protected EdgeView createEdgeView( Object cell ) {
		if( cell instanceof Edge )
			return createEdgeView( (Edge) cell );
		else
			return new EdgeView( cell );
	}
}
