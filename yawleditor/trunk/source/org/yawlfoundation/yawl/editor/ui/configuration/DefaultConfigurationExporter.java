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

package org.yawlfoundation.yawl.editor.ui.configuration;

import org.yawlfoundation.yawl.editor.ui.configuration.CPort;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.util.XNode;

import java.util.List;

public class DefaultConfigurationExporter {

    protected enum Direction { input, output }

    public DefaultConfigurationExporter() { }

    public String getTaskDefaultConfiguration(YAWLTask task) {
        if (task.hasDefaultInputPorts() || task.hasDefaultOutputPorts()) {
            XNode configNode = new XNode("defaultConfiguration");
            if (task.hasDefaultInputPorts()) {
                configNode.addChild(getPortNodes(Direction.input, task.getInputCPorts()));
            }
            if (task.hasDefaultOutputPorts()) {
                configNode.addChild(getPortNodes(Direction.output, task.getOutputCPorts()));
            }
            return configNode.toString();
        }
        else return null;
    }

    protected XNode getPortNodes(Direction dir, List<CPort> ports) {
        String name = (dir == Direction.input) ? "join" : "split";
        XNode node = new XNode(name);
        for (CPort port : ports) {
            if (port.getDefaultValue() != null) {
                node.addChild(getPortConfig(dir, port, port.getDefaultValue()));
            }
        }
        return node;
    }


    protected XNode getPortConfig(Direction dir, CPort port, String portValue) {
        String flowLabel = (dir == Direction.input) ? "flowSource" : "flowDestination";
        XNode portNode = new XNode("port");
        portNode.addAttribute("value", portValue);
        for (YAWLFlowRelation flow : port.getFlows()) {
            if (flow != null) {
                XNode flowNode = portNode.addChild(flowLabel);
                YAWLVertex vertex = (dir == Direction.input) ? flow.getSourceVertex() :
                        flow.getTargetVertex();
                flowNode.addAttribute("id", vertex.getID());
            }    
        }
        return portNode;
    }

}
