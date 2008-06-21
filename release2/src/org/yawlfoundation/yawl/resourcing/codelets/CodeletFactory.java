/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.codelets;

import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;

/**
 * This factory class creates and instantiates codelet instances found in this package.
 *
 * Create Date: 17/06/2008.
 *
 * @author Michael Adams
 * @version 2.0
 */

public class CodeletFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.codelets." ;

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'codeletName' must be the name of a class in this package
     * @param codeletName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractCodelet getInstance(String codeletName) {
        try {
            return (AbstractCodelet) Class.forName(pkg + codeletName).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
		       	System.out.println("Class not found - " + codeletName);
        }
        catch (IllegalAccessException iae) {
			      System.out.println("Illegal access - " + codeletName);
	    	}
        catch (InstantiationException ie) {
			      System.out.println("Instantiation - " + codeletName);
	    	}
        return null ;
    }


    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param codeletName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its allocation
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractCodelet getInstance(String codeletName, HashMap params) {
        AbstractCodelet newClass = getInstance(codeletName);
 //       if (newClass != null) newClass.setParams(params);   todo: base class
        return newClass ;
    }


    /**
     * Constructs and returns a list of instantiated codelet objects, one for each
     * of the different codelet classes available in this package
     *
     * @return a List of instantiated codelet objects
     */
    public static Set getCodelets() {

        HashSet codelets = new HashSet();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("codelets") ;
        String[] classes = new File(pkgPath).list(new CodeletClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            AbstractCodelet temp = getInstance(sansExtn);
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
                (name.startsWith("CodeletFactory")) ||        // and this class
                (name.startsWith("CodeletExecutionException")) ||
                (name.startsWith("AbstractCodelet")))         // and the base class
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}