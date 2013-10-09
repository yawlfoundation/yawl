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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import java.awt.*;
import java.util.HashMap;

/**
 * @author bradforl
 */
public class VertexLabel extends DefaultGraphCell {

    private YAWLVertex vertex;
    private String     label;


    public VertexLabel(YAWLVertex vertex, String label) {
        initialize(vertex, label);
        setFont(UserSettings.getDefaultFont());
        setForeground(UserSettings.getDefaultTextColour());
    }

    public void setForeground(Color foreColor) {
        HashMap map = new HashMap();
        GraphConstants.setForeground(map, foreColor);
        getAttributes().applyMap(map);
    }

    public Font getFont() {
        return GraphConstants.getFont(getAttributes());
    }

    public Color getForeground() {
        return GraphConstants.getForeground(getAttributes());
    }

    public void setFont(Font font) {
        HashMap map = new HashMap();
        GraphConstants.setFont(map, font);
        getAttributes().applyMap(map);
    }

    public void refreshLabelView() {
        setUserObject("<html><body style=\"width:" + vertex.getBounds().getWidth() * 3 + "\" align=\"center\">" + label + "</body></html>");
    }


    public void setLabel(String label) {
        this.label = label;
        if (label != null && vertex != null) {
            refreshLabelView();
        } else {
            setUserObject(null);
        }
    }

    public String getText() {
        return this.label;
    }

    private void initialize(YAWLVertex vertex, String label) {
        buildElement();
        setVertex(vertex);
        setLabel(label);
    }

    private void buildElement() {
        HashMap map = new HashMap();

        GraphConstants.setOpaque(map, false);
        GraphConstants.setAutoSize(map, true);
        GraphConstants.setForeground(map, Color.BLACK);
        GraphConstants.setResize(map,false);

        getAttributes().applyMap(map);
    }

    public YAWLVertex getVertex() {
        return vertex;
    }

    public void setVertex(YAWLVertex vertex) {
        this.vertex = vertex;
    }
}
