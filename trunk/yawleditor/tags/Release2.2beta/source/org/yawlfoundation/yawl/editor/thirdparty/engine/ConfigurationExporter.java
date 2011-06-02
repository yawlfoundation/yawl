package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.util.XNode;

import java.util.List;

public class ConfigurationExporter extends DefaultConfigurationExporter {

    public ConfigurationExporter() {
        super();
    }


    public String getTaskConfiguration(YAWLTask task) {
        XNode configNode = new XNode("configuration");
        configNode.addChild(getPortNodes(Direction.input, task.getInputCPorts()));
        if (task instanceof YAWLMultipleInstanceTask) {
            configNode.addChild(getMITaskConfig((YAWLMultipleInstanceTask) task));
        }
        if (task.hasCancellationSetMembers()) {
            XNode rem = configNode.addChild("rem");
            String value = task.isCancellationSetEnable() ? "activated" : "blocked";
            rem.addAttribute("value", value);
        }
        configNode.addChild(getPortNodes(Direction.output, task.getOutputCPorts()));
        return configNode.toString();
    }

    protected XNode getPortNodes(Direction dir, List<CPort> ports) {
        String name = (dir == Direction.input) ? "join" : "split";
        XNode node = new XNode(name);
        for (CPort port : ports) {
            if (port.getConfigurationSetting() != null) {
                node.addChild(getPortConfig(dir, port, port.getConfigurationSetting()));
            }
        }
        return node;
    }


    private XNode getMITaskConfig(YAWLMultipleInstanceTask task) {
        XNode miNode = new XNode("nofi");
        MultipleInstanceTaskConfigSet config = task.getConfigurationInfor();
        miNode.addChild("minIncrease", config.getIncreaseMin());
        miNode.addChild("maxDecrease", config.getReduceMax());
        miNode.addChild("thresIncrease", config.getIncreaseThreshold());
        miNode.addChild("creationMode", config.isForbidDynamic() ? "restict": "keep");
        return miNode;
    }
}
