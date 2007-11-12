/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.rsInterface.Docket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FilenameFilter;

/**
 * This factory class creates and instantiates instances of the various allocator
 * classes found in this package.
 *
 * Create Date: 10/07/2007. Last Date 09/11/2007
 *
 * @author Michael Adams {BPM Group, QUT Australia)
 * @version 1.0
 */

public class AllocatorFactory {

    static String pkg = "au.edu.qut.yawl.resourcing.allocators." ;

    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'allocatorName' must be the name of a class in this package
     * @param allocatorName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractAllocator getInstance(String allocatorName) {
        try {
//			AbstractAllocator newClass = (AbstractAllocator)
//                                         Class.forName(pkg + allocatorName).newInstance();
//            if (newClass != null) newClass.setName(allocatorName);
//            return newClass ;
            return (AbstractAllocator) Class.forName(pkg + allocatorName).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
			System.out.println("Class not found - " + allocatorName);
        }
        catch (IllegalAccessException iae) {
			System.out.println("Illegal access - " + allocatorName);
		}
        catch (InstantiationException ie) {
			System.out.println("Instantiation - " + allocatorName);
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
