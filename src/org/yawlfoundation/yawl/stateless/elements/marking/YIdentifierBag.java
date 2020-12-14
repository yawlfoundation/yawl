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

import org.yawlfoundation.yawl.elements.YNetElement;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * @author Michael Adams (updated for 2.0)
 * 
 */
public class YIdentifierBag {

    private final Map<YIdentifier, Integer> _idToQtyMap = new HashMap<YIdentifier, Integer>();
    public YNetElement _condition;


    public YIdentifierBag(YNetElement condition) {
        _condition = condition;
    }


    public void addIdentifier(YIdentifier identifier) {
        int amount = getAmount(identifier);
        _idToQtyMap.put(identifier, ++amount);
        identifier.addLocation(_condition);
    }


    public int getAmount(YIdentifier identifier) {
        int amount = 0;
        if (_idToQtyMap.containsKey(identifier)) {
            amount = _idToQtyMap.get(identifier);
        }
        return amount;
    }


    public boolean contains(YIdentifier identifier) {
        return _idToQtyMap.containsKey(identifier);
    }


    public List<YIdentifier> getIdentifiers() {
        List<YIdentifier> idList = new Vector<YIdentifier>();
        for (YIdentifier identifier : _idToQtyMap.keySet()) {
            int amount = _idToQtyMap.get(identifier);
            for (int i = 0; i < amount; i++) {
                idList.add(identifier);
            }
        }
        return idList;
    }


    public void remove(YIdentifier identifier, int amountToRemove) {
        if (_idToQtyMap.containsKey(identifier)) {
            int amountExisting = _idToQtyMap.get(identifier);
            if (amountToRemove <= 0) {
                throw new RuntimeException("Cannot remove " + amountToRemove
                        + " from YIdentifierBag:" + _condition + " " + identifier.toString());
            }
            else if (amountToRemove > amountExisting) {
                throw new RuntimeException("Cannot remove " + amountToRemove
                        + " tokens from YIdentifierBag:" + _condition
                        + " - this bag only contains " + amountExisting
                        + " identifiers of type " + identifier.toString());

            }

            int amountLeft = amountExisting - amountToRemove;
            if (amountLeft > 0) {
                _idToQtyMap.put(identifier, amountLeft);
            }
            else {
                _idToQtyMap.remove(identifier);
            } 
            identifier.removeLocation(_condition);
        }
        else {
            throw new RuntimeException("Cannot remove " + amountToRemove
                    + " tokens from YIdentifierBag:" + _condition
                    + " - this bag contains no"
                    + " identifiers of type " + identifier.toString()
                    + ".  It does have " + this.getIdentifiers()
                    + " (locations of " + identifier + ":" + identifier.getLocations() + " )"
            );
        }
    }


    public void removeAll() {
        Set<YIdentifier> identifiers = new HashSet<YIdentifier>(_idToQtyMap.keySet());
        for (YIdentifier identifier : identifiers) {
            while (identifier.getLocations().contains(_condition)) {
                identifier.clearLocation(_condition);
            }
            _idToQtyMap.remove(identifier);
        }
    }
}
