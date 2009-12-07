package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


/**
 * @author Michael Adams (a refactor of Mike Fowler's YSpecificationMap for v2.0)
 *         Date: 06/06/2008
 */
public class YSpecificationTable {

    // A mapping of specification identifiers to a list of versions of that specification
    // The key used is the spec's unique identifier (introduced in v2.0); for earlier
    // schema versions, it falls back to the spec's name (uri)
    private Hashtable<String, SpecList> _specs ;


    public YSpecificationTable() {
        _specs = new Hashtable<String, SpecList>();
    }


    public boolean loadSpecification(YSpecification spec) {
        String key = spec.getSpecificationID().getKey();
        SpecList list = _specs.get(key);
        if (list != null) {
            if (list.getSpecification(spec.getMetaData().getVersion()) != null)
                return false;
            else {
                list.add(spec);
                return true;
            }
        }
        else {
            _specs.put(key, new SpecList(spec));
            return true;
        }
    }

    public void unloadSpecification(YSpecification spec) {
        String key = spec.getSpecificationID().getKey();
        SpecList list = _specs.get(key);
        if (list != null) {
            list.remove(spec);

            if (list.isEmpty())                      // just unloaded the only version
                _specs.remove(key);
        }
    }


    public YSpecification getSpecification(YSpecificationID specid) {
        SpecList list = _specs.get(specid.getKey());
        if (list != null)
           return list.getSpecification(specid.getVersion());
        else
           return null ;
    }


    /**
     * Gets the latest version of the specification with the key passed
     * @param key either the identifier or uri of the specification
     * @return
     */
    public YSpecification getLatestSpecification(String key) {
        SpecList list = _specs.get(key);
        if (list != null)
           return list.getLatestVersion();
        else
           return null ;
    }

    public boolean contains(String key) {
        return _specs.containsKey(key);
    }

    public boolean contains(YSpecification spec) {
        return contains(spec.getSpecificationID());
    }

    public boolean contains(YSpecificationID specID) {
        return getSpecification(specID) != null ;
    }


    public Set<YSpecificationID> getSpecIDs() {
        Set<YSpecificationID> set = new HashSet<YSpecificationID>();
        for (SpecList list : _specs.values()) {
            set.addAll(list.getSpecificationIDs());
        }
        return set;
    }


    /********************************************************************************/

    private class SpecList extends ArrayList<YSpecification> {

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
