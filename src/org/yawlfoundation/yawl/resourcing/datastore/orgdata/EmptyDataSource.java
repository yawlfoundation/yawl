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

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import org.yawlfoundation.yawl.exceptions.YAuthenticationException;

/**
 * This class provides a default DataSource object to the ResourceManager in the event
 * that there is no actual Datasource set and/or initialised.
 *
 * Author: Michael Adams
 * Date: 10/04/2008
 */
public class EmptyDataSource extends DataSource {

    ResourceDataSet _rds;

    public EmptyDataSource() {
        _rds = new ResourceDataSet(this);
    }

    public ResourceDataSet getDataSource() {
        return _rds ;
    }

    public ResourceDataSet loadResources() {
        return null;
    }

    public void update(Object obj) { }

    public boolean delete(Object obj) { return false; }

    public String insert(Object obj) {
        return null; 
    }

    public void importObj(Object obj) { }

    public int execUpdate(String query) { return -1; }

    public boolean authenticate(String userid, String password) throws
            YAuthenticationException {
        return false;
    }
}
