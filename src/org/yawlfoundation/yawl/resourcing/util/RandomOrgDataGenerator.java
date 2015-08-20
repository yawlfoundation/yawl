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

package org.yawlfoundation.yawl.resourcing.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author: Michael Adams
 * Creation Date: 7/02/2008
 */
public class RandomOrgDataGenerator {

    private Logger _log = LogManager.getLogger(RandomOrgDataGenerator.class);
    private ResourceManager _rm;

    private String[] f = {"Alex", "Bill", "Carol", "Diane", "Errol", "Frank", "George",
                          "Hillary", "Irene", "Jack", "Kate", "Larry", "Michael",
                          "Neil", "Oprah", "Penny", "Quebert", "Ronald", "Sven",
                          "Thomas", "Uri", "Victoria", "Wendy", "Xavier", "Yvonne", "Zac"};

    private String[] l = {"Adams", "Brown", "Cooper", "Donald", "Evans", "Ford", "Green",
                          "Humphries", "Isaacs", "Jones", "Klein", "Locke", "McKay",
                          "Nelson", "O'Brien", "Petersen", "Quincy", "Roberts", "Smith",
                          "Thomson", "Ulrich", "Van Dyke", "Wilson", "Xerxes",
                          "Young", "Zappa"};

    public RandomOrgDataGenerator() {}

    public void generate(int count) {
        int howManyToCreate = Math.min(count, 100);
        _rm = ResourceManager.getInstance();
        ResourceDataSet orgDataSet = _rm.getOrgDataSet();

        // ensure it is valid to generate dummy data to org tables
        if (okToGenerate()) {
            try {

                // make some roles
                Role[] roles = createRoles(orgDataSet);

                // and some org groups
                OrgGroup[] orgGroups = createOrgGroups(orgDataSet);

                // and some Positions
                Position[] positions = createPositions(orgDataSet);

                // and some capabilities
                Capability[] capabilities = createCapabilities(orgDataSet);

                // link positions to groups
                for (int i=0; i<4; i++) positions[i].setOrgGroup(orgGroups[i]);

                // OK - let's make some Participants
                createParticipants(howManyToCreate, roles, positions, capabilities);

                // save the relation updates
                for (Role r : roles) r.save();
                for (Position p : positions) p.save();

                _log.info("GENERATE ORG DATA: Successfully created {} random " +
                        "Participants and associated org data", howManyToCreate);
            }
            catch (Exception e) {
                _log.error("Exception thrown generating random org data", e);
            }
        }
    }


    private boolean okToGenerate() {
        String msg = null;
        if (_rm.getOrgDataSet().getParticipantCount() > 0) {
            msg = "Participant table not empty";
        }
        if (! _rm.isPersisting()) {
            msg = "Persistence must be enabled to populate tables";
        }
        if (! _rm.isDefaultOrgDB()) {
            msg = "Cannot generate dummy data to non-YAWL-default database";
        }
        if (msg != null) {
            _log.error("GENERATE ORG DATA ERROR: {}. Generation of dummy data aborted.", msg);
        }
        return msg == null;
    }


    private Role[] createRoles(ResourceDataSet orgDataSet) {
        Role r4 = new Role("senior analyst");
        r4.setPersisting(true);
        Role r3 = new Role("analyst");
        r3.setPersisting(true);
        Role r2 = new Role("programmer");
        r2.setPersisting(true);
        Role r1 = new Role("apprentice programmer");
        r1.setPersisting(true);

        orgDataSet.addRole(r1);
        orgDataSet.addRole(r2);
        orgDataSet.addRole(r3);
        orgDataSet.addRole(r4);

        r1.setOwnerRole(r2);
        r2.setOwnerRole(r3);
        r3.setOwnerRole(r4);

        return new Role[] {r1, r2, r3, r4};
    }


    private OrgGroup[] createOrgGroups(ResourceDataSet orgDataSet) {
        OrgGroup o1 = new OrgGroup("SoftwareDiv", OrgGroup.GroupType.DIVISION, null,
                "Software Division", true);
        orgDataSet.addOrgGroup(o1);

        OrgGroup o2 = new OrgGroup("progDept", OrgGroup.GroupType.DEPARTMENT, o1,
                "Programming Department", true);
        orgDataSet.addOrgGroup(o2);

        OrgGroup o3 = new OrgGroup("teamA", OrgGroup.GroupType.TEAM, o2,
                "Code Team A", true);
        orgDataSet.addOrgGroup(o3);

        OrgGroup o4 = new OrgGroup("teamB", OrgGroup.GroupType.TEAM, o2,
                "Code Team B", true);
        orgDataSet.addOrgGroup(o4);

        return new OrgGroup[] {o1, o2, o3, o4};
    }


    private Position[] createPositions(ResourceDataSet orgDataSet) {
        Position p1 = new Position("Vice President");
        p1.setPersisting(true);
        Position p2 = new Position("manager");
        p2.setPersisting(true);
        Position p3 = new Position("supervisor");
        p3.setPersisting(true);
        Position p4 = new Position("level A");
        p4.setPersisting(true);

        orgDataSet.addPosition(p1);
        orgDataSet.addPosition(p2);
        orgDataSet.addPosition(p3);
        orgDataSet.addPosition(p4);

        p4.setReportsTo(p3);
        p3.setReportsTo(p2);
        p2.setReportsTo(p1);

        return new Position[] {p1, p2, p3, p4};
    }


    private Capability[] createCapabilities(ResourceDataSet orgDataSet) {
        Capability c1 = new Capability("firstaid", "first aid officer", true);
        Capability c2 = new Capability("java", "java programmer", true);
        Capability c3 = new Capability("forklift", "forklift license", true);
        Capability c4 = new Capability("mandarin", "mandarin speaker", true);
        orgDataSet.addCapability(c1);
        orgDataSet.addCapability(c2);
        orgDataSet.addCapability(c3);
        orgDataSet.addCapability(c4);

        return new Capability[] {c1, c2, c3, c4};
    }


    private void createParticipants(int howManyToCreate, Role[] roles,
                                    Position[] positions, Capability[] capabilities) {
        List<String> addedUsers = new ArrayList<String>();
        boolean unique;
        String last = "", first = "", user = "";
        Random rand = new Random();

        for (int i = 0; i < howManyToCreate; i++) {

            // ensure all userids are unique
            unique = false;
            while (! unique) {
                first = f[rand.nextInt(26)];
                last = l[rand.nextInt(26)];
                user = last + first.substring(0, 1);
                if (! addedUsers.contains(user)) {
                    addedUsers.add(user);
                    unique = true;
                }
            }

            Participant p = new Participant(last, first, user, true);

            p.setAdministrator(rand.nextBoolean());
            p.setPassword(PasswordEncryptor.encrypt("apple", "apple"));
            p.addPosition(positions[rand.nextInt(4)]);
            p.addCapability(capabilities[rand.nextInt(4)]);
            p.addRole(roles[rand.nextInt(4)]);
            p.getUserPrivileges().allowAll();
            _rm.addParticipant(p);
        }
    }

}
