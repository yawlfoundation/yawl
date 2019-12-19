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

package org.yawlfoundation.yawl.balancer.jmx;

import org.json.JSONException;
import org.json.JSONObject;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 13/6/17
 */
public class JMXStatistics {

    private final String _title;
    private final long _timestamp;
    protected final Map<String, String> _statMap;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    


    public JMXStatistics(JSONObject obj, String title, long timestamp) {
        _title = title;
        _timestamp = timestamp *  1000;                       // convert secs to msecs
        _statMap = jsonToMap(obj);
    }


    public long getTimestamp() { return _timestamp; }

    public String getTimestampFormatted() {
        return DATE_FORMAT.format(new Date(_timestamp));
    }

    public String getValue(String key) { return _statMap.get(key); }

    public int getIntValue(String key) {
        return StringUtil.strToInt(getValue(key), 0);
    }

    public double getDoubleValue(String key) {
        return StringUtil.strToDouble(getValue(key), 0);
    }

    protected Map<String, String> jsonToMap(JSONObject obj) {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> nameItr = obj.keys();
        while(nameItr.hasNext()) {
            String name = nameItr.next();
            try {
                map.put(name, obj.get(name).toString());
            }
            catch (JSONException je) {
                // bad value
            }
        }
        return map;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTimestampFormatted()).append('\n');
        for (String key : _statMap.keySet()) {
            sb.append(key).append(':').append(_statMap.get(key)).append('\n');
        }
        return sb.toString();
    }


    public XNode toXNode() {
        XNode root = new XNode(_title);
        root.addAttribute("timestamp", _timestamp);
        XNode values = root.addChild("values");
        for (String key : _statMap.keySet()) {
            values.addChild(key, _statMap.get(key));
        }
        return root;
    }
}
