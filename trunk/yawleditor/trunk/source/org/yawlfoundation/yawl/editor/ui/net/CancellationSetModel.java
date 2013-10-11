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

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CancellationSetModel implements GraphSelectionListener {

    // possible operations listeners need to deal with
    public static final int SET_CHANGED = 0;
    public static final int NO_VALID_SELECTION_FOR_SET_MEMBERSHIP = 3;
    public static final int VALID_SELECTION_FOR_SET_MEMBERSHIP = 4;

    public static final Color CANCELLATION_SET_TRIGGER_BACKGROUND = Color.LIGHT_GRAY;
    public static final Color CANCELLATION_SET_MEMBER_FOREGROUND = Color.RED;
    public static final Color NOT_CANCELLATION_SET_MEMBER_FOREGROUND =
            YAWLVertex.DEFAULT_VERTEX_FOREGROUND;

    private List<CancellationSetModelListener> _subscribers;
    private  NetGraph _graph;
    private CancellationSet _currentSet;


    public CancellationSetModel() {
        _graph = null;
        _currentSet = null;
        _subscribers = new ArrayList<CancellationSetModelListener>();
    }

    public CancellationSetModel(NetGraph graph) {
        this();
        setGraph(graph);
    }

    public void setGraph(NetGraph graph) {
        _graph = graph;
        graph.addGraphSelectionListener(this);
    }

    public void subscribe(CancellationSetModelListener subscriber) {
        _subscribers.add(subscriber);
//        notify(subscriber, SET_CHANGED);
//        if (_currentSet == null) {
//            notify(subscriber, NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
//        }
    }

    private void publishNotificationOfSetChange() {
        publishNotification(SET_CHANGED);
        if (_currentSet == null) {
            publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
        }
    }

    public void unsubscribe(CancellationSetModelListener subscriber) {
        _subscribers.remove(subscriber);
    }

    public void refresh() {
        publishNotificationOfSetChange();
    }

    public void publishNotification(int notificationType) {
        for(CancellationSetModelListener listener : _subscribers) {
            notify(listener, notificationType);
        }
    }

    public void changeCancellationSet(YAWLTask triggeringTask) {
        if (triggeringTask != null) {
            setCurrentCancellationSet(triggeringTask.getCancellationSet());
        }
        else {
            setCurrentCancellationSet(null);
        }
        publishNotificationOfSetChange();
    }

    public void setCurrentCancellationSet(CancellationSet cancellationSet) {
        _currentSet = cancellationSet;
    }

    public CancellationSet getCurrentCancellationSet() {
        return _currentSet;
    }

    public boolean addCellToCancellationSet(YAWLCell cell) {
        if (_currentSet != null) {
            if (_currentSet.add(cell)) {
                publishNotification(SET_CHANGED);
                return true;
            }
        }
        return false;
    }

    public boolean removeCellFromCancellationSet(YAWLCell cell) {
        if (_currentSet != null) {
            if (_currentSet.remove(cell)) {
                publishNotification(SET_CHANGED);
                return true;
            }
        }
        return false;
    }

    public YAWLTask getTriggeringTask() {
        if (_currentSet != null) {
            return _currentSet.getOwnerTask();
        }
        return null;
    }

    private void notify(CancellationSetModelListener listener, int notificationType) {
        if (_currentSet != null) {
            listener.notify(notificationType, _currentSet.getOwnerTask());
        }
        else {
            listener.notify(notificationType, null);
        }
    }

    // SelectionModel notification
    public void valueChanged(GraphSelectionEvent event) {
        if (_currentSet == null || ! hasValidCellSelected()) {
            publishNotification(NO_VALID_SELECTION_FOR_SET_MEMBERSHIP);
        }
        else {
            publishNotification(VALID_SELECTION_FOR_SET_MEMBERSHIP);
        }
    }

    public boolean hasValidCellSelected() {
        for (Object cell : _graph.getSelectionModel().getSelectionCells()) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (! (cell instanceof InputCondition || cell instanceof OutputCondition ||
                    (cell instanceof YAWLFlowRelation &&
                    !((YAWLFlowRelation) cell).connectsTwoTasks()))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasValidSelectedCellsForInclusion() {
        return getValidSelectedCellsForInclusion().length > 0;
    }

    public boolean hasValidSelectedCellsForExclusion() {
        return getValidSelectedCellsForExclusion().length > 0;
    }

    public Object[] getValidSelectedCellsForInclusion() {
        return filterSelectedCells(true);
    }


    public Object[] getValidSelectedCellsForExclusion() {
        return filterSelectedCells(false);
    }

    private Object[] filterSelectedCells(boolean include) {
        Object[] validCells = _graph.getSelectionModel().getSelectionCells();
        Set<Object> filteredSet = new HashSet<Object>();
        if (_currentSet != null) {
            for (Object validCell : validCells) {
                if (validCell instanceof VertexContainer) {
                    validCell = ((VertexContainer) validCell).getVertex();
                }
                boolean hasCell = _currentSet.contains((YAWLCell) validCell);
                if ((include && ! hasCell) || (! include && hasCell)) {
                    filteredSet.add(validCell);
                }
            }
        }
        return filteredSet.toArray();
    }
}
