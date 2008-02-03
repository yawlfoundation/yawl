/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.filters;

import org.yawlfoundation.yawl.resourcing.rsInterface.Docket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;

/**
 * This factory class creates and instantiates instances of the various filter
 * classes found in this package.
 *
 *  Create Date: 10/07/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class FilterFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.filters." ;
    
    /**
     * Instantiates a class of the name passed.
     *
     * pre: filterName must be the name of a class in this package
     * @param filterName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractFilter getInstance(String filterName) {
        try {
//			AbstractFilter newClass = (AbstractFilter)
//                                       Class.forName(pkg + filterName).newInstance() ;
//            if (newClass != null) newClass.setName(filterName);
//            return newClass ;
            return (AbstractFilter) Class.forName(pkg + filterName).newInstance() ;
        }
        catch (ClassNotFoundException cnfe) {
			System.out.println("Class not found - " + filterName);
        }
        catch (IllegalAccessException iae) {
			System.out.println("Illegal access - " + filterName);
		}
        catch (InstantiationException ie) {
			System.out.println("Instantiation - " + filterName );
		}
        return null ;
    }

    /**
     * Instantiates a class of the name passed (via a call to the above method)
     * @param filterName the name of the class to instantiate
     * @param params a Map of parameters required by the class to perform its filtering
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractFilter getInstance(String filterName, HashMap params) {
        AbstractFilter newClass = getInstance(filterName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }

    /**
     * Constructs and returns a list of instantiated filter objects, one for each
     * of the different filter classes available in this package
     *
     * @return a Set of instantiated filter objects
     */
    public static Set getFilters() {

        HashSet filters = new HashSet();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("filters") ;
        String[] classes = new File(pkgPath).list(new FilterClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            AbstractFilter temp = getInstance(sansExtn);
            if (temp != null) filters.add(temp);
        }
        return filters;
    }


    /**
     * This inner class is used by the File.list call in 'getFilters' so that only
     * valid class files of this package are included
     */
    private static class FilterClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("FilterFactory")) ||           // and this class
                (name.startsWith("Generic")) ||
                (name.startsWith("AbstractFilter")))            // and the base classes
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }

}
