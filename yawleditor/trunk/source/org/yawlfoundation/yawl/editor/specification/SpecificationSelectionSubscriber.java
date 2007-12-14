package org.yawlfoundation.yawl.editor.specification;

import org.jgraph.event.GraphSelectionEvent;

public interface SpecificationSelectionSubscriber {
  public void receiveGraphSelectionNotification(int state, GraphSelectionEvent event);
}