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

package org.yawlfoundation.yawl.worklet.rdr;

import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.util.Hashtable;
import java.util.Map;

/**
 * A cache of loaded rule sets
 * @author Michael Adams
 * @date 27/03/12
 */
public class RdrCache {
    
    private Map<String, RdrSet> _cache;
    
    public RdrCache() {
        _cache = new Hashtable<String, RdrSet>();
    }


    public Map<String, RdrSet> getAll() { return _cache; }

    
    public RdrSet get(YSpecificationID specID) {
        if (specID == null) return null;
        RdrSet set = _cache.get(specID.getUri());
        if (set == null) {
            set = new RdrSet(specID);               // loads rules from store
            put(specID.getUri(), set);
        }
        return set;
    }
    
    public RdrSet put(YSpecificationID specID, RdrSet set) {
        if (! (specID == null || set == null)) {
            return _cache.put(specID.getUri(), set);
        }
        return null;
    }


    public RdrSet remove(YSpecificationID specID) {
        return (specID != null) ? _cache.remove(specID.getUri()) : null;
    }
    
    public RdrSet refresh(YSpecificationID specID) {
        remove(specID);
        return get(specID);
    }

    public boolean contains(YSpecificationID specID) {
        return _cache.containsKey(specID.getUri());
    }


    public RdrSet get(String name) {
        if (name == null) return null;
        RdrSet set = _cache.get(name);
        if (set == null) {
            set = new RdrSet(name);               // loads rules from store
            put(name, set);
        }
        return set;
    }

    public RdrSet put(String name, RdrSet set) {
        if (! (name == null || set == null)) {
            return _cache.put(name, set);
        }
        return null;
    }

    public RdrSet remove(String name) {
        return (name != null) ? _cache.remove(name) : null;
    }

    public RdrSet refresh(String name) {
        remove(name);
        return get(name);
    }

    public boolean contains(String name) {
        return _cache.containsKey(name);
    }

    public void update(RdrSet set) {
        _cache.put(set.getName(), set);
    }

}
