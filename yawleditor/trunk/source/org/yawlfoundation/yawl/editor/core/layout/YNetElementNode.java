package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.util.XNode;

/**
 * The abstract base class of YTaskLayout and YConditionLayout
 *
 * @author Michael Adams
 * @date 22/06/12
 */
public abstract class YNetElementNode extends YLayoutNode {

    protected void parse(XNode node) {
        setID(node.getAttributeValue("id"));
        if (node.getName().equals("vertex")) {
            parseVertex(node);
        }
        else {
            parseContainer(node);
        }
    }

    protected XNode toXNode() {
        return isContainer() ? toContainerNode() : toVertexNode(false);
    }


    protected void parseVertex(XNode node) {
        if (node != null) {
            parseAttributes(node.getChild("attributes"));
            parseVertexDesignNotes(node);
        }
    }


    protected void parseContainer(XNode node) {
        parseVertex(node.getChild("vertex"));
        parseLabelBounds(node.getChild("label"));
    }


    protected XNode toVertexNode(boolean contained) {
        XNode node = new XNode("vertex");
        if (! contained) node.addAttribute("id", getID());
        node.addChild(getAttributesNode());
        if (hasDesignNotes()) node.addChild(getDesignNotesNode());
        return node;
    }


    protected XNode toContainerNode() {
        XNode node = new XNode("container");
        node.addAttribute("id", getID());
        node.addChild(toVertexNode(true));
        if (hasLabel()) node.addChild(getLabelNode());
        return node;
    }

}
