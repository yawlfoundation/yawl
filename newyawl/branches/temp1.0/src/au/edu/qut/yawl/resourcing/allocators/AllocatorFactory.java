/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.rsInterface.Docket;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

/**
 * This factory class creates and instantiates instances of the various allocator
 * classes found in this package.
 *
 * @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 10/07/2007
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
			AbstractAllocator newClass = (AbstractAllocator)
                                           Class.forName(pkg + allocatorName).newInstance() ;
            if (newClass != null) newClass.setName(allocatorName);
            return newClass ;
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
     * @param params a Map of parameters required by the class to perform its constraint
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractAllocator getInstance(String allocatorName, HashMap params) {
        AbstractAllocator newClass = getInstance(allocatorName);
        if (newClass != null) newClass.setParams(params);
        return newClass ;
    }

    /**
     * Constructs and returns a list of instantiated constraint objects, one for each
     * of the different allocator classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    public static Set getAllocators() {

        HashSet allocators = new HashSet();

        // retrieve a list of (filtered) class names in this package

//        String[] classes = new File(".").list(new AllocatorClassFileFilter());
        String pkgPath = Docket.getPackageFileDir("allocators") ;

        String[] classes = new File(pkgPath).list(new AllocatorClassFileFilter());
  
        System.out.println("allocator classes size: " + classes.length);

        for (String aClass : classes) {

            System.out.println("class name: " + aClass);


            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));

            System.out.println("class name: " + sansExtn);

            AbstractAllocator temp = getInstance(sansExtn);

            System.out.println("temp != null: " + temp != null);
            
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
