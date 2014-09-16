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

package org.yawlfoundation.yawl.configuration.element;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class TaskConfigurationCache {

    private final Map<NetGraphModel, Map<YAWLTask, TaskConfiguration>> _cache;

    private static final TaskConfigurationCache INSTANCE = new TaskConfigurationCache();


    private TaskConfigurationCache() {
        _cache = new HashMap<NetGraphModel, Map<YAWLTask, TaskConfiguration>>();
    }


    public static TaskConfigurationCache getInstance() {
        return INSTANCE;
    }


    public TaskConfiguration add(NetGraphModel model, YAWLTask task) {
        Map<YAWLTask, TaskConfiguration> map = _cache.get(model);
        if (map == null) {
            map = new HashMap<YAWLTask, TaskConfiguration>();
            _cache.put(model, map);
        }
        TaskConfiguration configuration = new TaskConfiguration(task, model);
        map.put(task, configuration);
        return configuration;
    }


    public TaskConfiguration get(NetGraphModel model, YAWLTask task) {
        if (model == null || task == null) return null;
        Map<YAWLTask, TaskConfiguration> map = _cache.get(model);
        return map != null ? map.get(task) : null;
    }


    public TaskConfiguration getOrAdd(NetGraphModel model, YAWLTask task) {
        TaskConfiguration configuration = get(model, task);
        return configuration != null ? configuration : add(model, task);
    }


    public TaskConfiguration remove(NetGraphModel model, YAWLTask task) {
        if (model == null || task == null) return null;
        Map<YAWLTask, TaskConfiguration> map = _cache.get(model);
        return map != null ? map.remove(task) : null;
    }


}
