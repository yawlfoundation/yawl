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

package org.yawlfoundation.yawl.editor.ui.resourcing.panel;

import org.yawlfoundation.yawl.editor.core.resourcing.BasicOfferInteraction;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceDialog;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * @author Michael Adams
 * @date 23/05/13
 */
public class ConstraintsPanel extends JPanel implements ItemListener {

    private JCheckBox chkFourEyes;
    private JComboBox cbxFourEyes;
    private JCheckBox chkFamTask;
    private JComboBox cbxFamTask;

    private static final String FOUR_EYES_NAME = "SeparationOfDuties";
    private static final String FOUR_EYES_PARAM_NAME = "familiarTask";


    public ConstraintsPanel(ResourceDialog owner, Set<YAtomicTask> preTasks) {
        setBorder(new TitledBorder("Constraints"));
        addContent(owner, preTasks);
     //   setPreferredSize(new Dimension(425, 145));
    }


    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (source == chkFourEyes) {
            enableCombo(cbxFourEyes, selected);
        }
        else if (source == chkFamTask) {
            enableCombo(cbxFamTask, selected);
        }
    }


    public void enableCombos() {
        enableCombo(cbxFourEyes, chkFourEyes.isSelected());
        enableCombo(cbxFamTask, chkFamTask.isSelected());
    }


    public void load(BasicOfferInteraction offerInteraction) {
        String famTask = offerInteraction.getFamiliarParticipantTask();
        if (famTask != null) {
            chkFamTask.setSelected(selectItem(cbxFamTask, famTask));
            if (! chkFamTask.isSelected()) {
                showMissingTaskWarning("familiar task", famTask);
            }
        }
        enableCombo(cbxFamTask, chkFamTask.isSelected());

        for (AbstractConstraint constraint : offerInteraction.getConstraintSet().getAll()) {
            if (constraint.getName().equals(FOUR_EYES_NAME)) {
                famTask = constraint.getParamValue(FOUR_EYES_PARAM_NAME);
                if (famTask != null) {
                    chkFourEyes.setSelected(selectItem(cbxFourEyes, famTask));
                    if (! chkFourEyes.isSelected()) {
                        showMissingTaskWarning("separation of duties", famTask);
                    }
                }
            }
        }
        enableCombo(cbxFourEyes, chkFourEyes.isSelected());

        disableIfNoPresetTasks();
    }


    public void save(BasicOfferInteraction offerInteraction) {
        String famTask = null;
        if (chkFamTask.isSelected()) {
            famTask = getSelectedTaskID(cbxFamTask);
        }
        offerInteraction.setFamiliarParticipantTask(famTask);

        offerInteraction.getConstraintSet().clear();
        String fourEyesTask = null;
        if (chkFourEyes.isSelected()) {
            fourEyesTask = getSelectedTaskID(cbxFourEyes);
            if (fourEyesTask != null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put(FOUR_EYES_PARAM_NAME, fourEyesTask);
                offerInteraction.getConstraintSet().add(FOUR_EYES_NAME, params);
            }
        }
        if (famTask != null && famTask.equals(fourEyesTask)) {
            showClashWarning(famTask);
        }
    }


    private void addContent(ResourceDialog owner, Set<YAtomicTask> preTasks) {
        setLayout(new GridLayout(0,1));
        add(createFamTaskPanel(owner, preTasks));
        add(createFourEyesPanel(owner, preTasks));
    }


    private JPanel createFamTaskPanel(ResourceDialog owner, Set<YAtomicTask> preTasks) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(34, 5, 22, 5));
        chkFamTask = createCheckBox("Choose completer(s) of task:", owner);
        cbxFamTask = createCombo(owner, preTasks);
        panel.add(chkFamTask, BorderLayout.CENTER);
        panel.add(cbxFamTask, BorderLayout.EAST);
    //    panel.setSize(410, 25);
        return panel;
    }


    private JPanel createFourEyesPanel(ResourceDialog owner, Set<YAtomicTask> preTasks) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(7, 5, 50, 5));
        chkFourEyes = createCheckBox("Do not choose completer(s) of task:", owner);
        cbxFourEyes = createCombo(owner, preTasks);
        panel.add(chkFourEyes, BorderLayout.CENTER);
        panel.add(cbxFourEyes, BorderLayout.EAST);
    //    panel.setSize(410, 25);
        return panel;
    }


    private JComboBox createCombo(ResourceDialog owner, Set<YAtomicTask> preTasks) {
        JComboBox combo = new JComboBox();
        combo.setPreferredSize(new Dimension(250, 25));
        combo.addActionListener(owner);
        addItems(combo, preTasks);
        combo.setEnabled(false);
        return combo;
    }


    private JCheckBox createCheckBox(String caption, ResourceDialog owner) {
        JCheckBox box = new JCheckBox(caption);
        box.addItemListener(this);
        box.addItemListener(owner);
        return box;
    }


    private void addItems(JComboBox combo, Set<YAtomicTask> preTasks) {
        String currentNetID = YAWLEditor.getNetsPane().getSelectedYNet().getID();
        java.util.List<String> items = new ArrayList<String>();
        for (YAtomicTask preTask : preTasks) {
            String taskNetID = preTask.getNet().getID();
            String suffix = taskNetID.equals(currentNetID) ? "" :
                    " (" + preTask.getNet().getID() + ")";
            items.add(preTask.getID() + suffix);
        }
        Collections.sort(items);
        for (String item : items) combo.addItem(item);
    }


    private boolean selectItem(JComboBox combo, String item) {
        for (int i=0; i < combo.getItemCount(); i++) {
            String taskID = StringUtil.firstWord((String) combo.getItemAt(i));
            if (taskID != null && taskID.equals(item)) {
                combo.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }


    private void enableCombo(JComboBox combo, boolean enable) {
        combo.setEnabled(enable && combo.getItemCount() > 0);
        combo.setToolTipText(enable && ! combo.isEnabled() ?
            "Only those prior tasks with resourced decompositions are listed here" : null);
    }


    private String getSelectedTaskID(JComboBox combo) {
        String selection = (String) combo.getSelectedItem();
        if (selection != null) {
            if (selection.contains(" (")) {
                selection = selection.substring(0, selection.indexOf(" ("));
            }
            return selection;
        }
        return null;
    }


    private void disableIfNoPresetTasks() {
        if (cbxFamTask.getItemCount() == 0) {
            chkFamTask.setEnabled(false);
            cbxFamTask.setEnabled(false);
            chkFourEyes.setEnabled(false);
            cbxFourEyes.setEnabled(false);
        }
    }


    private void showClashWarning(String taskName) {
        showWarningDialog("Task Selection Clash",
            "Task '" + taskName + "' has been selected for both constraint options.\n" +
            "This will result in an empty distribution set at runtime, since\n" +
            "the constraints are mutually exclusive. If both constraints\n" +
            "are required, please ensure each refers to a different task.");
    }


    private void showMissingTaskWarning(String constraint, String taskName) {
        showWarningDialog("Invalid Task Selection",
            "The current specification has task '" + taskName + "' selected as\n" +
            "the parameter for a " + constraint + " constraint, but there is\n" +
            "no task of that name preceding the current task. The constraint has\n" +
            "been deselected.");
    }


    private void showWarningDialog(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

}
