package org.yawlfoundation.yawl.engine;

/**
 * @author Mike Fowler
 *         Date: 05-Sep-2006
 */
public class YSpecificationID implements Comparable<YSpecificationID>
{
    private String specName;
    private double version;

    public YSpecificationID(String specName, double version)
    {
        this.specName = specName;
        this.version = version;
    }

    public String getSpecName()
    {
        return specName;
    }

    public double getVersion()
    {
        return version;
    }

    public void setSpecName(String name) { specName = name ; }

    public void setVersion(double ver) { version = ver ; }

    @Override public boolean equals(Object obj)
    {
        if (obj instanceof YSpecificationID)
        {
            YSpecificationID id = (YSpecificationID) obj;

            return id.getSpecName().equals(getSpecName()) && id.getVersion() == getVersion();
        }
        else
        {
            return false;
        }
    }

    @Override public String toString()
    {
        return specName + " - version " + version;
    }


    public int compareTo(YSpecificationID o)
    {
        if(specName.equals(o.getSpecName()))
        {
            if(version == o.getVersion()) return 0;
            else if(version > o.getVersion()) return 1;
            else return -1;
        }
        else return o.getSpecName().compareTo(specName);
    }
}
