/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Stores the layout information for a particular net
 *
 * @author Michael Adams
 * @date 7/06/12
 */
public class YNetLayout {

    private YNet _net;                                 // net this layout describes
    private String _cancellationTaskID;
    private Rectangle _bounds;
    private Rectangle _viewport;
    private Color _fillColor;
    private String _bgImagePath;
    private double _scale;
    private NumberFormat _nbrFormatter;
    private Map<String, YTaskLayout> _tasks;
    private Map<String, YConditionLayout> _conditions;
    private Map<String, YFlowLayout> _flows;


    // hidden constructor
    private YNetLayout() {
        _tasks = new Hashtable<String, YTaskLayout>();
        _conditions = new Hashtable<String, YConditionLayout>();
        _flows = new Hashtable<String, YFlowLayout>();
    }


    /**
     * Creates a new layout for a net
     * @param net the net that this layout describes
     * @param formatter a number format for this specific locale
     */
    public YNetLayout(YNet net, NumberFormat formatter) {
        this();
        _net = net;
        _nbrFormatter = formatter;
    }


    /**
     * Create a new task layout contained by this net. This method should only be used
     * when the actual YTask is not available to pass to the YTaskLayout constructor
     * @param id the id of the new task
     * @return an instantiated YTaskLayout instance
     */
    public YTaskLayout newTaskLayoutInstance(String id) {
        YTask genericTask = new YAtomicTask(id, YTask._AND, YTask._XOR, _net);
        return new YTaskLayout(genericTask, _nbrFormatter);
    }


    /**
     * Create a new condition layout contained by this net. This method should only be
     * used when the actual YCondition is not available to pass to the YConditionLayout
     * constructor
     * @param id the id of the new condition
     * @return an instantiated YCondition instance
     */
    public YConditionLayout newConditionLayoutInstance(String id) {
        return new YConditionLayout(new YCondition(id, _net), _nbrFormatter);
    }


    /**
     * Create a new flow layout contained by this net. This method should only be used
     * when the actual YFlow is not available to pass to the YFlowLayout constructor
     * @param sourceID the id of the flow's source task/condition
     * @param targetID the id of the flow's target task/condition
     * @return an instantiated YFlowLayout instance
     */
    public YFlowLayout newFlowLayoutInstance(String sourceID, String targetID) {
        YCondition genericSource = new YCondition(sourceID, _net);
        YCondition genericTarget = new YCondition(targetID, _net);
        return new YFlowLayout(genericSource, genericTarget, _nbrFormatter);
    }


    /**********************************************************************/

    public String getID() { return _net.getID(); }


    public YNet getNet() { return _net; }

    public void setNet(YNet net) { _net = net; }


    public Rectangle getBounds() { return _bounds; }

    public void setBounds(Rectangle bounds) { _bounds = bounds; }


    public Rectangle getViewport() { return _viewport; }

    public void setViewport(Rectangle frame) { _viewport = frame; }


    public Color getFillColor() { return _fillColor; }

    public void setFillColor(Color color) { _fillColor = color; }


    public String getBackgroundImagePath() { return _bgImagePath; }

    public void setBackgroundImagePath(String path) { _bgImagePath = path; }


    public ImageIcon getBackgroundImage() {
        if (_bgImagePath != null) {
            try {
                ImageIcon bgImage = new ImageIcon(YLayoutUtil.loadIcon(_bgImagePath));
                bgImage.setDescription(_bgImagePath);   // store path
                return bgImage;
            }
            catch (IOException ioe) {
                // fall through to below
            }
        }
        return null;
    }


    public double getScale() { return _scale; }

    public void setScale(double scale) { _scale = scale; }


    public String getCancellationTaskID() { return _cancellationTaskID; }

    public void setCancellationTaskID(String id) { _cancellationTaskID = id; }

    public YTask getCancellationTask() {
        return (YTask) _net.getNetElement(_cancellationTaskID);
    }


    /*********************************************************************/

    public Map<String, YTaskLayout> getTaskLayoutMap() { return _tasks; }

    public Set<YTaskLayout> getTaskLayouts() {
        return new HashSet<YTaskLayout>(_tasks.values());
    }

    public void setTaskLayouts(Map<String, YTaskLayout> tasks) { _tasks = tasks; }

