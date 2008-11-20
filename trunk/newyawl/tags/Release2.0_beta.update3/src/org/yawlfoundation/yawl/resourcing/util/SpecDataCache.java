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
        SpecList list = _specs.get(spec.getID());
        if (list != null) {
            if (list.getSpecData(spec.getSpecVersion()) != null)
                return false;
            else {
                list.add(spec);
                return true;
            }
        }
        else {
            _specs.put(spec.getID(), new SpecList(spec));
            return true;
        }
    }


    public void remove(String specID, String version) {
        SpecList list = _specs.get(specID);
        if (list != null) {
            list.removeVersion(version);

            if (list.isEmpty())                      //just unloaded the only version
                _specs.remove(specID);
        }
    }

    public void remove(YSpecificationID spec) {
        remove(spec.getSpecName(), spec.getVersionAsString());
    }


    public void remove(SpecificationData spec) {
        SpecList list = _specs.get(spec.getID());
        if (list != null) {
            list.remove(spec);

            if (list.isEmpty())                      //just unloaded the only version
                _specs.remove(spec.getID());
        }
    }

    public SpecificationData get(String specid) {
        SpecList list = _specs.get(specid);
        if (list != null)
           return list.getLatestVersion();
        else
           return null ;
    }

    public SpecificationData get(String specid, String version) {
        SpecList list = _specs.get(specid);
        if (list != null)
           return list.getSpecData(version);
        else
           return null ;
    }
    
    public SpecificationData get(YSpecificationID spec) {
        return get(spec.getSpecName(), spec.getVersionAsString());
    }



    public boolean contains(String specid) {
        return _specs.containsKey(specid);
    }

    public boolean contains(SpecificationData spec) {
        return contains(spec.getID(), spec.getSpecVersion());
    }

    public boolean contains(String specid, String version) {
        return get(specid, version) != null ;
    }

    public Set<String> getSpecIDs() {
        Set<String> set = new HashSet<String>();
        for (SpecList list : _specs.values()) {
            set.add(list.getSpecID());
        }
        return set;
    }

    /********************************************************************************/

    private class SpecList extends ArrayList<SpecificationData> {

        private String _specID ;

        // Constructors //
        public SpecList(String id) {
            super() ;
            _specID = id ;
        }

        public SpecList(SpecificationData spec) {
            this(spec.getID());
            this.add(spec);
        }

        public String getSpecID() { return _specID ; }

        public SpecificationData getSpecData(String version) {
            for (SpecificationData spec : this) {
                if (spec.getSpecVersion().equals(version))
                    return spec ;
            }
            return null ;
        }


        public SpecificationData getLatestVersion() {
            if (this.isEmpty()) return null;

            SpecificationData latestSpec = this.get(0);
            YSpecVersion latestVersion = new YSpecVersion(latestSpec.getSpecVersion()); 
            for (SpecificationData spec : this) {
                YSpecVersion thisVersion = new YSpecVersion(spec.getSpecVersion());
                if (thisVersion.compareTo(latestVersion) > 0)
                    latestSpec = spec;
            }
            return latestSpec ;
        }


        public boolean removeVersion(String version) {
            YSpecVersion remVersion = new YSpecVersion(version);

            for (SpecificationData spec : this) {
                YSpecVersion thisVersion = new YSpecVersion(spec.getSpecVersion());
                if (thisVersion.equals(remVersion))
                    return remove(spec);
            }
            return false;            // not found
        }



    }

}