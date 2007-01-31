package com.nexusbpm.editor.editors.specification;

import java.awt.Component;

import javax.swing.JComponent;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.EdgeRenderer;

public class NetEdgeRenderer extends EdgeRenderer {

	@Override
	public Component getRendererComponent(JGraph graph, CellView view, boolean sel, boolean focus, boolean preview) {
		// TODO Auto-generated method stub
		Component c = super.getRendererComponent(graph, view, sel, focus, preview);
		return c;
	}

}
