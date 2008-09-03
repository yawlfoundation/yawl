/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.allocators;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various allocator
 * classes found in this package.
 *
 * Create Date: 10/07/2007. Last Date 09/11/2007
 *
 * @author Michael Adams {BPM Group, QUT Australia)
 * @version 2.0
 */

public class AllocatorFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.allocators." ;
    static Logger _log = Logger.getLogger(AllocatorFactory.class) ;

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'allocatorName' must be the name of a class in this package
     * @param allocatorName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractAllocator getInstance(String allocatorName) {
        try {
            return (AbstractAllocator) Class.forName(pkg + allocatorName).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            _log.error("AllocatorFactory ClassNotFoundException: class '" + allocatorName +
                       "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            _log.error("AllocatorFactory IllegalAccessException: class '" + allocatorName +
                       "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            _log.error("AllocatorFactory InstantiationException: class '" + allocatorName +
                       "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            _log.error("AllocatorFactory ClassCastException: class '" + allocatorName +
                       "' does not extend AbstractAllocator - class ignored.");
        }
        
        return null ;
    }


    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param allocatorName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its allocation
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractAllocator getInstance(String allocatorName, HashMap params) {
        AbstractAllocator newClass = getInstance(allocatorName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }


    /**
     * Constructs and returns a list of instantiated allocator objects, one for each
     * of the different allocator classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    public static Set getAllocators() {

        HashSet allocators = new HashSet();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("allocators") ;
        String[] classes = new File(pkgPath).list(new AllocatorClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            AbstractAllocator temp = getInstance(sansExtn);           
            if (temp != null) allocators.add(temp);
        }
        return allocators;
    }


    /**
     * This class is used by the File.list call in 'getAllocators' so that only
     * valid class files of this package are included
     */
    private static class AllocatorClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("AllocatorFactory")) ||        // and this class
                (name.startsWith("Generic")) ||
                (name.startsWith("AbstractAllocator")))         // and the base classes
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}
