/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.text.MessageFormat;


import au.edu.qut.yawl.admintool.model.*;
import au.edu.qut.yawl.engine.*;
import au.edu.qut.yawl.authentication.User;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.io.UnsupportedEncodingException;

import net.sf.hibernate.tool.hbm2ddl.SchemaUpdate;
import sun.misc.BASE64Encoder;

import java.sql.ResultSet;
import java.sql.Statement;


public class DatabaseGatewayImpl {
    public static final int UPDATE_OPERATION = 1;
    public static final int DELETE_OPERATION = 2;
    public static final int SAVE_OPERATION = 3;

    private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://192.94.227.138:8080/yawl/ia");

    public static final int TIMEFORMAT_STRING = 1;
    public static final int TIMEFORMAT_LONG = 2;

    private boolean persistenceOn = false;

    private Configuration cfg = null;

    private static SessionFactory _factory = null;
    private MessageFormat binaryRelationFKMissingMsg =
            new MessageFormat(
                    "Database needs to contain a {0} [{1}] before you can connect it to a [{2}]");
    private static DatabaseGatewayImpl _self;


    private DatabaseGatewayImpl(boolean persistenceOn) throws HibernateException {
        this.persistenceOn = persistenceOn;
        initialise();
    }

    public static DatabaseGatewayImpl getInstance(boolean persistenceOn) throws HibernateException {
        if (_self == null) {
            _self = new DatabaseGatewayImpl(persistenceOn);
        }
        return _self;
    }

    public void initialise() throws HibernateException {

        if (persistenceOn) {

            // Create the Hibernate config, do not create database, the engine does this.
            try {
                cfg = new Configuration();
                //cfg.addClass(Capability.class);
                //cfg.addClass(HResOccupiesPosition.class);
                cfg.addClass(HumanResourceRole.class);
                //cfg.addClass(OrgGroup.class);
                //cfg.addClass(Position.class);
                cfg.addClass(Resource.class);
                //cfg.addClass(ResourceCapability.class);
                cfg.addClass(Role.class);

                cfg.addClass(YWorkItem.class);
                cfg.addClass(YLogIdentifier.class);
                cfg.addClass(YWorkItemEvent.class);

                _factory = cfg.buildSessionFactory();

            } catch (MappingException e) {
                e.printStackTrace();
            }
        }
    }

    private void doPersistAction(String query, int operation) throws YPersistenceException {
        if (!persistenceOn) {
            return;
        }
        try {
            Session session = _factory.openSession();
            Transaction tx = session.beginTransaction();
            if (UPDATE_OPERATION == operation) {
                session.update(query);
            } else if (DELETE_OPERATION == operation) {
                session.delete(query);
            } else if (SAVE_OPERATION == operation) {
                session.save(query);
            }
            session.flush();
            tx.commit();
            session.close();
        } catch (Exception e) {
            throw new YPersistenceException("Hibernate problem: " + e.getMessage(), e);
        }
    }

    private void doPersistAction(Object obj, int operation) throws YPersistenceException {
        if (!persistenceOn) {
            return;
        }
        try {
            Session session = _factory.openSession();
            Transaction tx = session.beginTransaction();
            if (UPDATE_OPERATION == operation) {
                session.update(obj);
            } else if (DELETE_OPERATION == operation) {
                session.delete(obj);
            } else if (SAVE_OPERATION == operation) {
                session.save(obj);
            }
            session.flush();
            session.evict(obj);
            tx.commit();
            session.close();
        } catch (HibernateException e) {
            throw new YPersistenceException("Hibernate problem: " + e.getMessage(), e);
        }
    }

    private Query createQuery(String queryString) throws YPersistenceException {
        if (!persistenceOn) {
            return null;
        }
        Query query = null;

        try {
            query = _factory.openSession().createQuery(queryString);
        } catch (HibernateException e) {
            throw new YPersistenceException("Failure to create Hibernate query object", e);
        }
        return query;
    }


