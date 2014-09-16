/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import java.util.UUID;

/**
 * This abstract class is to be extended by all data source implementations to
 * provide organisational data to the resourcing classes in a uniform and expected manner.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public abstract class DataSource {

    protected String _name ;

    public void setName(String name) { _name = name ; }

    public String getName() { return _name ; }


    /**
     * A default unique ID generator. Must be called from each table's 'insert' method
     * to create a new primary key for the row to be inserted. May be overridden by
     * implementations that use a different unique key format (or ignored in favour
     * of some other method that returns a unique ID).
     * @param prefix some (optional) characters to place at the start of the id
     * @return a unique String identifier
     */
    protected String getNextID(String prefix) {
        String id = UUID.randomUUID().toString();
        return prefix != null ? prefix + "-" + id : id;
    }

    /********************************************************************************/
    /********************************************************************************/

    // ABSTRACT METHODS //

    /**
     * Loads the entire contents of the org datastores into the relevant Participant,
     * Role, Capability, Position and OrgGroup objects
     *
     * @return a fully populated ResourceSet
     */
    public abstract ResourceDataSet loadResources() ;


    /**
     * updates a persisted object
     * @param obj the object to update
     */
    public abstract void update(Object obj) ;


    /**
     * deletes a persisted object
     * @param obj the object to delete
     */
    public abstract boolean delete(Object obj) ;


    /**
     * inserts (persists) a new object record into the datastore
     * @param obj the object to insert
     * @return a newly created unique identifier (primary key) for the inserted object
     */
    public abstract String insert(Object obj) ;


    /**
     * imports (persists) a new object into the datastore. The difference between this
     * method and 'insert' is that insert generates a new object id - this method does
     * not (it assumes the object already has a valid id).
     * @param obj the object to insert
     */
    public abstract void importObj(Object obj);


    /**
     * Executes an low-level update command
     * @param query the query to execute
     * @return the number of rows affected
     */
    public abstract int execUpdate(String query) ;


    /**
     * Allows a user/password pair to be passed to an external data source for
     * validation.
     * @param userid the userid
     * @param password the plain-text password
     * @return the outcome of the authentication
     */
    public abstract boolean authenticate(String userid, String password) throws
            YAuthenticationException;

}

