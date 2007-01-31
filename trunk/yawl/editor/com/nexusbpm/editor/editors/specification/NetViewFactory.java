package com.nexusbpm.editor.editors.specification;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

public class NetViewFactory extends DefaultCellViewFactory {

	@Override
	protected EdgeView createEdgeView(Object arg0) {
		return new NetEdgeView(arg0);
	}

	@Override
	protected PortView createPortView(Object arg0) {
		return new InvisiblePortView( arg0 );
	}

	@Override
	protected VertexView createVertexView(Object arg0) {
		if (arg0 instanceof NetCell) {
			return new NetCellView((NetCell) arg0);
		} else {
			return new VertexView(arg0);
		}
	}

}
