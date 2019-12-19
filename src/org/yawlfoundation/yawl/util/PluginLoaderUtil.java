/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/04/2014
 */
public class PluginLoaderUtil {

    private String _externalPaths;

    public PluginLoaderUtil() { }


    public void setExternalPaths(String externalPaths) {
        _externalPaths = externalPaths;
    }


    public <T> Map<String, Class<T>> load(Class<T> clazz) {
        return new YPluginLoader(_externalPaths).loadAsMap(clazz);
    }


    public <T> T loadInstance(Class<T> clazz, String instanceName) {
        return new YPluginLoader(_externalPaths).getInstance(clazz, instanceName);
    }


    public <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (Throwable t) {
            return null;
        }
    }


    public <T> Set<T> toInstanceSet(Collection<Class<T>> clazzSet) {
        Set<T> instanceSet = new HashSet<T>();
        for (Class<T> clazz : clazzSet) {
            try {
                instanceSet.add(clazz.newInstance());
            }
            catch (Throwable t) {
                // do nothing
            }
        }
        return instanceSet;
    }

}
