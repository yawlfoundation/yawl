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

package org.yawlfoundation.yawl.resourcing.codelets;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.PluginLoader;
import org.yawlfoundation.yawl.resourcing.util.PluginLoaderException;

import java.util.Set;

/**
 * This factory class creates and instantiates codelet instances.
 *
 * @author Michael Adams
 * @version 2.0 (17/06/2008)
 * @version 2.1 (13/09/2010)
 */

public class CodeletFactory {

    // don't include instantiations of these classes
    private static String[] _excludes = { "CodeletFactory", "CodeletInfo",
                                          "CodeletExecutionException", "AbstractCodelet" };

    private static String _pkg = "org.yawlfoundation.yawl.resourcing.codelets." ;
    private static Logger _log = Logger.getLogger(CodeletFactory.class);


    /**
     * Instantiates a single codelet instance
     * @param codeletName the canonical name of the codelet class (no extension)
     * deployed within the resource service
     * @return an instantiated codelet, or null if there was a problem 
     */
    public static AbstractCodelet getInstance(String codeletName) {
        try {
            return PluginLoader.loadInstance(AbstractCodelet.class, _pkg, codeletName);
        }
        catch (PluginLoaderException ple) {
            _log.error("CodeletFactory " + ple.getMessage());
        }
        return null ;
    }


    /**
     * Constructs and returns a list of instantiated codelet objects, one for each
     * of the different codelet classes available in this package and externally
     *
     * @return a Set of instantiated codelet objects
     */
    public static Set<AbstractCodelet> getInstances() {
        return PluginLoader.loadInstances(AbstractCodelet.class, _pkg, _excludes);
    }

}