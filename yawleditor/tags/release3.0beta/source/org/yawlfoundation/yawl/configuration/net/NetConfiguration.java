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

package org.yawlfoundation.yawl.configuration.net;

import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class NetConfiguration {

    private ConfigurationSettingInfo configurationSettings;
    private ServiceAutomatonTree serviceAutomaton;
    private NetGraphModel graphModel;


    public NetConfiguration(NetGraphModel model) {
        graphModel = model;
        configurationSettings = new ConfigurationSettingInfo();
    }

    public ConfigurationSettingInfo getSettings() {
        return configurationSettings;
    }

    public void createServiceAutonomous() {
  	    serviceAutomaton = new ServiceAutomatonTree(graphModel.getGraph());
    }

    public void setServiceAutonomous(ServiceAutomatonTree tree) {
        serviceAutomaton = tree;
    }

    public ServiceAutomatonTree getServiceAutonomous() {
    	return serviceAutomaton;
    }


}
