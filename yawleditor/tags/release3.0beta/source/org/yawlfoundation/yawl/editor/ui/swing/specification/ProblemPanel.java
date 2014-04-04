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

package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationMessage;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.MoreDialog;
import org.yawlfoundation.yawl.editor.ui.swing.ProblemTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProblemPanel extends JPanel implements FileStateListener, ListSelectionListener {

    private final ProblemTable problemTable;
    private final BottomPanel owner;
    private String title;


    public ProblemPanel(BottomPanel owner) {
        super();
        this.owner = owner;
        problemTable = new ProblemTable(this);
        buildContent();
        Publisher.getInstance().subscribe(this);
    }


    public void setProblemList(String title, List<ValidationMessage> problemList) {
        this.title = title + getTimestamp();
        if (problemList.isEmpty()) {
            problemList.add(new ValidationMessage("No problems reported."));
        }
        populateProblemTable(problemList);
        showTab();
    }


    public void specificationFileStateChange(FileState state) {
        if (state == FileState.Closed) problemTable.reset();
    }

    public void valueChanged(ListSelectionEvent event) {
        if (! event.getValueIsAdjusting()) {
            String longForm = problemTable.getLongMessageForSelectedRow();
            if (longForm != null) {
                MoreDialog dialog = new MoreDialog(problemTable, longForm);
                dialog.setLocationAdjacentTo(problemTable, problemTable.getCellRect(
                        problemTable.getSelectedRow(), 0, true));
                dialog.setVisible(true);
                problemTable.clearSelection();
            }
        }
    }


    public String getTitle() {
        return title;
    }

    public void showTab() {
        if (isVisible()) repaint();
        else setVisible(true);

        owner.selectProblemsTab();
    }


    private void buildContent() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4,5,5,5));
        JScrollPane problemScrollPane = new JScrollPane(problemTable);
        problemScrollPane.setBackground(Color.WHITE);
        add(problemScrollPane, BorderLayout.CENTER);
    }


    private void populateProblemTable(List<ValidationMessage> problemList) {
        problemTable.addMessages(problemList);
    }

    private String getTimestamp() {
        return new SimpleDateFormat(" (HH:mm)").format(new Date());
    }


}
