package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestCase;

import java.util.Set;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestHibernate extends TestCase {

    public void testCreateDB() {
        ResourceManager rm = ResourceManager.getInstance();

        System.out.print("rm is not null: ");
        System.out.println(rm != null) ;

        Set set = rm.getParticipants() ;

        System.out.print("set is not null: ");
        System.out.println(set != null) ;


        int s = rm.getParticipants().size();

        System.out.println(s);

//        // create a couple of roles
//        Role r = new Role("sportsman") ;
//        Role r2 = new Role("golfer");
//        r2.setOwnerRole(r);
//
//        // and OrgGroups
//        OrgGroup o = new OrgGroup("Marketing",OrgGroup.GroupType.DEPARTMENT, null, "");
//        OrgGroup o2 = new OrgGroup("Sales", OrgGroup.GroupType.TEAM, o, "") ;
//
//
//        // and Positions
//        Position po = new Position("manager") ;
//        po.setDescription("Manages the blah team");
//        po.setOrgGroup(o2);
//        Position po2 = new Position("boss") ;
//        po.setReportsTo(po2);
//
//        // and Capabilities
//        Capability c = new Capability("first aid", "can administer band-aids");
//
//        // now a Participant
//        Participant p = new Participant("Palmer","Arnold", "palmera");
//        p.setAdministrator(true);
//        p.setAvailable(true);
//        p.setDescription("pretty good golfer");
//        p.setNotes("some notes about arnold");
//        p.addCapability(c);
//        p.addPosition(po);
//        p.addRole(r2);
//
//        // add some User Privileges
//        UserPrivileges up = new UserPrivileges(p.getID());
//        up.setCanReorder(true);
//        up.setCanChainExecution(true);
//        p.setUserPrivileges(up);
//
//        // lets write a few things to the db
//        DataSource hdb = rm.getOrgDataSource() ;
//        hdb.insert(r);
//        hdb.insert(r2);
//        hdb.insert(c);
//        hdb.insert(o);
//        hdb.insert(o2);
//        hdb.insert(po2);
//        hdb.insert(po);
////        hdb.insertUserPrivileges(up);
//        hdb.insert(p);
//
    }
}
