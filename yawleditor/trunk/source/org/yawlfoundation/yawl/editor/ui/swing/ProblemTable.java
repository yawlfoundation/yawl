/*
 * Created on 18/11/2005
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.specification.ProblemList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ProblemTable extends JSingleSelectTable {

    private static final int MAX_ROW_HEIGHT = 5;

    public ProblemTable() {
        super();
        initialise();
    }

    private void initialise() {
        setModel(new MessageTableModel());
        setFillsViewportHeight(true);
        setTableHeader(null);
    }

    public void addMessage(String message) {
        getMessageModel().addMessage(message);
    }

    private MessageTableModel getMessageModel() {
        return (MessageTableModel) getModel();
    }

    public void setWidth(int width) {
//        int widestRow = getMessageModel().getLongestMessageLength() * 8;
        getColumnModel().getColumn(0).setPreferredWidth(width);
    }


    public void reset() {
        getMessageModel().reset();
    }


    public Dimension getPreferredScrollableViewportSize() {
        Dimension preferredSize = super.getPreferredScrollableViewportSize();

        preferredSize.setSize(
                preferredSize.getWidth(),
                Math.min(
                        preferredSize.getHeight(),
                        getFontMetrics(getFont()).getHeight() * MAX_ROW_HEIGHT
                )
        );

        return preferredSize;
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
        JLabel componentAsLabel = (JLabel) component;
        componentAsLabel.setHorizontalAlignment(JLabel.LEFT);
        return component;
    }

    public String getToolTipText(MouseEvent event){
        Point mousePosition = event.getPoint();
        if (rowAtPoint(mousePosition) >= 0) {
            String rowContent = (String) getValueAt(
                    rowAtPoint(mousePosition),
                    MessageTableModel.PROBLEM_COLUMN
            );

            if (rowContent != null) {
                return "<html><body style=\"width:300px\"><p>" + rowContent.toString() + "</p></body></html>";
            }
        }
        return "";
    }
}

class MessageTableModel extends AbstractTableModel {

    private ProblemList messages = new ProblemList();

    private static final String[] COLUMN_LABELS = { "Problem" };

    public static final int PROBLEM_COLUMN = 0;

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public int getLongestMessageLength() {
        int result = 0;
        for (String msg : messages) {
            result = Math.max(result, msg.length());
        }
        return result;
    }

    public void reset() {
        messages.clear();
    }


    public String getColumnName(int columnIndex) {
        return null;
    }

    public int getRowCount() {
        return (messages != null) ? messages.size() : 0;
    }

    public Object getValueAt(int row, int col) {
        return col == PROBLEM_COLUMN ? messages.get(row) : null;
    }

    public void addMessage(String message) {
        messages.add(message);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }
}