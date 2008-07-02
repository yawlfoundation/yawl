/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.codelets;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
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

    static String pkg = "org.yawlfoundation.yawl.resourcing.codelets." ;
    static Logger _log = Logger.getLogger(CodeletFactory.class);

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

        HashSet<AbstractCodelet> codelets = new HashSet<AbstractCodelet>();

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