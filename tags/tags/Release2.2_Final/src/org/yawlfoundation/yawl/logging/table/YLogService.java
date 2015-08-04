/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 23/04/2009
 */
public class YLogService {

    private long serviceID ;
    private String name;
    private String url;

    public YLogService() {}

    public YLogService(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public long getServiceID() {
        return serviceID;
    }

    public void setServiceID(long serviceID) {
        this.serviceID = serviceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean equals(Object other) {
        return (other instanceof YLogService) &&
                (this.getServiceID() == ((YLogService) other).getServiceID());
    }

    public int hashCode() {
        return (int) (31 * getServiceID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(100);
        xml.append(String.format("<service key=\"%d\">", serviceID));
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(url, "url"));
        xml.append("</service>");
        return xml.toString();
    }
}
