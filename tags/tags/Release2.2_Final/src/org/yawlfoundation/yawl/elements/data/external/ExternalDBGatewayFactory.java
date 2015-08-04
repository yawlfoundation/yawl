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

package org.yawlfoundation.yawl.elements.data.external;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YEngine;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
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

    static String pkg = "org.yawlfoundation.yawl.elements.data.external." ;
    static String pkgDirs = "elements/data/external";
    static Logger _log = Logger.getLogger(ExternalDBGatewayFactory.class) ;

    public final static String MAPPING_PREFIX = "#external:";

    public static boolean isExternalDBMappingExpression(String expression) {
        return (expression != null) && expression.startsWith(MAPPING_PREFIX);
    }

    public static String getMappingClassFromExpression(String expression) {
        return (expression != null) ? expression.split(":")[1] : null;
    }


    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'allocatorName' must be the name of a class in this package
     * @param classname the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractExternalDBGateway getInstance(String classname) {
        try {
            if (isExternalDBMappingExpression(classname)) {
                classname = getMappingClassFromExpression(classname);
            }
            return (AbstractExternalDBGateway) Class.forName(pkg + classname).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            _log.error("ExternalDBGatewayFactory ClassNotFoundException: class '" + classname +
                       "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            _log.error("ExternalDBGatewayFactory IllegalAccessException: class '" + classname +
                       "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            _log.error("ExternalDBGatewayFactory InstantiationException: class '" + classname +
                       "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            _log.error("ExternalDBGatewayFactory ClassCastException: class '" + classname +
                       "' does not extend AbstractDynAttribute - class ignored.");
        }

        return null ;
    }


    /**
     * Constructs and returns a list of instantiated dynAttribute objects, one for each
     * of the different dynAttribute classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    public static Set<AbstractExternalDBGateway> getInstances() {

        HashSet<AbstractExternalDBGateway> result = new HashSet<AbstractExternalDBGateway>();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = YEngine.getInstance().getEngineClassesRootFilePath() + pkgDirs;
        String[] classes = new File(pkgPath).list(new ExternalDBGatewayClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            AbstractExternalDBGateway temp = getInstance(sansExtn);
            if (temp != null) result.add(temp);
        }
        return result;
    }


    /**
     * This class is used by the File.list call in 'getInstances' so that only
     * valid class files of this package are included
     */
    private static class ExternalDBGatewayClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||           // ignore dirs
                (name.startsWith("ExternalDBGatewayFactory")) ||   // and this class
                (name.startsWith("AbstractExternalDBGateway")) ||  // and the base classes
                (name.startsWith("HibernateEngine")) ||            // and the engine
                (name.startsWith("package-info")))                 // and package-info
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}