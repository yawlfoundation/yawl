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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLMultipleInstanceTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
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
        miNode.addChild("creationMode", config.isForbidDynamic() ? "restrict": "keep");
        return miNode;
    }
}
