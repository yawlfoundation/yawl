/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.filters;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.PluginLoader;
import org.yawlfoundation.yawl.resourcing.util.PluginLoaderException;

import java.util.HashMap;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various filter
 * classes found in this package.
 *
 *  Create Date: 10/07/2007. Last Date: 14/11/2010
 *
 *  @author Michael Adams
 *  @version 2.0
 */

public class FilterFactory {

    // don't include instantiations of these classes
    private static String[] _excludes = { "FilterFactory", "AbstractFilter", "Generic" };

    private static String _pkg = "org.yawlfoundation.yawl.resourcing.filters." ;
    private static Logger _log = Logger.getLogger(FilterFactory.class);

    /**
     * Instantiates a single filter instance
     * @param filterName the canonical name of the filter class (no extension)
     * deployed within the resource service
     * @return an instantiated filter, or null if there was a problem
     */
    public static AbstractFilter getInstance(String filterName) {
        try {
            return PluginLoader.loadInstance(AbstractFilter.class, _pkg, filterName);
        }
        catch (PluginLoaderException ple) {
            _log.error("FilterFactory " + ple.getMessage());
        }
        return null ;
    }

    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param filterName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its filtering
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractFilter getInstance(String filterName,
                                             HashMap<String, String> params) {
        AbstractFilter newClass = getInstance(filterName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }

    /**
     * Constructs and returns a list of instantiated filter objects, one for each
     * of the different filter classes available in this package and externally
     *
     * @return a Set of instantiated filter objects
     */
    public static Set<AbstractFilter> getInstances() {
        return PluginLoader.loadInstances(AbstractFilter.class, _pkg, _excludes);
    }

}
