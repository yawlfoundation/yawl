/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.datastore.orgdata;

import au.edu.qut.yawl.resourcing.resource.*;

import java.util.HashMap;
import java.util.UUID;

/**
 * This abstract class is to be extended by all data source implementations to
 * provide organisational data to the resourcing classes in a uniform and expected manner.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public abstract class DataSource {

    protected String _name ;

    public void setName(String name) { _name = name ; }

    public String getName() { return _name ; }


    // a structure for passing the entire org data set to the ResourceManager.
    // Each member is a map in the format [object's id, object]
    public class ResourceDataSet {
        public HashMap<String,Participant> participantMap ;
        public HashMap<String,Role> roleMap ;
        public HashMap<String,Capability> capabilityMap;
        public HashMap<String,Position> positionMap;
        public HashMap<String,OrgGroup> orgGroupMap;

        public ResourceDataSet() {
            participantMap = new HashMap<String,Participant>();
            roleMap = new HashMap<String,Role>();
            capabilityMap = new HashMap<String,Capability>();
            positionMap = new HashMap<String,Position>();
            orgGroupMap = new HashMap<String,OrgGroup>();              
        }
    }

    /**
     * A default unique ID generator. Must be called from each table's 'insert' method
     * to create a new primary key for the row to be inserted. May be overridden by
     * implementations that use a different unique key format (or ignored in favour
     * of some other method that returns a unique ID).
     * @param prefix some characters to place at the start of the id
     * @return a unique String identitifer
     */
    protected String getNextID(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString()) ;
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
    public abstract void delete(Object obj) ;

    /**
     * inserts (persists) a new object record into the datastore
     * @param obj the object to insert
     * @return a newly created unique identifier (primary key) for the inserted object
     */
    public abstract String insert(Object obj) ;

}

