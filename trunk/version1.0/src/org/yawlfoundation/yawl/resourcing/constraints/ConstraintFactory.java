/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.constraints;

import org.yawlfoundation.yawl.resourcing.rsInterface.Docket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;

/**
 * This factory class creates and instantiates instances of the various constraint
 * classes found in this package.
 *
 *  Create Date: 10/07/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */

public class ConstraintFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.constraints." ;

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'constraintName' must be the name of a class in this package
     * @param constraintName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractConstraint getInstance(String constraintName) {
        try {
//			AbstractConstraint newClass = (AbstractConstraint)
//                                           Class.forName(pkg + constraintName).newInstance() ;
//            if (newClass != null) newClass.setName(constraintName);
//            return newClass ;
            return (AbstractConstraint) Class.forName(pkg + constraintName).newInstance();            
        }
        catch (ClassNotFoundException cnfe) {
			System.out.println("Class not found - " + constraintName);
        }
        catch (IllegalAccessException iae) {
			System.out.println("Illegal access - " + constraintName);
		}
        catch (InstantiationException ie) {
			System.out.println("Instantiation - " + constraintName );
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
