package au.edu.qut.yawl.editor.specification;

import org.jgraph.event.GraphSelectionEvent;

public interface SpecificationSelectionSubscriber {
  public void receiveSubscription(int state, GraphSelectionEvent event);
}