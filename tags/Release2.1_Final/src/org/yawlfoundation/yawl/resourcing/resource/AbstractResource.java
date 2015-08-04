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

package org.yawlfoundation.yawl.resourcing.resource;

/**
 * An abstract class representing a resource entity.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public abstract class AbstractResource {

    protected String _resourceID;
    protected boolean _isAvailable = true;
    protected String _description ;
    protected String _notes ;


    protected AbstractResource() {}


    public String getID() { return _resourceID; }

    public void setID(String id) { _resourceID = id ; }      


    public boolean isAvailable() {
        return true;
    //    return ResourceManager.getInstance().getCalendar().isAvailable(this);
    }

    public void setAvailable(boolean available) { _isAvailable = available; }

    public String getNotes() { return _notes; }

    public void setNotes(String notes) {  _notes = notes; }

    public String getDescription() { return _description;  }

    public void setDescription(String desc) {_description = desc; }

}
