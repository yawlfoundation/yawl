/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.allocators;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.PluginLoader;
import org.yawlfoundation.yawl.resourcing.util.PluginLoaderException;

import java.util.HashMap;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various allocator
 * classes.
 *
 * Create Date: 10/07/2007. Last Date 13/09/2010
 *
 * @author Michael Adams
 * @version 2.0
 */

public class AllocatorFactory {

    // don't include instantiations of these classes
    private static String[] _excludes = { "AllocatorFactory", "AbstractAllocator",
                                          "Generic" };

    private static String _pkg = "org.yawlfoundation.yawl.resourcing.allocators." ;
    private static Logger _log = Logger.getLogger(AllocatorFactory.class) ;

    /**
     * Instantiates a single allocator instance
     * @param allocatorName the canonical name of the allocator class (no extension)
     * deployed within the resource service
     * @return an instantiated allocator, or null if there was a problem 
     */
    public static AbstractAllocator getInstance(String allocatorName) {
        try {
            return PluginLoader.loadInstance(AbstractAllocator.class, _pkg, allocatorName);
        }
        catch (PluginLoaderException ple) {
            _log.error("AllocatorFactory " + ple.getMessage());
        }
        return null ;
    }


    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param allocatorName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its allocation
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractAllocator getInstance(String allocatorName,
                                                HashMap<String, String> params) {
        AbstractAllocator newClass = getInstance(allocatorName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }


    /**
     * Constructs and returns a list of instantiated allocator objects, one for each
     * of the different allocator classes available in this package and externally
     *
     * @return a Set of instantiated allocator objects
     */
    public static Set<AbstractAllocator> getInstances() {
        return PluginLoader.loadInstances(AbstractAllocator.class, _pkg, _excludes);
    }

}
