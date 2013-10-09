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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel.AbstractResourceListModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class ListDialog extends JDialog implements ActionListener, CaretListener {

    private JList listBox;
    private JTextField filterField;

    public ListDialog(JDialog owner) {
        super(owner);
        initialise();
        add(getContent());
        setPreferredSize(new Dimension(250, 400));
        pack();
    }

    public ListDialog(JDialog owner, ListModel listModel) {
        this(owner);
        setListModel(listModel);
    }

    public ListDialog(JDialog owner, ListModel listModel, String title) {
        this(owner, listModel);
        setTitle(title);
    }


    public void setListModel(ListModel listModel) { listBox.setModel(listModel); }

    public AbstractResourceListModel getListModel() {
        return (AbstractResourceListModel) listBox.getModel();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Cancel")) {
            listBox.clearSelection();
        }
        setVisible(false);
    }

    public void caretUpdate(CaretEvent caretEvent) {
        listBox.clearSelection();
        getListModel().filter(filterField.getText());
    }

    public java.util.List<Object> getSelections() {
        return getListModel().getSelections(listBox.getSelectedIndices());
    }


    private void initialise() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createFilterPanel(), BorderLayout.NORTH);
        content.add(createListBox(), BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter:"));
        filterField = new JTextField();
        filterField.setPreferredSize(new Dimension(180, 25));
        filterField.addCaretListener(this);
        filterPanel.add(filterField);
        return filterPanel;
    }

    private JScrollPane createListBox() {
        listBox = new JList();
        listBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    setVisible(false);
                }
            }
        });

        return new JScrollPane(listBox);
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton("Cancel"));
        panel.add(createButton("OK"));
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


}
