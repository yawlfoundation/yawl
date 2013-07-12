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

package org.yawlfoundation.yawl.elements;

/**
 * A simple version numbering implementation stored as a major part and a minor part
 * (both int) but represented externally as a dotted String (eg 5.12)
 *
 * @author Michael Adams
 *         Date: 18/10/2007
 *         Last Date: 05/06/08
 */

public class YSpecVersion implements Comparable<YSpecVersion> {
    private int _major;
    private int _minor;

    // Constructor with default starting version
    public YSpecVersion() {
        _major = 0;
        _minor = 1;
    }

    // Constructor with two ints
    public YSpecVersion(int major, int minor) {
        setVersion(major, minor);
    }

    // Constructor as string
    public YSpecVersion(String version) {
        if (version == null) version = "0.1";
        setVersion(version);
    }


    public String setVersion(int major, int minor) {
        _major = major;
        _minor = minor;
        return toString();
    }

    public String setVersion(String version) {
        try {
            if (version.indexOf('.') > -1) {
                String[] part = version.split("\\.");
                _major = Integer.parseInt(part[0]);
                _minor = Integer.parseInt(part[1]);
            } else {                        // handle versions numbers without a decimal
                _major = Integer.parseInt(version);
                _minor = 1;
            }
        } catch (NumberFormatException nfe) {
            setVersion(0, 1);             // default
        }

        return toString();
    }


    public String getVersion() { return toString();}


    public double toDouble() {                                         // legacy method
        try {
            return new Double(toString());
        } catch (Exception e) {
            return 0.1;                                               // default
        }
    }


    public String toString() {
        return String.format("%s.%s", String.valueOf(_major), String.valueOf(_minor));
    }

    public int getMajorVersion() { return _major; }

    public int getMinorVersion() { return _minor; }

    public String minorIncrement() {
        _minor++;
        return toString();
    }

    public String majorIncrement() {
        _major++;
        return toString();
    }

    public String minorRollback() {
        _minor--;
        return toString();
    }

    public String majorRollback() {
        _major--;
        return toString();
    }

    public int compareTo(YSpecVersion other) {
        if (this.equals(other))
            return 0;
        else if (this.equalsMajorVersion(other))
            return this.getMinorVersion() - other.getMinorVersion();
        else
            return this.getMajorVersion() - other.getMajorVersion();
    }

    public boolean equalsMajorVersion(YSpecVersion other) {
        return this.getMajorVersion() == other.getMajorVersion();
    }

    public boolean equalsMinorVersion(YSpecVersion other) {
        return this.getMinorVersion() == other.getMinorVersion();
    }

    public boolean equals(Object other) {
        return (other instanceof YSpecVersion) &&
                this.equalsMajorVersion((YSpecVersion) other) &&
                this.equalsMinorVersion((YSpecVersion) other);
    }

    public int hashCode() {
        return (17 * _major) * (31 * _minor);
    }
}
