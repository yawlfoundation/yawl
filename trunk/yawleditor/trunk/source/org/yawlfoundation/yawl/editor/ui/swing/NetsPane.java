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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class NetsPane extends JTabbedPane implements ChangeListener {


    public NetsPane() {
        super();
        addChangeListener(this);
    }


    public YAWLEditorNetPanel newNet(boolean select) {
        try {
            YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds());
            bindFrame(frame, select);
            NetGraph graph = frame.getNet();
            graph.getSelectionListener().publishState(graph.getSelectionModel(), null);

            return frame;
        }
        catch (Exception e) {
            return null;
        }
    }


    public void openNet(NetGraph graph) {
        YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds(), graph);
        bindFrame(frame, true);
        graph.getSelectionListener().publishState(graph.getSelectionModel(), null);
    }


    public void removeActiveNet() {
        YAWLEditorNetPanel frame = (YAWLEditorNetPanel) getSelectedComponent();
        if ((frame != null) && (! frame.getNet().getNetModel().isRootNet())) {
            if (removeNetConfirmed()) {
                frame.removeFromSpecification();
                remove(frame);
            }
        }
    }


    public void closeAllNets() {
        Component[] frames = getComponents();

        for (Component frame : frames) {
            ((YAWLEditorNetPanel) frame).resetFrame();
            remove(frame);
        }
    }

    public NetGraph getSelectedGraph() {
        YAWLEditorNetPanel frame = (YAWLEditorNetPanel) this.getSelectedComponent();
        return frame != null ? frame.getNet() : null;
    }

    public YNet getSelectedYNet() {
        return (YNet) getSelectedGraph().getNetModel().getDecomposition();
    }

    public void stateChanged(ChangeEvent e) {
        updateState(true);
    }

    private void bindFrame(final YAWLEditorNetPanel frame, boolean select) {
        int tabIndex = getInsertionIndex(frame);
        insertTab(frame.getTitle(), frame.getFrameIcon(), frame, null, tabIndex);
        if (select) setSelectedIndex(tabIndex);
        updateState(select);
    }



    private void updateState(boolean select) {
        YAWLEditorNetPanel frame = (YAWLEditorNetPanel) this.getSelectedComponent();
        Publisher publisher = Publisher.getInstance();
        if ((frame == null) || (frame.getNet() == null)) {
            if (publisher.getSpecificationState() != SpecificationState.NoNetsExist) {
                publisher.publishState(SpecificationState.NoNetSelected);
            }
            return;
        }
        publisher.publishState(SpecificationState.NetSelected);
        if (select) {
            SpecificationModel.getInstance().getPropertiesLoader().setGraph(frame.getNet());
        }
        try {
            getSelectedGraph().getSelectionListener().forceActionUpdate();
        //    getSelectedGraph().getCancellationSetModel().refresh();
        }
        catch (Exception e) {}
    }


    private boolean removeNetConfirmed() {
        Object[] choices = {"Remove Net", "Cancel"};
        int selection = JOptionPane.showOptionDialog(this,
                "This will permanently remove the selected Net from the\n" +
                        "Specification and cannot be undone. Are you sure?",
                "Remove Selected Net", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, choices, choices[1]);

        return selection == 0 ;
    }


    private int getInsertionIndex(YAWLEditorNetPanel frame) {
        int i = 0;
        if (! frame.containsRootNet()) {                      // root net always first
            for (i = 1; i < getTabCount(); i++) {
                if (getTitleAt(i).compareToIgnoreCase(frame.getTitle()) > 0) break;
            }
        }
        return i;
    }


}