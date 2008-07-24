/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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
 * @author Michael Adams (refactored for v2.0, 06/08)
 * 
 */
public class YIdentifier {

    // a location may be a condition or a task
    private List _locations = new Vector();

    private List<String> locationNames = new Vector<String>();
    private List<YIdentifier> _children = new Vector<YIdentifier>();
    private YIdentifier _parent;
    private String id = null;
    private String _idString;


    public YIdentifier() {
         _idString = YEngine.getInstance().getNextCaseNbr();
     }

     public YIdentifier(String idString) {
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


    public boolean isImmediateChildOf(YIdentifier identifier) {
        return (_parent == identifier);
    }


    public String toString() {
        return this._idString;
    }


    public synchronized void addLocation(YPersistenceManager pmgr,
                                         YConditionInterface condition)
            throws YPersistenceException {
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

        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void removeLocation(YPersistenceManager pmgr,
                                            YConditionInterface condition)
            throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot remove null condition from this identifier.");
        }

        _locations.remove(condition);

        if (condition instanceof YCondition && !(condition instanceof YInputCondition)) {
            String locName = condition.toString();
            locationNames.remove(locName.substring(locName.indexOf(":") + 1, locName.length()));
        } else {
            locationNames.remove(condition.toString());
        }

        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
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


    public synchronized List getLocations() {
        return _locations;
    }

    
    public YIdentifier getAncestor() {
        if (null != this.getParent()) {
            return getAncestor();
        } else
            return this;
    }


    public boolean equals(Object another) {
        return (another instanceof YIdentifier) &&
                another.toString().equals(this.toString());
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
