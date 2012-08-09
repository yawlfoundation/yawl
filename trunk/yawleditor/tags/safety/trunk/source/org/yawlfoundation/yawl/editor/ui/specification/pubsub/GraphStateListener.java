package org.yawlfoundation.yawl.editor.ui.specification.pubsub;

import org.jgraph.event.GraphSelectionEvent;

public interface GraphStateListener {

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event);
}