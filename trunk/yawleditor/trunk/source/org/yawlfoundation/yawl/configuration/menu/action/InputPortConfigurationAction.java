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

public class InputPortConfigurationAction extends ProcessConfigurationAction
        implements TooltipTogglingWidget {


    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Input Ports...");
        putValue(Action.LONG_DESCRIPTION, "Configure the input ports for this task.");
        putValue(Action.SMALL_ICON, getMenuIcon("arrow_in"));

    }


    public InputPortConfigurationAction() {
        super();
    }

    public void actionPerformed(ActionEvent event) {

        final YAWLTask task = this.task;

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                InputConfigurationJDialog dialog = new InputConfigurationJDialog(
                        new JFrame(), task, net);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                });
                dialog.setMinimumSize(dialog.getSize());
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(YAWLEditor.getInstance());
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
        super.setEnabled(enable && taskConfiguration != null &&
                taskConfiguration.isConfigurable());
    }

    public String getDisabledTooltipText() {
        return null;
    }


    public String getEnabledTooltipText() {
        return "Configure the input ports for this task.";
    }

    /**
     *
     * @author jingxin
     */
    private class InputConfigurationJDialog extends JDialog {

        private List<CPort> inputPorts;
        private NetConfiguration netConfiguration;
        private TaskConfiguration taskConfiguration;

        private final YAWLTask task;
        private final NetGraph net;
        private final int size;
        private TableColumn column = null;
        private TableColumn IDcolumn = null;
        private final ActionEvent simulateEvent = new ActionEvent(this,1001,"Preview Process Configuration");
        private final PreviewConfigurationProcessAction simulateAction=
                PreviewConfigurationProcessAction.getInstance();

        /** Creates new form NewJDialog */
        public InputConfigurationJDialog(Frame parent, YAWLTask task, NetGraph net) {
            super(parent, true);
            this.task = task;
            this.net = net;
            netConfiguration = NetConfigurationCache.getInstance().get(net.getNetModel());
            taskConfiguration = TaskConfigurationCache.getInstance()
                    .get(net.getNetModel(), task);
            size = taskConfiguration.getInputCPorts().size();
            initInputPorts();
            initComponents();

        }

        public void  initInputPorts(){
            this.inputPorts = taskConfiguration.getInputCPorts();
        }
        public Object[][] getPortsInformation(){
            Object[][] rowInfor = new Object[size][3];


            for(int i=0; i<size ; i++){
                rowInfor[i][0] = this.inputPorts.get(i).getID();
                rowInfor[i][1] = this.inputPorts.get(i).getSourceTasksLabels();
                rowInfor[i][2] = this.inputPorts.get(i).getConfigurationSetting();
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
            InputPortsConfigurationTable = new JTable();
            AllowButton = new JButton();
            BlockButton = new JButton();
            HideButton = new JButton();
            DefaultButton = new JButton();
            SetDefaultButton = new JButton();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.setTitle("Input Ports Configuration");
            jLabel1.setFont(new Font("Tahoma", 0, 14));
            jLabel1.setText("Please select which ports you want to configure.");



            InputPortsConfigurationTable.setModel(new javax.swing.table.DefaultTableModel(
                    this.getPortsInformation(),
                    new String [] {
                            "Port ID", "Source Nodes",  "Configuration"
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
                column = InputPortsConfigurationTable.getColumnModel().getColumn(i);
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



            for(int i=0; i<this.InputPortsConfigurationTable.getRowCount(); i++){
                column.setCellRenderer(new ConfigurationTableCellRenderer());
            }
            IDcolumn.setCellRenderer(new PortIDRenderer());

            jScrollPane1.setViewportView(InputPortsConfigurationTable);

            AllowButton.setText("Activate");
            AllowButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    AllowButtonActionPerformed(evt);
                }
            });

            BlockButton.setText("Block");
            BlockButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    BlockButtonActionPerformed(evt);
                }
            });

            HideButton.setText("Hide");
            HideButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    HideButtonActionPerformed(evt);
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
                                                    .addComponent(AllowButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(BlockButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(HideButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(DefaultButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                                                    .addComponent(SetDefaultButton))
                                            .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                    395, Short.MAX_VALUE))
                                    .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(33, 33, 33)
                                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 176, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(SetDefaultButton)
                                            .addComponent(AllowButton)
                                            .addComponent(BlockButton)
                                            .addComponent(HideButton)
                                            .addComponent(DefaultButton))
                                    .addGap(20, 20, 20))
            );

            if(netConfiguration.getSettings().isAllowBlockingInputPorts()){
                this.BlockButton.setEnabled(true);
            }else{
                this.BlockButton.setEnabled(false);
            }
            if(netConfiguration.getSettings().isAllowChangingDefaultConfiguration()){
                this.SetDefaultButton.setEnabled(true);
            }else{
                this.SetDefaultButton.setEnabled(false);
            }

            this.InputPortsConfigurationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            this.InputPortsConfigurationTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    jTableMouseReleased(evt);
                }
            });
            pack();
        }// </editor-fold>

        private void jTableMouseReleased(java.awt.event.MouseEvent evt) {
            boolean defaultFlag = false;
            boolean defaultBlockFlag = true;
            boolean defaultActivateFlag = true;
            boolean blockFlag = true;
            boolean activateFlag = true;
            String s = null;
            int length = this.InputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.InputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.InputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
                if(this.inputPorts.get(portId).getDefaultValue() != null){
                    defaultFlag = true;
                }
                if(netConfiguration.getServiceAutonomous() != null){
                    blockFlag = blockFlag && (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForBlock(task, "INPUT", portId));
                    if(this.inputPorts.get(portId).getConfigurationSetting().equals(CPort.BLOCKED)){
                        activateFlag = activateFlag &&
                                (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForActivate(task, "INPUT", portId));
                    }

                    if(this.inputPorts.get(portId).getDefaultValue() != null){
                        if(this.inputPorts.get(portId).getDefaultValue().equals(CPort.BLOCKED)){
                            defaultBlockFlag = defaultBlockFlag && (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForBlock(task, "INPUT", portId));
                        } else if (this.inputPorts.get(portId).getDefaultValue().equals(CPort.ACTIVATED)){
                            defaultActivateFlag = defaultActivateFlag &&
                                    (netConfiguration.getServiceAutonomous().processCorrectnessCheckingForActivate(task, "INPUT", portId));
                        }
                    }

                }
                s = (String) InputPortsConfigurationTable.getValueAt(selectedRows[i], 2);
            }
            this.DefaultButton.setEnabled(defaultFlag && defaultBlockFlag && defaultActivateFlag);
            this.BlockButton.setEnabled(blockFlag && ! matchText(s, "blocked"));
            this.AllowButton.setEnabled(activateFlag && ! matchText(s, "activated"));
            HideButton.setEnabled(! matchText(s, "hidden"));
        }

        private boolean matchText(String s, String other) {
            return (s != null) && (s.equals(other));
        }


        private void toggleEnabled(JButton btn) {
            AllowButton.setEnabled(btn != AllowButton);
            BlockButton.setEnabled(btn != BlockButton);
            HideButton.setEnabled(btn != HideButton);
        }

        private void AllowButtonActionPerformed(ActionEvent evt) {
            int length = this.InputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.InputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                ActivateAPort(selectedRows, i);
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
            toggleEnabled(AllowButton);
        }

        private void ActivateAPort(int[] selectedRows, int i) {
            int portId = (Integer) this.InputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
            this.inputPorts.get(portId).setConfigurationSetting("activated");
            this.InputPortsConfigurationTable.setValueAt(this.inputPorts.get(portId).getConfigurationSetting(), selectedRows[i], 2);
            if(netConfiguration.getServiceAutonomous() != null ){
                netConfiguration.getServiceAutonomous().changeCurrentStateAfterActivate(task, "INPUT", portId);
            }
        }

        private void BlockButtonActionPerformed(ActionEvent evt) {
            int length = this.InputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.InputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                BlockAPort(selectedRows, i);
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
            toggleEnabled(BlockButton);
        }

        private void BlockAPort(int[] selectedRows, int i) {
            int portId = (Integer) this.InputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
            this.inputPorts.get(portId).setConfigurationSetting("blocked");
            this.InputPortsConfigurationTable.setValueAt("blocked", selectedRows[i], 2);
            if(netConfiguration.getServiceAutonomous() != null ){
                netConfiguration.getServiceAutonomous().changeCurrentStateAfterBlock(task, "INPUT", portId);
            }
        }

        private void HideButtonActionPerformed(ActionEvent evt) {
            int length = this.InputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.InputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                HideAPort(selectedRows, i);
            }
            if(netConfiguration.getSettings().isApplyAutoGreyOut()){
                this.simulateAction.actionPerformed(simulateEvent);
            }
            toggleEnabled(HideButton);
        }

        private void HideAPort(int[] selectedRows, int i) {
            int portId = (Integer) this.InputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
            this.inputPorts.get(portId).setConfigurationSetting("hidden");
            this.InputPortsConfigurationTable.setValueAt("hidden", selectedRows[i], 2);
        }

        private void DefaultButtonActionPerformed(ActionEvent evt) {
            int length = this.InputPortsConfigurationTable.getSelectedRowCount();
            int[] selectedRows = this.InputPortsConfigurationTable.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.InputPortsConfigurationTable.getValueAt(selectedRows[i], 0);
                if(this.inputPorts.get(portId).getDefaultValue() != null){
                    if(this.inputPorts.get(portId).getDefaultValue().equals(CPort.ACTIVATED)){
                        ActivateAPort(selectedRows, i);
                    }else if(this.inputPorts.get(portId).getDefaultValue().equals(CPort.BLOCKED)){
                        BlockAPort(selectedRows, i);
                    }else if(this.inputPorts.get(portId).getDefaultValue().equals(CPort.HIDDEN)){
                        HideAPort(selectedRows, i);
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
                    SetInputPortDefaultConfigurationJDialog dialog = new SetInputPortDefaultConfigurationJDialog(new JFrame(), true, net, task);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    });
                    dialog.setLocationRelativeTo(YAWLEditor.getInstance());
                    dialog.setMinimumSize(dialog.getSize());
                    dialog.setVisible(true);
                }
            });
        }


        // Variables declaration - do not modify
        private JButton AllowButton;
        private JButton BlockButton;
        private JButton DefaultButton;
        private JButton HideButton;
        private JButton SetDefaultButton;
        private JTable InputPortsConfigurationTable;
        private JLabel jLabel1;
        private JScrollPane jScrollPane1;

    }

    private class SetInputPortDefaultConfigurationJDialog extends JDialog {

        private final NetGraph net;
        private final YAWLTask task;
        private final Object[][] rowInfor;
        private List<CPort> inputPorts;

        public void  initInputPorts(){
            TaskConfiguration taskConfiguration = TaskConfigurationCache.getInstance()
                                .get(net.getNetModel(), task);
            this.inputPorts = taskConfiguration.getInputCPorts();
            System.out.println(this.inputPorts.size());
        }
        public Object[][] getPortsInformation(){
            int size = this.inputPorts.size();
            Object[][] rowInfor = new Object[size ][3];
            for(int i=0; i<size ; i++){
                rowInfor[i][0] = this.inputPorts.get(i).getID();
                rowInfor[i][1] = this.inputPorts.get(i).getSourceTasksLabels();
                rowInfor[i][2] = this.inputPorts.get(i).getDefaultValue();
            }
            return rowInfor;
        }


        /** Creates new form NewJDialog */
        public SetInputPortDefaultConfigurationJDialog(Frame parent, boolean modal, NetGraph net, YAWLTask task) {
            super(parent, modal);
            this.net = net;
            this.task = task;
            initInputPorts();
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
            HideButton = new JButton();
            EmptyButton = new JButton();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.setTitle("Input Ports Default Configuration Setting");

            jTable1.setModel(new javax.swing.table.DefaultTableModel(
                    rowInfor,
                    new String [] {
                            "Port ID", "Source Nodes", "Default Configuration"
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
                    HideButton.setEnabled(! matchText(s, "hidden"));
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

            HideButton.setText("Hide");
            HideButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    HideButtonActionPerformed(evt);
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
                                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(AllowButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(BlockButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(HideButton)
                                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                                                    .addComponent(EmptyButton))
                                            .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                    395, Short.MAX_VALUE))
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
                                            .addComponent(BlockButton)
                                            .addComponent(HideButton))
                                    .addGap(20, 20, 20))
            );
            this.jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            pack();
        }// </editor-fold>

        private void AllowButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.inputPorts.get(portId).setDefaultValue(CPort.ACTIVATED);
                this.jTable1.setValueAt("activated", selectedRows[i], 2);
            }
            toggleEnabled(AllowButton);
        }

        private void BlockButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.inputPorts.get(portId).setDefaultValue(CPort.BLOCKED);
                this.jTable1.setValueAt(CPort.BLOCKED, selectedRows[i], 2);
            }
            toggleEnabled(BlockButton);
        }

        private void HideButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.inputPorts.get(portId).setDefaultValue(CPort.HIDDEN);
                this.jTable1.setValueAt(CPort.HIDDEN, selectedRows[i], 2);
            }
            toggleEnabled(HideButton);
        }

        private void EmptyButtonActionPerformed(ActionEvent evt) {
            int length = jTable1.getSelectedRowCount();
            int[] selectedRows = this.jTable1.getSelectedRows();
            for(int i=0; i<length; i++){
                int portId = (Integer) this.jTable1.getValueAt(selectedRows[i], 0);
                this.inputPorts.get(portId).setDefaultValue(null);
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
            HideButton.setEnabled(btn != HideButton);
        }


        // Variables declaration - do not modify
        private JButton AllowButton;
        private JButton BlockButton;
        private JButton HideButton;
        private JScrollPane jScrollPane1;
        private JTable jTable1;
        private JButton EmptyButton;
        // End of variables declaration

    }

}
