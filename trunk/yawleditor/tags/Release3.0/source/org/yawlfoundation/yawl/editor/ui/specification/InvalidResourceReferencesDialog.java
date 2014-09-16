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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/07/13
 */
public class InvalidResourceReferencesDialog extends JDialog implements ActionListener {

    private static final Dimension DETAILS_ON = new Dimension(430, 400);
    private static final Dimension DETAILS_OFF = new Dimension(430, 170);

    private boolean detailsHidden = true;
    private JButton btnDetails;

    public InvalidResourceReferencesDialog(Set<InvalidReference> invalidSet) {
        super(YAWLEditor.getInstance());
        setTitle("Invalid Resource References Detected");
        initialise();
        add(getContent(invalidSet));
        pack();
        setSize(DETAILS_OFF);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("details")) {
            toggleDetails();
        }
        else {
            setVisible(false);
        }
    }

    private void initialise() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(YAWLEditor.getInstance());
    }


    private JPanel getContent(Set<InvalidReference> invalidSet) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createTopPanel(invalidSet.size()), BorderLayout.NORTH);
        panel.add(createDetailsPanel(invalidSet), BorderLayout.CENTER);
        panel.add(createButtonBar(), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel createTopPanel(int count) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getWarnIcon(), BorderLayout.WEST);
        panel.add(getMessage(count), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createDetailsPanel(Set<InvalidReference> invalidSet) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,5,5));

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(420, 200));
        panel.add(pane);

        for (InvalidReference reference : invalidSet) {
            textArea.append(reference.getMessage());
            textArea.append("\n");
        }

        return panel;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton());
        return panel;
     }


    private JButton createButton() {
        JButton button = new JButton("OK");
        button.setActionCommand("OK");
        button.setMnemonic("OK".charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


    private JPanel getWarnIcon() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10,10,10,10));
        JLabel label = new JLabel();
        label.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        panel.add(label);
        return panel;
    }

    private JPanel getMessage(int count) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10,10,10,10));
        StringBuilder s = new StringBuilder();
        s.append("<HTML>");
        s.append("The loaded specification contains ");
        s.append(count);
        s.append(" resource reference") ;
        s.append((count > 1) ? "s<BR>that do " : "<BR>that does ");
        s.append("not exist in the connected Resource Service.");
        s.append("<BR><BR>These references will be discarded on next save.");
        s.append("</HTML>");
        panel.add(new JLabel(s.toString()), BorderLayout.CENTER);

        btnDetails = new JButton();
        btnDetails.setText(getDetailsButtonText());
        btnDetails.setHorizontalAlignment(SwingConstants.LEFT);
        btnDetails.setBorderPainted(false);
        btnDetails.setOpaque(false);
        btnDetails.setFocusPainted(false);
        btnDetails.setActionCommand("details");
        btnDetails.addActionListener(this);
        panel.add(btnDetails, BorderLayout.SOUTH);
        return panel;
    }

    private String getDetailsButtonText() {
        StringBuilder s = new StringBuilder(50);
        s.append("<HTML><FONT color=\"#000099\"><U>");
        s.append(detailsHidden ? "Show" : "Hide");
        s.append(" Details</U></FONT></HTML >");
        return s.toString();
    }

    private void toggleDetails() {
        setSize(detailsHidden ? DETAILS_ON : DETAILS_OFF);
        detailsHidden = ! detailsHidden;
        btnDetails.setText(getDetailsButtonText());
    }

}
