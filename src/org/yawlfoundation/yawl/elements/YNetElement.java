/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
 * 
 * Abstract class. A super class of YExternalNetElement.   Has little relation to the
 * YAWL paper.
 * @author Lachlan Aldred
 * 
 */
public abstract class YNetElement implements Cloneable, Comparable {
    private String _id;

    /**
     * Constructor
     * @param id
     */
    protected YNetElement(String id) {
        _id = id;
    }

    /**
     * @return the id of this YNetElement
     */
    public String getID() {
        return _id;
    }

    public void setID(String id) { _id = id; }


    public String toString() {
        String fullClassName = getClass().getName();
        String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 2);
        return shortClassName + ":" + getID();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int compareTo(Object o) {
        YNetElement ne = (YNetElement) o;
        return getID().compareTo(ne.getID());
    }
}
