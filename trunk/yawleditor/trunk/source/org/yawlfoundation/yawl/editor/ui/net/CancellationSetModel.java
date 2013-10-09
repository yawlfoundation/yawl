/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;


import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;

import java.awt.Color;
import java.util.LinkedList;
import java.util.HashSet;

public class CancellationSetModel implements GraphSelectionListener {

  // possible operations listeners need to deal with

  public static final int SET_CHANGED = 0;
  
  public static final int NO_VALID_SELECTION_FOR_SET_MEMBERSHIP = 3;
  public static final int VALID_SELECTION_FOR_SET_MEMBERSHIP = 4;
  
  public static final Color CANCELLATION_SET_TRIGGER_BACKGROUND = Color.LIGHT_GRAY;

  public static final Color CANCELLATION_SET_MEMBER_FOREGROUND = Color.RED;
  public static final Color NOT_CANCELLATION_SET_MEMBER_FOREGROUND = 
    YAWLVertex.DEFAULT_VERTEX_FOREGROUND;
  
  private transient LinkedList subscribers = new LinkedList();
	private transient NetGraph graph;

  private CancellationSet currentCancellationSet = null;
  
  public CancellationSetModel() {
  	this.graph = null;
  	this.currentCancellationSet = null;
  }
  
  public CancellationSetModel(NetGraph graph) {
    setGraph(graph);
  }
  
  public void setGraph(NetGraph graph) {
		this.graph = graph;
		graph.addGraphSelectionListener(this);
  }
  
  public void subscribe(final CancellationSetModelListener subscriber) {
    subscribers.add(subscriber);
    notify(subscriber, SET_CHANGED);
    if (currentCancellationSet == null) {
      notify(subscriber, NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
    }
  }
  
  private void publishNotificationOfSetChange() {
    publishNotification(SET_CHANGED);
    if (currentCancellationSet == null) {
      publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
    }
  }
  
  public void unsubscribe(final CancellationSetModelListener subscriber) {
    subscribers.remove(subscriber);
  }
  
  public void refresh() {
    publishNotificationOfSetChange();
  }

  public void publishNotification(final int notificationType) {
    for(int i = 0; i < subscribers.size();  i++) {
      CancellationSetModelListener listener = 
        (CancellationSetModelListener) subscribers.get(i);
      notify(listener, notificationType);
    }
  }

  public void changeCancellationSet(YAWLTask triggeringTask) {
    if (triggeringTask != null) {
    	setCurrentCancellationSet(triggeringTask.getCancellationSet());
    } else {
    	setCurrentCancellationSet(null);
    }
    publishNotificationOfSetChange(); 
  }
  
  public void setCurrentCancellationSet(CancellationSet currentCancellationSet) {
  	this.currentCancellationSet = currentCancellationSet;
  }
  
  public CancellationSet getCurrentCancellationSet() {
  	return this.currentCancellationSet;
  }
  
  public boolean addCellToCancellationSet(YAWLCell cell) {
    if (currentCancellationSet != null) {
      if (currentCancellationSet.addMember(cell)) {
        publishNotification(SET_CHANGED);
        return true;
      }
    }
    return false;    
  }

  public boolean removeCellFromCancellationSet(YAWLCell cell) {
    if (currentCancellationSet != null) {
      if (currentCancellationSet.removeMember(cell)) {
        publishNotification(SET_CHANGED);
        return true;
      }
    }
    return false;    
  }
  
  public YAWLTask getTriggeringTask() {
    if (currentCancellationSet != null) {
      return currentCancellationSet.getTriggeringTask();
    } 
    return null;
  }
  
  private void notify(CancellationSetModelListener listener, int notificationType) {
    if (currentCancellationSet != null) {
      listener.notify(notificationType, currentCancellationSet.getTriggeringTask());
    } else {
      listener.notify(notificationType, null);
    }
  }

  // SelectionModel notification
  
  public void valueChanged(GraphSelectionEvent event) {
    if(currentCancellationSet == null) {
      publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
      return;
    }
    if(graph.getSelectionModel().isSelectionEmpty()) {
      publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
      return;
    } 
    Object[] validSelectedCells = 
      getValidCancellationSetCells(graph.getSelectionModel().getSelectionCells());

    if (validSelectedCells.length > 0) {
      publishNotification(VALID_SELECTION_FOR_SET_MEMBERSHIP);
    } else {
      publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
    }
  }
  
  public Object[] getValidCancellationSetCells(Object[] cells) {
    HashSet validCells = new HashSet();
    
    for(int i = 0;  i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        VertexContainer container = (VertexContainer) cells[i];
        cells[i] = container.getVertex();
      }

      if (cells[i] instanceof InputCondition || 
          cells[i] instanceof OutputCondition) {
        continue;
      }
      if (cells[i] instanceof YAWLFlowRelation && 
          !((YAWLFlowRelation) cells[i]).connectsTwoTasks()) {
        continue;
      }
      validCells.add(cells[i]);
    }
    return validCells.toArray();
  }

  public Object[] getValidCellsForInclusion(Object[] cells) {
    Object[] validCells = getValidCancellationSetCells(cells);
    HashSet validCellsToInclude = new HashSet();
    if (currentCancellationSet != null) {
      for(int i = 0;  i < validCells.length; i++) {
        if (!currentCancellationSet.contains((YAWLCell) validCells[i])) {
          validCellsToInclude.add(validCells[i]);
        }
      }
    }    
    return validCellsToInclude.toArray();
  }

  public Object[] getValidCellsForExclusion(Object[] cells) {
    Object[] validCells = getValidCancellationSetCells(cells);
    HashSet validCellsToExclude = new HashSet();
    if (currentCancellationSet != null) {
      for(int i = 0;  i < validCells.length; i++) {
        if (currentCancellationSet.contains((YAWLCell) validCells[i])) {
          validCellsToExclude.add(validCells[i]);
        }
      }
    }
    return validCellsToExclude.toArray();
  }
  
  public Object[] getValidSelectedCellsForInclusion() {
    return getValidCellsForInclusion(graph.getSelectionModel().getSelectionCells());
  }

  public Object[] getValidSelectedCellsForExclusion() {
    return getValidCellsForExclusion(graph.getSelectionModel().getSelectionCells());
  }
}
