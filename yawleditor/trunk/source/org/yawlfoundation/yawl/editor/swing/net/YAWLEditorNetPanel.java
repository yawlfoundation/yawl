/*
 * Created on 26/01/2004, 18:22:08
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2004 Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.swing.net;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class YAWLEditorNetPanel extends JPanel implements MouseWheelListener {

    private static final long serialVersionUID = 1L;
    private static SpecificationModel model;
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
        model = SpecificationModel.getInstance() ;
    }


    // creates a new net
    public YAWLEditorNetPanel(Rectangle bounds) {
        this();
        setBounds(bounds);
        NetGraph newGraph = new NetGraph();
        newGraph.buildNewGraphContent(cropRectangle(bounds, 15));
        setNet(newGraph, createTitle());
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
            newTitle = "New Net " + counter;
            if (SpecificationUtilities.getNetModelFromName(model,newTitle) == null) {
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
        model.removeNet(getNet().getNetModel());
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
        model.addNet(getNet().getNetModel());
        icon = NetUtilities.getIconForNetModel(net.getNetModel());
    }

    public boolean containsRootNet() {
        return getNet().getNetModel().isStartingNet();
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
        YAWLEditorDesktop parent = (YAWLEditorDesktop) this.getParent();
        if (parent != null) {
            int index = parent.indexOfComponent(this);
            if (index > -1) {
                parent.setTitleAt(index, title);
            }
        }
    }

    public void showRenameDialog() {
        String oldName = getNet().getNetModel().getName();
        String newName = null;
        while(SpecificationUtilities.getNetModelFromName(SpecificationModel.getInstance(),newName) != getNet().getNetModel()) {
            newName = JOptionPane.showInputDialog(this,
                    "Change Net Name to:",
                    oldName);
            if (newName == null) {
                newName = oldName;
            }
            if (SpecificationModel.getInstance().isValidNewDecompositionName(newName)) {
                setNetName(newName);
            }
        }
    }


    public JScrollPane getScrollPane() {
        return scrollPane;
    }


    public ImageIcon getFrameIcon() { return icon;}

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