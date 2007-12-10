package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.resource.*;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestJDBC extends TestCase {


    public void testDB() {
        ResourceManager rm = ResourceManager.getInstance();

        DataSource db = rm.getOrgDataSource();

        // work with some objects
        Participant p = rm.getParticipant("a0515b67-2435-4f73-a6da-f1bb797aba04");

        // updates tested ok
//        p.setDescription("updated description");
//
//        Role r = p.getRoles().iterator().next();
//        r.setDescription("a role note");
//
//        Position po = p.getPositions().iterator().next();
//        po.setDescription("a position note");
//
//        OrgGroup o = po.getOrgGroup();
//        o.setDescription("an orggroup note");
//
//        Capability c = p.getCapabilities().iterator().next();
//        c.setDescription("a capability note");

//        db.update(c);
//        db.update(r);
//        db.update(o);
//        db.update(po);
//        db.update(p);
//

        //delete
         db.delete(p);

//        db.delete(c);
//        db.delete(r);
//        db.delete(o);
//        db.delete(po);



//        DataSource db = rm.getOrgDataSource() ;
//        Random rand = new Random();
//
//        String[] f = {"Bob", "Bill", "George", "Henry", "John", "Mary", "Sue", "Ann",
//                      "May", "Kate"} ;
//        String[] l = {"Smith", "Jones", "Brown", "Black", "Roberts", "Lewis", "Johns",
//                      "Green", "Gold", "Davies" } ;
//
//        for (int i=0; i<100; i++) {
//            String first = f[rand.nextInt(10)] ;
//            String last = l[rand.nextInt(10)] ;
//            String user = last + first.substring(0,1) ;
//            Participant p = new Participant(last, first, user) ;
//            p.setAdministrator(rand.nextBoolean());
//
//            Position po = new Position("a position");
//            Capability c = new Capability("a capability", "some description") ;
//            Role r = new Role("a role");
//            p.addPosition(po);
//            p.addCapability(c);
//            p.addRole(r);
//            p.getUserPrivileges().setCanReorder(true);
//
//            db.insertPosition(po);
//            db.insertRole(r);
//            db.insertCapability(c);
//            db.insertUserPrivileges(p.getUserPrivileges());
//            db.insertParticipant(p);
//        }

//        List res = db.selectRoleParticipants("RO-60ee7015-94eb-4ccc-930f-20c05a5d5259") ;
//        Participant p = (Participant) res.iterator().next() ;
//        System.out.println(p.getFullName());

//        p = db.selectParticipant("PA-756e795b-d08a-430f-889f-7030ceb2e878");
//        System.out.println(p.getFullName());

//        List res = db.selectAllParticipants() ;
//        Iterator i = res.iterator() ;
//        while (i.hasNext()) {
//           Participant p = (Participant) i.next() ;
//            System.out.println(p.getSummaryXML());
//        }


//        rm.loadResources();
//        System.out.println(rm.getParticipantsAsXML()) ;




    }
}
