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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public abstract class YPropertiesBean {

    protected YSpecificationHandler specHandler;
    protected YControlFlowHandler flowHandler;
    protected NetGraph graph;
    protected NetGraphModel model;
    protected YPropertySheet propertySheet;

    public YPropertiesBean(YPropertySheet sheet) {
        propertySheet = sheet;
        specHandler = SpecificationModel.getHandler();
        flowHandler = specHandler.getControlFlowHandler();
    }


    protected void setGraph(NetGraph g) {
        graph = g;
        model = SpecificationModel.getNets().get(graph.getName());
    }


    protected NetGraph getGraph() { return graph; }

    protected NetGraphModel getModel() { return model; }

    public YPropertySheet getSheet() { return propertySheet; }


    protected YNet getSelectedYNet() {
        return YAWLEditor.getNetsPane().getSelectedYNet();
    }



    protected void setDirty() {
        SpecificationUndoManager.getInstance().setDirty(true);
    }

    protected void refreshCellView(GraphCell cell) {
        NetCellUtilities.applyViewChange(graph, graph.getViewFor(cell));
    }


    protected void setReadOnly(String propertyName, boolean isReadOnly) {
        getSheet().setReadOnly(propertyName, isReadOnly);
    }


    protected void firePropertyChange(String propertyName, Object newValue) {
        getSheet().firePropertyChange(propertyName, newValue);
    }


    protected void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message, title,
                JOptionPane.WARNING_MESSAGE);
    }


    protected Color hexToColor(String hexStr) {

        // expects the format #123456
        if ((hexStr == null) || (hexStr.length() < 7)) {
            return Color.WHITE;
        }

        try {
            int r = Integer.valueOf(hexStr.substring(1, 3), 16);
            int g = Integer.valueOf(hexStr.substring(3, 5), 16);
            int b = Integer.valueOf(hexStr.substring(5, 7), 16);
            return new Color(r, g, b);
        }
        catch (NumberFormatException nfe) {
            return Color.WHITE;
        }
    }

    protected String colorToHex(Color color) {
        String hex = "#";
        hex += intToHex(color.getRed());
        hex += intToHex(color.getGreen());
        hex += intToHex(color.getBlue());
        return hex;
    }

    private String intToHex(int i) {
        String hex = Integer.toHexString(i).toUpperCase();
        if (hex.length() == 1) hex = "0" + hex;
        return hex;
    }
}
