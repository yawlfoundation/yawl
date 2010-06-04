/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;

import java.sql.*;
import java.util.*;

/**
 * A vanilla JDBC implementation of the DataSource abstraction
 *
 *  @author Michael Adams
 *  v0.1, 28/08/2007
 */

public class jdbcImpl extends DataSource {

    // var's required to connect to the appropriate database
    private final String dbDriver = "org.postgresql.Driver";
    private final String dbUrl = "jdbc:postgresql:yawl";
    private final String dbUser = "postgres";
    private final String dbPassword = "yawl";

    private Connection connection = null;
    private static final Logger _log = Logger.getLogger(jdbcImpl.class);


    // the constructor - loads the driver and creates tables if they don't already exist
    public jdbcImpl() {
        super();
        if (loadDriver(dbDriver)) checkTables();
    }

    /******************************************************************************/

    // GENERIC JDBC METHODS //

    /**
     * @param driver - the driver to load
     */
     private boolean loadDriver(String driver) {
         try {
             Class.forName(driver);
             return true ;
          } catch( ClassNotFoundException e ) {
              _log.error("Cannot load database driver");
             return false ;
          }
      }


    /**
	 * Open the connection
     * @return true if connection is successfully opened
	 */
	private boolean openConnection() {
        boolean result = false ;
        try {
            if (connection == null)
		        connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            if (connection != null) { result = ! connection.isClosed(); }

        } catch( SQLException e ) {
		       _log.error("Cannot connect to database", e);
               return false ;                              // no connection
        }
        return result ;                    // either no connection or it's closed
	}



