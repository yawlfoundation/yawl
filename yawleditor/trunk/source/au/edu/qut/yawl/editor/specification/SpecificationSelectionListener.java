/*
 * Created on 08/09/2006
 * YAWLEditor v1.4.4 
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.specification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgraph.graph.GraphSelectionModel;

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;

/**
 * @author Lindsay Bradford
 */

public class SpecificationSelectionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final SpecificationSelectionListener INSTANCE = new SpecificationSelectionListener();
  
  public static SpecificationSelectionListener getInstance() {
    return INSTANCE;
  }
  
  public static final int STATE_NO_ELEMENTS_SELECTED = 0;
  public static final int STATE_COPYABLE_ELEMENTS_SELECTED = 1;
  public static final int STATE_DELETABLE_ELEMENTS_SELECTED = 2;
  public static final int STATE_ONE_OR_MORE_ELEMENTS_SELECTED = 3;
  public static final int STATE_MORE_THAN_ONE_VERTEX_SELECTED = 4;
  public static final int STATE_SINGLE_TASK_SELECTED = 5;

  private HashMap stateSubscriberMap = new HashMap();
  
  public void subscribe(SpecificationSelectionSubscriber subscriber, int[] statesOfInterest) {
    for(int i = 0; i < statesOfInterest.length; i++) {
      LinkedList stateSubscribers = (LinkedList) stateSubscriberMap.get(new Long(statesOfInterest[i]));
      
      if (stateSubscribers == null) {
        stateSubscribers = new LinkedList();
        stateSubscriberMap.put(new Long(statesOfInterest[i]), stateSubscribers);
      }
      stateSubscribers.add(subscriber);
    }
  }
  
  public void publishSubscriptions(GraphSelectionModel model) {
    if (model.isSelectionEmpty()) {
      publishState(STATE_NO_ELEMENTS_SELECTED);
    } else {
      publishState(STATE_ONE_OR_MORE_ELEMENTS_SELECTED);
    }
    if (copyableElementsSelected(model)) {
      publishState(STATE_COPYABLE_ELEMENTS_SELECTED);
    }
    if (deletableElementsSelected(model)) {
      publishState(STATE_DELETABLE_ELEMENTS_SELECTED);
    }
    if (moreThanOneVertexSelected(model)) {
      publishState(STATE_MORE_THAN_ONE_VERTEX_SELECTED);
    }
    /*
    if (singleTaskSelected()) {
      publishState(STATE_SINGLE_TASK_SELECTED);
    }
    */
  }
  
  private void publishState(int state) {
    LinkedList stateSubscribers = (LinkedList) stateSubscriberMap.get(new Long(state));
    if (stateSubscribers != null) {
      Iterator subscriberIterator = stateSubscribers.iterator();
      while (subscriberIterator.hasNext()) {
        SpecificationSelectionSubscriber subscriber = 
            (SpecificationSelectionSubscriber) subscriberIterator.next();
        subscriber.receiveSubscription(state);
      }
    }
  }
  
  private boolean copyableElementsSelected(GraphSelectionModel model) {
    Object[] elements = model.getSelectionCells();
    
    for(int i = 0; i < elements.length; i++) {
      if (!(elements[i] instanceof YAWLCell)) {
        return true;  
      } 
      
      YAWLCell element = (YAWLCell) elements[i];
      if (element.isCopyable()) {
        return true;  
      }        
    }
    return false;
  }
  
  private boolean deletableElementsSelected(GraphSelectionModel model) {
    Object[] elements = model.getSelectionCells();
    
    for(int i = 0; i < elements.length; i++) {
      if (!(elements[i] instanceof YAWLCell)) {
        return true;
      } 

      YAWLCell element = (YAWLCell) elements[i];
      if (element.isRemovable()) {
        return true;
      }        
    }
    return false;
  }

  private boolean moreThanOneVertexSelected(GraphSelectionModel model) {
    int validElementCount = 0;
    Object[] elements = model.getSelectionCells();

    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof YAWLVertex
          || elements[i] instanceof VertexContainer) {
        validElementCount++;
      }
    }
    if (validElementCount >= 2) {
      return true;
    }
    return false;
  }
}
