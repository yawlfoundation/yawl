/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Reads values from .properties files
 */
public class PropertyReader {

    private static Map<String, Properties> _props = new Hashtable<String, Properties>();
    private static final PropertyReader INSTANCE = new PropertyReader();

    public static final String YAWL = "/yawl.properties";
    public static final String SCHEDULING = "/schedule.properties";


    private PropertyReader() { }      // private constructor

    public static PropertyReader getInstance() { return INSTANCE; }

    /********************************************************************************/


    /**
     * Gets a property value from a .properties file
     * @param propFile the name of the .properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws java.io.IOException if the file can't be loaded or if it doesn't contain the key
     */
    public String getProperty(String propFile, String key) throws IOException {
        Properties prop = _props.get(propFile);

        // load the properties if not previously loaded
        if (prop == null) {
            prop = new Properties();
            try {
                prop.load(this.getClass().getResourceAsStream(propFile));
                _props.put(propFile, prop);                   // cache for later calls
            }
            catch (IOException e) {
                throw new IOException("cannot load properties from file: " + propFile, e);
            }
        }

        // get the value for the specified key
        String value = prop.getProperty(key);
        if (value == null) {
            throw new IOException("property " + key + " not found in file " + propFile);
        }

        return value;
    }


    /**
     * Gets a property value from the yawl.properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws IOException if the file can't be loaded or if it doesn't contain the key
     */
    public String getYAWLProperty(String key) throws IOException {
        return getProperty(YAWL, key);
    }


    /**
     * Gets a property value from the yawl.properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws IOException if the file can't be loaded or if it doesn't contain the key
     */
    public String getSchedulingProperty(String key) throws IOException {
        return getProperty(SCHEDULING, key);
    }


    /**
     * Gets a boolean type property value from a .properties file
     * @param propFile the name of the .properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws IOException if the file can't be loaded or if it doesn't contain the key
     */
    public boolean getBooleanProperty(String propFile, String key) throws IOException {
        return Boolean.parseBoolean(getProperty(propFile, key));
    }


    /**
     * Gets an integer type property value from a .properties file
     * @param propFile the name of the .properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws IOException if the file can't be loaded or if it doesn't contain the key
     * or the value is not a valid integer
     */
    public int getIntProperty(String propFile, String key) throws IOException {
        String value = getProperty(propFile, key);
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException nfe) {
            throw new IOException("Value '" + value + "' for key '" + key +
                    "' cannot be parsed as an integer type", nfe);
        }
    }


    /**
     * Gets a long type property value from a .properties file
     * @param propFile the name of the .properties file
     * @param key the property key to get the value for
     * @return the value for the requested key
     * @throws IOException if the file can't be loaded or if it doesn't contain the key
     * or the value is not a valid long
     */
    public long getLongProperty(String propFile, String key) throws IOException {
        String value = getProperty(propFile, key);
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException nfe) {
            throw new IOException("Value '" + value + "' for key '" + key +
                    "' cannot be parsed as a long type", nfe);
        }
    }

}
