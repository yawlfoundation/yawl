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

package org.yawlfoundation.yawl.resourcing.datastore;

import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

/**
 * Simple class to facilitate the persistence of a checkedout automated task.
 * 
 * Author: Michael Adams
 * Creation Date: 2/09/2008
 */
public class PersistedAutoTask {

    private long _id;                                    // hibernate primary key
    private String _wirStr;
    private String _wirID;


    public PersistedAutoTask() {}

    public PersistedAutoTask(WorkItemRecord wir) {
        _wirID = wir.getID();
        _wirStr = wir.toXML();
        Persister.getInstance().insert(this);                
    }

    public void unpersist() {
        Persister.getInstance().delete(this);
    }

    public WorkItemRecord getWIR() {
        if (_wirStr != null)
            return Marshaller.unmarshalWorkItem(_wirStr);
        else
            return null;
    }


    public String get_wirStr() { return _wirStr; }

    public void set_wirStr(String wirStr) { _wirStr = wirStr; }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_wirID() { return _wirID; }

    public void set_wirID(String wirID) { _wirID = wirID; }
}
