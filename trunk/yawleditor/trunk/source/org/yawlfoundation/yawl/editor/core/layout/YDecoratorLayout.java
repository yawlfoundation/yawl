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

import org.yawlfoundation.yawl.util.XNode;

import java.text.NumberFormat;

/**
 * Stores the layout information for a particular decorator of a task
 *
 * @author Michael Adams
 * @date 19/06/12
 */
public class YDecoratorLayout extends YLayoutNode {

    private YTaskLayout _owner;
    private DecoratorType _type;
    private DecoratorPosition _position;


    /**
     * Creates a new Decorator layout for a particular task. Note that this
     * constructor is called only via a YTaskLayout object.
     * @param taskLayout the parent task layout
     * @param formatter a number format for the specific locale
     */
    protected YDecoratorLayout(YTaskLayout taskLayout, NumberFormat formatter) {
        _owner = taskLayout;
        setNumberFormatter(formatter);
    }


    public DecoratorType getType() { return _type; }

    public void setType(DecoratorType type) { _type = type; }

    public void setType(String s) { _type = DecoratorType.fromString(s); }


    public boolean isSplit() { return _type.isSplit(); }

    public boolean isJoin() { return _type.isJoin(); }


    public YTaskLayout getParent() { return _owner; }


    public DecoratorPosition getPosition() { return _position; }

    public void setPosition(DecoratorPosition position) { _position = position; }

    public void setPosition(int p) { _position = DecoratorPosition.valueOf(p); }


    /**
     * Parse the XML node describing this decorator's layout. This method is called
     * from this decorator's parent YTaskLayout.
     * @param node the node containing the XML layout descriptors of this decorator
     */
    protected void parse(XNode node) {
        _type = DecoratorType.fromString(node.getAttributeValue("type"));
        _position = DecoratorPosition.valueOf(
                YLayoutUtil.strToInt(node.getChildText("position")));
        parseAttributes(node.getChild("attributes"));
    }


    /**
     * Creates an XNode representation of this decorator's layout, to generate its XML
     * representation. This method is called from this decorator's parent YTaskLayout.
     * @return a populated XNode object
     */
    protected XNode toXNode() {
        XNode node = new XNode("decorator");
        node.addAttribute("type", _type.toString());
        node.addChild("position", _position.getCardinality());
        node.addChild(getAttributesNode());
        return node;
    }

}