    //########################################################################
    //new getters
    public Resource[] getResources() throws YPersistenceException {
        if (persistenceOn) {
            return (Resource[]) getObjectsForClass(Resource.class.getName()).toArray(new Resource[0]);
        } else {
            //  Get the user from the engine
            String sessionHandle = new String();
            try {
                sessionHandle = iaClient.connect("admin", "YAWL");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            List result = new LinkedList();
            result = iaClient.getUsers(sessionHandle);

            Resource[] res = new Resource[result.size()];

            if (result.size() == 0) {
                return new Resource[0];
            }

            for (int i = 0; i < result.size(); i++) {
                User user = (User) result.get(i);
                HumanResource human = new HumanResource(user.getUserID());
                human.setPassword(user.getPassword());
                human.setIsAdministrator(user.getIsAdmin());
                human.setGivenName("");
                human.setSurname("");
                human.setDescription("");
                res[i] = human;
            }

            return res;

        }
    }

    public Resource getResource(String resourceID) throws YPersistenceException {
        try {
            Query query = createQuery("from " + Resource.class.getName() + " as resource where resource.id = '" + resourceID + "'");
            if (query != null) {
                List resourceLst = query.list();
                if (resourceLst.size() < 1) {
                    return null;
                }
                return (Resource) query.iterate().next();
            } else {
                //  Get the user to the engine
                String sessionHandle = new String();
                try {
                    sessionHandle = iaClient.connect("admin", "YAWL");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                List result = new LinkedList();
                HumanResource human = null;
                result = iaClient.getUsers(sessionHandle);

                if (result.size() == 0) {
                    return human;
                }

                for (int i = 0; i < result.size(); i++) {
                    User user = (User) result.get(i);
                    if (user.getUserID().equalsIgnoreCase(resourceID)) {
                        human = new HumanResource(resourceID);
                        human.setPassword(user.getPassword());
                        human.setIsAdministrator(user.getIsAdmin());
                    }

                }
                return human;
            }
        } catch (HibernateException e) {
            throw new YPersistenceException("Unexpected hibernate problem.", e);
        }
    }

    public void deleteResource(String resourceID) throws YPersistenceException {
        doPersistAction(getResource(resourceID), DELETE_OPERATION);
    }

    public void addResource(Resource resource) throws YPersistenceException {
        doPersistAction(resource, SAVE_OPERATION);
    }

    public void addOrEditResource(Resource resource) throws YPersistenceException {
        try {
            doPersistAction(resource, SAVE_OPERATION);
        } catch (YPersistenceException ype) {
            try {
                doPersistAction(resource, UPDATE_OPERATION);
            } catch (YPersistenceException e) {
                throw e;
            }
        }
    }


    public HumanResourceRole[] getHresPerformsRoles() throws YPersistenceException {
        return (HumanResourceRole[])
                getObjectsForClass(HumanResourceRole.class.getName()).toArray(new HumanResourceRole[0]);
    }

    public HumanResourceRole getHumanResourceRole(String resourceID, String roleName) throws YPersistenceException {
        try {
            Query roleQuery = createQuery(
                    "from " + HumanResourceRole.class.getName() + " as row where " +
                    "row.HumanResource = '" + resourceID + "' and row.Role = '" + roleName + "'");
            List roleLst = roleQuery.list();
            if (roleLst.size() < 1) {
                return null;
            }
            return (HumanResourceRole) roleQuery.iterate().next();
        } catch (HibernateException e) {
            throw new YPersistenceException("Unexpected hibernate problem.", e);
        }
    }

    public Resource[] getResourcesPerformingRole(String role) throws YPersistenceException {
        String userids = "";
        try {
            Query query = createQuery("from au.edu.qut.yawl.admintool.model.HumanResourceRole as hresrole where hresrole.Role = '" + role + "'");

            List resourcesinrole = query.list();
            if (resourcesinrole.size() < 1) {
                return null;

            }

            List resources = new ArrayList();
            for (int i = 0; i < resourcesinrole.size(); i++) {
                HumanResourceRole resourceinrole = (HumanResourceRole) resourcesinrole.get(i);
                resources.add(resourceinrole.getHumanResource());
            }
            return (Resource[]) resources.toArray(new Resource[resources.size()]);
        } catch (HibernateException e) {
            throw new YPersistenceException("Failure to get user id");
        }
    }

    public Role[] getRolesPerformedByResource(String resourceID) throws YPersistenceException {
        try {
            Query roleQuery = createQuery(
                    "from " + HumanResourceRole.class.getName() + " as hresrole where " +
                    "hresrole.HumanResource = '" + resourceID + "'");
            List humanResourceRoleList = roleQuery.list();
            if (humanResourceRoleList.size() < 1) {
                return null;
            }
            List roles = new ArrayList();
            for (int i = 0; i < humanResourceRoleList.size(); i++) {
                HumanResourceRole humanResourceRole = (HumanResourceRole) humanResourceRoleList.get(i);
                roles.add(humanResourceRole.getRole());
            }
            return (Role[]) roles.toArray(new Role[roles.size()]);
        } catch (HibernateException e) {
            throw new YPersistenceException("Unexpected hibernate problem.", e);
        }
    }

    public void addHresPerformsRole(String resourceID, String selectrole) throws YPersistenceException {
        Resource resource = getResource(resourceID);
        if (resource == null) {
            throw new YPersistenceException(
                    binaryRelationFKMissingMsg.format(new String[]{"resource", resourceID, "role"}));
        }
        Role role = getRole(selectrole);
        if (role == null) {
            throw new YPersistenceException(
                    binaryRelationFKMissingMsg.format(new String[]{"role", selectrole, "resource"}));
        }
        HumanResourceRole hResRole = new HumanResourceRole();
        hResRole.setHumanResource(resource);
        hResRole.setRole(role);
        doPersistAction(hResRole, SAVE_OPERATION);
    }

    public void delHResPerformsRole(String resourceID, String selectrole) throws YPersistenceException {
        doPersistAction(getHumanResourceRole(resourceID, selectrole), DELETE_OPERATION);
    }


    public Role[] getRoles() throws YPersistenceException {
        return (Role[]) getObjectsForClass(Role.class.getName()).toArray(new Role[0]);
    }

    public Role getRole(String roleName) throws YPersistenceException {
        try {
            Query roleQuery = createQuery(
                    "from " + Role.class.getName() +
                    " as role where role.roleName = '" + roleName + "'");
            List roleLst = roleQuery.list();
            if (roleLst.size() < 1) {
                return null;
            }
            return (Role) roleQuery.iterate().next();
        } catch (HibernateException e) {
            throw new YPersistenceException("Unexpected hibernate problem.", e);
        }
    }

    /**
     * PRE: Role exists in DB
     */
    public void deleteRole(String roleName) throws YPersistenceException {
        Role role = getRole(roleName);
        doPersistAction(role, DELETE_OPERATION);
    }

    public void addRole(String role) throws YPersistenceException {
        Role roleObj = new Role();
        roleObj.setRoleName(role);
        this.doPersistAction(roleObj, SAVE_OPERATION);
    }


    private List getObjectsForClass(String className) throws YPersistenceException {
        try {
            Query query = createQuery("from " + className);
            if (query != null) {
                return query.list();
            } else {
                return null;
            }
        } catch (HibernateException e) {
            throw new YPersistenceException("Unexpected hibernate problem.", e);
        }
    }

    public void assignHuman2Roles(String resourceID, String selectrole) throws YPersistenceException {

        /*
          Should be in a transaction...
         */

        Resource resource = getResource(resourceID);
        /*
          Remove all old roles to human matches
          from eg.Cat as cat where cat.name='Fritz'
        */
        String query = "from HumanResourceRole as hresrole where hresrole.HumanResource = '" + resourceID + "'";
        doPersistAction(query, DELETE_OPERATION);

        if (resource == null) {
            throw new YPersistenceException(binaryRelationFKMissingMsg.format(new String[]{"resource", resourceID, "role"}));
        }
        /*
          We are adding multiple roles
        */
        StringTokenizer roles = new StringTokenizer(selectrole, "$");
        while (roles.hasMoreTokens()) {

            Role role = getRole(roles.nextToken());
            if (role == null) {
                throw new YPersistenceException(binaryRelationFKMissingMsg.format(new String[]{"role", selectrole, "resource"}));
            }
            HumanResourceRole hResRole = new HumanResourceRole();
            hResRole.setHumanResource(resource);
            hResRole.setRole(role);

            doPersistAction(hResRole, SAVE_OPERATION);

        }

    }

    public void assignRole2Humans(String roleID, String selecthuman) throws YPersistenceException {
        if (selecthuman != null && roleID != null) {
            Role role = getRole(roleID);
            /*
              Remove all old role to human matches
              from eg.Cat as cat where cat.name='Fritz'
            */
            String query = "from HumanResourceRole as hresrole where hresrole.Role = '" + roleID + "'";
            doPersistAction(query, DELETE_OPERATION);

            if (role == null) {
                throw new YPersistenceException(
                        binaryRelationFKMissingMsg.format(new String[]{"resource", roleID, "role"}));
            }
            /*
              We are adding multiple humans
            */
            StringTokenizer humans = new StringTokenizer(selecthuman, "$");
            while (humans.hasMoreTokens()) {

                Resource resource = getResource(humans.nextToken());
                if (humans == null) {
                    throw new YPersistenceException(
                            binaryRelationFKMissingMsg.format(new String[]{"role", selecthuman, "resource"}));
                }
                HumanResourceRole hResRole = new HumanResourceRole();
                hResRole.setHumanResource(resource);
                hResRole.setRole(role);

                doPersistAction(hResRole, SAVE_OPERATION);

            }
        } else {
            //failure
        }
    }


    public List getWorkItemStatus(String specid) {
        List items = null;

        try {

            String hql = "Select count(item._status),item._status from au.edu.qut.yawl.engine.YWorkItem as item group by item._status";

            //insert where query on specid here
            if (!specid.equalsIgnoreCase("All Specifications")) {
                hql = hql + " where yawlcase.specification='" + specid + "'";
            }

            Query query = createQuery(hql);
            items = query.list();

            System.out.println(items);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return items;
    }


    public List getAverageSpecTimes(int timeformat, int granularity) {
        List items = new LinkedList();
        List resultitems = new LinkedList();
        try {
            String hql = "Select yawlcase.created, yawlcase.completed,yawlcase.specification from au.edu.qut.yawl.engine.YLogIdentifier as yawlcase";
            Query query = createQuery(hql);
            items = query.list();
            System.out.println("rows for average spec times: " + items.size());

            HashMap map = new HashMap();


            for (int i = 0; i < items.size(); i++) {
                Object[] average = (Object[]) items.get(i);

                if ((average[2] != null) && (average[1] != null) && (average[0] != null)) {

                    if (map.get(average[2]) != null) {
                        List l = (List) map.get(average[2]);
                        Long newlong = new Long((new Long((String) average[1])).longValue() - (new Long((String) average[0])).longValue());
                        l.add(newlong);
                        System.out.println("already there...adding to list: " + l + " " + average[2]);

                    } else {
                        List l = new LinkedList();
                        System.out.println(new Long((String) average[0]).longValue());
                        System.out.println(new Long((String) average[1]).longValue());

                        Long valueaverage = new Long(new Long((String) average[1]).longValue() - new Long((String) average[0]).longValue());


                        l.add(valueaverage);
                        map.put(average[2], l);
                        System.out.println("NOT there...creating list: " + l + " " + average[2]);
                    }
                }

            }

            System.out.println(map);
            Set keys = map.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                System.out.println(key);
                List valuelist = (List) map.get(key);
                long l = 0;
                for (int i = 0; i < valuelist.size(); i++) {
                    l = l + ((Long) valuelist.get(i)).longValue();
                }
                l = l / valuelist.size();
                Object[] oarray = new Object[2];

                if (timeformat == TIMEFORMAT_LONG) {
                    oarray[0] = new Integer(DateTransform.getTime(l, granularity));
                } else {
                    oarray[0] = DateTransform.transform((long) l);
                }
                oarray[1] = key;
                resultitems.add(oarray);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultitems;
    }

    public List executeQueryCases(String hql) {
	List result = new LinkedList();
	try {
	    Query query = createQuery(hql);
	    System.out.println("Executing Query: " + hql);
	    List items = query.list();
	    System.out.println("rows: " + items.size());
	    for (int i = 0; i < items.size(); i++) {
		YLogIdentifier caseinfo = (YLogIdentifier) items.get(i);
		result.add(caseinfo);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}	
	return result;
    }

    public List executeQueryWorkItems(String hql) {
	List result = new LinkedList();
	try {
	    Query query = createQuery(hql);
	    System.out.println("Executing Query: " + hql);
	    List items = query.list();
	    System.out.println("rows: " + items.size());
	    for (int i = 0; i < items.size(); i++) {
		YWorkItemEvent caseinfo = (YWorkItemEvent) items.get(i);
		result.add(caseinfo);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}	
	return result;
    }


    public List getCaseProcessingTime(String specid, int timeformat, int granularity) {

        List items = null;
        List caseitems = new LinkedList();
        try {
            String hql = "Select yawlcase.created, yawlcase.completed, yawlcase.identifier from au.edu.qut.yawl.engine.YLogIdentifier as yawlcase";

            if (!specid.equalsIgnoreCase("All Specifications")) {
                hql = hql + " where yawlcase.specification='" + specid + "'";
            }


            Query query = createQuery(hql);
            items = query.list();
            System.out.println("rows: " + items.size());
            for (int i = 0; i < items.size(); i++) {
                Object[] caseinfo = (Object[]) items.get(i);
                if ((caseinfo[0] != null) && (caseinfo[1] != null) && (caseinfo[2] != null)) {

                    long comptimemillis = new Long(((String) (caseinfo[1]))).longValue() - new Long(((String) (caseinfo[0]))).longValue();
                    int comptime = DateTransform.getTime(comptimemillis, granularity);

                    System.out.println(comptime);

                    Object[] data = new Object[2];
                    data[1] = caseinfo[2] + " :case";
                    if (timeformat == TIMEFORMAT_LONG) {
                        data[0] = new Integer(comptime);
                    } else {
                        data[0] = DateTransform.transform((long) comptimemillis);
                    }

                    caseitems.add(data);
                }
            }

            System.out.println(items);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return caseitems;
    }


    public static synchronized String encrypt(String plaintext) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = null;

        md = MessageDigest.getInstance("SHA"); //step 2
        md.update(plaintext.getBytes("UTF-8")); //step 3

        byte raw[] = md.digest(); //step 4
        String hash = (new BASE64Encoder()).encode(raw); //step 5
        return hash; //step 6
    }


    public String[] getSpecs() {
        List items = new LinkedList();
        String[] specids = null;
        try {
            String hql = "Select distinct yawlcase.specification from au.edu.qut.yawl.engine.YLogIdentifier as yawlcase";
            Query query = createQuery(hql);
            items = query.list();
            specids = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                String s = (String) items.get(i);
                specids[i] = s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return specids;
    }

    public String[] getCases() {
        List items = new LinkedList();
        String[] caseids = null;
        try {
            String hql = "Select distinct yawlcase.identifier from au.edu.qut.yawl.engine.YLogIdentifier as yawlcase";
            Query query = createQuery(hql);
            items = query.list();
            caseids = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                String s = (String) items.get(i);
                caseids[i] = s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return caseids;
    }

    public String[] getLoggedResources() {
        List items = new LinkedList();
        List resourceids = null;
	String[] finallist = null;
        try {
	    /*
	      Need to remove null values from this list
	     */
            String hql = "Select distinct yawlevent.resource from au.edu.qut.yawl.engine.YWorkItemEvent as yawlevent";
            Query query = createQuery(hql);
            items = query.list();
            resourceids = new LinkedList();
            for (int i = 0; i < items.size(); i++) {
                String s = (String) items.get(i);
		
		System.out.println("Found resource -" + s + "- in the database");

		if (s!=null && !s.equals("null")) {
		    resourceids.add(s);
		}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

	finallist = new String[resourceids.size()];
	for (int i = 0; i < resourceids.size();i++) {
	    finallist[i] = (String) resourceids.get(i);
	}	
	
        return finallist;
    }


    public String[] getTasks() {
        List items = new LinkedList();
        String[] taskids = null;
        try {
            String hql = "Select distinct yawlevent.taskid from au.edu.qut.yawl.engine.YWorkItemEvent as yawlevent";
            Query query = createQuery(hql);
            items = query.list();
            taskids = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                String s = (String) items.get(i);
                taskids[i] = s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskids;
    }
 
    public String[] getWorkItems() {
        List items = new LinkedList();
        String[] workitemids = null;
        try {
            String hql = "Select distinct yawlevent.identifier from au.edu.qut.yawl.engine.YWorkItemEvent as yawlevent";
            Query query = createQuery(hql);
            items = query.list();
            workitemids = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                String s = (String) items.get(i);
                workitemids[i] = s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return workitemids;
    }
    
}

