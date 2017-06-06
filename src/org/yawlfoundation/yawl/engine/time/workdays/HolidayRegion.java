package org.yawlfoundation.yawl.engine.time.workdays;

/**
 * @author Michael Adams
 * @date 5/6/17
 */
public class HolidayRegion {

    private String _country;
    private String _region;


    protected HolidayRegion() { }                            // for persistence

    public HolidayRegion(String country, String region) {
        _country = country;
        _region = region;
    }


    public String getCountry() { return _country; }

    public void setCountry(String country) { _country = country; }


    public String getRegion() { return _region; }

    public void setRegion(String region) { _region = region; }


    public boolean matches(String country, String region) {
        if (! _country.equals(country)) return false;

        // region is optional
        return _region == null ? region == null : _region.equals(region);
    }
    
}
