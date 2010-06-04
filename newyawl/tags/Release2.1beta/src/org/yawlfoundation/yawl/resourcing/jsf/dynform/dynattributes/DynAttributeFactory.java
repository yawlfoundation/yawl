/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes;

import com.sun.rave.web.ui.component.PanelLayout;
import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various dynamic form
 * attribute classes found in this package.
 *
 * Create Date: 18/05/2009.
 *
 * @author Michael Adams
 * @version 2.0
 */

public class DynAttributeFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes." ;
    static Logger _log = Logger.getLogger(DynAttributeFactory.class) ;


    public static void applyAttributes(PanelLayout parentPanel, WorkItemRecord wir, Participant p) {
        Set<AbstractDynAttribute> classes = getInstances();
        for (AbstractDynAttribute attributeClass : classes) {
            attributeClass.applyAttributes(parentPanel, wir, p);
        }
    }

    public static void adjustFields(List<DynFormField> fieldList, WorkItemRecord wir, Participant p) {
        Set<AbstractDynAttribute> classes = getInstances();
        for (AbstractDynAttribute attributeClass : classes) {
            attributeClass.adjustFields(fieldList, wir, p);
        }
    }


    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'allocatorName' must be the name of a class in this package
     * @param classname the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static AbstractDynAttribute getInstance(String classname) {
        try {
            return (AbstractDynAttribute) Class.forName(pkg + classname).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            _log.error("DynAttributeFactory ClassNotFoundException: class '" + classname +
                       "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            _log.error("DynAttributeFactory IllegalAccessException: class '" + classname +
                       "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            _log.error("DynAttributeFactory InstantiationException: class '" + classname +
                       "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            _log.error("DynAttributeFactory ClassCastException: class '" + classname +
                       "' does not extend AbstractDynAttribute - class ignored.");
        }

        return null ;
    }


    /**
     * Constructs and returns a list of instantiated dynAttribute objects, one for each
     * of the different dynAttribute classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    public static Set<AbstractDynAttribute> getInstances() {

        HashSet<AbstractDynAttribute> result = new HashSet<AbstractDynAttribute>();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("jsf/dynform/dynattributes") ;
        String[] classes = new File(pkgPath).list(new DynAttributeClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            AbstractDynAttribute temp = getInstance(sansExtn);
            if (temp != null) result.add(temp);
        }
        return result;
    }


    /**
     * This class is used by the File.list call in 'getAllocators' so that only
     * valid class files of this package are included
     */
    private static class DynAttributeClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("DynAttributeFactory")) ||     // and this class
                (name.startsWith("AbstractDynAttribute")))      // and the base classes
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}