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
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 29/01/2004
 * Time: 16:17:58
 * 
 */
public class TestConnections extends TestCase {
    private YSessionCache _sessionCache;
    private YEngine _engine;

    public void setUp() throws YAuthenticationException {
        _engine = YEngine.getInstance();
        _sessionCache = _engine.getSessionCache();
    }


    public void testConnect() throws YPersistenceException {
        clearUsers();
        try {
            _engine.addExternalClient(new YExternalClient("fred", "head", "doco"));
        } catch (YPersistenceException e) {
            fail(e.getMessage());
        }
        boolean valid = _sessionCache.checkConnection("gobbledey gook");
        assertFalse(valid);

        String outcome = _sessionCache.connect("fred", "1234");
        assertTrue(outcome.startsWith("<fail"));

        outcome = _sessionCache.connect("fred", "head");
        assertFalse(outcome.startsWith("<fail"));

        _engine.addExternalClient(new YExternalClient("derf", "wert", null));
        _engine.removeExternalClient("derf");
    }


    private void clearUsers()  {
        _sessionCache.clear();
    }

    
    public void testUnbreakable() throws YAuthenticationException {
        clearUsers();

        boolean valid;
        valid = _sessionCache.checkConnection(null);
        assertFalse(valid);

        valid = _sessionCache.checkConnection("123");
        assertFalse(valid);
    }

    public void testRobust() throws YPersistenceException {
        clearUsers();
        boolean added = _engine.addExternalClient(new YExternalClient(null, null, null));
        assertFalse(added);

        String outcome = _sessionCache.connect(null, null);
        assertEquals("<failure>Null user name</failure>", outcome);
    }

    public void testRemoveUser() throws YPersistenceException {
        clearUsers();
        _engine.addExternalClient(new YExternalClient("fred", "head", "doco"));

        String handle = _sessionCache.connect("fred", "head");

        _engine.addExternalClient(new YExternalClient("derf", "wert", null));
        _engine.removeExternalClient("derf");

        boolean valid = _sessionCache.checkConnection(handle);
        assertFalse(valid);
        _engine.removeExternalClient("derf");
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestConnections.class);
        return suite;
    }
}
