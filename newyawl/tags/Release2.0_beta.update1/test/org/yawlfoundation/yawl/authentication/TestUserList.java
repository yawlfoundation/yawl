/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.authentication;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 29/01/2004
 * Time: 16:17:58
 * 
 */
public class TestUserList extends TestCase {
    private UserList _userList;

    public void setUp() throws YAuthenticationException {
        _userList = UserList.getInstance();

    }


    public void testConnect() throws YAuthenticationException {
        clearUsers();
        try {
            _userList.addUser("fred", "head", false);
        } catch (YAuthenticationException e) {
            fail(e.getMessage());
        }
        Exception f = null;
        try {
            _userList.checkConnection("gobbledey gook");
        } catch (YAuthenticationException e) {
            f= e;
        }
        assertNotNull(f);

        f= null;
        try {
            _userList.connect("fred", "1234");
        } catch (YAuthenticationException e) {
            f= e;
        }
        assertEquals(
                "Password (1234) not valid.",
                f.getMessage());
        String key = null;
        try {
            key = _userList.connect("fred", "head");
            assertTrue(key,
                    _userList.checkConnection(key)
                    ==
                    UserList._permissionGranted);
        } catch (YAuthenticationException e) {
            e.printStackTrace();
        }
        _userList.addUser("derf", "wert", true);
        _userList.removeUser("derf", "fred");
    }

    private void clearUsers()  {

            _userList.clear();

    }

    public void testUnbreakable() throws YAuthenticationException {
        clearUsers();
        Exception f= null;
        try {
            _userList.checkConnection(null);
        } catch (YAuthenticationException e) {
            f = e;
        }
        assertNotNull(f);

        f = null;
        try {
            _userList.checkConnection("123");
        } catch (YAuthenticationException e) {
            f = e;
        }
        assertNotNull(f);
    }

    public void testRobust() throws YAuthenticationException {
        clearUsers();
        try {
            _userList.addUser(null, null, false);
        } catch (YAuthenticationException e) {

        }
        String k = null;
        Exception f = null;
        try {
            k = _userList.connect(null, null);
        } catch (YAuthenticationException e) {
            f= e;
        }
        assertTrue(f.getMessage(),
                f.getMessage().equals(
                        "Userid (null) not valid."));
    }

    public void testRemoveUser() throws YAuthenticationException {
        clearUsers();
        try {
            _userList.addUser("fred", "head", false);
        } catch (YAuthenticationException e) {
            fail(e.getMessage());
        }
        String k = null;
        try {
            k = _userList.connect("fred", "head");
        } catch (YAuthenticationException e) {
            fail(e.getMessage());
        }
        _userList.addUser("derf", "wert", true);
        _userList.removeUser("derf", "fred");
        Exception f = null;
        try {
            _userList.checkConnection(k);
        } catch (YAuthenticationException e) {
            f = e;
        }
        assertNotNull(f);
        _userList.removeUser("derf", "fred");
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestUserList.class);
        return suite;
    }
}
