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
public class MultiInstanceDialog extends JDialog implements ActionListener {

    private MIAttributePanel minPanel;
    private MIAttributePanel maxPanel;
    private MIAttributePanel thresholdPanel;
    private JCheckBox chkDynamic;
    private JButton okButton;

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
        setResizable(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
        setPreferredSize(new Dimension(630, 210));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {

        }
        setVisible(false);
    }


    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(3, 7, 7, 7));
        minPanel = new MIAttributePanel("Minimum");
        maxPanel = new MIAttributePanel("Maximum");
        thresholdPanel = new MIAttributePanel("Threshold");
        chkDynamic = new JCheckBox("Allow dynamic instance creation");
        JPanel miPanel = new JPanel();
        miPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        miPanel.add(minPanel);
        miPanel.add(maxPanel);
        miPanel.add(thresholdPanel);
        JPanel subPanel = new JPanel(new BorderLayout());
        subPanel.add(miPanel, BorderLayout.CENTER);
        subPanel.add(chkDynamic, BorderLayout.SOUTH);
        content.add(subPanel);
        content.add(createButtonBar());
        return content;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Cancel"));
        okButton = createButton("OK");
        panel.add(okButton);
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
    }


    private void initContent(MIAttributePanel panel, String miQuery, int defValue) {
        if (miQuery != null) {
            int minInstances = StringUtil.strToInt(miQuery, 0);
            if (minInstances > 0) {
                panel.setContent(minInstances);
            }
            else panel.setContent(extractVariableFromQuery(miQuery));
        }
        else panel.setContent(defValue);
    }


    private String extractVariableFromQuery(String query) {
        String variable = query.substring(0, query.lastIndexOf('/'));  // cut '/text()'
        return variable.substring(variable.lastIndexOf('/'));          // cut .../
    }


    private void updateTask(YTask task) {
        task.setUpMultipleInstanceAttributes(minPanel.getContent(), maxPanel.getContent(),
                thresholdPanel.getContent(), getCreationMode());
    }

    public void setCreationMode(String mode) {
         chkDynamic.setSelected(mode != null &&
                 mode.equals(YMultiInstanceAttributes._creationModeDynamic));
    }

    public String getCreationMode() {
        return chkDynamic.isSelected() ?
                YMultiInstanceAttributes._creationModeDynamic :
                YMultiInstanceAttributes._creationModeStatic;
    }


    /****************************************************************************/

    class MIAttributePanel extends JPanel implements ActionListener {

        private JSpinner spnExactly;
        private JComboBox cbxVariable;

        private JRadioButton rbExactly;
        private JRadioButton rbVariable;
        private JRadioButton rbNoLimit;

        MIAttributePanel(String label) {
            setBorder(new TitledBorder(label));
            setLayout(new BorderLayout());
            add(createButtonPanel(), BorderLayout.WEST);
            add(createRHSPanel(), BorderLayout.CENTER);
            setPreferredSize(new Dimension(200, 90));

            // init
            rbExactly.setSelected(true);
            enableComponents(true, false);
            if (label.equals("Minimum")) rbNoLimit.setVisible(false);
        }


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


        private JPanel createRHSPanel() {
            spnExactly = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
            cbxVariable = new JComboBox(getNetVars());
            JPanel rhsPanel = new JPanel(new GridLayout(3,0));
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


        public String getContent() {
            if (rbNoLimit.isSelected()) return String.valueOf(Integer.MAX_VALUE);
            if (rbExactly.isSelected()) return String.valueOf(spnExactly.getValue());
            return (String) cbxVariable.getSelectedItem();
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