    public void setTaskLayouts(Set<YTaskLayout> tasks) {
        if (tasks != null) {
            for (YTaskLayout layout : tasks) _tasks.put(layout.getID(), layout);
        }
    }

    public void addTaskLayout(YTaskLayout taskLayout) {
        _tasks.put(taskLayout.getID(), taskLayout);
    }

    public YTaskLayout getTaskLayout(String taskID) { return _tasks.get(taskID); }

    public YTaskLayout removeTaskLayout(String taskID) {return _tasks.remove(taskID); }


    /*********************************************************************/

    public Map<String, YConditionLayout> getConditionLayoutMap() { return _conditions; }

    public Set<YConditionLayout> getConditionLayouts() {
        return new HashSet<YConditionLayout>(_conditions.values());
    }

    public void setConditionLayouts(Map<String, YConditionLayout> conditions) {
        _conditions = conditions;
    }

    public void setConditionLayouts(Set<YConditionLayout> conditions) {
        if (conditions != null) {
            for (YConditionLayout layout : conditions) {
                _conditions.put(layout.getID(), layout);
            }
        }
    }

    public void addConditionLayout(YConditionLayout conditionLayout) {
        _conditions.put(conditionLayout.getID(), conditionLayout);
    }

    public YConditionLayout getConditionLayout(String conditionID) {
        return _conditions.get(conditionID);
    }

    public YConditionLayout removeConditionLayout(String conditionID) {
        return _conditions.remove(conditionID);
    }


    /*********************************************************************/

    /**
     * Gets the combined set of all task and condition layouts for this net
     * @return a combined set of task and condition layouts
     */
    public Set<YLayoutNode> getLayoutNodes() {
        Set<YLayoutNode> nodes = new HashSet<YLayoutNode>(_conditions.values());
        nodes.addAll(_tasks.values());
        return nodes;
    }


    /**
     * A convenience method to add a node of any kind (task, flow or condition)
     * @param layoutNode the layout node to add
     */
    public void addLayoutNode(YLayoutNode layoutNode) {
        if (layoutNode instanceof YTaskLayout) {
            addTaskLayout((YTaskLayout) layoutNode);
        }
        else if (layoutNode instanceof YConditionLayout) {
            addConditionLayout((YConditionLayout) layoutNode);
        }
        else if (layoutNode instanceof YFlowLayout) {
            addFlowLayout((YFlowLayout) layoutNode);
        }
    }

    /*********************************************************************/

    public Map<String, YFlowLayout> getFlowLayoutMap() { return _flows; }

    public Set<YFlowLayout> getFlows() {
        return new HashSet<YFlowLayout>(_flows.values());
    }

    public void setFlows(Map<String, YFlowLayout> flows) { _flows = flows; }

    public void setFlows(Set<YFlowLayout> flowLayouts) {
        if (flowLayouts != null) {
            for (YFlowLayout layout : flowLayouts) _flows.put(layout.getID(), layout);
        }
    }

    public void addFlowLayout(YFlowLayout flowLayout) {
        _flows.put(flowLayout.getID(), flowLayout);
    }

    public YFlowLayout getFlowLayout(String sourceID, String targetID) {
        return _flows.get(sourceID + "::" + targetID);
    }

    public YFlowLayout removeFlowLayout(String sourceID, String targetID) {
        return _flows.remove(sourceID + "::" + targetID);
    }


    /*********************************************************************/

    /**
     * Parses the layout content for this net, and populates its layout tree. This
     * method is called from this net's parent YLayout object.
     * @param netNode the XML node describing this net's layout
     * @throws YLayoutParseException
     */
    protected void parse(XNode netNode) throws YLayoutParseException {
        setBounds(YLayoutUtil.parseRect(netNode.getChild("bounds"), _nbrFormatter));
        setViewport(YLayoutUtil.parseRect(netNode.getChild("viewport"), _nbrFormatter));
        parseFillColor(netNode.getAttributeValue("bgColor"));
        parseBackgroundImage(netNode.getChild("bgImage"));
        parseScale(netNode.getChild("scale"));
        parseCancellationTask(netNode.getChild("cancellationtask"));

        for (XNode containerNode : netNode.getChildren("container")) {
            parseNetElement(containerNode);
        }

        for (XNode vertexNode : netNode.getChildren("vertex")) {
            parseNetElement(vertexNode);
        }

        for (XNode flowNode : netNode.getChildren("flow")) {
            parseFlow(flowNode);
        }
    }


