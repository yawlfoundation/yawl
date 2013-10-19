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

package org.yawlfoundation.yawl.editor.ui.specification.pubsub;

import org.jgraph.event.GraphSelectionEvent;

import java.util.*;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class Publisher {

    private SpecificationState _specState;
    private FileState _fileState;

    private Set<SpecificationStateListener> _specListeners;
    private Set<FileStateListener> _fileListeners;
    private Map<GraphState, Set<GraphStateListener>> _graphListeners;

    private static Publisher INSTANCE;


    private Publisher() {
        _specListeners = new HashSet<SpecificationStateListener>();
        _fileListeners = new HashSet<FileStateListener>();
        initGraphListenersMap();
        _fileState = FileState.Closed;
        _specState = SpecificationState.NoNetsExist;
    }


    public static Publisher getInstance() {
        if (INSTANCE == null) INSTANCE = new Publisher();
        return INSTANCE;
    }


    public void subscribe(SpecificationStateListener listener) {
        _specListeners.add(listener);
        listener.specificationStateChange(_specState);
    }


    public void subscribe(FileStateListener listener) {
        _fileListeners.add(listener);
        listener.specificationFileStateChange(_fileState);
    }


    public void subscribe(GraphStateListener listener,
                          List<GraphState> interestedStates) {
        for (GraphState state : interestedStates) {
            _graphListeners.get(state).add(listener);
        }
    }


    public void publishState(SpecificationState state) {
        for (SpecificationStateListener listener : _specListeners) {
            listener.specificationStateChange(state);
        }
    }


    public void publishState(FileState state) {
        for (FileStateListener listener : _fileListeners) {
            listener.specificationFileStateChange(state);
        }
    }


    public void publishState(GraphState state, GraphSelectionEvent event) {
        for (GraphStateListener listener : _graphListeners.get(state)) {
            listener.graphSelectionChange(state, event);
        }
    }


    public void setSpecificationState(SpecificationState state) {
        _specState = state;
        publishState(state);
    }

    public SpecificationState getSpecificationState() {
      return _specState;
    }


    public void publishAddNetEvent() {
        if (getSpecificationState() == SpecificationState.NoNetsExist) {
            setSpecificationState(SpecificationState.NetsExist);
        }
        publishState(SpecificationState.NetDetailChanged);
    }


    public void publishRemoveNetEvent(boolean noNets) {
        if (noNets) {
            setSpecificationState(SpecificationState.NoNetsExist);
        }
        publishState(SpecificationState.NetDetailChanged);
    }


    public void setFileState(FileState state) {
        _fileState = state;
        publishState(state);
    }

    public FileState getFileState() {
      return _fileState;
    }


    public void publishOpenFileEvent() {
        setFileState(FileState.Open);
    }


    public void publishCloseFileEvent() {
        setFileState(FileState.Closed);
    }

    public void publishFileBusyEvent() {
        publishState(FileState.Busy);
    }

    public void publishFileUnbusyEvent() {
        publishState(_fileState);
    }


    private void initGraphListenersMap() {
        _graphListeners = new Hashtable<GraphState, Set<GraphStateListener>>();
        for (GraphState state : GraphState.values()) {
            _graphListeners.put(state, new HashSet<GraphStateListener>());
        }
    }

}
