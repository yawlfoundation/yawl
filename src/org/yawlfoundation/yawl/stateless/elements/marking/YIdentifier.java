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

package org.yawlfoundation.yawl.stateless.elements.marking;

import org.yawlfoundation.yawl.stateless.elements.YCondition;
import org.yawlfoundation.yawl.stateless.elements.YInputCondition;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.stateless.elements.YTask;

import java.util.*;

/**
 * This class has control over data structures that allow for
 * storing an identifier and managing a set of children.
 *
 * @author Lachlan Aldred
 * @author Michael Adams (refactored for v2.0, 06/08 & 04/09)
 */
public class YIdentifier {

    // a location may be a condition or a task
    private final List<YNetElement> _locations = new ArrayList<>();
    private List<String> locationNames = new ArrayList<String>();
    private List<YIdentifier> _children = new ArrayList<YIdentifier>();
    private YIdentifier _parent;
    private String id = null;
    private String _idString;

    private long _logKey = -1;                    // the FK of the logged task instance


    public YIdentifier(String idString) {
        if (idString == null) {
            idString = UUID.randomUUID().toString();
        }
        _idString = idString;
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

    public void setChildren(List<YIdentifier> children) {
        _children = children;
    }


    public Set<YIdentifier> getDescendants() {
        Set<YIdentifier> descendants = new HashSet<YIdentifier>();
        descendants.add(this);

        for (YIdentifier child : _children) {
            if (child != null) descendants.addAll(child.getDescendants());
        }
        return descendants;
    }


    public YIdentifier createChild() {
        String newID = String.format("%s.%d", this._idString, _children.size() + 1);
        return createChildWithID(newID);
    }

    public void clearChildren() {
        _children.clear();
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
    public YIdentifier createChild(int childNum) {
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
        return createChildWithID(this._idString + "." + childNumStr);
    }


    private YIdentifier createChildWithID(String id) {
        YIdentifier identifier = new YIdentifier(id);
        _children.add(identifier);
        identifier.set_parent(this);
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


    public synchronized void addLocation(YNetElement condition) {
        if (condition == null) {
            throw new RuntimeException("Cannot add null condition to this identifier.");
        }
        _locations.add(condition);

        if ((condition instanceof YCondition) && !(condition instanceof YInputCondition)) {
            String locName = condition.toString();
            locationNames.add(locName.substring(locName.indexOf(":") + 1, locName.length()));
        } else {
            locationNames.add(condition.toString());
        }
    }


    public synchronized void clearLocations() {
        _locations.clear();
        locationNames.clear();
    }


    public synchronized void clearLocation(YNetElement condition) {
        removeLocation(condition);
    }


    public synchronized void removeLocation(YNetElement condition) {
        if (condition == null) {
            throw new RuntimeException("Cannot remove null condition from this identifier.");
        }

        _locations.remove(condition);

        if (condition instanceof YCondition && !(condition instanceof YInputCondition)) {
            String locName = condition.toString();
            locationNames.remove(locName.substring(locName.indexOf(":") + 1));
        } else {
            locationNames.remove(condition.toString());
        }
    }


    public synchronized void addLocation(YTask task) {
        if (task == null) {
            throw new RuntimeException("Cannot add null task to this identifier.");
        }
        _locations.add(task);
        locationNames.add(task.getID());
    }


    public synchronized void removeLocation(YTask task) {
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
        if (this == other) return true;
        if (other instanceof YIdentifier) {
            YIdentifier otherID = (YIdentifier) other;
            if ((toString() != null) && toString().equals(otherID.toString())) {
                return (getParent() == null) ? (otherID.getParent() == null) :
                        getParent().equals(otherID.getParent());
            }
        }
        return false;
    }

    public boolean equalsOrIsAncestorOf(YIdentifier another) {
        return equals(another) || isAncestorOf(another);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    public int hashCode() {
        return this.toString().hashCode();
    }
}
