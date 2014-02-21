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

package org.yawlfoundation.yawl.editor.ui.swing.net;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationFactory;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.NetsPane;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class YAWLEditorNetPanel extends JPanel implements MouseWheelListener {

    private NetGraph net;
    private JScrollPane scrollPane;
    private String title;
    private ImageIcon icon;
    private boolean closable ;


    private YAWLEditorNetPanel() {
        super();
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setPreferredSize(getSize());
    }


    // creates a new net
    public YAWLEditorNetPanel(Rectangle bounds, String title) throws YControlFlowHandlerException {
        this();
        setBounds(bounds);
        if (title == null) title = createTitle();
        YNet yNet = SpecificationModel.getHandler().getControlFlowHandler().addNet(title);
        NetGraph newGraph = new NetGraph(yNet);
        new SpecificationFactory().populateGraph(yNet, newGraph);
        setNet(newGraph, title);
    }

    
    // opens an existing net (from file)
    public YAWLEditorNetPanel(Rectangle bounds, NetGraph graph) {
        this();
        setBounds(bounds);
        setNet(graph, graph.getName());
    }


    private String createTitle() {
        String newTitle = "";
        boolean validNameFound = false;
        int counter = 0;
        while (!validNameFound) {
            counter++;
            newTitle = "Net" + counter;
            if (SpecificationModel.getNets().getNetModelFromName(newTitle) == null) {
                validNameFound = true;
            }
        }
        return newTitle;
    }


    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isShiftDown()) {
            boolean wheelUp = (e.getWheelRotation() < 0);

            // Ctrl + Shift + Up -> scroll right; Ctrl + Shift + Down -> scroll left 
            if (e.isControlDown()) {
                JViewport viewport = scrollPane.getViewport();
                Point pt = viewport.getViewPosition();
                pt.x += wheelUp ? 10 : -10;
                pt.x = Math.max(0, pt.x);
                pt.x = Math.min(viewport.getView().getWidth() - viewport.getWidth(), pt.x);
                viewport.setViewPosition(pt);
            }
            else {

                // Shift + Up -> zoom in ; Shift + Down -> zoom out
                double scale = net.getScale();
                if (wheelUp) {
                    net.setScale(scale + 0.01);
                } else {
                    if (scale > 0.1) net.setScale(scale - 0.01);
                }
            }
        }
        else e.getComponent().getParent().dispatchEvent(e);        // def. pass through
    }

    public Rectangle getCurrentViewportBounds() {
        return scrollPane.getVisibleRect();
    }


    public void removeFromSpecification() {
        SpecificationModel.getNets().remove(getNet().getNetModel());
    }


    public void setNet(final NetGraph net, final String title) {
        this.net = net;
        net.setFrame(this);
        net.addMouseWheelListener(this);
        scrollPane = new JScrollPane();
        scrollPane.getViewport().add(net, BorderLayout.CENTER);
        add(scrollPane);

        if (title != null) {
            setTitle(title);
            setNetName(title);
        }
        SpecificationModel.getNets().add(getNet().getNetModel());
        icon = NetUtilities.getIconForNetModel(net.getNetModel());
    }

    public boolean containsRootNet() {
        return getNet().getNetModel().isRootNet();
    }

    public NetGraph getNet() {
        return net;
    }


    public void resetFrame() {
        remove(scrollPane);
        net.setFrame(null);
        getLayout().removeLayoutComponent(scrollPane);
        scrollPane = null;
        net = null;
    }


    public void setNetName(String title) {
        this.title = title;
        if (getNet() != null) {
            getNet().setName(title);
        }
    }


    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
        NetsPane parent = (NetsPane) this.getParent();
        if (parent != null) {
            int index = parent.indexOfComponent(this);
            if (index > -1) {
                parent.setTitleAt(index, title);
            }
        }
    }

    public boolean isValidNewDecompositionName(String name) {
        return ! (name == null || SpecificationModel.getHandler().getControlFlowHandler()
                .getDecompositionIds().contains(name));
    }



    public JScrollPane getScrollPane() {
        return scrollPane;
    }


    public ImageIcon getFrameIcon() { return icon; }

    public void setFrameIcon(ImageIcon i) { icon = i; }


    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    private Rectangle cropRectangle(Rectangle r, int crop) {
        return new Rectangle(r.x, r.y, r.width - crop, r.height - crop);
    }
}