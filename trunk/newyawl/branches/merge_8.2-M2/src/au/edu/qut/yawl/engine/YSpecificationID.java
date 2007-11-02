package au.edu.qut.yawl.engine;

/**
 * @author Mike Fowler
 *         Date: 05-Sep-2006
 */
public class YSpecificationID implements Comparable<YSpecificationID>
{
    private String specID;
    private double version;

    public YSpecificationID(String specID, double version)
    {
        this.specID = specID;
        this.version = version;
    }

    public String getSpecID()
    {
        return specID;
    }

    public double getVersion()
    {
        return version;
    }

    @Override public boolean equals(Object obj)
    {
        if (obj instanceof YSpecificationID)
        {
            YSpecificationID id = (YSpecificationID) obj;

            return id.getSpecID().equals(getSpecID()) && id.getVersion() == getVersion();
        }
        else
        {
            return false;
        }
    }

    @Override public String toString()
    {
        return specID + " - version " + version;
    }


    public int compareTo(YSpecificationID o)
    {
        if(specID.equals(o.getSpecID()))
        {
            if(version == o.getVersion()) return 0;
            else if(version > o.getVersion()) return 1;
            else return -1;
        }
        else return o.getSpecID().compareTo(specID);
    }
}
