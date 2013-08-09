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
    private Set<ProblemListStateListener> _problemListListeners;
    private Map<GraphState, Set<GraphStateListener>> _graphListeners;

    private static Publisher INSTANCE;


    private Publisher() {
        _specListeners = new HashSet<SpecificationStateListener>();
        _fileListeners = new HashSet<FileStateListener>();
        _problemListListeners = new HashSet<ProblemListStateListener>();
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


    public void subscribe(ProblemListStateListener listener) {
        _problemListListeners.add(listener);
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


    public void publishState(ProblemListState state) {
        for (ProblemListStateListener listener : _problemListListeners) {
            listener.contentChange(state);
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
