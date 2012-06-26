package org.yawlfoundation.yawl.editor.ui.specification.pubsub;

import org.jgraph.event.GraphSelectionEvent;

public interface SpecificationSelectionSubscriber {

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event);
}