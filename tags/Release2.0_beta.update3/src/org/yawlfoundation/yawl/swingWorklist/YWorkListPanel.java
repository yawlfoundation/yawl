/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.swingWorklist;

import org.yawlfoundation.yawl.engine.gui.YAdminGUI;
import org.yawlfoundation.yawl.swingWorklist.util.TableSorter;

import javax.swing.*;
import java.awt.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 15/05/2003
 * Time: 10:49:04
 * 
 */
public class YWorkListPanel extends JPanel {
    private JTable _myTable;


    public YWorkListPanel(YWorklistTableModel worklistModel, String caption,
                          Dimension size,
                          JButton actionButton,
                          JButton cancelTaskButton,
                          JButton addInstanceButton,
                          JButton viewDataButton) {
        setLayout(new BorderLayout(10, 10));
        TableSorter sorter = new TableSorter(worklistModel); //ADDED THIS
        _myTable = new JTable(sorter);
        _myTable.setPreferredScrollableViewportSize(size);
        _myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        worklistModel.addRow("InstanceValidator", new String[]{"", "", "", "", "", ""});

        synchronized (_myTable) {
            _myTable.setModel(worklistModel);
            _myTable.getColumnModel().getColumn(0).setPreferredWidth(45);
            _myTable.getColumnModel().getColumn(1).setPreferredWidth(45);
            _myTable.getColumnModel().getColumn(2).setPreferredWidth(250);
            _myTable.getColumnModel().getColumn(3).setPreferredWidth(100);
            _myTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            _myTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        }
        worklistModel.removeRow("InstanceValidator");
        sorter.addMouseListenerToHeaderInTable(_myTable); //ADDED THIS
        // Place table in JScrollPane
        JScrollPane scrollPane = new JScrollPane(_myTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Add to Screen
        add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPane = null;
        if (cancelTaskButton != null && addInstanceButton != null) {
            buttonPane = new JPanel(new GridLayout(1, 4, 10, 10));
            buttonPane.add(cancelTaskButton);
            buttonPane.add(addInstanceButton);
            addInstanceButton.setEnabled(false);
            buttonPane.add(viewDataButton);
            buttonPane.add(actionButton);
        } else {
            buttonPane = new JPanel(new BorderLayout());
            buttonPane.add(actionButton, BorderLayout.EAST);
        }
        buttonPane.setBackground(YAdminGUI._apiColour);
        add(buttonPane, BorderLayout.SOUTH);
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), caption),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        this.setBackground(YAdminGUI._apiColour);
    }


    public JTable getMyTable() {
        return _myTable;
    }
}
