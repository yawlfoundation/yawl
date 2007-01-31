package com.nexusbpm.editor.editors.specification;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;

import au.edu.qut.yawl.elements.YFlow;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class NetEdge extends DefaultEdge {

	public NetEdge() {
		super();
	}

	public NetEdge(Object arg0, AttributeMap arg1) {
		super(arg0, arg1);
	}

	public NetEdge(Object arg0) {
		super(arg0);
	}
	public String toString() {
//		YFlow flow = ((EditorDataProxy<YFlow>) this.getUserObject()).getData();
//		Integer i = flow.getEvalOrdering();
//		String s = flow.getXpathPredicate();
//		if (i == null) i = 1;
//		if (s == null) s = "";
//		return "[" + i + "]:" + s;
		return null;
	}
	
}
