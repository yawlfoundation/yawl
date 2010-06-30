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
import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various constraint
 * classes found in this package.
 *
 *  Create Date: 10/07/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class ConstraintFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.constraints." ;
    static Logger _log = Logger.getLogger(ConstraintFactory.class);

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'constraintName' must be the name of a class in this package
     * @param constraintName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractConstraint getInstance(String constraintName) {
        try {
            return (AbstractConstraint) Class.forName(pkg + constraintName).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            _log.error("ConstraintFactory ClassNotFoundException: class '" + constraintName +
                       "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            _log.error("ConstraintFactory IllegalAccessException: class '" + constraintName +
                       "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            _log.error("ConstraintFactory InstantiationException: class '" + constraintName +
                       "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            _log.error("ConstraintFactory ClassCastException: class '" + constraintName +
                       "' does not extend AbstractConstraint - class ignored.");
        }
        return null ;
    }

    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param constraintName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its constraint
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractConstraint getInstance(String constraintName, HashMap params) {
        AbstractConstraint newClass = getInstance(constraintName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }

    /**
     * Constructs and returns a list of instantiated constraint objects, one for each
     * of the different constraint classes available in this package
     *
     * @return a List of instantiated constraint objects
     */
    public static Set getConstraints() {

        HashSet constraints = new HashSet();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("constraints") ;
        String[] classes = new File(pkgPath).list(new ConstraintClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));

            AbstractConstraint temp = getInstance(sansExtn);
            
            if (temp != null) constraints.add(temp);
        }
        return constraints;
    }


    /**
     * This inner class is used by the File.list call in 'getConstraints' so that only
     * valid class files of this package are included
     */
    private static class ConstraintClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("ConstraintFactory")) ||       // and this class
                (name.startsWith("Generic")) ||
                (name.startsWith("AbstractConstraint")))        // and the base classes
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}
