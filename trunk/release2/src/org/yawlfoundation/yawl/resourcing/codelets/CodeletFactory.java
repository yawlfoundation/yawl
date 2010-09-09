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
import org.yawlfoundation.yawl.resourcing.util.Docket;
import org.yawlfoundation.yawl.resourcing.util.PluginLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This factory class creates and instantiates codelet instances found in this package.
 *
 * Create Date: 17/06/2008.
 *
 * @author Michael Adams
 * @version 2.0
 */

public class CodeletFactory {

    static String _pkg = "org.yawlfoundation.yawl.resourcing.codelets." ;
    static Logger _log = Logger.getLogger(CodeletFactory.class);

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'codeletName' must be the name of a class in this package
     * @param codeletName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */

    public static AbstractCodelet getInstance(String codeletName) {
        return getInstance(_pkg, codeletName);
    }

    public static AbstractCodelet getInstance(String pkg, String codeletName) {
        try {
            return (AbstractCodelet) Class.forName(pkg + codeletName).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            _log.error("CodeletFactory ClassNotFoundException: class '" + codeletName +
                       "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            _log.error("CodeletFactory IllegalAccessException: class '" + codeletName +
                       "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            _log.error("CodeletFactory InstantiationException: class '" + codeletName +
                       "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            _log.error("CodeletFactory ClassCastException: class '" + codeletName + 
                       "' does not extend AbstractCodelet - class ignored.");
        }
        return null ;
    }


    /**
     * Constructs and returns a list of instantiated codelet objects, one for each
     * of the different codelet classes available in this package
     *
     * @return a Set of instantiated codelet objects
     */
    public static Set<AbstractCodelet> getCodelets() {

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("codelets") ;
        String[] classes = new File(pkgPath).list(new CodeletClassFileFilter());
        Set<AbstractCodelet> codelets = getInstances(_pkg, classes);

        codelets.addAll(getExternalCodelets());     // add any external plugin codelets

        return codelets;
    }


    private static Set<AbstractCodelet> getExternalCodelets() {
        Set<AbstractCodelet> codelets = new HashSet<AbstractCodelet>();
        List<String> plugins = PluginLoader.getPluginNames("codelets");
        if (plugins != null) {
            String pkg = plugins.remove(0) + ".";
            codelets = getInstances(pkg, plugins.toArray(new String[0]));
        }
        return codelets;
    }


    private static Set<AbstractCodelet> getInstances(String pkg, String[] classNames) {
        Set<AbstractCodelet> codelets = new HashSet<AbstractCodelet>();
        for (String className : classNames) {

            // strip off the file extension
            String sansExtn = className.substring(0, className.lastIndexOf('.'));
            AbstractCodelet temp = getInstance(pkg, sansExtn);
            if (temp != null) codelets.add(temp);
        }
        return codelets;
    }


    /**
     * This class is used by the File.list call in 'getcodelets' so that only
     * valid class files of this package are included
     */
    private static class CodeletClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("CodeletFactory")) ||          // and this class
                (name.startsWith("CodeletInfo")) ||        // and the transporter
                (name.startsWith("CodeletExecutionException")) ||
                (name.startsWith("AbstractCodelet")))           // and the base class
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}