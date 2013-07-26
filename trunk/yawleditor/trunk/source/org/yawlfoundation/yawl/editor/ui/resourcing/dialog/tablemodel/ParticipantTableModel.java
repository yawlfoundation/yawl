package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog.ListDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel.ParticipantListModel;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class ParticipantTableModel extends AbstractResourceTableModel {

    private List<Participant> _participants;

    public ParticipantTableModel() { super(); }


    public int getRowCount() {
        return isEnabled() && _participants != null ? _participants.size() : 0;
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

    public List<Participant> getValues() {
        return _participants != null ? _participants : Collections.<Participant>emptyList();
    }


    public void handleAddRequest() {
        ListDialog listDialog = new ListDialog(getOwner(), new ParticipantListModel(),
                "All Participants");
        listDialog.setVisible(true);
        if (_participants == null) _participants = new ArrayList<Participant>();
        for (Object o : listDialog.getSelections()) {
            Participant p = (Participant) o;
            if (! _participants.contains(p)) _participants.add(p);
        }
        Collections.sort(_participants, new ParticipantNameComparator());
        fireTableDataChanged();
    }


    public void handleRemoveRequest(int[] selectedRows) {
        List<Participant> toRemove = new ArrayList<Participant>();
        for (int row : selectedRows) {
            toRemove.add(_participants.get(row));
        }
        _participants.removeAll(toRemove);
        fireTableDataChanged();
    }

    // editing not required for this model
    public void handleEditRequest(int selectedRow) { }

}

