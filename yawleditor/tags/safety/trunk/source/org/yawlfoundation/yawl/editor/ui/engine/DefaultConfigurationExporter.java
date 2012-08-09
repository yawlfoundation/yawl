package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.ui.elements.model.CPort;
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
                flowNode.addAttribute("id", vertex.getEngineId());
            }    
        }
        return portNode;
    }

}
