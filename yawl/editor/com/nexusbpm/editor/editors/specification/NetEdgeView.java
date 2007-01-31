package com.nexusbpm.editor.editors.specification;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;

public class NetEdgeView extends EdgeView {

	public static transient EdgeRenderer renderer = new NetEdgeRenderer();
	
	public NetEdgeView() {
		super();
	}

	public NetEdgeView(Object arg0) {
		super(arg0);
	}

	@Override
	public CellViewRenderer getRenderer() {
		return renderer;
	}

}
