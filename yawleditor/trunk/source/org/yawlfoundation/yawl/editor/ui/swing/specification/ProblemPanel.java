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

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.editor.ui.swing.ProblemTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProblemPanel extends JPanel
        implements FileStateListener, ProblemListStateListener {

    private ProblemTable problemTable;
    private BottomPanel owner;
    private String title;


    public ProblemPanel(BottomPanel owner) {
        super();
        this.owner = owner;
        problemTable = new ProblemTable();
        buildContent();
        Publisher.getInstance().subscribe((FileStateListener) this);
        Publisher.getInstance().subscribe((ProblemListStateListener) this);
    }


    public void setProblemList(String title, List<String> problemList) {
        this.title = title;
        if (problemList.isEmpty()) {
            problemList.add("No problems reported.");
        }
        populateProblemTable(problemList);
    }


    public void specificationFileStateChange(FileState state) {
        if (state == FileState.Closed) problemTable.reset();
    }

    public String getTitle() {
        return title;
    }

    public void contentChange(ProblemListState state) {
        if (state == ProblemListState.Entries) {
            if (isVisible()) {
                repaint();
            }
            else {
                setVisible(true);
            }
            owner.selectProblemsTab();
        }
    }


    private void buildContent() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4,5,5,5));
        JScrollPane problemScrollPane = new JScrollPane(problemTable);
//        problemScrollPane.setPreferredSize(getPreferredSize());
        problemScrollPane.setBackground(Color.WHITE);
        add(problemScrollPane, BorderLayout.CENTER);
    }


    private void populateProblemTable(List<String> problemList) {
        problemTable.reset();
        if (! problemList.isEmpty()) {
            for (String problem : problemList) {
                if (problem != null) problemTable.addMessage(problem.trim());
            }
//            problemTable.setWidth();
        }
    }

}
