/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
