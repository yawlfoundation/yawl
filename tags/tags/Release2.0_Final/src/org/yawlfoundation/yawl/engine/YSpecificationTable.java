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
    private Hashtable<String, SpecList> _specs ;


    public YSpecificationTable() {
        _specs = new Hashtable<String, SpecList>();
    }

    public boolean loadSpecification(YSpecification spec) {
        SpecList list = _specs.get(spec.getID());
        if (list != null) {
            if (list.getSpecification(spec.getMetaData().getVersion()) != null)
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

    public void unloadSpecification(YSpecification spec) {
        SpecList list = _specs.get(spec.getID());
        if (list != null) {
            list.remove(spec);

            if (list.isEmpty())                      //just unloaded the only version            
                _specs.remove(spec.getID());
        }
    }

    public YSpecification getSpecification(String specid) {
        SpecList list = _specs.get(specid);
        if (list != null)
           return list.getLatestVersion();
        else
           return null ;
    }

    public YSpecification getSpecification(String specid, YSpecVersion version) {
        SpecList list = _specs.get(specid);
        if (list != null)
           return list.getSpecification(version);
        else
           return null ;
    }

    public YSpecification getSpecification(YSpecificationID id) {
        return getSpecification(id.getSpecName(), id.getVersion());
    }

    public boolean contains(String specid) {
        return _specs.containsKey(specid);
    }

    public boolean contains(YSpecification spec) {
        return contains(spec.getID(), spec.getMetaData().getVersion());
    }

    public boolean contains(YSpecificationID specID) {
        return contains(specID.getSpecName(), specID.getVersion());
    }

    public boolean contains(String specid, YSpecVersion version) {
        return getSpecification(specid, version) != null ;
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

        private String _specID ;

        // Constructors //
        public SpecList(String id) {
            super() ;
            _specID = id ;
        }

        public SpecList(YSpecification spec) {
            this(spec.getID());
            this.add(spec);
        }

        public String getSpecID() { return _specID ; }

        public YSpecification getSpecification(YSpecVersion version) {
            for (YSpecification ySpec : this) {
                if (ySpec.getMetaData().getVersion().equals(version))
                    return ySpec ;
            }
            return null ;
        }

        public Set<YSpecificationID> getSpecificationIDs() {
            Set<YSpecificationID> set = new HashSet<YSpecificationID>();
            for (YSpecification ySpec : this) {
                set.add(new YSpecificationID(ySpec.getID(),
                        ySpec.getMetaData().getVersion()));
            }
            return set;
        }

        public YSpecification getLatestVersion() {
            if (this.isEmpty()) return null;

            YSpecification latest = this.get(0);
            for (YSpecification ySpec : this) {
                if (ySpec.getMetaData().getVersion().compareTo(
                    latest.getMetaData().getVersion()) > 0)
                    latest = ySpec;
            }
            return latest ;
        }

    }

}
