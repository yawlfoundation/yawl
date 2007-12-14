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

package org.yawlfoundation.yawl.editor.specification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.graph.GraphSelectionModel;

import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;


/**
 * @author Lindsay Bradford
 */

public class SpecificationSelectionListener {

  private static final long serialVersionUID = 1L;

  private static final SpecificationSelectionListener INSTANCE = new SpecificationSelectionListener();
  
  public static SpecificationSelectionListener getInstance() {
    return INSTANCE;
  }
/**
 * Represents a sates where no elements have been selected at all.
 */  
  public static final int STATE_NO_ELEMENTS_SELECTED = 0;
  
  /**
   * Represents a state where a selection set contains at least one copyable element. 
   */
  public static final int STATE_COPYABLE_ELEMENTS_SELECTED = 1;
  
  /**
   * Represents a state where a selection set contains at least one deletable element.
   */
  public static final int STATE_DELETABLE_ELEMENTS_SELECTED = 2;
  
  /**
   * Represents a state where a selection set contains one or more elements.
   */
  public static final int STATE_ONE_OR_MORE_ELEMENTS_SELECTED = 3;
  
  /**
   * Represents a state where a selection set contains at least two vertex elements.
   */
  public static final int STATE_MORE_THAN_ONE_VERTEX_SELECTED = 4;
  
  /**
   * Represents a state where a seelction set contains just a single task.
   */
  public static final int STATE_SINGLE_TASK_SELECTED = 5;
  
  /**
   * Represents a state where a seelction set contains just a single net element.
   */
  public static final int STATE_SINGLE_ELEMENT_SELECTED = 6;
  

  /**
   * A mapping of possible selection states against subscribers that care to receive
   * notifications of a particular state.
   */
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
  
  /**
   * This method interrogates the selected cells suppled with the <code>model</code> specified, 
   * and based on the state of the selection set, will send events to subscribers that have asked for
   * notification of the selection being in a particular state of interest.
   * @param model
   */
  public void publishSubscriptions(GraphSelectionModel model, GraphSelectionEvent event) {
    if (model.isSelectionEmpty()) {
      publishState(STATE_NO_ELEMENTS_SELECTED, event);
    } else {
      publishState(STATE_ONE_OR_MORE_ELEMENTS_SELECTED, event);
    }
    if (copyableElementsSelected(model)) {
      publishState(STATE_COPYABLE_ELEMENTS_SELECTED, event);
    }
    if (deletableElementsSelected(model)) {
      publishState(STATE_DELETABLE_ELEMENTS_SELECTED, event);
    }
    if (moreThanOneVertexSelected(model)) {
      publishState(STATE_MORE_THAN_ONE_VERTEX_SELECTED, event);
    }
    if (singleTaskSelected(model)) {
      publishState(STATE_SINGLE_TASK_SELECTED, event);
    }
    if (singleElementSelected(model)) {
      publishState(STATE_SINGLE_ELEMENT_SELECTED, event);
    }
  }
  
  private void publishState(int state, GraphSelectionEvent event) {
    LinkedList stateSubscribers = (LinkedList) stateSubscriberMap.get(new Long(state));
    if (stateSubscribers != null) {
      Iterator subscriberIterator = stateSubscribers.iterator();
      while (subscriberIterator.hasNext()) {
        SpecificationSelectionSubscriber subscriber = 
            (SpecificationSelectionSubscriber) subscriberIterator.next();
        subscriber.receiveGraphSelectionNotification(state, event);
      }
    }
  }

  /**
   * Returns true if there exists at least 1 element within the
   * selection set supplied by <code>model</code> that is allowed to be copied, 
   * false otherwise.
   * @param model
   * @return
   */

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
  
  /**
   * Returns true if there exists at least 1 element within the
   * selection set supplied by <code>model</code> that is allowed to be deleted, 
   * false otherwise.
   * @param model
   * @return
   */

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

  /**
   * Returns true if there exists at least 2 vertex elements within the
   * selection set supplied by <code>model</code>, false otherwise.
   * @param model
   * @return
   */
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
  
  /**
   * Returns true if the selection set supplied by 
   * <code>model</code> contains only a single task, false otherwise.
   * @param model
   * @return
   */
  private boolean singleTaskSelected(GraphSelectionModel model) {
    Object[] elements = model.getSelectionCells();

    if (elements.length != 1) {
      return false;
    }
    
    if (elements[0] instanceof YAWLTask) {
      return true;
    }
    
    if (elements[0] instanceof VertexContainer) {
      if (((VertexContainer) elements[0]).getVertex() instanceof YAWLTask) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns true if the selection set supplied by 
   * <code>model</code> contains only a single element, false otherwise.
   * @param model
   * @return
   */
  private boolean singleElementSelected(GraphSelectionModel model) {
    Object[] elements = model.getSelectionCells();

    if (elements.length != 1) {
      return false;
    }
    return true;
  }

}
