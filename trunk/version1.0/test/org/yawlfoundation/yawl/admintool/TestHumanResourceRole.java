/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.admintool.model.Role;
import org.yawlfoundation.yawl.admintool.model.Resource;
import org.yawlfoundation.yawl.admintool.model.HumanResource;
import org.yawlfoundation.yawl.admintool.model.HumanResourceRole;
import org.hibernate.HibernateException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 29/09/2005
 * Time: 15:18:49
 * 
 */
public class TestHumanResourceRole extends TestCase {
    private DatabaseGatewayImpl _model;

    public TestHumanResourceRole(String name) {
        super(name);
    }

    public void setUp() throws YPersistenceException, HibernateException {

	    _model = DatabaseGatewayImpl.getInstance(true);
        cleanup();

        _model.addRole("Manager");
        _model.addRole("Worker");
        _model.addRole("Buyer");
        _model.addRole("Earner");
        HumanResource fredRes = new HumanResource("fred");
        fredRes.setDescription("A description about Fred");
        fredRes.setGivenName("Fred");
        fredRes.setIsAdministrator(true);
        fredRes.setIsOfResSerPosType("Resource");
        fredRes.setPassword("password");
        fredRes.setSurname("Jones");
        _model.addResource(fredRes);

        HumanResource peteRes = new HumanResource("Pete");
        peteRes.setDescription("A description about Pete");
        peteRes.setGivenName("Pete");
        peteRes.setIsAdministrator(true);
        peteRes.setIsOfResSerPosType("Resource");
        peteRes.setPassword("password");
        peteRes.setSurname("Hubbert");
        _model.addResource(peteRes);

        _model.addHresPerformsRole("fred", "Manager");
    }

    private void cleanup() throws YPersistenceException {
        Role[] roles = _model.getRoles();
        for (int i = 0; i < roles.length; i++) {
            Role role = roles[i];
            _model.deleteRole(role.getRoleName());
        }
        Resource[] resources = _model.getResources();
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            _model.deleteResource(resource.getRsrcID());
        }

        HumanResourceRole[] hResPerformsRole = _model.getHresPerformsRoles();
        for (int i = 0; i < hResPerformsRole.length; i++) {
            HumanResourceRole humanResourceRole = hResPerformsRole[i];
            _model.delHResPerformsRole(humanResourceRole.getHumanResource().getRsrcID(),
                    humanResourceRole.getRole().getRoleName());
        }
    }


    public void testSimpleAddToHResRole() {
        try {
            _model.addHresPerformsRole("fred", "Buyer");
        } catch (YPersistenceException e) {
            fail(e.getMessage());
        }
    }

    public void testGetRolesPerformedByResource() throws YPersistenceException {
        Role[] roles = _model.getRolesPerformedByResource("fred");
        assertTrue(roles.length == 1);
        assertEquals("Manager", roles[0].getRoleName());
    }

    public void testPreserveReferentialIntegrity() throws YPersistenceException {
        try {
            _model.deleteRole("Manager");
        } catch (YPersistenceException e) {
            fail(e.getMessage());
        }
        assertEquals(null, _model.getHumanResourceRole("fred", "Manager"));
    }

    public void testEnforceReferentialIntegrity() {
        try {
            _model.addHresPerformsRole("Bert_Unknown", "Blacksmith_Undeclared");
        } catch (YPersistenceException e) {
            assertEquals("Database needs to contain a resource [Bert_Unknown] before you can connect it to a [role]", e.getMessage());
        }
    }
}
