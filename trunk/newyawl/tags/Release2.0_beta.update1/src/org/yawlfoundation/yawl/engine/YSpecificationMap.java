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
    private Hashtable<String, Hashtable<Double, YSpecification>> specid2versions;

    public YSpecificationMap()
    {
        this.specid2versions = new Hashtable<String, Hashtable<Double, YSpecification>>();
    }

    public boolean loadSpecification(YSpecification spec)
    {
        if(specid2versions.containsKey(spec.getID()))
        {
            Hashtable<Double, YSpecification> versions2Spec = specid2versions.get(spec.getID());

            if(versions2Spec.containsKey(spec.getMetaData().getVersion().getVersionAsDouble()))
            {
                return false;
            }
            else
            {
                versions2Spec.put(spec.getMetaData().getVersion().getVersionAsDouble(), spec);
                return true;
            }
        }
        else
        {
            Hashtable<Double, YSpecification> versions2Spec = new Hashtable<Double, YSpecification>();
            versions2Spec.put(spec.getMetaData().getVersion().getVersionAsDouble(), spec);
            specid2versions.put(spec.getID(), versions2Spec);
            return true;
        }
    }

    public void unloadSpecification(YSpecification spec)
    {
        unloadSpecification(spec.getID(), spec.getMetaData().getVersion());
    }

    public void unloadSpecification(String specid, YSpecVersion version)
    {
        if(specid2versions.containsKey(specid))
        {
            Hashtable<Double, YSpecification> versions2Spec = specid2versions.get(specid);
            if(versions2Spec.containsKey(version.getVersionAsDouble()))
            {
                versions2Spec.remove(version.getVersionAsDouble());
            }

            if(versions2Spec.size() == 0) //just unloaded the only version
            {
                specid2versions.remove(specid);
            }
        }
    }

    public YSpecification getSpecification(String specid)
    {
        if(specid2versions.containsKey(specid))
        {
            Hashtable<Double, YSpecification> versions2Spec = specid2versions.get(specid);

            Double highestKey = Double.MIN_VALUE;
            for(Double value : versions2Spec.keySet())
            {
                if(value > highestKey) highestKey = value;
            }

            return versions2Spec.get(highestKey);
        }
        else return null;
    }

    public YSpecification getSpecification(String specid, YSpecVersion version)
    {
        if(specid2versions.containsKey(specid))
        {
            Hashtable<Double, YSpecification> versions2Spec = specid2versions.get(specid);
            return versions2Spec.get(version.getVersionAsDouble());
        }
        else return null;
    }

    public YSpecification getSpecification(YSpecificationID id)
    {
        return getSpecification(id.getSpecName(), id.getVersion());
    }

    public boolean contains(String spec)
    {
        return specid2versions.containsKey(spec);
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
        if(specid2versions.containsKey(specid))
        {
            Hashtable<Double, YSpecification> versions2Spec = specid2versions.get(specid);
            return versions2Spec.containsKey(version.getVersionAsDouble());
        }
        else return false;
    }

    public Set<YSpecificationID> getSpecIDs()
    {
        Set<YSpecificationID> set = new HashSet<YSpecificationID>();
        for(String specid : specid2versions.keySet())
        {
            for(Double version : specid2versions.get(specid).keySet())
            {
                YSpecVersion specVersion = new YSpecVersion(String.valueOf(version));
                set.add(new YSpecificationID(specid, specVersion));
            }
        }

        return set;
    }   
}
