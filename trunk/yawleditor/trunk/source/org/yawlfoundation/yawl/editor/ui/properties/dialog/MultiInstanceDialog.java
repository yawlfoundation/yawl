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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class MultiInstanceDialog extends JDialog
        implements ActionListener, ChangeListener {

    private MIAttributePanel minPanel;
    private MIAttributePanel maxPanel;
    private MIAttributePanel thresholdPanel;
    private JCheckBox chkDynamic;
    private JLabel statusLabel;
    private JButton btnOK;

    private String strValue;
    private Vector<String> integralVars;
    private boolean initialising;

    private final YNet net;
    private final YTask task;


    public MultiInstanceDialog(YNet net, String taskID) {
        super();
        this.net = net;
        task = (YTask) net.getNetElement(taskID);
        setTitle("Multiple Instance Attributes for Task: " + taskID);
        add(createContent());
        initValues();
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
        setPreferredSize(new Dimension(630, 230));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            updateTask();
        }
        setVisible(false);
    }


    public void stateChanged(ChangeEvent e) {
        if (! (initialising)) {
            validateState((JSpinner) e.getSource());
        }
    }


    public String getCurrentStringValue() { return strValue; }


    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(3, 7, 7, 7));
        minPanel = new MIAttributePanel(this, "Minimum");
        maxPanel = new MIAttributePanel(this, "Maximum");
        thresholdPanel = new MIAttributePanel(this, "Threshold");
        JPanel miPanel = new JPanel();
        miPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        miPanel.add(minPanel);
        miPanel.add(maxPanel);
        miPanel.add(thresholdPanel);
        JPanel subPanel = new JPanel(new BorderLayout());
        subPanel.add(miPanel, BorderLayout.CENTER);
        subPanel.add(createLowerPanel(), BorderLayout.SOUTH);
        content.add(subPanel);
        content.add(createButtonBar());
        return content;
    }


    private JPanel createLowerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0,5,0,5));
        chkDynamic = new JCheckBox("Allow dynamic instance creation");
        panel.add(chkDynamic, BorderLayout.WEST);
        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Cancel"));
        btnOK = createButton("OK");
        panel.add(btnOK);
        return panel;
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }


    private void initValues() {
        initialising = true;
        YMultiInstanceAttributes miAttributes = task.getMultiInstanceAttributes();
        initContent(minPanel, miAttributes.getMinInstancesQuery(), 1);
        initContent(maxPanel, miAttributes.getMaxInstancesQuery(), 2);
        initContent(thresholdPanel, miAttributes.getThresholdQuery(), 1);
        setCreationMode(miAttributes.getCreationMode());
        setCurrentValueString();
        initialising = false;
    }


    private void initContent(MIAttributePanel panel, String miQuery, int defValue) {
        if (miQuery != null) {
            int value = StringUtil.strToInt(miQuery, 0);
            if (value > 0) {
                panel.setContent(value);
            }
            else panel.setContent(miQuery);
        }
        else panel.setContent(defValue);
    }


    private void updateTask() {
        task.setUpMultipleInstanceAttributes(minPanel.getContent(), maxPanel.getContent(),
                thresholdPanel.getContent(), getCreationMode());
        setCurrentValueString();
    }


    private void setCreationMode(String mode) {
         chkDynamic.setSelected(mode != null &&
                 mode.equals(YMultiInstanceAttributes.CREATION_MODE_DYNAMIC));
    }


    private void setStatusText(String text) {
        statusLabel.setText(text);
        btnOK.setEnabled(text.isEmpty());
    }


    private String getCreationMode() {
        return chkDynamic.isSelected() ?
                YMultiInstanceAttributes.CREATION_MODE_DYNAMIC :
                YMultiInstanceAttributes.CREATION_MODE_STATIC;
    }


    private void setCurrentValueString() {
        strValue = minPanel.getSummaryString() + ", " +
                maxPanel.getSummaryString() + ", " +
                thresholdPanel.getSummaryString() + ", " +
                Character.toUpperCase(getCreationMode().charAt(0));
    }


    private void validateState(JSpinner spinner) {
        String statusText = "";
        int min = minPanel.getIntValue();
        int max = maxPanel.getIntValue();
        int threshold = thresholdPanel.getIntValue();
        if (minPanel.isSpinnerOf(spinner)) {
            if (max < min && max > 0) {
                statusText = "Minimum can't exceed maximum";
            }
            else if (threshold < min && threshold > 0) {
                statusText = "Minimum can't exceed threshold";
            }
        }
        else if (maxPanel.isSpinnerOf(spinner)) {
            if (max < min && min > 0) {
                statusText = "Maximum can't be less than minimum";
            }
        }
        else if (thresholdPanel.isSpinnerOf(spinner)) {
            if (threshold < min && min > 0) {
                statusText = "Threshold can't be less than minimum";
            }
        }
        setStatusText(statusText);
    }


    /****************************************************************************/

    class MIAttributePanel extends JPanel implements ActionListener {

        private JSpinner spnExactly;
        private JComboBox cbxVariable;

        private JRadioButton rbExactly;
        private JRadioButton rbVariable;
        private JRadioButton rbNoLimit;

        private final String label;

        MIAttributePanel(ChangeListener listener, String label) {
            this.label = label;
            setBorder(new CompoundBorder(new TitledBorder(label),
                    new EmptyBorder(5,5,5,5)));
            setLayout(new BorderLayout());
            add(createButtonPanel(), BorderLayout.WEST);
            add(createRHSPanel(listener), BorderLayout.CENTER);
            setPreferredSize(new Dimension(200, 110));

            // init
            rbExactly.setSelected(true);
            enableComponents(true, false);
            if (label.equals("Minimum")) rbNoLimit.setVisible(false);
        }

        boolean isSpinnerOf(JSpinner spinner) {
            return spnExactly == spinner;
        }


        public String getLabel() { return label; }


        private JPanel createButtonPanel() {
            ButtonGroup group = new ButtonGroup();
            rbExactly = createButton("Exactly:  ", group);
            rbVariable = createButton("Variable: ", group);
            rbNoLimit = createButton("No limit", group);
            JPanel rbPanel = new JPanel(new GridLayout(3,0));
            rbPanel.add(rbExactly);
            rbPanel.add(rbVariable);
            rbPanel.add(rbNoLimit);
            return rbPanel;
        }


        private JPanel createRHSPanel(ChangeListener listener) {
            spnExactly = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
            spnExactly.addChangeListener(listener);
            cbxVariable = new JComboBox(getIntegralNetVars());
            JPanel rhsPanel = new JPanel(new GridLayout(3,0,5,5));
            rhsPanel.add(spnExactly);
            rhsPanel.add(cbxVariable);
            rhsPanel.setPreferredSize(new Dimension(150, 20));
            return rhsPanel;
        }


        private JRadioButton createButton(String label, ButtonGroup group) {
            JRadioButton button = new JRadioButton(label);
            button.setActionCommand(label);
            button.addActionListener(this);
            group.add(button);
            return button;
        }


        public void actionPerformed(ActionEvent event) {
            String action = event.getActionCommand();
            if (action.equals("Exactly:  ")) {
                enableComponents(true, false);
            }
            else if (action.equals("Variable: ")) {
                enableComponents(false, true);
            }
            else if (action.equals("No limit")) {
                enableComponents(false, false);
            }
            validateState(spnExactly);
        }


        public void setContent(int value) {
            if (value == Integer.MAX_VALUE && rbNoLimit.isEnabled()) {
                rbNoLimit.setSelected(true);
                enableComponents(false, false);
            }
            else {
                spnExactly.setValue(value);
                rbExactly.setSelected(true);
                enableComponents(true, false);
            }
        }


        public void setContent(String value) {
            cbxVariable.setSelectedItem(value);
            rbVariable.setSelected(true);
        }


        public int getIntValue() {
            if (rbNoLimit.isSelected()) return Integer.MAX_VALUE;
            if (rbExactly.isSelected()) return (Integer) spnExactly.getValue();
            return 0;   // var selected
        }

        public String getContent() {
            if (rbNoLimit.isSelected()) return String.valueOf(Integer.MAX_VALUE);
            if (rbExactly.isSelected()) return String.valueOf(spnExactly.getValue());
            return (String) cbxVariable.getSelectedItem();
        }

        public String getSummaryString() {
            if (rbNoLimit.isSelected()) return Character.toString('\u221E');  // infinity
            if (rbExactly.isSelected()) return String.valueOf(spnExactly.getValue());
            return "V"; // for variable
        }


        private void enableComponents(boolean enableSpinner, boolean enableCombo) {
            spnExactly.setEnabled(enableSpinner);
            cbxVariable.setEnabled(enableCombo);
        }

        private Vector<String> getIntegralNetVars() {
            if (integralVars == null) {
                integralVars = new Vector<String>();
                Set<YVariable> netVars = new HashSet<YVariable>(net.getLocalVariables().values());
                netVars.addAll(net.getInputParameters().values());
                for (YVariable var : netVars) {
                    if (XSDType.isIntegralType(var.getDataTypeName())) {
                        integralVars.add(var.getPreferredName());
                    }
                }
                Collections.sort(integralVars);
            }
            rbVariable.setEnabled(! integralVars.isEmpty());
            return integralVars;
        }

    }

}
