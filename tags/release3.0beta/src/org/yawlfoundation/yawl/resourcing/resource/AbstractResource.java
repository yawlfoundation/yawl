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

package org.yawlfoundation.yawl.resourcing.resource;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An abstract class representing a resource entity.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public abstract class AbstractResource implements Cloneable {

    public enum BlockType { Hard, Soft }

    protected String _resourceID;
    protected boolean _isAvailable = true;
    protected String _description;
    protected String _notes;

    // the msecs this resource is 'offline' for after use, before it can be used again
    // is a Long object to handle null values stored in db
    protected Long _blockedDuration;
    protected BlockType _blockType;
 

    protected AbstractResource() {
        _blockedDuration = 0L;
        _blockType = BlockType.Hard;
    }

    public boolean equals(Object o) {
        return (o == this) || ((o instanceof AbstractResource) &&
               (((AbstractResource) o).getID() != null) &&
               ((AbstractResource) o).getID().equals(_resourceID));
    }

    public int hashCode() {
        return _resourceID == null ? 17 * 31 * 31 : 17 * _resourceID.hashCode();
    }

    protected Object clone() throws CloneNotSupportedException {
        AbstractResource cloned = (AbstractResource) super.clone();
        cloned.setID(null);
        return cloned;
    }

    protected void merge(AbstractResource resource) {
        setDescription(resource.getDescription());
        setNotes(resource.getNotes());
        setBlockType(resource.getBlockType());
        setBlockedDuration(resource.getBlockedDuration());
    }

    public abstract String getName();

    public String getID() { return _resourceID; }

    public void setID(String id) { _resourceID = id ; }      


    // returns true if this resource has no calendar entries for this moment
    public boolean isAvailable() {
        return ResourceManager.getInstance().getCalendar().isAvailable(this);
    }


    // returns true if this resource has no calendar entries between 'from' and 'to' dates
    public boolean isAvailable(long from, long to) {
        return ResourceManager.getInstance().getCalendar().isAvailable(null, this, from, to);
    }


    public String getNotes() { return _notes; }

    public void setNotes(String notes) {  _notes = notes; }


    public String getDescription() { return _description;  }

    public void setDescription(String desc) { _description = desc; }


    public long getBlockedDuration() {
        return (_blockedDuration) != null ? _blockedDuration : 0;
    }

    public void setBlockedDuration(long duration) { _blockedDuration = duration; }

    public void setBlockedDuration(String duration) {
        _blockedDuration = StringUtil.durationStrToMSecs(duration);
    }

    public String getBlockType() { return get_blockType(); }

    public void setBlockType(String s) { set_blockType(s); }

    public String toString() {
        return String.format("%s: %s (%s)", getClass().getName(), getName(), getID());
    }


    // for hibernate
    private String get_blockType() { return _blockType.name(); }

    private void set_blockType(String s) {
        try {
            _blockType = BlockType.valueOf(s);
        }
        catch (Exception e) {
            _blockType = BlockType.Hard;                  // default
        }
    }
    
}
