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

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import org.yawlfoundation.yawl.resourcing.util.Docket;

import java.util.*;
import java.io.*;

/**
 * This factory class creates and instantiates instances of the various data source
 * classes found in this package.
 *
 * @author Michael Adams
 *  v0.1, 10/07/2007
 */

public class DataSourceFactory {

    static String pkg = "org.yawlfoundation.yawl.resourcing.datastore.orgdata." ;
    /**
     * Instantiates a class of the name passed.
     *
     * @pre 'dbName' must be the name of a class in this package
     * @param dbName the name of the class to instantiate
     * @return the instantiated class, or null if there was a problem
     */
    public static DataSource getInstance(String dbName) {
        try {
            DataSource newClass = (DataSource) Class.forName(pkg + dbName).newInstance() ;
            if (newClass != null) newClass.setName(dbName);
            return newClass ;
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println("Class not found - " + dbName);
        }
        catch (IllegalAccessException iae) {
            System.out.println("Illegal access - " + dbName);
        }
        catch (InstantiationException ie) {
            System.out.println("Instantiation - " + dbName );
        }
        return null ;
    }


    /**
     * Constructs and returns a list of instantiated DataSource objects, one for each
     * of the different DataSource classes available in this package
     *
     * @return a List of instantiated DataSource objects
     */
    public static Set getDataSources() {

        HashSet ds = new HashSet();

        // retrieve a list of (filtered) class names in this package
        String pkgPath = Docket.getPackageFileDir("datastore/orgdata") ;
        String[] classes = new File(pkgPath).list(new DataSourceClassFileFilter());

        for (String aClass : classes) {

            // strip off the file extension
            String sansExtn = aClass.substring(0, aClass.lastIndexOf('.'));
            DataSource temp = getInstance(sansExtn);
            if (temp != null) ds.add(temp);
        }
        return ds;
    }

    /********************************************************************************/
    
    /**
     * This class is used by the File.list call in 'getDataSources' so that only
     * valid class files of this package are included
     */
    private static class DataSourceClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (( new File(dir, name).isDirectory() ) ||        // ignore dirs
                (name.startsWith("DataSource")))               // and this & base class
                return false;

            return name.toLowerCase().endsWith( ".class" );     // only want .class files
        }
    }
}
