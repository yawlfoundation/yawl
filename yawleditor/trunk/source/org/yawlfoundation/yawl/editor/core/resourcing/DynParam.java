/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing;

/**
 * A class that encapsulates one dynamic parameter - i.e. a data variable that at
 * runtime will contain a value corresponding to a participant or role that the
 * task is to be offered to.
 */
public class DynParam implements Comparable<DynParam> {

    public enum Refers { Participant, Role }  // Dynamic Parameter types

    private String _name ;              // the name of the data variable
    private Refers _refers ;               // participant or role

    /** the constructor
     *
     * @param name - the name of a data variable of this task that will contain
     *               a runtime value specifying a particular participant or role.
     * @param refers - either Participant or Role
     */
    public DynParam(String name, Refers refers) {
        _name = name ;
        _refers = refers ;
    }

/*******************************************************************************/

    // GETTERS & SETTERS //

    public String getName() { return _name ; }

    public Refers getRefers() { return _refers ; }

    public void setName(String name) { _name = name; }

    public void setRefers(Refers refers) { _refers = refers; }

    public String getRefersString() {
        return _refers == Refers.Participant ? "participant" : "role" ;
    }

/*******************************************************************************/

    public int compareTo(DynParam other) {
        int compare = getName().compareTo(other.getName());
        if (compare == 0) {
            compare = getRefers().compareTo(other.getRefers());
        }
        return compare;
    }

    /** this is for the spec file */
    public String toXML() {
        StringBuilder xml = new StringBuilder("<param>");
        xml.append("<name>").append(_name).append("</name>");
        xml.append("<refers>").append(getRefersString()).append("</refers>");
        xml.append("</param>");
        return xml.toString();
    }

}  // end of private class DynParam
