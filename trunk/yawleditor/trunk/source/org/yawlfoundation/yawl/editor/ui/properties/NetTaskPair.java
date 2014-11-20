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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class NetTaskPair {

    private Set<YAWLVertex> _vertexSet;
    private YAWLTask _task;
    private NetGraph _graph;
    private YNet _net;
    private YDecomposition _decomposition;
    private String _simpleText;

    public NetTaskPair(YAWLTask task, NetGraph graph) {
        _task = task;
        _graph = graph;
    }

    public NetTaskPair(YNet net, YDecomposition decomposition, YAWLTask task) {
        _net = net;
        _decomposition = decomposition;
        _task = task;
        _simpleText = (_decomposition != null) ? getText(decomposition) : getText(net);
    }

    public NetTaskPair(YNet net, Set<YAWLVertex> vertexSet) {
        _net = net;
        _vertexSet = vertexSet;
        _simpleText = getText(net);
    }

    public YAWLTask getTask() { return _task; }

    public void setTask(YAWLTask task) { _task = task; }


    public Set<YAWLVertex> getVertexSet() { return _vertexSet; }

    public void setVertexSet(Set<YAWLVertex> set) { _vertexSet = set; }

    public boolean hasMultipleTasks() { return _vertexSet != null; }


    public NetGraph getGraph() { return _graph; }

    public void setGraph(NetGraph graph) { _graph = graph; }


    public YNet getNet() { return _net; }

    public void setNet(YNet net) { _net = net; }


    public YDecomposition getDecomposition() { return _decomposition; }

    public void setDecomposition(YDecomposition decomp) { _decomposition =  decomp; }


    public String getSimpleText() { return toString(); }

    public void setSimpleText(String text) { _simpleText = text; }


    public String toString() {
        return _simpleText != null ? _simpleText : "";
    }


    private String getText(YDecomposition decomposition) {
        StringBuilder s = new StringBuilder();

        // only count locals at the net level
        if (decomposition instanceof YNet) {
            int locals = ((YNet) decomposition).getLocalVariables().size();
            if (locals > 0) {
                s.append("Local(").append(locals).append(") ");
            }
        }
        s.append(getIOText(decomposition));
        return s.toString();
    }


    private String getIOText(YDecomposition decomposition) {
        Set<String> inputs = decomposition.getInputParameters().keySet();
        Set<String> outputs = decomposition.getOutputParameters().keySet();
        int io = 0;
        int i = inputs.size();
        int o = outputs.size();

        for (String input : inputs) if (outputs.contains(input)) io++;
        StringBuilder s = new StringBuilder();
        if (i - io > 0) s.append("In(").append(i - io).append(") ");
        if (io > 0) s.append("I/O(").append(io).append(") ");
        if (o - io > 0) s.append("Out(").append(o - io).append(")");
        return s.toString().trim();
    }

}

