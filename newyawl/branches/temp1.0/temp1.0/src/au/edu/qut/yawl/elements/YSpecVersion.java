package au.edu.qut.yawl.elements;

/**
 * Created by IntelliJ IDEA. User: Default Date: 18/10/2007 Time: 12:44:41 To change this
 * template use File | Settings | File Templates.
 */
public class YSpecVersion {
    private int _major ;
    private int _minor ;

    public YSpecVersion() {
        _major = 1 ;
        _minor = 1 ;
    }

    public YSpecVersion(int major, int minor) {
        setVersion(major, minor) ;
    }

    public YSpecVersion(String version) {
        setVersion(version);
    }


    public String setVersion(int major, int minor) {
        _major = major ;
        _minor = minor ;
        return getVersion();
    }

    public String setVersion(String version) {
        String[] part = version.split("\\.");
        _major = Integer.parseInt(part[0]);
        _minor = Integer.parseInt(part[1]);
        return getVersion();
    }

    public String getVersion() {
        return String.format("%s.%s", String.valueOf(_major), String.valueOf(_minor));
    }

    public int getMajorVersion() { return _major; }

    public int getMinorVersion() { return _minor; }

    public String minorIncrement() {
        _minor++ ;
        return getVersion();
    }

    public String majorIncrement() {
        _major++ ;
        return getVersion();
    }
}
