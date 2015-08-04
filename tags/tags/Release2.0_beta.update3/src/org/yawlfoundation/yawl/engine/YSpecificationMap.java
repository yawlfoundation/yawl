package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * @author Mike Fowler
 *         Date: 24-Aug-2006
 */
public class YSpecificationMap
{
    // [specid, [version, specification]
    
    private Hashtable<String, Hashtable<String, YSpecification>> _specid2versions;

    public YSpecificationMap()
    {
        _specid2versions = new Hashtable<String, Hashtable<String, YSpecification>>();
    }

    public boolean loadSpecification(YSpecification spec)
    {
        // if a version of this spec is loaded
        if(_specid2versions.containsKey(spec.getID()))
        {
            Hashtable<String, YSpecification> versions2Spec = _specid2versions.get(spec.getID());

            // don't load if this version is already loaded
            if(versions2Spec.containsKey(spec.getMetaData().getVersion().toString()))
            {
                return false;
            }
            else
            {
                versions2Spec.put(spec.getMetaData().getVersion().toString(), spec);
                return true;
            }
        }
        else  // no versions of this spec loaded
        {
            Hashtable<String, YSpecification> versions2Spec = new Hashtable<String, YSpecification>();
            versions2Spec.put(spec.getMetaData().getVersion().toString(), spec);
            _specid2versions.put(spec.getID(), versions2Spec);
            return true;
        }
    }

    public void unloadSpecification(YSpecification spec)
    {
        unloadSpecification(spec.getID(), spec.getMetaData().getVersion());
    }

    public void unloadSpecification(String specid, YSpecVersion version)
    {
        if(_specid2versions.containsKey(specid))
        {
            Hashtable<String, YSpecification> versions2Spec = _specid2versions.get(specid);
            if(versions2Spec != null)
            {
                versions2Spec.remove(version.toString());

                if(versions2Spec.size() == 0)         //just unloaded the only version
                {
                    _specid2versions.remove(specid);
                }
            }
        }
    }

    public YSpecification getSpecification(String specid)
    {
        YSpecification result = null;

        if(_specid2versions.containsKey(specid))
        {
            Hashtable<String, YSpecification> versions2Spec = _specid2versions.get(specid);

            // get spec with highest version number
            for(YSpecification spec : versions2Spec.values())
            {
                if (result == null) {
                    result = spec;
                }
                else {
                    YSpecVersion resultVer = result.getMetaData().getVersion();
                    YSpecVersion currentVer = spec.getMetaData().getVersion();
                    if (currentVer.compareTo(resultVer) > 0) result = spec;
                }
            }
        }
        return result;
    }

    public YSpecification getSpecification(String specid, YSpecVersion version)
    {
        if(_specid2versions.containsKey(specid))
        {
            Hashtable<String, YSpecification> versions2Spec = _specid2versions.get(specid);
            return versions2Spec.get(version.toString());
        }
        else return null;
    }

    public YSpecification getSpecification(YSpecificationID id)
    {
        return getSpecification(id.getSpecName(), id.getVersion());
    }

    public boolean contains(String spec)
    {
        return _specid2versions.containsKey(spec);
    }

    public boolean contains(YSpecification spec)
    {
        return contains(spec.getID(), spec.getMetaData().getVersion());
    }

    public boolean contains(YSpecificationID specID)
    {
        return contains(specID.getSpecName(), specID.getVersion());
    }

    public boolean contains(String specid, YSpecVersion version)
    {
        if(_specid2versions.containsKey(specid))
        {
            Hashtable<String, YSpecification> versions2Spec = _specid2versions.get(specid);
            return versions2Spec.containsKey(version.toString());
        }
        else return false;
    }

    public Set<YSpecificationID> getSpecIDs()
    {
        Set<YSpecificationID> set = new HashSet<YSpecificationID>();
        for(String specid : _specid2versions.keySet())
        {
            for(YSpecification spec : _specid2versions.get(specid).values())
            {
                set.add(new YSpecificationID(specid, spec.getMetaData().getVersion()));
            }
        }

        return set;
    }   
}