	/**
	 * close the connection to the database
	 */
	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
            _log.error("Cannot close the connection", e);
		}
	}


	/**
	 * @return a statement object (or null if the statement could not be created)
	 */
	private Statement createStatement() {
		Statement statement = null;
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
            _log.error("Problem creating statement", e);
		}
		return statement;
	}

    /**
     * Executes a select statement
     * @param statement the statement object to execute the query through
     * @param sql the SQL select statement
     * @return a ResultSet with the results of the selection (if successful)
     */
    private ResultSet getResultSet(Statement statement, String sql) {
		ResultSet rs = null;
		try {
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
            _log.error("Problem executing query", e);
		}
		return rs;
	}

    /**
     * executes a select statement
     * @param sql the select statement to execute
     * @return  a ResultSet with the results of the selection (if successful)
     */
    private ResultSet execSelect(String sql) {
        if (openConnection()) {
            Statement s = createStatement() ;
            if (s != null) return getResultSet(s, sql);
        }
        return null ;
    }

    /**
     * Executes an UPDATE, INSERT or DELETE statement
     * @param sql the statement to execute
     * @return the number of affected rows (or -1 if not successful)
     */
    public int execUpdate(String sql) {
        if (openConnection()) {
            Statement s = createStatement() ;
            if (s != null) {
                try {
                    return s.executeUpdate(sql);
                }
                catch (SQLException e) {
                   _log.error("Problem executing update", e);
                }
            }
        }
        else _log.error("Connection is not open for executeUpdate.");

        return -1;
    }

    /**
	 * Dump database connection metadata
	 */
	public void printDump() {

		DatabaseMetaData md;

		try {
            openConnection() ;
            md = connection.getMetaData();

		 	_log.info("Product name: " + md.getDatabaseProductName());
		    _log.info("Driver name: " + md.getDriverName());
            closeConnection() ;
        } catch (SQLException e) {
		    _log.error("Exception creating or reading database metadata", e) ;
		}
	}

    /****************************************************************************/

    // PARTICULAR INTERNAL METHODS OF THIS IMPLEMENTATION //

    /** an enumeration of table names */
    private enum tbl { rsj_capability, rsj_role, rsj_orggroup, rsj_position,
                       rsj_participant, rsj_participant_role,
                       rsj_participant_position, rsj_participant_capability }


    /**
     * Checks that all required tables exist
     */
    private void checkTables() {
        Set<tbl> tableSet = EnumSet.allOf(tbl.class);
        String tableName ;
        String[] tableType = {"TABLE"} ;

        try {
            // get a list of tablenames from the database
            openConnection();
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getTables( null, null, null, tableType );

            // remove any existing resource tables from the set (others ignored)
            while (rs.next()) {
                tableName = rs.getString("TABLE_NAME") ;
                if (tableName.startsWith("rsj_"))
                    tableSet.remove(tbl.valueOf(tableName)) ;
            }
        }
        catch (SQLException sqle) {
            _log.error("Exception retrieving table names from database", sqle);
        }

        // tableSet now contains only missing tables (if any)
        for (tbl table : tableSet) makeTable(table) ;
    }
    

    /**
     * Create the required tables if they don't already exist
     * @param table the table to create
     */
    private void makeTable(tbl table) {
        StringBuilder stmt = new StringBuilder("CREATE TABLE ");
        stmt.append(table.name()) ;

        switch (table) {
            case rsj_capability:
                stmt.append(" (capabilityid varchar(255) NOT NULL,")
                    .append(" capability varchar(255), description text,") 
                    .append(" CONSTRAINT rsj_capability_pkey PRIMARY KEY (capabilityid))");
                break;
            case rsj_orggroup:
                stmt.append(" (groupid varchar(255) NOT NULL, groupname varchar(255),")
                    .append(" grouptype varchar(255), description text,")
                    .append(" belongsto varchar(255),")
                    .append(" CONSTRAINT rsj_orggroup_pkey PRIMARY KEY (groupid))");
                break;
            case rsj_position:
                stmt.append(" (p_id varchar(255) NOT NULL,  positionid varchar(255),")
                    .append(" title varchar(255), description text,")
                    .append(" orggroup varchar(255), reportsto varchar(255),")
                    .append(" CONSTRAINT rsj_position_pkey PRIMARY KEY (p_id))");
                break;
            case rsj_role:
                stmt.append(" (roleid varchar(255) NOT NULL, rolename varchar(255),")
                    .append(" description text, belongsto varchar(255),")
                    .append(" CONSTRAINT rsj_role_pkey PRIMARY KEY (roleid))");
                break;
            case rsj_participant:
                stmt.append(" (participantid varchar(255) NOT NULL, description text,")
                    .append(" notes text, available bool, lastname varchar(255),")
                    .append(" firstname varchar(255), userid varchar(255),")
                    .append(" pword varchar(255), administrator bool,")
                    .append(" CONSTRAINT rsj_participant_pkey PRIMARY KEY (participantid))");
                break;
            case rsj_participant_role:
                stmt.append(" (participantid varchar(255) NOT NULL,")
                    .append(" roleid varchar(255) NOT NULL,")
                    .append(" CONSTRAINT rsj_participant_role_pkey")
                    .append(" PRIMARY KEY (participantid, roleid),")
                    .append(" CONSTRAINT fk_rsj_participant_role FOREIGN KEY (roleid)")
                    .append(" REFERENCES rsj_role (roleid) ON UPDATE NO ACTION")
                    .append(" ON DELETE NO ACTION,")
                    .append(" CONSTRAINT fk_rsj_role_participant FOREIGN KEY")
                    .append(" (participantid) REFERENCES rsj_participant (participantid)")
                    .append(" ON UPDATE NO ACTION ON DELETE NO ACTION)") ;
                break ;
            case rsj_participant_capability:
                stmt.append(" (participantid varchar(255) NOT NULL,")
                    .append(" capabilityid varchar(255) NOT NULL,")
                    .append(" CONSTRAINT rsj_participant_capability_pkey")
                    .append(" PRIMARY KEY (participantid, capabilityid),")
                    .append(" CONSTRAINT fk_rsj_participant_capability FOREIGN KEY")
                    .append(" (capabilityid) REFERENCES rsj_capability (capabilityid)")
                    .append(" ON UPDATE NO ACTION ON DELETE NO ACTION,")
                    .append(" CONSTRAINT fk_rsj_capability_participant FOREIGN KEY")
                    .append(" (participantid) REFERENCES rsj_participant (participantid)")
                    .append(" ON UPDATE NO ACTION ON DELETE NO ACTION)");
                break;
            case rsj_participant_position:
                stmt.append(" (participantid varchar(255) NOT NULL,")
                    .append(" p_id varchar(255) NOT NULL,")
                    .append(" CONSTRAINT rsj_participant_position_pkey")
                    .append(" PRIMARY KEY (participantid, p_id),")
                    .append(" CONSTRAINT fk_rsj_participant_position FOREIGN KEY")
                    .append(" (p_id) REFERENCES rsj_position (p_id)")
                    .append(" ON UPDATE NO ACTION ON DELETE NO ACTION,")
                    .append(" CONSTRAINT fk_rsj_position_participant FOREIGN KEY")
                    .append(" (participantid) REFERENCES rsj_participant (participantid)")
                    .append(" ON UPDATE NO ACTION ON DELETE NO ACTION)");
                break;
            default : stmt = null ;
        }
        if (stmt != null) execUpdate(stmt.toString());
    }


    /**
     * constructs a select statement using the parameters passed
     * @param table the table to select from
     * @param field the [field] in 'WHERE [field] = [value]'
     * @param value the [value] in the above
     * @return a constructed select statement String
     */
    private String selectWhere(String table, String field, String value) {
        return String.format("SELECT * FROM %s WHERE %s = '%s'", table, field, value) ;
    }

    /**
     * constructs a 'select *' statement
     * @param table the table to select from
     * @return a constructed select statement String
     */
    private String selectAll(String table) {
        return String.format("SELECT * FROM %s", table) ;
    }


    /**
     * selects all objects stored in the table passed (unqualified selection)
     * @param table the table to get the rows from
     * @return a List of instantiated objects, one for each row in the table
     */
    private List selectAll(tbl table) {
        return select(table, null, null) ;
    }


    /**
     * selects objects stored in the table passed, qualified by the field and value
     * passed. Effectively 'SELECT * FROM [table] WHERE [field] = [value]'
     * @param table the table to get the rows from
     * @param field the name of the field to query in the where clause
     * @param value the value to compare against the field values
     * @return a List of instantiated objects, one for each row in the table that
     *         matches: [field] = [value]
     */
    private List select(tbl table, String field, String value) {
        List result = new ArrayList() ;

        // create the query string - if field is null then its a simple 'SELECT *'
        String qry = (field != null)? selectWhere(table.name(), field, value) :
                                      selectAll(table.name()) ;
        // execute the selection
        ResultSet rs = execSelect(qry) ;

        if (rs != null) {
            try {
                // for each table row, create its object and add it to the result List
                switch (table) {
                    case rsj_participant:
                        while(rs.next()) result.add(makeParticipant(rs)); break ;
                    case rsj_role:
                        while(rs.next()) result.add(makeRole(rs)); break ;
                    case rsj_position:
                        while(rs.next()) result.add(makePosition(rs)); break ;
                    case rsj_orggroup:
                        while(rs.next()) result.add(makeOrgGroup(rs)); break ;
                    case rsj_capability:
                        while(rs.next()) result.add(makeCapability(rs)); break ;
                }
            }
            catch (SQLException sqle) {
                _log.error("Exception working with ResultSet", sqle) ;
            }
        }
        if (result.isEmpty()) return null ;
        else return result ;
    }


    /**
     * selects a single object from the datastore
     * @param table the table to query
     * @param field the name of the field to query in the where clause
     * @param value the value to compare against the field values
     * @return the object matching [field] = [value]. If more than one row is matched,
     *         only the first if returned - the rest are ignored.
     */
    private Object selectScalar(tbl table, String field, String value) {

        // execute the select statement
        ResultSet rs = execSelect(selectWhere(table.name(), field, value)) ;

        if (rs != null) {
            try {
                if (rs.next()) {      // first record wanted only

                    // create the object
                    switch (table) {
                        case rsj_participant: return makeParticipant(rs);
                        case rsj_role: return makeRole(rs);
                        case rsj_position: return makePosition(rs);
                        case rsj_orggroup: return makeOrgGroup(rs);
                        case rsj_capability: return makeCapability(rs);
                    }
                }
            }
            catch (SQLException sqle) {
                _log.error("Exception working with ResultSet", sqle) ;
            }
        }
        return null ;
    }


    /**
     * Constructs a Participant object from a ResultSet row
     * @param rs the ResultSet containing the row of data
     * @return a (partially) constructed Participant. Note that only the data members
     *         are added here to the Participant object. The many-to-many Roles,
     *         Positions and Capability Sets references need to be added elsewhere.
     * @throws SQLException if there's a ResultSet issue
     */
    private Participant makeParticipant(ResultSet rs) throws SQLException {
        Participant p = new Participant() ;
        p.setID(rs.getString("ParticipantID")) ;
        p.setFirstName(rs.getString("Firstname"));
        p.setLastName(rs.getString( "Lastname" ));
        p.setUserID(rs.getString("UserID"));
        p.setPassword(rs.getString("pword"));
        p.setDescription(rs.getString("description"));
        p.setNotes(rs.getString("notes"));
        p.setAdministrator(rs.getBoolean("administrator"));
        return p ;
    }


    /**
     * Constructs a Participant object from a ResultSet row
     * @param rs the ResultSet containing the row of data
     * @return a (mostly) constructed Role. Note that the id of the Role this Role
     *         belongs to is stored here for instatiation of the Role reference elsewhere.
     * @throws SQLException if there's a ResultSet issue
     */
    private Role makeRole(ResultSet rs) throws SQLException {
        Role r = new Role() ;
        r.setID(rs.getString("RoleID"));
        r.setName(rs.getString("RoleName"));
        r.setDescription(rs.getString("Description"));
        r.set_belongsToID(rs.getString("BelongsTo"));
        return r ;
    }


    /**
     * Constructs a Capability object from a ResultSet row
     * @param rs the ResultSet containing the row of data
     * @return a fully constructed Capability object.
     * @throws SQLException if there's a ResultSet issue
     */
    private Capability makeCapability(ResultSet rs) throws SQLException {
        Capability c = new Capability() ;
        c.setID(rs.getString("CapabilityID"));
        c.setCapability(rs.getString("Capability"));
        c.setDescription(rs.getString("Description"));
        return c ;
    }


    /**
     * Constructs a Position object from a ResultSet row
     * @param rs the ResultSet containing the row of data
     * @return a (mostly) constructed Position. Note that the id of the Position this
     *         Position reports to, and the id of the OrgGRoup this Position belongs to,
     *         are stored here (in the Position object) for instatiation of the Position
     *         and OrgGroup object references elsewhere.
     * @throws SQLException if there's a ResultSet issue
     */
    private Position makePosition(ResultSet rs) throws SQLException {
        Position p = new Position() ;
        p.setID(rs.getString("p_id"));
        p.setPositionID(rs.getString("PositionID"));
        p.setTitle(rs.getString("Title"));
        p.setDescription(rs.getString("Description"));
        p.set_orgGroupID(rs.getString("OrgGroup"));
        p.set_reportsToID(rs.getString("reportsTo"));
        return p ;
    }


    /**
     * Constructs an OrgGroup object from a ResultSet row
     * @param rs the ResultSet containing the row of data
     * @return a (mostly) constructed OrgGroup. Note that the id of the OrgGroup this
     *         OrgGroup belongs to are stored here (in the OrgGroup object) for
     *         instatiation of the OrgGroup object references elsewhere.
     * @throws SQLException if there's a ResultSet issue
     */
    private OrgGroup makeOrgGroup(ResultSet rs) throws SQLException {
        OrgGroup o = new OrgGroup() ;
        o.setID(rs.getString("GroupID"));
        o.set_groupType(rs.getString("GroupType"));
        o.setGroupName(rs.getString("GroupName"));
        o.setDescription(rs.getString("Description"));
        o.set_belongsToID(rs.getString("BelongsTo"));
        return o ;
    }



    /**
     * gets a List of Role, Position or Capability object id's (depending on the table
     * passed) corresponding to a particular participant id.
     * @param table the (foreign-key) table to get the id's from
     * @param pid the Participant id
     * @return a List of object ids associated with the participant
     */
    private List<String> selectParticipantAttributeIDs(tbl table, String pid) {
        List<String> ids = new ArrayList<String>() ;
        ResultSet rs = execSelect(selectWhere(table.name(), "ParticipantID", pid));

        if (rs != null) {
            try {
                switch (table) {
                    case rsj_participant_role:
                        while(rs.next()) ids.add(rs.getString("RoleID")); break ;
                    case rsj_participant_position:
                        while(rs.next()) ids.add(rs.getString("p_id")); break ;
                    case rsj_participant_capability:
                        while(rs.next()) ids.add(rs.getString("CapabilityID")); break ;
                }
                rs.close();
            }
            catch (SQLException sqle) {
                _log.error("Exception working with " + table.name() + " ResultSet", sqle);
                return null ;
            }
        }
        return ids ;
    }


    /**
     * gets a List of Participant object id's corresponding to a particular attribute
     * id (ie Role, Position or Capability) depending on the table passed
     * @param table the (foreign-key) table to get the id's from
     * @param id the id
     * @return a List of participant ids associated with the object
     */
    private List<String> selectAttributeParticipantIDs(tbl table, String id) {
        List<String> ids = new ArrayList<String>() ;
        String field = null;

        switch (table) {
            case rsj_participant_role: field = "RoleID" ; break ;
            case rsj_participant_position: field = "p_id" ; break ;
            case rsj_participant_capability: field = "CapabilityID" ; break ;
        }

        ResultSet rs = execSelect(selectWhere(table.name(), field, id));

        if (rs != null) {
            try {
               while(rs.next()) ids.add(rs.getString("ParticipantID"));
               rs.close();
            }
            catch (SQLException sqle) {
                _log.error("Exception working with " + table.name() + " ResultSet", sqle);
                return null ;
            }
        }
        return ids ;
    }


    /**
     * Loads all Capability records from the data store.
     * @return a map of Capability objects of the form [capabilityID, Capability]
     */
    private HashMap<String, Capability> loadCapabilities() {
        HashMap<String, Capability> map = null ;

        // get records from db and re-make objects
        List<Capability> capList = selectAll(tbl.rsj_capability) ;

        // map capabilities
        if (capList != null) {
            map = new HashMap<String, Capability>() ;
            for (Capability c : capList) map.put(c.getID(), c);
        }
        return map ;
    }


    /**
     * Loads all OrgGroup records from the data store, and correctly maps 'belongs to'
     * OrgGroup references
     * @return a map of OrgGroup objects of the form [groupID, OrgGroup]
     */
    private HashMap<String, OrgGroup> loadOrgGroups() {
        HashMap<String, OrgGroup> map = null ;

        // get records from db and re-make objects
        List<OrgGroup> ogList = selectAll(tbl.rsj_orggroup);

        // re-map org group tree structure - first build map of all orgGroups
        if (ogList != null) {
            map = new HashMap<String, OrgGroup>() ;
            for (OrgGroup o : ogList) map.put(o.getID(), o);

            // ... then set the relevant 'belongs to' OrgGroup (by id)
            for (OrgGroup o : ogList) o.setBelongsTo(map.get(o.get_belongsToID()));
        }
        return map ;
    }


    /**
     * Loads all Position records from the data store, and correctly maps 'belongs to'
     * OrgGroup and 'reports to' Position references
     * @return a map of Position objects of the form [id, Position]
     */
    private HashMap<String, Position> loadPositions(Map<String, OrgGroup> ogMap) {
        HashMap<String, Position> map = null ;

        // get records from db and re-make objects
        List<Position> posList = selectAll(tbl.rsj_position) ;

        if (posList != null) {
            map = new HashMap<String, Position>() ;

            // map position objects and re-create links to OrgGroup objects
            for (Position p : posList) {
                map.put(p.getID(), p);
                p.setOrgGroup(ogMap.get(p.get_orgGroupID()));
            }

            // ... then set the relevant 'reports to' Position (by id)
            for (Position p : posList) p.setReportsTo(map.get(p.get_reportsToID()));
        }
        return map ;
    }


    /**
     * Loads all Role records from the data store, and correctly maps 'belongs to'
     * Role references
     * @return a map of Role objects of the form [roleID, Role]
     */
    private HashMap<String, Role> loadRoles() {
        HashMap<String, Role> map = null ;

        // get records from db and re-make objects
        List<Role> roleList = selectAll(tbl.rsj_role);
        
        // re-map role tree structure - first build map of all roles
        if (roleList != null) {
            map = new HashMap<String, Role>() ;
            for (Role r : roleList) map.put(r.getID(), r);

            // ... then set the relevant "belongs to' role (by id)
            for (Role r : roleList) r.setOwnerRole(map.get(r.get_belongsToID()));
        }
        return map ;
    }


    /**
     * Loads all Participant records from the data store, and maps the
     * many-to-many relations between each Participant and its Role, Position and
     * Capability Sets
     * @return a map of Participant objects of the form [participantID, Participant]
     */
    private HashMap<String,Participant> loadParticipants(ResourceDataSet ds) {
        HashMap<String, Participant> map = null ;

        // get records from db and re-make objects
        List<Participant> pList = selectAll(tbl.rsj_participant) ;

        // map sets to already instantiated objects (via their maps)
        if (pList != null) {
            map = new HashMap<String, Participant>() ;
            List<String> ids ;

            for (Participant p : pList) {
                String pid = p.getID();

                // add attribute objects
                ids = selectParticipantAttributeIDs(tbl.rsj_participant_role, pid) ;
                for (String rid : ids) p.addRole(ds.getRole(rid));

                ids = selectParticipantAttributeIDs(tbl.rsj_participant_position, pid) ;
                for (String posid : ids) p.addPosition(ds.getPosition(posid));

                ids = selectParticipantAttributeIDs(tbl.rsj_participant_capability, pid) ;
                for (String cid : ids) p.addCapability(ds.getCapability(cid));

                // add participant to map
                map.put(pid, p) ;
            }
        }
        return map ;
    }

    /****************************************************************************/

    // INSERT/UPDATE/DELETE METHODS FOR EACH RESOURCE CLASS //

    // Participant //

    private void updateParticipant(Participant p) {
        
        String qry = String.format(
                   "UPDATE rsj_participant SET description = '%s', notes = '%s'," +
                   " available = %b, lastname = '%s', firstname = '%s', userid = '%s'," +
                   " pword = '%s', administrator = %b WHERE participantID = '%s'",
                   p.getDescription(), p.getNotes(), p.isAvailable(), p.getLastName(),
                   p.getFirstName(), p.getUserID(), p.getPassword(), p.isAdministrator(),
                   p.getID());
        execUpdate(qry);

        // remove and re-add many-to-many relations (in case they've changed)
        deleteParticipantAttributeRows(p.getID()) ;
        insertParticpantAttributeRows(p, p.getID()) ;
    }


    private String insertParticipant(Participant p) {
        String id = getNextID("PA") ;
        String qry = String.format(
               "INSERT INTO rsj_participant VALUES " +
               "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%b')",
               id, p.getDescription(), p.getNotes(), p.isAvailable(),
               p.getLastName(), p.getFirstName(), p.getUserID(), p.getPassword(),
               p.isAdministrator());
        if (qry != null) execUpdate(qry);

        insertParticpantAttributeRows(p, id);                // cascade to joined tables
        return id ;
    }


    private void deleteParticipant(Participant p) {
        String qry = String.format(
                    "DELETE FROM rsj_participant WHERE participantID = '%s'", p.getID());
        execUpdate(qry);
        deleteParticipantAttributeRows(p.getID());           // cascade to joined tables
    }


    private void deleteParticipantAttributeRows(String pid) {
        String qry = String.format(
               "DELETE FROM rsj_participant_capability WHERE participantID = '%s'", pid);
        execUpdate(qry);
        qry = String.format(
                 "DELETE FROM rsj_participant_position WHERE participantID = '%s'", pid);
        execUpdate(qry);
        qry = String.format(
                     "DELETE FROM rsj_participant_role WHERE participantID = '%s'", pid);
        execUpdate(qry);
    }


    private void insertParticpantAttributeRows(Participant p, String id) {
        String qry ;
        for (Role r : p.getRoles()) {
            qry = String.format(
                    "INSERT INTO rsj_participant_role VALUES ('%s', '%s')",
                    id, r.getID()) ;
            if (qry != null) execUpdate(qry);
        }
        for (Capability c : p.getCapabilities()) {
            qry = String.format(
                    "INSERT INTO rsj_participant_capability VALUES ('%s', '%s')",
                    id, c.getID()) ;
            if (qry != null) execUpdate(qry);
        }
        for (Position po : p.getPositions()) {
            qry = String.format(
                    "INSERT INTO rsj_participant_position VALUES ('%s', '%s')",
                    id, po.getID()) ;
            if (qry != null) execUpdate(qry);
        }
    }



    // Role //

    private void updateRole(Role r) {
        String ownerRole = (r.getOwnerRole() != null) ? r.getOwnerRole().getID() : null ;
        String qry = String.format(
                   "UPDATE rsj_Role SET rolename = '%s', description = '%s', " +
                   "belongsTo = '%s' WHERE roleID = '%s'",
                   r.getName(), r.getDescription(), ownerRole, r.getID());
        execUpdate(qry);          
    }


    private String insertRole(Role r) {
        String id = getNextID("RO") ;
        String ownerRole = (r.getOwnerRole() != null) ? r.getOwnerRole().getID() : null ;
        String qry = String.format(
                   "INSERT INTO rsj_role VALUES ('%s', '%s', '%s', '%s')",
                    id, r.getName(), r.getDescription(), ownerRole);
        if (qry != null) execUpdate(qry);
        return id;
    }


    private void deleteRole(Role r) {
        String qry = String.format(
                   "DELETE FROM rsj_Role WHERE roleID = '%s'", r.getID());
        execUpdate(qry);
    }


    // Capability //

    private void updateCapability(Capability c) {
        String qry = String.format(
                    "UPDATE rsj_Capability SET capability = '%s', description = '%s' " +
                    "WHERE capabilityID = '%s'",
                     c.getCapability(), c.getDescription(), c.getID());
        execUpdate(qry);
    }


    private String insertCapability(Capability c) {
        String id = getNextID("CA") ;
        String qry = String.format(
                   "INSERT INTO rsj_capability VALUES ('%s', '%s', '%s')",
                    id, c.getCapability(), c.getDescription());
        if (qry != null) execUpdate(qry);
        return id;
    }


    private void deleteCapability(Capability c) {
        String qry = String.format(
                   "DELETE FROM rsj_Capability WHERE CapabilityID = '%s'", c.getID());
        execUpdate(qry);
    }


    // Position //

    private void updatePosition(Position p) {
        String belongsTo = (p.getOrgGroup() != null) ? p.getOrgGroup().getID() : null ;
        String reportsTo = (p.getReportsTo() != null) ? p.getReportsTo().getID() : null ;
        String qry = String.format(
                  "UPDATE rsj_Position SET positionID = '%s', title = '%s', " +
                  "description = '%s', orgGroup = '%s', reportsTo = '%s' WHERE P_ID = '%s'",
                   p.getPositionID(), p.getTitle(), p.getDescription(),
                   belongsTo, reportsTo, p.getID());
        execUpdate(qry);
    }


    private String insertPosition(Position p) {
        String id = getNextID("PO") ;
        String belongsTo = (p.getOrgGroup() != null) ? p.getOrgGroup().getID() : null ;
        String reportsTo = (p.getReportsTo() != null) ? p.getReportsTo().getID() : null ;
        String qry = String.format(
               "INSERT INTO rsj_position VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
                id, p.getPositionID(), p.getTitle(), p.getDescription(),
                belongsTo, reportsTo);
        if (qry != null) execUpdate(qry);
        return id;
    }


    private void deletePosition(Position p) {
        String qry = String.format(
                   "DELETE FROM rsj_Position WHERE P_ID = '%s'",p.getID());
        execUpdate(qry);
    }


    // OrgGroup //

    private String insertOrgGroup(OrgGroup o) {
        String id = getNextID("OG") ;
        String belongsTo = (o.getBelongsTo() != null) ? o.getBelongsTo().getID() : null ;
        String qry = String.format(
                   "INSERT INTO rsj_orggroup VALUES ('%s', '%s', '%s', '%s', '%s')",
                    id, o.getGroupName(), o.get_groupType(),
                    o.getDescription(), belongsTo);

        if (qry != null) execUpdate(qry);
        return id;
    }


    private void updateOrgGroup(OrgGroup o) {
        String belongsTo = (o.getBelongsTo() != null) ? o.getBelongsTo().getID() : null ;
        String qry = String.format(
                    "UPDATE rsj_orggroup SET GroupName = '%s', GroupType = '%s', " +
                    "description = '%s', belongsTo = '%s' WHERE GroupID = '%s'",
                     o.getGroupName(), o.get_groupType(), o.getDescription(),
                     belongsTo, o.getID());
        execUpdate(qry);
    }


    private void deleteOrgGroup(OrgGroup o) {
        String qry = String.format(
                   "DELETE FROM rsj_orggroup WHERE groupID = '%s'", o.getID());
        execUpdate(qry);
    }


    /***********************************************************************************/

    // BASE CLASS IMPLEMENTATIONS (see DataSource for the purpose of each method) //


    public ResourceDataSet loadResources() {
        ResourceDataSet ds = new ResourceDataSet(this) ;

        // load capabilties first - they have no cyclical dependencies
        ds.setCapabilities(loadCapabilities(), this) ;

        // do org groups next (lowest in the hierarchy)
        ds.setOrgGroups(loadOrgGroups(), this) ;

        // then do Positions using the org group mapping returned above
        ds.setPositions(loadPositions(ds.getOrgGroupMap()), this);

        // roles next
        ds.setRoles(loadRoles(), this) ;

        // ok, all pre-required maps built, now recreate participants
        ds.setParticipants(loadParticipants(ds), this) ;

        return ds ;
    }


    /**
     * inserts the object into the database
     * @param obj the object to insert
     * @return a unique primary key identifier generated during the insert
     */
    public String insert(Object obj) {
        if (obj instanceof OrgGroup) return insertOrgGroup((OrgGroup) obj) ;
        else if (obj instanceof Capability) return insertCapability((Capability) obj) ;
        else if (obj instanceof Position) return insertPosition((Position) obj) ;
        else if (obj instanceof Role) return insertRole((Role) obj);
        else if (obj instanceof Participant) return insertParticipant((Participant) obj);

        // should never get here
        return null ;
    }

    public void importObj(Object obj) { insert(obj); }


    /**
     * updates an existing object in the database with new values
     * @param obj the object to update
     */
    public void update(Object obj) {
        if (obj instanceof OrgGroup) updateOrgGroup((OrgGroup) obj) ;
        else if (obj instanceof Capability) updateCapability((Capability) obj) ;
        else if (obj instanceof Position) updatePosition((Position) obj) ;
        else if (obj instanceof Role) updateRole((Role) obj);
        else if (obj instanceof Participant) updateParticipant((Participant) obj);
    }


    /**
     * deletes the object from the database
     * @param obj the object to delete
     */
    public void delete(Object obj) {
        if (obj instanceof OrgGroup) deleteOrgGroup((OrgGroup) obj) ;
        else if (obj instanceof Capability) deleteCapability((Capability) obj) ;
        else if (obj instanceof Position) deletePosition((Position) obj) ;
        else if (obj instanceof Role) deleteRole((Role) obj);
        else if (obj instanceof Participant) deleteParticipant((Participant) obj);
    }

    
    public boolean authenticate(String userid, String password) throws
            YAuthenticationException {
        return false;
    }


}
