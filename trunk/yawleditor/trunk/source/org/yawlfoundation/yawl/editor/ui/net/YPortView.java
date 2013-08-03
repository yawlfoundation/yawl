package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;

/**
 * @author Michael Adams
 * @date 31/07/13
 */
public class YPortView extends PortView {

    public static transient PortRenderer renderer = new YPortRenderer();


    public YPortView() { super(); }

    public YPortView(Object cell) { super(cell); }


    public CellViewRenderer getRenderer() {
   		return renderer;
   	}


}

