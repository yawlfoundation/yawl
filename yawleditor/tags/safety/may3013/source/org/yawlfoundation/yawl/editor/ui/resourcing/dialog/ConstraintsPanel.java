package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Michael Adams
 * @date 23/05/13
 */
public class ConstraintsPanel extends JPanel implements ItemListener {

    private JCheckBox chkFourEyes;
    private JComboBox cbxFourEyes;
    private JCheckBox chkFamTask;
    private JComboBox cbxFamTask;


    public ConstraintsPanel() {
        setBorder(new TitledBorder("Constraints"));
        addContent();
        setPreferredSize(new Dimension(425, 145));
    }


    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (source == chkFourEyes) {
            cbxFourEyes.setEnabled(selected);
        }
        else if (source == chkFamTask) {
            cbxFamTask.setEnabled(selected);
        }
    }



    private void addContent() {
        setLayout(new BorderLayout());
        add(createPiledPanel(), BorderLayout.NORTH);
        add(createFamTaskPanel(), BorderLayout.CENTER);
        add(createFourEyesPanel(), BorderLayout.SOUTH);
    }


    private JPanel createPiledPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 5, 8, 5));
        panel.add(new JCheckBox("Allow task to be piled"), BorderLayout.PAGE_START);
        panel.setSize(410, 25);
        return panel;
    }


    private JPanel createFamTaskPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(7, 5, 8, 5));
        chkFamTask = createCheckBox("Choose completer(s) of task:");
        cbxFamTask = createCombo();
        panel.add(chkFamTask, BorderLayout.CENTER);
        panel.add(cbxFamTask, BorderLayout.EAST);
        panel.setSize(410, 25);
        return panel;
    }


    private JPanel createFourEyesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(7, 5, 10, 5));
        chkFourEyes = createCheckBox("Do not choose completer(s) of task:");
        cbxFourEyes = createCombo();
        panel.add(chkFourEyes, BorderLayout.CENTER);
        panel.add(cbxFourEyes, BorderLayout.EAST);
        panel.setSize(410, 25);
        return panel;
    }


    private JComboBox createCombo() {
        JComboBox combo = new JComboBox();
        combo.setPreferredSize(new Dimension(175, 25));
        combo.setEnabled(false);
        return combo;
    }


    private JCheckBox createCheckBox(String caption) {
        JCheckBox box = new JCheckBox(caption);
        box.addItemListener(this);
        return box;
    }

}