    /**
     * Creates an XNode representation of this net's layout, to generate its XML
     * representation. This method is called from this net's parent YLayout object.
     * @return a populated XNode object
     */
    protected XNode toXNode() {
        XNode node = new XNode("net");
        node.addAttribute("id", _net.getID());
        if (! (_fillColor == null || _fillColor.equals(Color.WHITE))) {
            node.addAttribute("bgColor", _fillColor.getRGB());
        }
        node.addChild(YLayoutUtil.getRectNode("bounds", _bounds));
        node.addChild("frame");                   // required for schema, but never used
        node.addChild(YLayoutUtil.getRectNode("viewport", _viewport));

        if (_bgImagePath != null) {
            node.addChild("bgImage", _bgImagePath);
        }
        if (Math.abs(_scale - 1) > 0.01) {                   // allow for rounding error
            node.addChild("scale", _scale);
        }
        if (_cancellationTaskID != null) {
            node.addChild("cancellationtask", _cancellationTaskID);
        }
        for (YConditionLayout layout : _conditions.values()) {
            node.addChild(layout.toXNode());
        }
        for (YTaskLayout layout : _tasks.values()) {
            node.addChild(layout.toXNode());
        }
        for (YFlowLayout layout : _flows.values()) {
            node.addChild(layout.toXNode());
        }
        return node;
    }


    // parses the XML layout info for tasks and conditions
    private void parseNetElement(XNode node) throws YLayoutParseException {
        String id = node.getAttributeValue("id");
        if (id == null) {
            throw new YLayoutParseException(node.getName() + " node has null id " +
                    "in Net '" + _net.getID() + "' ");
        }
        YExternalNetElement element = _net.getNetElement(id);
        if (element == null) {
            throw new YLayoutParseException("No element with id=" + id +
                    " in Net '" + _net.getID() + "' ");
        }
        if (element instanceof YCondition) {
            YConditionLayout conditionLayout =
                    new YConditionLayout((YCondition) element, _nbrFormatter);
            conditionLayout.parse(node);
            _conditions.put(id, conditionLayout);
        }
        else if (element instanceof YTask) {
            YTaskLayout taskLayout = new YTaskLayout((YTask) element, _nbrFormatter);
            taskLayout.parse(node);
            _tasks.put(id, taskLayout);
        }
    }


    // parses the XML layout info for flows
    private void parseFlow(XNode node) throws YLayoutParseException {
        YExternalNetElement source =
                getFlowElement(node.getAttributeValue("source"), "source");
        YExternalNetElement target =
                getFlowElement(node.getAttributeValue("target"), "target");
        YFlowLayout flowLayout = new YFlowLayout(source, target, _nbrFormatter);
        flowLayout.parse(node);
        _flows.put(flowLayout.getID(), flowLayout);
    }


    // gets the source or target task/condition for a flow
    private YExternalNetElement getFlowElement(String id, String type)
            throws YLayoutParseException {
        if (id == null) {
            throw new YLayoutParseException(" Flow node has null " + type +
                    " value in Net '" + _net.getID() + "' ");
        }
        YExternalNetElement element = _net.getNetElement(id);
        if (element == null) {
            throw new YLayoutParseException("No element with id=" + id +
                    " for flow " + type + " value in Net '" + _net.getID() + "' ");
        }
        return element;
    }


    private void parseFillColor(String rgb) {
        _fillColor = (rgb != null) ? new Color(YLayoutUtil.strToInt(rgb)) :
                YLayout.DEFAULT_FILL_COLOR;
    }


    private void parseBackgroundImage(XNode imageNode) {
        if (imageNode != null) {
            _bgImagePath = imageNode.getText();
        }
    }


    private void parseScale(XNode scaleNode) {
        _scale = (scaleNode == null) ? 1.0 :
                StringUtil.strToDouble(scaleNode.getText(), 1.0);
    }


    private void parseCancellationTask(XNode cancelNode) {
        if (cancelNode != null) {
            _cancellationTaskID = cancelNode.getText();
        }
    }
}
