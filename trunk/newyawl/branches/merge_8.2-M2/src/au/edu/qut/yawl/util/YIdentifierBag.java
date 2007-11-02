/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YPersistenceManager;
import au.edu.qut.yawl.exceptions.YPersistenceException;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * 
 */
public class YIdentifierBag {
    private Map _idToQtyMap = new HashMap();
    public YConditionInterface _condition;


    public YIdentifierBag(YConditionInterface condition) {
        _condition = condition;
    }


    public void addIdentifier(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        int amount = 0;
        if (_idToQtyMap.containsKey(identifier)) {
            amount = ((Integer) _idToQtyMap.get(identifier)).intValue();
        }
        _idToQtyMap.put(identifier, new Integer(++amount));
        identifier.addLocation(pmgr, _condition);
    }


    public int getAmount(YIdentifier identifier) {
        if (_idToQtyMap.containsKey(identifier)) {
            return ((Integer) _idToQtyMap.get(identifier)).intValue();
        } else
            return 0;
    }


    public boolean contains(YIdentifier identifier) {
        return _idToQtyMap.containsKey(identifier);
    }


    public List getIdentifiers() {
        List idList = new Vector();
        Set keys = _idToQtyMap.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
            YIdentifier identifier = (YIdentifier) iter.next();
            int amnt = ((Integer) _idToQtyMap.get(identifier)).intValue();
            for (int i = 0; i < amnt; i++) {
                idList.add(identifier);
            }
        }
        return idList;
    }


    public void remove(YPersistenceManager pmgr, YIdentifier identifier, int amountToRemove) throws YPersistenceException {
        if (_idToQtyMap.containsKey(identifier)) {
            int amountExisting = ((Integer) _idToQtyMap.get(identifier)).intValue();
            if (amountToRemove <= 0) {
                throw new RuntimeException("You cannot remove " + amountToRemove
                        + " from YIdentifierBag:" + _condition + " " + identifier.toString());
            } else if (amountExisting > amountToRemove) {
                _idToQtyMap.put(identifier, new Integer(amountExisting - amountToRemove));
                identifier.removeLocation(pmgr, _condition);
            } else if (amountToRemove == amountExisting) {
                _idToQtyMap.remove(identifier);
                identifier.removeLocation(pmgr, _condition);
            } else {
                throw new RuntimeException("You cannot remove " + amountToRemove
                        + " tokens from YIdentifierBag:" + _condition
                        + " - this bag only contains " + amountExisting
                        + " identifiers of type " + identifier.toString());
            }
        } else {
            throw new RuntimeException("You cannot remove " + amountToRemove
                    + " tokens from YIdentifierBag:" + _condition
                    + " - this bag contains no"
                    + " identifiers of type " + identifier.toString()
                    + ".  It does have " + this.getIdentifiers()
                    + " (locations of " + identifier + ":" + identifier.getLocations() + " )"
            );
        }
    }


    public void removeAll() {
        Iterator keys = new Vector(_idToQtyMap.keySet()).iterator();
        while (keys.hasNext()) {
            YIdentifier identifier = (YIdentifier) keys.next();
            _idToQtyMap.remove(identifier);
            while (identifier.getLocations().contains(_condition)) {
                identifier.getLocations().remove(_condition);
            }
        }
    }
}
