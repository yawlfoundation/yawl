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

package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/06/12
 */
public class YTaskLayout extends YNetElementNode {

    private YDecoratorLayout _split;
    private YDecoratorLayout _join;
    private String _icon;

    public YTaskLayout(YTask task, NumberFormat formatter) {
        setID(task.getID());
        setNumberFormatter(formatter);
    }


    public YDecoratorLayout getSplitLayout() { return _split; }

    public void setSplitLayout(YDecoratorLayout layout) { _split = layout; }

    public boolean hasSplitLayout() { return _split != null; }


    public YDecoratorLayout getJoinLayout() { return _join; }

    public void setJoinLayout(YDecoratorLayout layout) { _join = layout; }

    public boolean hasJoinLayout() { return _join != null; }


    public Set<YDecoratorLayout> getDecorators() {
        if (hasDecorator()) {
            Set<YDecoratorLayout> decorators = new HashSet<YDecoratorLayout>(2);
            if (hasSplitLayout()) decorators.add(_split);
            if (hasJoinLayout()) decorators.add(_join);
            return decorators;
        }
        return Collections.emptySet();
    }


    public String getIconPath() { return _icon; }

    public void setIconPath(String icon) { _icon = icon;}


    public boolean hasDecorator() { return hasJoinLayout() || hasSplitLayout(); }

    public boolean isContainer() { return hasLabel() || hasDecorator(); }


    public YDecoratorLayout newDecoratorLayoutInstance() {
        return new YDecoratorLayout(this, getNumberFormatter());
    }

    public void addDecoratorLayout(YDecoratorLayout decoratorLayout) {
        if (decoratorLayout.isJoin()) {
            _join = decoratorLayout;
        }
        else {
            _split = decoratorLayout;
        }
    }


    public ImageIcon getIcon() {
        if (getIconPath() == null) return null;

        try {
            return new ImageIcon(YLayoutUtil.loadIcon(getIconPath()));
        }
        catch (Exception e) {
            return null;
        }
    }


    protected void parseVertex(XNode node) {
        if (node != null) {
            super.parseVertex(node);
            parseIconPath(node);
        }
    }


    protected void parseContainer(XNode node) {
        super.parseContainer(node);
        for (XNode decoratorNode : node.getChildren("decorator")) {
            parseDecorator(decoratorNode);
        }
    }


    private void parseDecorator(XNode node) {
        if (node == null) return;

        YDecoratorLayout decoratorLayout = new YDecoratorLayout(this, getNumberFormatter());
        decoratorLayout.parse(node);
        addDecoratorLayout(decoratorLayout);
    }


    private void parseIconPath(XNode node) {
        String path = node.getChildText("iconpath");
        if (path != null) {
            if (path.contains("yawl/editor/resources")) {
                path = path.replace("yawl/editor/resources", "yawl/editor/ui/resources");
            }
            setIconPath(path);
        }
    }

    protected XNode toVertexNode(boolean contained) {
        XNode node = super.toVertexNode(contained);
        if (_icon != null) node.insertChild(0, new XNode("iconpath", _icon));
        return node;
    }


    protected XNode toContainerNode() {
        XNode node = super.toContainerNode();
        if (hasSplitLayout()) node.addChild(_split.toXNode());
        if (hasJoinLayout()) node.addChild(_join.toXNode());
        return node;
    }

}
