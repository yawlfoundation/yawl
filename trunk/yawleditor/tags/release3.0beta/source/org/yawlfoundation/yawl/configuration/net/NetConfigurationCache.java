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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class NetConfigurationCache {

    private Map<NetGraphModel, NetConfiguration> _cache;

    private static final NetConfigurationCache INSTANCE = new NetConfigurationCache();

    private NetConfigurationCache() {
        _cache = new HashMap<NetGraphModel, NetConfiguration>();
    }

    public static NetConfigurationCache getInstance() {
        return INSTANCE;
    }


    public NetConfiguration add(NetGraphModel model) {
        NetConfiguration netConfiguration = new NetConfiguration(model);
        _cache.put(model, netConfiguration);
        return netConfiguration;
    }


    public NetConfiguration get(NetGraphModel model) {
        return _cache.get(model);
    }

    public NetConfiguration remove(NetGraphModel model) {
        return _cache.remove(model);
    }

    public NetConfiguration getOrAdd(NetGraphModel model) {
        NetConfiguration configuration = get(model);
        if (configuration == null) {
            configuration = add(model);
        }
        return configuration;
    }

}
