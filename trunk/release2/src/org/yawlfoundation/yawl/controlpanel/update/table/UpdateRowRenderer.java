package org.yawlfoundation.yawl.controlpanel.update.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateRowRenderer extends DefaultTableCellRenderer {

    private java.util.List<UpdateRow> _rows;

    private static final Color REMOVING = new Color(215, 24, 14);
    private static final Color UPDATEABLE = new Color(5, 163, 203);
    private static final Color ADDING = new Color(7, 167, 38);

    public UpdateRowRenderer(java.util.List<UpdateRow> rows) {
        _rows = rows;
    }


    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        UpdateRow updateRow = _rows.get(row);
        if (updateRow.isRemoving()) {
            setForeground(REMOVING);
        }
        else if (updateRow.isAdding()) {
            setForeground(ADDING);
        }
        else if (updateRow.hasNewVersion() && updateRow.isInstalled()) {
            setForeground(UPDATEABLE);
        }
        else setForeground(Color.BLACK);

        if (column < 2) {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        else if (column < 4) {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    //    if (column == UpdateTableModel.COL_INSTALL) {
        else {
            setHorizontalAlignment(SwingConstants.CENTER);
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(updateRow.getInstallAction());
            checkBox.setEnabled(updateRow.isInstallable());
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            return checkBox;
        }

        return this;
    }


    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.setRenderingHints(rh);
        super.paint(g);
    }


}
