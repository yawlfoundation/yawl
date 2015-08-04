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

package org.yawlfoundation.yawl.elements.predicate;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of classes in this package that
 * implement the PredicateEvaluator interface.
 *
 * @author Michael Adams
 * @date 05/12/2012
 */

public class PredicateEvaluatorFactory {

    // don't include instantiations of these classes
    private static String[] _excludes = {"PredicateEvaluatorFactory",
            "PredicateEvaluatorCache", "PredicateEvaluator"};

    private static final String PKG = "org.yawlfoundation.yawl.elements.predicate.";
    private static Logger _log = Logger.getLogger(PredicateEvaluatorFactory.class);


    /**
     * Constructs and returns a list of instantiated allocator objects, one for each
     * of the different allocator classes available in this package and externally
     *
     * @return a Set of instantiated allocator objects
     */
    public static Set<PredicateEvaluator> getInstances() {
        return getInstances(PredicateEvaluator.class, PKG, _excludes);
    }


    /******************************************************************************/

    /**
     * Instantiates all the internal pluggable classes matching the parameters passed.
     *
     * @param c        the type of class to instantiate
     * @param pkg      the classes package
     * @param excludes an array of the class names to not instantiate from those found
     * @return a Set of instantiated classes, or null if there was a problem
     */
    private static <T> Set<T> getInstances(Class<T> c, String pkg, String[] excludes) {
        String pkgPath = getAbsolutePath(pkg);
        String[] classes = new File(pkgPath).list(new ClassFileFilter(excludes));
        return getInstanceSet(c, pkg, classes);
    }


    private static String getAbsolutePath(String pkg) {
        String catalinaBase = System.getProperty("catalina.base");
        String classesBase = "/webapps/yawl/WEB-INF/classes/";
        String pkgDir = pkg.replaceAll("\\.", "/");
        return catalinaBase + classesBase + pkgDir;
    }


    /**
     * Instantiates all the classes matching the parameters passed.
     *
     * @param c          the (base) type of class to instantiate
     * @param pkg        the classes package
     * @param classNames an array of the class names to instantiate
     * @return a Set of instantiated classes, or an empty set if there was a problem
     */
    private static <T> Set<T> getInstanceSet(Class<T> c, String pkg,
                                             String[] classNames) {
        Set<T> instances = new HashSet<T>();
        for (String className : classNames) {

            // strip off the file extension
            String sansExtn = className.substring(0, className.lastIndexOf('.'));
            try {
                instances.add(loadInstance(c, null, pkg + sansExtn));
            } catch (IllegalArgumentException e) {
                _log.warn(e.getMessage());
            }
        }
        return instances;
    }

    /**
     * Instantiates a single pluggable class.
     *
     * @param c         the type of class to instantiate
     * @param localPkg  the class's package (may be null if class is located externally
     *                  to the resource service)
     * @param className the canonical name of the required class (no extension)
     * @return the instantiated class, or null if there was a problem
     * @throws IllegalArgumentException a wrapper for the various exceptions that may occur
     */
    public static <T> T loadInstance(Class<T> c, String localPkg, String className)
            throws IllegalArgumentException {
        if ((localPkg != null) && (!className.contains(".")))
            className = localPkg + className;

        try {
            return (T) Class.forName(className).newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("ClassNotFoundException: class '" +
                    className + "' could not be found - class ignored.", cnfe);
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("IllegalAccessException: class '" +
                    className + "' could not be accessed - class ignored.", iae);
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException("InstantiationException: class '" +
                    className + "' could not be instantiated - class ignored.", ie);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("ClassCastException: class '" +
                    className + "' does not extend its Abstract Class - class ignored.", cce);
        }
    }


    /**
     * This class is used by the File.list call in 'getPluginNames'
     * so that only valid class files are included
     */
    private static class ClassFileFilter implements FilenameFilter {

        private String[] excludes;

        public ClassFileFilter() { }

        public ClassFileFilter(String[] excludeArray) {
            excludes = excludeArray;
        }


        private boolean isExcluded(String name) {
            if (excludes != null) {
                for (String s : excludes) {
                    if (name.startsWith(s)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean accept(File dir, String name) {
            return (!new File(dir, name).isDirectory()) &&    // ignore dirs
                    (!isExcluded(name)) &&                     // ignore excludes
                    name.toLowerCase().endsWith(".class");     // only want .class files
        }
    }

}
