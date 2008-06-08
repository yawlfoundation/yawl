package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YSpecVersion;

/**
 * @author Mike Fowler
 *         Date: 05-Sep-2006
 */
public class YSpecificationID implements Comparable<YSpecificationID>
{
    private String specName;
    private YSpecVersion version;

    public YSpecificationID(String specName, YSpecVersion version)
    {
        this.specName = specName;
        this.version = version;
    }

    public String getSpecName()
    {
        return specName;
    }

    public String getVersionAsString()
    {
        return version.toString();
    }

    public YSpecVersion getVersion() { return version; }

    public void setSpecName(String name) { specName = name ; }

    public void setVersion(String ver) { version.setVersion(ver) ; }

    public void setVersion(YSpecVersion ver) { version = ver ; }

    @Override public boolean equals(Object obj)
    {
        if (obj instanceof YSpecificationID)
        {
            YSpecificationID id = (YSpecificationID) obj;

            return id.getSpecName().equals(getSpecName()) &&
                   id.getVersion().equals(getVersion());
        }
        else
        {
            return false;
        }
    }

    @Override public String toString()
    {
        return specName + " - version " + version.toString();
    }


    public int compareTo(YSpecificationID o)
    {
        if(specName.equals(o.getSpecName()))
        {
          return version.compareTo(o.getVersion());
        }
        else return o.getSpecName().compareTo(specName);
    }
}
