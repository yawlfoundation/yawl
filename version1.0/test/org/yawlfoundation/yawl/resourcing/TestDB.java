package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

import java.util.*;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestDB extends TestCase {


    public void testDB() {

        int HOW_MANY_PARTICIPANTS_TO_CREATE = 20 ;

        ResourceManager rm = ResourceManager.getInstance();
        rm.setPersisting(true);
        rm.initOrgDataSource("HibernateImpl", -1) ;
        Random rand = new Random();

        String[] f = {"Alex", "Bill", "Carol", "Diane", "Errol", "Frank", "George",
                      "Hilary", "Irene", "Joanne"} ;
        String[] l = {"Smith", "Jones", "Brown", "Black", "Roberts", "Lewis", "Johns",
                      "Green", "Gold", "Davies"} ;

        Role r2 = new Role("a larger role") ;
        r2.setPersisting(true);
        Role r = new Role("a shared role");
        r.setPersisting(true);

        rm.addRole(r2);
        rm.addRole(r);
        r.setOwnerRole(r2);

        OrgGroup o = new OrgGroup("mega", OrgGroup.GroupType.DIVISION, null, "mega") ;
        o.setPersisting(true);
        rm.addOrgGroup(o);

        OrgGroup o2 = new OrgGroup("minor", OrgGroup.GroupType.TEAM, o, "minor") ;
        o2.setPersisting(true);
        rm.addOrgGroup(o2);

        Position po = new Position("a position");
        po.setPersisting(true);
        Position p2 = new Position("manager") ;
        p2.setPersisting(true);
        rm.addPosition(p2);
        rm.addPosition(po);
        po.setReportsTo(p2);
        po.setOrgGroup(o2);
        p2.setOrgGroup(o2);

        Capability c = new Capability("a capability", "some description", true) ;
        rm.addCapability(c);


        for (int i=0; i < HOW_MANY_PARTICIPANTS_TO_CREATE; i++) {
            String first = f[rand.nextInt(10)] ;
            String last = l[rand.nextInt(10)] ;
            String user = last + first.substring(0,1) ;
            Participant p = new Participant(last, first, user, true) ;
            rm.addParticipant(p);

            p.setAdministrator(rand.nextBoolean());
            p.setPassword("apple");
 
            p.addPosition(po);
            p.addCapability(c);
            p.addRole(r);
            p.getUserPrivileges().allowAll();

        }

////        List res = odb.selectRoleParticipants("RO-60ee7015-94eb-4ccc-930f-20c05a5d5259") ;
////        Participant p = (Participant) res.iterator().next() ;
////        System.out.println(p.getFullName());
////
////        p = odb.selectParticipant("PA-756e795b-d08a-430f-889f-7030ceb2e878");
////        System.out.println(p.getFullName());
//
//        List res = odb.selectAllParticipants() ;
//        Iterator i = res.iterator() ;
//        while (i.hasNext()) {
//            Participant p = (Participant) i.next() ;
//            System.out.println(p.getFullName());
//
//            Set set = p.getRoles() ;
//            if (set != null) {
//               Role rx = (Role) set.iterator().next() ;
//               System.out.println(rx.toString()) ;
//            }
//            else System.out.println("role set is null");
//        }
//
//        List<Role> rList = odb.selectAllRoles();
//
//        for (Role rx : rList) System.out.println(rx.toString()) ;

//        rm.loadResources();
//
//        HashSet<Participant> pSet = rm.getParticipants();
//        Iterator i = pSet.iterator() ;
//        while (i.hasNext()) {
//            Participant p = (Participant) i.next() ;
//            System.out.println(p.getFullName());
//
//            Set set = p.getRoles() ;
//            if (set != null) {
//               Role rx = (Role) set.iterator().next() ;
//               System.out.println(rx.toString()) ;
//            }
//            else System.out.println("role set is null");
//
//            set = p.getPositions() ;
//            if (set != null) {
//               Position px = (Position) set.iterator().next() ;
//               System.out.println(px.toString()) ;
//            }
//            else System.out.println("role set is null");
//
//            set = p.getCapabilities() ;
//            if (set != null) {
//               Capability cx = (Capability) set.iterator().next() ;
//               System.out.println(cx.toString()) ;
//            }
//            else System.out.println("role set is null");
//        }
//
//        HashSet<Role> rList = rm.getRoles();
//        for (Role rx : rList) System.out.println(rx.toString()) ;
//
//        HashSet<Capability> cList = rm.getCapabilities();
//        for (Capability cx : cList) System.out.println(cx.toString()) ;
//
//        HashSet<Position> pList = rm.getPositions();
//        for (Position px : pList) System.out.println(px.toString()) ;
//
//        HashSet<OrgGroup> oList = rm.getOrgGroups();
//        for (OrgGroup ox : oList) System.out.println(ox.toString()) ;
//
    }
}
