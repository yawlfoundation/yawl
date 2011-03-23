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

package org.yawlfoundation.yawl.elements.state;

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * This class has control over data structures that allow for
 * storing an identifer and managing a set of children.
 * @author Lachlan Aldred
 *
 * @author Michael Adams (refactored for v2.0, 06/08 & 04/09)
 * 
 */
public class YIdentifier {

    // a location may be a condition or a task
    private List<YNetElement> _locations = new Vector<YNetElement>();

    private List<String> locationNames = new Vector<String>();
    private List<YIdentifier> _children = new Vector<YIdentifier>();
    private YIdentifier _parent;
    private String id = null;
    private String _idString;

    private long _logKey = -1 ;                    // the FK of the logged task instance


    public YIdentifier() { }                       // only for hibernate


    public YIdentifier(String idString) {
        _idString = (idString != null) ? idString : YEngine.getInstance().getNextCaseNbr();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List<String> names) {
        locationNames = names;
    }

    public String get_idString() {
        return _idString;
    }

    public void set_idString(String id) {
        _idString = id;
    }

    public void set_children(List<YIdentifier> children) {
        _children = children;
    }

    public List<YIdentifier> get_children() {
        return _children;
    }

    public YIdentifier get_parent() {
        return _parent;
    }

    public void set_parent(YIdentifier parent) {
        _parent = parent;
    }


    public List<YIdentifier> getChildren() {
        return _children;
    }


    public Set<YIdentifier> getDescendants() {
        Set<YIdentifier> descendants = new HashSet<YIdentifier>();
        descendants.add(this);

        for (YIdentifier child : _children) {
            descendants.addAll(child.getDescendants());
        }
        return descendants;
    }


    public YIdentifier createChild(YPersistenceManager pmgr) throws YPersistenceException {
        String newID = String.format("%s.%d", this._idString, _children.size() + 1);
        return createChildWithID(pmgr, newID);
    }

    public void clearChildren() {
        _children = new Vector<YIdentifier>();
    }

    public boolean removeChild(YIdentifier child) {
        return _children.remove(child);
    }


    /**
     * Creates a child identifier.
     *
     * @param childNum
     * @return the child YIdentifier object with id == childNum
     */
    public YIdentifier createChild(YPersistenceManager pmgr, int childNum)
            throws YPersistenceException {
        if (childNum < 1) {
            throw new IllegalArgumentException("Childnum must be > 0");
        }
        String childNumStr = "" + childNum;
        for (YIdentifier child : _children) {
            String childID = child.toString();
            String childIDSuffix = childID.substring(childID.lastIndexOf('.') + 1);
            if (childNumStr.equals(childIDSuffix)) {
                throw new IllegalArgumentException(
                                   "Childnum uses an int already being used.");
            }
        }
        return createChildWithID(pmgr, this._idString + "." + childNumStr);
    }


    private YIdentifier createChildWithID(YPersistenceManager pmgr, String id)
            throws YPersistenceException {

        YIdentifier identifier = new YIdentifier(id);
        _children.add(identifier);
        identifier.set_parent(this);

        if (pmgr != null) {
            pmgr.storeObjectFromExternal(identifier);
            pmgr.updateObjectExternal(this);
        }
        return identifier;
    }


    public YIdentifier getParent() {
        return _parent;
    }

    public boolean hasParent() { return _parent != null; } 


    public boolean isImmediateChildOf(YIdentifier identifier) {
        return (_parent == identifier);
    }

    public boolean isAncestorOf(YIdentifier identifier) {
        YIdentifier parent = identifier.getParent();
        return parent != null && (parent.equals(this) || isAncestorOf(parent));
    }

    
    public String toString() {
        return _idString;
    }


    public synchronized void addLocation(YPersistenceManager pmgr, YNetElement condition)
            throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot add null condition to this identifier.");
        }
        _locations.add(condition);

        if ((condition instanceof YCondition) && !(condition instanceof YInputCondition)) {
            String locName = condition.toString();
            locationNames.add(locName.substring(locName.indexOf(":") + 1, locName.length()));
        }
        else {
            locationNames.add(condition.toString());
        }

        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }

    
    public synchronized void clearLocations(YPersistenceManager pmgr)
            throws YPersistenceException {
        _locations.clear();
        locationNames.clear();
        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void clearLocation(YPersistenceManager pmgr, YNetElement condition)
            throws YPersistenceException {
        removeLocation(pmgr, condition);
        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void removeLocation(YPersistenceManager pmgr,
                                            YNetElement condition)
            throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot remove null condition from this identifier.");
        }

        _locations.remove(condition);

        if (condition instanceof YCondition && !(condition instanceof YInputCondition)) {
            String locName = condition.toString();
            locationNames.remove(locName.substring(locName.indexOf(":") + 1, locName.length()));
        }
        else {
            locationNames.remove(condition.toString());
        }
    }


    public synchronized void addLocation(YPersistenceManager pmgr, YTask task)
            throws YPersistenceException {
        if (task == null) {
            throw new RuntimeException("Cannot add null task to this identifier.");
        }
        _locations.add(task);
        locationNames.add(task.getID());

        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void removeLocation(YPersistenceManager pmgr, YTask task)
            throws YPersistenceException {
        if (task == null) {
            throw new RuntimeException("Cannot remove null task from this identifier.");
        }
        _locations.remove(task);
        locationNames.remove(task.getID());
    }


    public synchronized List<YNetElement> getLocations() {
        return _locations;
    }

    
    public YIdentifier getRootAncestor() {
        return getRootAncestor(this);
    }

    
    private YIdentifier getRootAncestor(YIdentifier identifier) {
        YIdentifier parent = identifier.getParent();
        return (parent != null) ? getRootAncestor(parent) : identifier;
    }

    public long getLogKey() {
        return _logKey;
    }

    public void setLogKey(long key) {
        _logKey = key;
    }

    public boolean equals(Object other) {
        return (this == other) ||
              ((other instanceof YIdentifier) && other.toString().equals(this.toString()));
    }

    public boolean equalsOrIsAncestorOf(YIdentifier another) {
        return equals(another) || isAncestorOf(another);
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    public int hashCode() {
        return this.toString().hashCode();
    }
}
