/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.util;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 7/05/2008
 */
public class YProperties {

    private String _properties = null ;

    private static YProperties _me = null;

    private YProperties() { }

    public static YProperties getInstance() {
        if (_me == null) _me = new YProperties() ;
        return _me ;
    }

    public void setProperties(String s) { _properties = s ; }

    public String getProperties() { return _properties ; }


    private List<String> getProperties(String key) {
        ArrayList<String> result = new ArrayList<String>();
        if (_properties != null) {
            String[] lines = _properties.split("\n");
            for (String line : lines) {
                if (line.startsWith(key)) {
                    String[] parts = line.split("=");
                    result.add(parts[1]);                    
                }
            }
        }
        return result;
    }


    public List<YAWLServiceReference> getServices() {
        ArrayList<YAWLServiceReference> result = new ArrayList<YAWLServiceReference>();
        List<String> serviceStrings = getProperties("service");

        for (String text : serviceStrings) {
            String[] parts = text.split(";");
            if (parts.length == 4) {
                YAWLServiceReference ys = new YAWLServiceReference(parts[1], null);
                ys.set_serviceName(parts[0]);
                ys.set_assignable(parts[2].equalsIgnoreCase("true"));
                ys.setDocumentation(parts[3]);
                result.add(ys);
            }
            // else _log.error("Invalid format for services in yawl.properties");
        }
        return result ;
    }

}
