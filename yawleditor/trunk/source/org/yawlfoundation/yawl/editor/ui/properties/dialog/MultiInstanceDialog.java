package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
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

    private String strValue;

    private YNet net;
    private YTask task;


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
        setPreferredSize(new Dimension(630, 210));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            updateTask();
        }
        setVisible(false);
    }


    public void stateChanged(ChangeEvent e) {
        String statusText = "";
        JSpinner spinner = (JSpinner) e.getSource();
        int value = (Integer) spinner.getValue();
        int min = minPanel.getIntValue();
        int max = maxPanel.getIntValue();
        int threshold = thresholdPanel.getIntValue();
        if (minPanel.isSpinnerOf(spinner)) {
            if (min > max) {
                statusText = "Minimum can't exceed maximum";
                spinner.setValue(--min);
            }
            else if (min > threshold) {
                statusText = "Minimum can't exceed threshold";
                spinner.setValue(--min);
            }
        }
        statusLabel.setText(statusText);
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
        statusLabel = new JLabel("Minimum can't exceed maximum");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Cancel"));
        panel.add(createButton("OK"));
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
        YMultiInstanceAttributes miAttributes = task.getMultiInstanceAttributes();
        initContent(minPanel, miAttributes.getMinInstancesQuery(), 1);
        initContent(maxPanel, miAttributes.getMaxInstancesQuery(), 2);
        initContent(thresholdPanel, miAttributes.getThresholdQuery(), 1);
        setCreationMode(miAttributes.getCreationMode());
        setCurrentValueString();
    }


    private void initContent(MIAttributePanel panel, String miQuery, int defValue) {
        if (miQuery != null) {
            int value = StringUtil.strToInt(miQuery, 0);
            if (value > 0) {
                panel.setContent(value);
            }
            else panel.setContent(extractVariableFromQuery(miQuery));
        }
        else panel.setContent(defValue);
    }


    private String extractVariableFromQuery(String query) {
        String variable = query.substring(0, query.lastIndexOf('/'));  // cut '/text()'
        return variable.substring(variable.lastIndexOf('/'));          // cut .../
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


    private String getCreationMode() {
        return chkDynamic.isSelected() ?
                YMultiInstanceAttributes.CREATION_MODE_DYNAMIC :
                YMultiInstanceAttributes.CREATION_MODE_STATIC;
    }


    private void setCurrentValueString() {
        strValue = minPanel.getSummaryString() + ',' +
                maxPanel.getSummaryString() + ',' +
                thresholdPanel.getSummaryString() + ',' +
                Character.toUpperCase(getCreationMode().charAt(0));
    }

    /****************************************************************************/

    class MIAttributePanel extends JPanel implements ActionListener {

        private JSpinner spnExactly;
        private JComboBox cbxVariable;

        private JRadioButton rbExactly;
        private JRadioButton rbVariable;
        private JRadioButton rbNoLimit;

        private String label;

        MIAttributePanel(ChangeListener listener, String label) {
            this.label = label;
            setBorder(new TitledBorder(label));
            setLayout(new BorderLayout());
            add(createButtonPanel(), BorderLayout.WEST);
            add(createRHSPanel(listener), BorderLayout.CENTER);
            setPreferredSize(new Dimension(200, 90));

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
            cbxVariable = new JComboBox(getNetVars());
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
            return -1;   // var selected
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

        private Vector<String> getNetVars() {
            Vector<String> numericVars = new Vector<String>();
            Set<YVariable> variables = new HashSet<YVariable>(net.getLocalVariables().values());
            variables.addAll(net.getInputParameters().values());
            for (YVariable variable : variables) {
                if (XSDType.getInstance().isIntegralType(variable.getDataTypeName())) {
                   numericVars.add(variable.getPreferredName());
                }
            }
            Collections.sort(numericVars);
            return numericVars;
        }

    }

}
