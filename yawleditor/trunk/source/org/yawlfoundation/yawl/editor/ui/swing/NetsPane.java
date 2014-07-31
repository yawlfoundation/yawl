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

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.properties.PropertiesLoader;
import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.ui.util.FileDrop;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;

public class NetsPane extends JTabbedPane implements ChangeListener {

    private final PropertiesLoader _propertiesLoader;

    public NetsPane() {
        super();
        _propertiesLoader = new PropertiesLoader();
        addFileDropListener();
        addChangeListener(this);
    }


    public YAWLEditorNetPanel newNet(boolean select, String name) {
        try {
            YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds(), name);
            bindFrame(frame, select);
            NetGraph graph = frame.getNet();
            graph.getSelectionListener().publishState(graph.getSelectionModel(), null);
            YPluginHandler.getInstance().netAdded(graph.getNetModel());
            return frame;
        }
        catch (Exception e) {
            return null;
        }
    }


    public void setVisible(boolean visible)  {
        if (visible && getTabCount() > 0) highlightSelectedTab();
        super.setVisible(visible);
    }


    public void openNet(NetGraph graph) {
        YAWLEditorNetPanel frame = new YAWLEditorNetPanel(getBounds(), graph);
        bindFrame(frame, true);
        graph.getSelectionListener().publishState(graph.getSelectionModel(), null);
    }


    public void removeActiveNet() throws YControlFlowHandlerException {
        YAWLEditorNetPanel frame = (YAWLEditorNetPanel) getSelectedComponent();
        if ((frame != null) && (! frame.getNet().getNetModel().isRootNet())) {
            if (removeNetConfirmed()) {
                frame.removeFromSpecification();
                remove(frame);
                YPluginHandler.getInstance().netRemoved(frame.getNet().getNetModel());
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

    public void setSelectedTab(String caption) {
        for (int i=0; i<getTabCount(); i++) {
            if (getTitleAt(i).equals(caption)) {
                setSelectedIndex(i);
                updateState(true);
                break;
            }
        }
    }

    public void renameTab(String oldCaption, String newCaption) {
        for (int i=0; i<getTabCount(); i++) {
            if (getTitleAt(i).equals(oldCaption)) {
                setTitleAt(i, newCaption);
                if (isVisible()) highlightSelectedTab();
                break;
            }
        }
    }

    public void resetRootNet() {
        YAWLEditorNetPanel rootNetFrame = null;
        for (int i=0; i < getTabCount(); i++) {
            YAWLEditorNetPanel panel = (YAWLEditorNetPanel) getComponentAt(i);
            NetGraphModel model = panel.getNet().getNetModel();
            if (model.isRootNet()) {
                rootNetFrame = panel;
                break;
            }
        }
        if (rootNetFrame != null) {
            setIconAt(0, NetUtilities.getSubNetIcon());
            remove(rootNetFrame);
            insertTab(rootNetFrame.getTitle(), NetUtilities.getRootNetIcon(),
                    rootNetFrame, null, 0);
            setSelectedIndex(0);
        }
    }

    public YNet getSelectedYNet() {
        return (YNet) getSelectedGraph().getNetModel().getDecomposition();
    }

    public void stateChanged(ChangeEvent e) {
        updateState(true);
    }

    private void bindFrame(final YAWLEditorNetPanel frame, boolean select) {
        int tabIndex = getInsertionIndex(frame);
        insertTab(frame.getTitle(), frame.getIcon(), frame, null, tabIndex);
        if (select) setSelectedIndex(tabIndex);
        updateState(select);
    }



    private void updateState(boolean select) {
        YAWLEditorNetPanel frame = (YAWLEditorNetPanel) this.getSelectedComponent();
        Publisher publisher = Publisher.getInstance();
        if ((frame == null) || (frame.getNet() == null)) {
            publisher.publishNoNetSelectedEvent();
        }
        else {
            publisher.publishNetSelectedEvent();
            if (select) {
                _propertiesLoader.setGraph(frame.getNet());
                if (isVisible()) highlightSelectedTab();
            }
            try {
                getSelectedGraph().getSelectionListener().forceActionUpdate();
            }
            catch (Exception e) {}
        }
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


    public void highlightSelectedTab() {
        try {
            for (int i = 0; i < getTabCount(); i++) {         // unhighlight all
                String caption = getTitleAt(i);
                if (caption.startsWith("<html>")) {
                    setTitleAt(i, resetCaption(caption));
                    break;
                }
            }
            int selectedIndex = getSelectedIndex();
            if (selectedIndex < getTabCount()) {           // try to avoid exception
                setTitleAt(selectedIndex, highlightCaption(getTitleAt(selectedIndex)));
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe) {
            // nothing to do - no side-effects
        }
    }


    private String resetCaption(String caption) {
        return caption.replaceAll("<\\w*>|</\\w*>", "");
    }


    private String highlightCaption(String caption) {
        return "<html><b>" + caption + "</b></html>";
    }


    private void addFileDropListener() {
        new FileDrop( this, new FileDrop.Listener() {
                public void filesDropped(File[] files) {
                    if (files != null && files.length > 0) {
                        String fileName = files[0].getAbsolutePath();
                        if (fileName.endsWith(".yawl")) {
                            FileOperations.open(fileName);
                        }
                        else LogWriter.warn("Invalid file format dragged onto editor");
                    }
                }
        });
    }

}