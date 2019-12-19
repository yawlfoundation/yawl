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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


/**
 * @author Michael Adams (modelled on YSpecificationTable)
 *         Date: 01/09/2008
 */
public class SpecDataCache {
    private Hashtable<String, SpecList> _specs ;


    public SpecDataCache() {
        _specs = new Hashtable<String, SpecList>();
    }

    public boolean add(SpecificationData spec) {
        SpecList list = _specs.get(spec.getID().getKey());
        if (list != null) {
            if (list.getSpecData(spec.getSpecVersion()) != null)
                return false;
            else {
                list.add(spec);
                return true;
            }
        }
        else {
            _specs.put(spec.getID().getKey(), new SpecList(spec));
            return true;
        }
    }


    public void remove(YSpecificationID specID) {
        SpecList list = _specs.get(specID.getKey());
        if (list != null) {
            list.removeVersion(specID);

            if (list.isEmpty())                      //just unloaded the only version
                _specs.remove(specID.getKey());
        }
    }


    public void remove(SpecificationData spec) {
        SpecList list = _specs.get(spec.getID().getKey());
        if (list != null) {
            list.remove(spec);

            if (list.isEmpty())                      //just unloaded the only version
                _specs.remove(spec.getID().getKey());
        }
    }


    public SpecificationData get(YSpecificationID specID) {
        SpecList list = _specs.get(specID.getKey());
        if (list != null)
           return list.getSpecData(specID.getVersionAsString());
        else
           return null ;
    }


    public Set<YSpecificationID> getSpecIDs() {
        Set<YSpecificationID> set = new HashSet<YSpecificationID>();
        for (SpecList list : _specs.values()) {
            set.add(list.getSpecID());
        }
        return set;
    }

    /********************************************************************************/

    private class SpecList extends ArrayList<SpecificationData> {

        private YSpecificationID _specID ;

        // Constructor //
        public SpecList(SpecificationData spec) {
            _specID = spec.getID();
            this.add(spec);
        }

        public YSpecificationID getSpecID() { return _specID ; }

        public SpecificationData getSpecData(String version) {
            for (SpecificationData spec : this) {
                if (spec.getSpecVersion().equals(version))
                    return spec ;
            }
            return null ;
        }


        public SpecificationData getLatestVersion() {
            SpecificationData latestSpec = null;
            if (! this.isEmpty()) {
                latestSpec = this.get(0);
                if (this.size() > 1) {
                    YSpecVersion latestVersion = latestSpec.getID().getVersion();
                    for (SpecificationData spec : this) {
                        YSpecVersion thisVersion = spec.getID().getVersion();
                        if (thisVersion.compareTo(latestVersion) > 0)
                            latestSpec = spec;
                    }
                }
            }
            return latestSpec ;
        }


        public boolean removeVersion(YSpecificationID specID) {
            YSpecVersion remVersion = specID.getVersion();

            for (SpecificationData spec : this) {
                YSpecVersion thisVersion = spec.getID().getVersion();
                if (thisVersion.equals(remVersion))
                    return remove(spec);
            }
            return false;            // not found
        }

    }

}