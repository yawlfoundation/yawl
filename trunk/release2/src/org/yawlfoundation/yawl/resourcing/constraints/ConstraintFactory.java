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

package org.yawlfoundation.yawl.resourcing.constraints;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.PluginLoader;
import org.yawlfoundation.yawl.resourcing.util.PluginLoaderException;

import java.util.HashMap;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various constraint
 * classes found in this package.
 *
 *  Create Date: 10/07/2007. Last Date: 14/09/2010
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class ConstraintFactory {

    // don't include instantiations of these classes
    private static String[] _excludes = { "ConstraintFactory", "AbstractConstraint",
                                          "Generic" };

    private static String _pkg = "org.yawlfoundation.yawl.resourcing.constraints." ;
    private static Logger _log = Logger.getLogger(ConstraintFactory.class);

    /**
     * Instantiates a single constraint instance
     * @param constraintName the canonical name of the constraint class (no extension)
     * deployed within the resource service
     * @return an instantiated constraint, or null if there was a problem
     */
    public static AbstractConstraint getInstance(String constraintName) {
        try {
            return PluginLoader.loadInstance(AbstractConstraint.class, _pkg, constraintName);
        }
        catch (PluginLoaderException ple) {
            _log.error("ConstraintFactory " + ple.getMessage());
        }
        return null ;
    }

    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param constraintName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its constraint
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractConstraint getInstance(String constraintName,
                                                 HashMap<String, String> params) {
        AbstractConstraint newClass = getInstance(constraintName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }

    /**
     * Constructs and returns a list of instantiated constraint objects, one for each
     * of the different constraint classes available in this package
     *
     * @return a Set of instantiated constraint objects
     */
    public static Set<AbstractConstraint> getInstances() {
        return PluginLoader.loadInstances(AbstractConstraint.class, _pkg, _excludes);
    }

}
