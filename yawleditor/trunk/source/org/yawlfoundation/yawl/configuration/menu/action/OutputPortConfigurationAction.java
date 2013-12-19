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

package org.yawlfoundation.yawl.configuration.menu.action;

import org.yawlfoundation.yawl.configuration.CPort;
import org.yawlfoundation.yawl.configuration.ConfigurationTableCellRenderer;
import org.yawlfoundation.yawl.configuration.PortIDRenderer;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.element.TaskConfigurationCache;
import org.yawlfoundation.yawl.configuration.net.NetConfiguration;
import org.yawlfoundation.yawl.configuration.net.NetConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;


public class OutputPortConfigurationAction extends ProcessConfigurationAction
        implements TooltipTogglingWidget {


    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Output Ports...");
        putValue(Action.LONG_DESCRIPTION, "Configure the output ports for this task.");
        putValue(Action.SMALL_ICON, getPNGIcon("arrow_out"));
    }


    public OutputPortConfigurationAction() {
        super();
    }

    public void actionPerformed(ActionEvent event) {
        final YAWLTask task = this.task;

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                OutputConfigurationJDialog dialog = new OutputConfigurationJDialog(new JFrame(), task, net);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                });
                dialog.setLocationRelativeTo(YAWLEditor.getInstance());
                dialog.setSize(dialog.getSize());
                dialog.setResizable(false);
                dialog.setVisible(true);
            }
        });
    }

    public void setEnabled(boolean enable) {
        if (net == null || task == null) {
            super.setEnabled(false);
            return;
        }
        TaskConfiguration taskConfiguration = TaskConfigurationCache.getInstance()
                .get(net.getNetModel(), task);
        super.setEnabled(enable && task != null && taskConfiguration != null &&
                taskConfiguration.isConfigurable());
    }

    public String getDisabledTooltipText() {
        return null;
    }


    public String getEnabledTooltipText() {
        return "Configure the output ports for this task.";
    }

    /**
     *
     * @author jingxin
     */
    private class OutputConfigurationJDialog extends JDialog {

        private List<CPort> OutputPorts;
        private NetConfiguration netConfiguration;
        private TaskConfiguration taskConfiguration;

        private final YAWLTask task;

        private final int size;

        private TableColumn column = null;
        private TableColumn IDcolumn = null;
        private final NetGraph net;
        private final ActionEvent simulateEvent = new ActionEvent(this,1001,"Preview Process Configuration");
        private final PreviewConfigurationProcessAction simulateAction =
                PreviewConfigurationProcessAction.getInstance();

        /** Creates new form NewJDialog */
        public OutputConfigurationJDialog(Frame parent, YAWLTask task, NetGraph net) {
            super(parent, true);
            this.task = task;
            this.net = net;
            netConfiguration = NetConfigurationCache.getInstance().get(net.getNetModel());
            taskConfiguration = TaskConfigurationCache.getInstance()
                    .get(net.getNetModel(), task);
            size = taskConfiguration.getOutputCPorts().size();
            initOutputPorts();
            initComponents();
        }

        public void  initOutputPorts(){
            this.OutputPorts = taskConfiguration.getOutputCPorts();
        }
        public Object[][] getPortsInformation(){
            Object[][] rowInfor = new Object[size][3];


            for(int i=0; i<size ; i++){
                rowInfor[i][0] = this.OutputPorts.get(i).getID();
                rowInfor[i][1] = this.OutputPorts.get(i).getTargetTasksLabels();
                rowInfor[i][2] = this.OutputPorts.get(i).getConfigurationSetting();
            }
            return rowInfor;
        }
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jLabel1 = new JLabel();
            jScrollPane1 = new JScrollPane();
            outputPortsConfigurationTable = new JTable();
            ActivateButton = new JButton();
            BlockButton = new JButton();
            SetDefaultButton = new JButton();
            DefaultButton = new JButton();

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.setTitle("Output Ports Configuration");
            jLabel1.setFont(new Font("Tahoma", 0, 14));
            jLabel1.setText("Please select which ports you want to configure.");

            outputPortsConfigurationTable.setModel(new javax.swing.table.DefaultTableModel(
                    this.getPortsInformation(),
                    new String [] {
                            "Port ID", "Target Nodes",  "Configuration"
                    }
            ) {
                final Class[] types = new Class [] {
                        Integer.class, String.class, String.class
                };
                final boolean[] canEdit = new boolean [] {
                        false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });


            for (int i = 0; i < 3; i++) {
                column = outputPortsConfigurationTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(15);
                    IDcolumn = column;
                }
                if (i == 1) {
                    column.setPreferredWidth(160);
                }
                if (i == 2) {
                    column.setPreferredWidth(50);
                }
            }

            for(int i=0; i<this.outputPortsConfigurationTable.getRowCount(); i++){
                column.setCellRenderer(new ConfigurationTableCellRenderer());
            }
            IDcolumn.setCellRenderer(new PortIDRenderer());
            jScrollPane1.setViewportView(outputPortsConfigurationTable);

            ActivateButton.setText("Activate");
            ActivateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ActivateButtonActionPerformed(evt);
                }
            });

            BlockButton.setText("Block");
            BlockButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    BlockButtonActionPerformed(evt);
                }
            });



            DefaultButton.setText("Use Default");
            DefaultButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    DefaultButtonActionPerformed(evt);
                }
            });

            SetDefaultButton.setText("Set Defaults");
            SetDefaultButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    SetDefaultButtonActionPerformed(evt);
                }
            });

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                                    .addContainerGap())
                                            .addGroup(layout.createSequentialGroup()
                                                    .addGap(4, 4, 4)
                                                    .addComponent(ActivateButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(BlockButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(DefaultButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                                                    .addComponent(SetDefaultButton)
                                                    .addGap(21, 21, 21))))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGap(19, 19, 19)
                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(SetDefaultButton)
                                            .addComponent(DefaultButton)
                                            .addComponent(BlockButton)
                                            .addComponent(ActivateButton))
                                    .addGap(34, 34, 34))
            );

            // bindingGroup.bind();
            if(netConfiguration.getSettings().isAllowChangingDefaultConfiguration()){
                this.SetDefaultButton.setEnabled(true);
            }else{
                this.SetDefaultButton.setEnabled(false);
            }
            this.outputPortsConfigurationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            this.jScrollPane1.setAutoscrolls(true);
            this.outputPortsConfigurationTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    jTableMouseReleased(evt);
                }
            });
            //this.jScrollPane1.setMaximumSize(new Dimension(this.jScrollPane1.getWidth()*6,this.jScrollPane1.getHeight()*6));

            pack();
        }// </editor-fold>

        private void ActivateButtonActionPerformed(ActionEvent evt) {
            int length = this.outputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.outputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                ActivateAPort(selectedRows, i);
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
            toggleEnabled(ActivateButton);
        }

        private void ActivateAPort(int[] selectedRows, int i) {
            int portId = (Integer) this.outputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
            this.OutputPorts.get(portId).setConfigurationSetting("activated");
            this.outputPortsConfigurationTable.setValueAt("activated", selectedRows[i], 2);
            if(netConfiguration.getServiceAutonomous() != null){
                netConfiguration.getServiceAutonomous().changeCurrentStateAfterActivate(task, "OUTPUT", portId);
            }
        }

        private void BlockButtonActionPerformed(ActionEvent evt) {
            int length = this.outputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.outputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                BlockAPort(selectedRows, i);
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
            toggleEnabled(BlockButton);
        }

        private void BlockAPort(int[] selectedRows, int i) {
            int portId = (Integer) this.outputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
            this.OutputPorts.get(portId).setConfigurationSetting("blocked");
            this.outputPortsConfigurationTable.setValueAt("blocked", selectedRows[i], 2);
            if(netConfiguration.getServiceAutonomous() != null ){
                netConfiguration.getServiceAutonomous().changeCurrentStateAfterBlock(task, "OUTPUT", portId);
            }
        }

        private void DefaultButtonActionPerformed(ActionEvent evt) {
            int length = this.outputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.outputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.outputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
                if(this.OutputPorts.get(portId).getDefaultValue() != null){
                    if(this.OutputPorts.get(portId).getDefaultValue().equals(CPort.ACTIVATED)){
                        ActivateAPort(selectedRows, i);
                    } else if (this.OutputPorts.get(portId).getDefaultValue().equals(CPort.BLOCKED)){
                        BlockAPort(selectedRows, i);
                    }
                }
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
        }

        private void SetDefaultButtonActionPerformed(ActionEvent evt) {
            final YAWLTask task = this.task;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    SetOutputPortDefaultConfigurationJDialog dialog = new SetOutputPortDefaultConfigurationJDialog(new JFrame(), net, task);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    });
                    dialog.setLocationRelativeTo(YAWLEditor.getInstance());
                    dialog.setSize(dialog.getSize());
                    dialog.setVisible(true);
                }
            });
        }
        private void jTableMouseReleased(java.awt.event.MouseEvent evt) {
            boolean DefaultButtonFlag = false;
            boolean defaultBlockFlag = true;
            boolean defaultActivateFlag = true;
            boolean blockFlag = true;
            boolean activateFlag = true;
            String s = null;
            int length = this.outputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.outputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.outputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
                if(this.OutputPorts.get(portId).getDefaultValue() != null){
                    DefaultButtonFlag = true;
                }
                if(netConfiguration.getServiceAutonomous() != null){
                    blockFlag = blockFlag && (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForBlock(task, "OUTPUT", portId));

                    if(this.OutputPorts.get(portId).getConfigurationSetting().equals(CPort.BLOCKED)){
                        activateFlag = activateFlag
                                && (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForActivate(task, "OUTPUT", portId));
                    }
                    if(this.OutputPorts.get(portId).getDefaultValue() != null){
                        if(this.OutputPorts.get(portId).getDefaultValue().equals(CPort.BLOCKED)){
                            defaultBlockFlag = defaultBlockFlag && (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForBlock(task, "OUTPUT", portId));
                        } else if (this.OutputPorts.get(portId).getDefaultValue().equals(CPort.ACTIVATED)){
                            defaultActivateFlag = defaultActivateFlag &&
                                    (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForActivate(task, "OUTPUT", portId));
                        }
                    }
                }
                s = (String) outputPortsConfigurationTable.getValueAt(selectedRows[i], 2);

            }
            this.DefaultButton.setEnabled(DefaultButtonFlag && defaultBlockFlag && defaultActivateFlag);
            this.BlockButton.setEnabled(blockFlag && ! matchText(s, "blocked"));
            this.ActivateButton.setEnabled(activateFlag && ! matchText(s, "activated"));
        }

        private boolean matchText(String s, String other) {
            return (s != null) && (s.equals(other));
        }


        private void toggleEnabled(JButton btn) {
            ActivateButton.setEnabled(btn != ActivateButton);
            BlockButton.setEnabled(btn != BlockButton);
        }


        // Variables declaration - do not modify
        private JButton ActivateButton;
        private JButton BlockButton;
        private JButton DefaultButton;
        private JButton SetDefaultButton;
        private JTable outputPortsConfigurationTable;
        private JLabel jLabel1;
        private JScrollPane jScrollPane1;

    }

    private class SetOutputPortDefaultConfigurationJDialog extends JDialog {

        private final NetGraph net;
        private final YAWLTask task;
        private final Object[][] rowInfor;
        private List<CPort> outputPorts;

        public void  initOutputPorts(){
            TaskConfiguration taskConfiguration = TaskConfigurationCache.getInstance()
                   .get(net.getNetModel(), task);
            this.outputPorts = taskConfiguration.getOutputCPorts();
            System.out.println(this.outputPorts.size());
        }
        public Object[][] getPortsInformation(){
            int size = this.outputPorts.size();
            Object[][] rowInfor = new Object[size ][3];
            for(int i=0; i<size ; i++){
                rowInfor[i][0] = this.outputPorts.get(i).getID();
                rowInfor[i][1] = this.outputPorts.get(i).getTargetTasksLabels();
                rowInfor[i][2] = this.outputPorts.get(i).getDefaultValue();
            }
            return rowInfor;
        }


        /** Creates new form NewJDialog  */
        public SetOutputPortDefaultConfigurationJDialog(Frame parent, NetGraph net, YAWLTask task) {
            super(parent, true);
            this.net = net;
            this.task = task;
            initOutputPorts();
            this.rowInfor = this.getPortsInformation();
            initComponents();

        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jScrollPane1 = new JScrollPane();
            jTable1 = new JTable();
            AllowButton = new JButton();
            BlockButton = new JButton();
            EmptyButton = new JButton();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.setTitle("Output Ports Default Configuration Setting");

            jTable1.setModel(new javax.swing.table.DefaultTableModel(
                    rowInfor,
                    new String [] {
                            "Port ID", "Target Nodes", "Default Configuration"
                    }
            ) {
                final Class[] types = new Class [] {
                        Integer.class, String.class, String.class
                };
                final boolean[] canEdit = new boolean [] {
                        false, false, false
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });

            jTable1.getColumnModel().getColumn(0).setCellRenderer(new PortIDRenderer());
            jTable1.getColumnModel().getColumn(2).setCellRenderer(new ConfigurationTableCellRenderer());
            jScrollPane1.setViewportView(jTable1);
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(15);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(50);
            jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    String s = (String) jTable1.getValueAt(jTable1.getSelectedRow(), 2);
                    AllowButton.setEnabled(! matchText(s, "activated"));
                    BlockButton.setEnabled(! matchText(s, "blocked"));
                }
            });


            AllowButton.setText("Activate");
            AllowButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    AllowButtonActionPerformed(evt);
                }
            });

            BlockButton.setText("Block ");
            BlockButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    BlockButtonActionPerformed(evt);
                }
            });

            EmptyButton.setText("No Default");
            EmptyButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    EmptyButtonActionPerformed(evt);
                }
            });

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(AllowButton)
                                                    .addGap(88, 88, 88)
                                                    .addComponent(BlockButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                                                    .addComponent(EmptyButton))
                                            .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
                                    .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(33, 33, 33)
                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 176, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(EmptyButton)
                                            .addComponent(AllowButton)
                                            .addComponent(BlockButton))
                                    .addGap(20, 20, 20))
            );

            this.jScrollPane1.setAutoscrolls(true);
            this.jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            pack();
        }// </editor-fold>

        private void AllowButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.outputPorts.get(portId).setDefaultValue(CPort.ACTIVATED);
                this.jTable1.setValueAt("activated", selectedRows[i], 2);
            }
            toggleEnabled(AllowButton);
        }

        private void BlockButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.outputPorts.get(portId).setDefaultValue(CPort.BLOCKED);
                this.jTable1.setValueAt(CPort.BLOCKED, selectedRows[i], 2);
            }
            toggleEnabled(BlockButton);
        }

        private void EmptyButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.outputPorts.get(portId).setDefaultValue(null);
                this.jTable1.setValueAt(null, selectedRows[i], 2);
            }
            toggleEnabled(EmptyButton);
        }

        private boolean matchText(String s, String other) {
            return (s != null) && (s.equals(other));
        }


        private void toggleEnabled(JButton btn) {
            AllowButton.setEnabled(btn != AllowButton);
            BlockButton.setEnabled(btn != BlockButton);
        }

        // Variables declaration - do not modify
        private JButton AllowButton;
        private JButton BlockButton;
        private JButton EmptyButton;
        private JScrollPane jScrollPane1;
        private JTable jTable1;

        // End of variables declaration

    }

}
