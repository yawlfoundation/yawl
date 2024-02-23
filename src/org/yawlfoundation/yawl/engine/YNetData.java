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

package org.yawlfoundation.yawl.engine;

public class YNetData {

    String _data = null;
    String _id = null;

    public YNetData() { }

    public YNetData(String caseID) {
        _id = caseID ;
    }

    public String getData() {
        return _data;
    }

    public void setData(String data) {
        _data = data;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getId() {
        return _id;
    }

    public boolean equals(Object other) {
        return (other instanceof YNetData) &&    // instanceof = false if other is null
                ((getId() != null) ? getId().equals(((YNetData) other).getId())
                : super.equals(other));
    }

    public int hashCode() {
        return (getId() != null) ? getId().hashCode() : super.hashCode();
    }

    public String toString() {
        return String.format("ID: %s; DATA: %s", _id, _data);
    }

}