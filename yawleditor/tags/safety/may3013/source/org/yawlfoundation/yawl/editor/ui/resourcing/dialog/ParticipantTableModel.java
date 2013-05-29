package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class ParticipantTableModel extends AbstractResourceTableModel {

    private List<Participant> _participants;

    public ParticipantTableModel() { super(); }

    public ParticipantTableModel(Set<Object> values) {
        this();
        _participants = cast(values, Participant.class, new ParticipantNameComparator());
    }


    public int getRowCount() {
        return _participants != null ? _participants.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        Participant p = _participants.get(row);
        return String.format("%s (%s)", p.getFullName(), p.getUserID());
    }

    public void setValues(List<Participant> participants) {
        _participants = participants;
        fireTableDataChanged();
    }

    public List<Participant> getValues() { return _participants; }

}

