/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 29/09/2014
 */
public class AddRuleDialog extends JDialog
        implements ActionListener, ItemListener, ListSelectionListener {

    private JButton _btnAdd;
    private JButton _btnClose;
    private JTextArea _txtDescription;
    private JComboBox _cbxType;
    private JComboBox _cbxTask;
    private JLabel _cbxTaskPrompt;
    private JTextField _txtCondition;
    private ConclusionTablePanel _conclusionPanel;
    private DataContextTablePanel _dataContextPanel;


    public AddRuleDialog(AtomicTask task) {
        super(YAWLEditor.getInstance());
        setTitle("Add Worklet Rule for Specification: " +
                SpecificationModel.getHandler().getID());
        setModal(true);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(getContent(task));
        setPreferredSize(new Dimension(800, 500));
        setMinimumSize(new Dimension(300, 300));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Cancel")) {
            setVisible(false);
        }
        else if (cmd.equals("Add Rule")) {
            addRule();
        }
        else if (cmd.equals("Add & Close")) {
            addRule();
            setVisible(false);
        }
    }


    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            Object item = event.getItem();
            if (item instanceof RuleType) {
                RuleType selectedType = (RuleType) item;
                enabledTaskCombo(selectedType);
                if (selectedType.isCaseLevelType()) {
                    _dataContextPanel.setVariables(getDataContext(null));
                }
                else if (selectedType == RuleType.ItemSelection) {
                    // only show worklet tasks in tasks combo
                }
            }
            else {    // task combo
                _dataContextPanel.setVariables(getDataContext((AtomicTask) item));
            }
        }
    }

    // table selection
    public void valueChanged(ListSelectionEvent event) {
        if (! event.getValueIsAdjusting()) {
            ListSelectionModel lsm = (ListSelectionModel) event.getSource();
            VariableRow row = _dataContextPanel.getVariableAtRow(lsm.getMinSelectionIndex());
            if (row != null) {
                String condition = row.getName();
                String value = row.getValue();
                if (value != null) {
                    if (row.getDataType().equals("string")) {
                        value = "\"" + value + "\"";
                    }
                    condition += " = " + value;
                }
                _txtCondition.setText(condition);
            }
        }
    }


    private JPanel getContent(AtomicTask task) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(8,8,8,8));
        panel.add(getEntryPanel(task), BorderLayout.CENTER);
        panel.add(getButtonBar(this), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel getEntryPanel(AtomicTask task) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getActionPanel(task), BorderLayout.WEST);
        panel.add(getDataPanel(task), BorderLayout.CENTER);
        return panel;
    }


    private JPanel getActionPanel(AtomicTask task) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getRulePanel(task), BorderLayout.NORTH);
        panel.add(getDescriptionPanel(), BorderLayout.SOUTH);
        panel.add(getConclusionPanel(), BorderLayout.CENTER);
        return panel;
    }


    private JPanel getDataPanel(AtomicTask task) {
        _dataContextPanel = new DataContextTablePanel(this);
        _dataContextPanel.setVariables(getDataContext(task));
        return _dataContextPanel;
    }


    private JPanel getRulePanel(AtomicTask task) {
        JPanel panel = new JPanel(new SpringLayout());
        _cbxTask = getTaskCombo(task);
        _cbxType = getTypeCombo();
        _txtCondition = new JTextField();
        addContent(panel, "Rule Type:", _cbxType);
        _cbxTaskPrompt = addContent(panel, "Task:", _cbxTask);
        addContent(panel, "Condition:", _txtCondition);
        SpringUtil.makeCompactGrid(panel, 3, 2, 6, 6, 8, 8);

        if (isWorkletTask(task)) {
            _cbxType.setSelectedItem(RuleType.ItemSelection);
        }

        return panel;
    }

    private JLabel addContent(JPanel panel, String prompt, Component c) {
        JLabel label = new JLabel(prompt, JLabel.LEADING);
        label.setFont((Font) UIManager.get("TitledBorder.font"));
        label.setForeground((Color) UIManager.get("TitledBorder.titleColor"));
        panel.add(label);
        label.setLabelFor(c);
        panel.add(c);
        return label;
    }


    private JComboBox getTypeCombo() {
        JComboBox combo = new JComboBox(RuleType.values());

        combo.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList jList, Object o,
                                                          int i, boolean b, boolean b1) {
                return new JLabel(((RuleType) o).toLongString());
            }
        });

        combo.addItemListener(this);
        return combo;
    }


    private JComboBox getTaskCombo(AtomicTask task) {
        Vector<YAWLAtomicTask> taskVector = new Vector<YAWLAtomicTask>();
        for (NetGraphModel model : SpecificationModel.getNets()) {
            for (YAWLAtomicTask netTask : NetUtilities.getAtomicTasks(model)) {
                 taskVector.add(netTask);
            }
        }
        JComboBox combo = new JComboBox(taskVector);

        combo.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList jList, Object o,
                                                          int i, boolean b, boolean b1) {
                return new JLabel(((YAWLAtomicTask) o).getLabel());
            }
        });

        if (task != null) {
            combo.setSelectedItem(task);
        }
        combo.addItemListener(this);
        return combo;
    }


    private JPanel getDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Description (optional)"));
        _txtDescription = new JTextArea(5, 20);
        _txtDescription.setLineWrap(true);
        _txtDescription.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(_txtDescription);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    private JPanel getConclusionPanel() {
        _conclusionPanel = new ConclusionTablePanel();
        return _conclusionPanel;
    }


    protected JPanel getButtonBar(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        panel.add(createButton("Cancel", listener));
        _btnAdd = createButton("Add Rule", listener);
        _btnAdd.setEnabled(false);
        panel.add(_btnAdd);
        _btnClose = createButton("Add & Close", listener);
        _btnClose.setEnabled(false);
        panel.add(_btnClose);
        return panel;
    }


    protected JButton createButton(String caption, ActionListener listener) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(90,25));
        btn.addActionListener(listener);
        return btn;
    }


    private boolean isWorkletTask(AtomicTask task) {
        if (task == null) return false;
        YAWLServiceGateway decomposition =
                (YAWLServiceGateway) task.getDecomposition();
        if (decomposition != null) {
            YAWLServiceReference service = decomposition.getYawlService();
            if (service != null) {
                String uri = service.getServiceID();
                return uri != null && uri.contains("workletService/ib");
            }
        }
        return false;
    }


    private void enabledTaskCombo(RuleType ruleType) {
        _cbxTask.setEnabled(ruleType.isItemLevelType());
        _cbxTaskPrompt.setEnabled(ruleType.isItemLevelType());
    }

    private java.util.List<VariableRow> getDataContext(AtomicTask task) {
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();
        YDecomposition decomposition;
        if (task == null) {       // case level
            decomposition = SpecificationModel.getNets().getRootNet().getDecomposition();
            if (decomposition != null) {
                for (YVariable local : ((YNet) decomposition).getLocalVariables().values()) {
                    rows.add(new VariableRow(local, false, decomposition.getID()));
                }
            }
        }
        else {
            decomposition = task.getDecomposition();
        }

        if (decomposition != null) {
            String id = task != null ? task.getID() : decomposition.getID();
            for (YParameter input : decomposition.getInputParameters().values()) {
                rows.add(new VariableRow(input, false, id));
            }
        }

        Collections.sort(rows);
        return rows;
    }


    private void addRule() {

    }

}
