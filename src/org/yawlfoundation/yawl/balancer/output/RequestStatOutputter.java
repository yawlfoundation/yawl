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

package org.yawlfoundation.yawl.balancer.output;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/8/17
 */
public class RequestStatOutputter extends AbstractLoadOutputter {


    public RequestStatOutputter(String engineName) {
        super("requests", engineName, ".log");
    }


    protected void writeValues(Map<String, String> values) throws IOException { }


    protected String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("time,timestamp,name,min,max,mean,count,perSec,");
        sb.append("prevMin,prevMax,prevMean,prevCount,prevPerSec,");
        sb.append("allMin,allMax,allMean,allCount,allPerSec");
        return sb.toString();
    }

    protected void finalise() { }

}
