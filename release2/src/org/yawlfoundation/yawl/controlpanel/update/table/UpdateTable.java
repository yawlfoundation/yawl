package org.yawlfoundation.yawl.controlpanel.update.table;

import org.yawlfoundation.yawl.controlpanel.update.Differ;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 17/08/2014
 */
public class UpdateTable extends JTable {

    private static final Color EVEN_ROW_COLOR = Color.WHITE;
    private static final Color ODD_ROW_COLOR = new Color(211,223,237);


    public UpdateTable(Differ differ) {
        super();
        init(differ);
    }

    public boolean hasUpdates() { return getTableModel().hasUpdates(); }

    public void refresh(Differ differ) { getTableModel().setDiffer(differ); }


    private void init(Differ differ) {
        setModel(new UpdateTableModel(differ));
        getTableHeader().setReorderingAllowed(false);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(false);
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(new Color(202,202,202));
        setRowHeight(getRowHeight() + 8);
        setColumnWidths();
        java.util.List<UpdateRow> rows = getTableModel().getRows();
        setDefaultRenderer(String.class, new UpdateRowRenderer(rows));
        setDefaultRenderer(Boolean.class, new UpdateRowRenderer(rows));
    }


    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
        component.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
        if (isRowSelected(row)) {
            component.setBackground(getSelectionBackground());
        }
        return component;
    }


    private void setColumnWidths() {
        getColumnModel().getColumn(0).setPreferredWidth(120);
        getColumnModel().getColumn(1).setPreferredWidth(290);
        getColumnModel().getColumn(2).setPreferredWidth(50);
        getColumnModel().getColumn(3).setPreferredWidth(50);
        getColumnModel().getColumn(4).setPreferredWidth(70);
    }


    private UpdateTableModel getTableModel() { return ((UpdateTableModel) getModel()); }
}
