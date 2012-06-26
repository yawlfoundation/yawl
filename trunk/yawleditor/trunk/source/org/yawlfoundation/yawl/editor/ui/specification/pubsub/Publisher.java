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

    private Set<SpecificationModelListener> _specListeners;
    private Set<SpecificationFileModelListener> _fileListeners;
    private Map<GraphState, Set<SpecificationSelectionSubscriber>> _graphListeners;

    private static Publisher _instance;


    private Publisher() {
        _specListeners = new HashSet<SpecificationModelListener>();
        _fileListeners = new HashSet<SpecificationFileModelListener>();
        initGraphListenersMap();
        _fileState = FileState.Idle;
        _specState = SpecificationState.NoNetsExist;
    }


    public static Publisher getInstance() {
        if (_instance == null) _instance = new Publisher();
        return _instance;
    }


    public void subscribe(SpecificationModelListener listener) {
        _specListeners.add(listener);
        listener.specificationStateChange(_specState);
    }


    public void subscribe(SpecificationFileModelListener listener) {
        _fileListeners.add(listener);
        listener.specificationFileStateChange(_fileState);
    }


    public void subscribe(SpecificationSelectionSubscriber listener,
                          List<GraphState> interestedStates) {
        for (GraphState state : interestedStates) {
            _graphListeners.get(state).add(listener);
//            listener.graphSelectionChange(state, null);
        }
    }


    public void publishState(SpecificationState state) {
        for (SpecificationModelListener listener : _specListeners) {
            listener.specificationStateChange(state);
        }
    }


    public void publishState(FileState state) {
        for (SpecificationFileModelListener listener : _fileListeners) {
            listener.specificationFileStateChange(state);
        }
    }

    public void publishState(GraphState state, GraphSelectionEvent event) {
        for (SpecificationSelectionSubscriber listener : _graphListeners.get(state)) {
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
        setFileState(FileState.Ready);
    }


    public void publishCloseFileEvent() {
        setFileState(FileState.Idle);
    }

    public void publishFileBusyEvent() {
        publishState(FileState.Busy);
    }

    public void publishFileUnbusyEvent() {
        publishState(_fileState);
    }


    private void initGraphListenersMap() {
        _graphListeners = new Hashtable<GraphState, Set<SpecificationSelectionSubscriber>>();
        for (GraphState state : GraphState.values()) {
            _graphListeners.put(state, new HashSet<SpecificationSelectionSubscriber>());
        }
    }

}
