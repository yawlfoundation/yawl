package org.yawlfoundation.yawl.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Author: Michael Adams
 * Creation Date: 16/04/2010
 */
public class YBuildProperties {

    Properties _buildProps;


    public YBuildProperties() { }


    public void load(InputStream inputStream) {
        try {
            _buildProps = new Properties();
            _buildProps.load(inputStream);
        }
        catch (Exception e) {
            _buildProps = null;
        }
    }


    public String getBuildNumber() {
        return _buildProps.getProperty("BuildNumber");
    }

    public String getVersion() {
        return _buildProps.getProperty("Version");
    }

    public String getBuildDate() {
        return _buildProps.getProperty("BuildDate");
    }


    public String toXML() {
        XNode root = new XNode("buildproperties");
        for (Object o : _buildProps.keySet()) {
            String key = (String) o;
            String value = _buildProps.getProperty(key);
            root.addChild(key, value);
        }
        return root.toString();
    }


}
