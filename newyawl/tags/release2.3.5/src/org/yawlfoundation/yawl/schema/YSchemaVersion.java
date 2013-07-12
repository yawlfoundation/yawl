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

package org.yawlfoundation.yawl.schema;

import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of schema versions, with associated methods
 * @author Michael Adams
 * @date 11/05/2011
 */
public enum YSchemaVersion {

    Beta2 ("Beta 2", 0.2),
    Beta3 ("Beta 3", 0.3),
    Beta4 ("Beta 4", 0.4),
    Beta6 ("Beta 6", 0.6),
    Beta7 ("Beta 7.1", 0.7),
    TwoPointZero ("2.0", 2.0),
    TwoPointOne  ("2.1", 2.1),
    TwoPointTwo  ("2.2", 2.2);


    public static YSchemaVersion DEFAULT_VERSION = TwoPointTwo;

    private final String betaNS = "http://www.citi.qut.edu.au/yawl";
    private final String betaSchemaLocation = betaNS +
            " d:/yawl/schema/YAWL_SchemaBeta7.1.xsd";

    private final String twoNS = "http://www.yawlfoundation.org/yawlschema";

    private final String headerTemplate =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
            "<specificationSet version=\"%s\" xmlns=\"%s\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"%s\">";

    private final String schemaPackagePath = "/org/yawlfoundation/yawl/unmarshal/";

    private final String _name;
    private final double _compareVal;


    private static final Map<String, YSchemaVersion> _fromStringMap =
            new HashMap<String, YSchemaVersion>();


    static {
        for (YSchemaVersion version : values()) {
            _fromStringMap.put(version.toString(), version);
        }
    }


    // the constructor
    YSchemaVersion(String name, double compareVal) {
        _name = name;
        _compareVal = compareVal;
    }


    @Override
    public String toString() { return _name; }

    public static YSchemaVersion fromString(String s) {
        return (s != null) ? _fromStringMap.get(s) : null;
    }


    public String betaNS() { return betaNS; }

    public String betaSchemaLocation() { return betaSchemaLocation; }

    public String twoNS() { return twoNS; }

    public String twoSchemaLocation() {
        return String.format("%s %s/%s", twoNS, twoNS, getSchemaFileName());
    }



    public boolean isVersionAtLeast(YSchemaVersion referenceVersion) {
        return Double.compare(this._compareVal, referenceVersion._compareVal) > -1; 
    }


    public static boolean isValidVersionString(String s) {
        return (fromString(s) != null);
    }


    public static YSchemaVersion defaultVersion() { return DEFAULT_VERSION; }


    public boolean usesSimpleRootData() {
        return isBeta2() || Beta3.equals(this);
    }


    public boolean isSchemaValidating() {
        return ! isBeta2();
    }


    public String getSchemaLocation(String s) {
        YSchemaVersion version = fromString(s);
        return (version != null) ? version.getSchemaLocation() : null;
    }


    public String getSchemaLocation() {
        return String.format("%s %s", getNameSpace(), getSchemaURL());
    }


    public String getNameSpace() {
        return (isBetaVersion() ? betaNS : twoNS);
    }


    // generate version-specific header    
    public String getHeader() {
        return isBetaVersion() ?
                String.format(headerTemplate, Beta7, betaNS, betaSchemaLocation) :
                String.format(headerTemplate, _name, twoNS, twoSchemaLocation());
    }


    public boolean isBetaVersion() {
        switch(this) {
            case TwoPointTwo:
            case TwoPointOne:
            case TwoPointZero: return false;

            default : return true;
        }
    }


    public boolean isBeta2() {
        return this.equals(Beta2);
    }


    public URL getSchemaURL() {
        return getClass().getResource(getAbsoluteSchemaFileName());
    }


    /**********************************************************************/

    private String getAbsoluteSchemaFileName() {
        return schemaPackagePath + getSchemaFileName();
    }


    private String getSchemaFileName() {
        return isBeta2() ? "YAWL_Schema.xsd"
                    : String.format("YAWL_Schema%s.xsd", toCompactString());
    }

    private String toCompactString() {
        return StringUtils.deleteWhitespace(toString());
    }

}
