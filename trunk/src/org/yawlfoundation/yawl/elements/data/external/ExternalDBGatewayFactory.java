/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements.data.external;

import org.yawlfoundation.yawl.util.PluginLoaderUtil;

import java.util.Map;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various external
 * database gateway classes found in this package.
 *
 * Create Date: 13/08/2009.
 *
 * @author Michael Adams
 * @version 2.1
 */

public class ExternalDBGatewayFactory {

    private static Map<String, Class<AbstractExternalDBGateway>> _classMap;

    private static final String BASE_PACKAGE = "org.yawlfoundation.yawl.elements.data.external.";
    public static final String MAPPING_PREFIX = "#external:";
    private static final PluginLoaderUtil _loader = new PluginLoaderUtil();


    private ExternalDBGatewayFactory() { }  // block initialisation - static methods only

    public static void setExternalPaths(String externalPaths) {
         _loader.setExternalPaths(externalPaths);
     }

    public static boolean isExternalDBMappingExpression(String expression) {
        return (expression != null) && expression.startsWith(MAPPING_PREFIX);
    }

    public static String getMappingClassFromExpression(String expression) {
        return (expression != null) ? expression.split(":")[1] : null;
    }

    public static String getBasePackage() {
        return BASE_PACKAGE;
    }


    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'allocatorName' must be the name of a class in this package
     * @param classname the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractExternalDBGateway getInstance(String classname) {
        if (isExternalDBMappingExpression(classname)) {
            classname = getMappingClassFromExpression(classname);
        }
        classname = qualify(classname, "");
        Class<AbstractExternalDBGateway> gatewayClass = getClassMap().get(classname);
        return (gatewayClass != null) ? _loader.newInstance(gatewayClass) :
                _loader.loadInstance(AbstractExternalDBGateway.class, classname);
    }


    /**
     * Constructs and returns a list of instantiated objects, one for each
     * of the different classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    public static Set<AbstractExternalDBGateway> getInstances() {
        return _loader.toInstanceSet(getClassMap().values());
    }


    private static Map<String, Class<AbstractExternalDBGateway>> getClassMap() {
        if (_classMap == null) {
            _classMap = _loader.load(AbstractExternalDBGateway.class);
        }
        return _classMap;
    }


    private static String qualify(String className, String pkg) {
        return (! (className == null || className.contains("."))) ?
            BASE_PACKAGE + pkg + className : className;
    }


}