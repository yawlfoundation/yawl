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

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 13/6/18
 */
public class CombinedBusynessOutputter extends AbstractLoadOutputter {

    private String[] buffer = new String[4];
    private static final Object MUTEX = new Object();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CombinedBusynessOutputter() {
        super("combined", "busyness", ".log");
        clear();
    }

    @Override
    void writeValues(Map<String, String> values) throws IOException {
        insert(values);
        if (filled()) {
            synchronized (MUTEX) {
                String fDate = SDF.format(new Date());
                _out.write(fDate);
                for (String s : buffer) {
                    if (s == null) continue;
                    _out.write(',');
                    _out.write(s);
                }
                _out.write('\n');
                _out.flush();
                clear();
            }
        }
    }

    public void add(Map<String, String> values) {
        if (_out != null) {
            try {
                writeValues(values);
            }
            catch (IOException ioe) {
                // forget it
            }
        }
    }

    private void insert(Map<String, String> values) {
        int index = StringUtil.strToInt(values.get("index"), -1);
        if (index >= 0 && index <= 3) {
            buffer[index] = values.get("busyness");
        }
    }

    private boolean filled() {
       for (String s : buffer) {
           if (s == null) return false;
       }
       return true;
    }

    private void clear() {
        for (int i=0; i<buffer.length; i++) {
            buffer[i] = null;
        }
    }

    @Override
    String getHeader() {
        return "time,e1,e2,e3,e4";
    }

    @Override
    void finalise() throws IOException {

    }
}
