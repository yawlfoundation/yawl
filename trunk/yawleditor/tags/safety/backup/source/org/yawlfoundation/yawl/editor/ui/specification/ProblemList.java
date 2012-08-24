package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.ProblemListState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

import java.util.ArrayList;
import java.util.Collection;


public class ProblemList extends ArrayList<String> {

    private ProblemListState _state = ProblemListState.NoEntries;

    public void add(int index, String element) {
        super.add(index, element);
        publishIfNecessary();
    }

    public boolean add(String element) {
        boolean result = super.add(element);
        publishIfNecessary();
        return result;
    }

    public boolean addAll(Collection<? extends String> c) {
        boolean result = super.addAll(c);
        publishIfNecessary();
        return result;
    }

    public boolean addAll(int index, Collection<? extends String> c) {
        boolean result = super.addAll(index, c);
        publishIfNecessary();
        return result;
    }

    public boolean remove(String element) {
        boolean result = super.remove(element);
        publishIfNecessary();
        return result;
    }

    public boolean removeAll(Collection<?> c) {
        boolean result = super.removeAll(c);
        publishIfNecessary();
        return result;
    }

    public boolean retainAll(Collection<?> c) {
        boolean result = super.retainAll(c);
        publishIfNecessary();
        return result;
    }

    public void clear() {
        super.clear();
        publishIfNecessary();
    }


    private void publishIfNecessary() {
        if (_state == ProblemListState.NoEntries && size() > 0) {
            _state = ProblemListState.Entries;
            Publisher.getInstance().publishState(_state);
        }
        else if (_state == ProblemListState.Entries && size() == 0) {
            _state = ProblemListState.NoEntries;
            Publisher.getInstance().publishState(_state);
        }
    }


}
