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

package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A mapping of specification identifiers to a list of versions of that specification.
 * The key used is the spec's unique identifier (introduced in v2.0); for earlier
 * schema versions, it falls back to the spec's name (uri)
 *
 * @author Michael Adams (a refactor of Mike Fowler's YSpecificationMap)
 * @since 2.0
 * @date 06/06/2008
 */
public class YSpecificationTable
        extends ConcurrentHashMap<String, YSpecificationTable.SpecList> {


    public YSpecificationTable() { super(); }


    public boolean loadSpecification(YSpecification spec) {
        String key = spec.getSpecificationID().getKey();
        SpecList list = super.get(key);
        if (list != null) {
            if (list.getSpecification(spec.getMetaData().getVersion()) != null)
                return false;
            else {
                list.add(spec);
                return true;
            }
        }
        else {
            super.put(key, new SpecList(spec));
            return true;
        }
    }

    public void unloadSpecification(YSpecification spec) {
        if (spec != null) {
            String key = spec.getSpecificationID().getKey();
            SpecList list = super.get(key);
            if (list != null) {
                list.remove(spec);

                if (list.isEmpty())                    // just unloaded the only version
                    super.remove(key);
            }    
        }
    }


    public YSpecification getSpecification(YSpecificationID specid) {
        if (specid != null) {
            SpecList list = super.get(specid.getKey());
            if (list != null) {
                return list.getSpecification(specid.getVersion());
            }
        }
        return null ;
    }


    /**
     * Gets the latest version of the specification with the key passed
     * @param key either the identifier or uri of the specification
     * @return the specification with the latest version number that matches the key
     */
    public YSpecification getLatestSpecification(String key) {
        if (key != null) {
            SpecList list = super.get(key);
            if (list != null) return list.getLatestVersion();
        }
        return null ;
    }

    public boolean contains(String key) {
        return super.containsKey(key);
    }

    public boolean contains(YSpecification spec) {
        return contains(spec.getSpecificationID());
    }

    public boolean contains(YSpecificationID specID) {
        return getSpecification(specID) != null ;
    }


    public Set<YSpecificationID> getSpecIDs() {
        Set<YSpecificationID> set = new HashSet<YSpecificationID>();
        for (SpecList list : this.values()) {
            set.addAll(list.getSpecificationIDs());
        }
        return set;
    }


    /********************************************************************************/

    protected class SpecList extends ArrayList<YSpecification> {

        // Constructor //
        public SpecList(YSpecification spec) {
            super();
            this.add(spec);
        }


        public YSpecification getSpecification(YSpecVersion version) {
            for (YSpecification ySpec : this) {
                if (ySpec.getSpecificationID().getVersion().equals(version))
                    return ySpec ;
            }
            return null ;
        }


        public Set<YSpecificationID> getSpecificationIDs() {
            Set<YSpecificationID> set = new HashSet<YSpecificationID>();
            for (YSpecification ySpec : this) {
                set.add(ySpec.getSpecificationID());
            }
            return set;
        }


        public YSpecification getLatestVersion() {
            YSpecification latest = null;                      // default for empty list
            if (! this.isEmpty()) {
                latest = this.get(0);                          // at least one
                if (this.size() > 1) {                         // more than one
                    for (YSpecification ySpec : this) {
                        if (ySpec.getSpecificationID().getVersion().compareTo(
                                latest.getSpecificationID().getVersion()) > 0)
                            latest = ySpec;
                    }
                }
            }
            return latest ;
        }

    }

}
