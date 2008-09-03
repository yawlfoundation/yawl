/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YEngineStateException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jdom.JDOMException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/02/2004
 * Time: 12:43:53
 * 
 */
public class TestRestServiceMethods extends TestCase{
    private YEngine _engine;
    private YSpecification _specification;
    private YSpecification _specification2;


    public TestRestServiceMethods(String name){
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YEngineStateException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        URL makeMusic = getClass().getResource("MakeMusic.xml");
        URL makeMusic2 = getClass().getResource("MakeMusic2.xml");
        File mmFile = new File(makeMusic.getFile());
        File mm2File = new File(makeMusic2.getFile());
        _specification = (YSpecification) YMarshal.
                unmarshalSpecifications(mmFile.getAbsolutePath()).get(0);
        _specification2 = (YSpecification) YMarshal.
                unmarshalSpecifications(mm2File.getAbsolutePath()).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(_specification);
        _engine.loadSpecification(_specification2);

    }


    public void testGetTask(){
        YTask task = _engine.getTaskDefinition(_specification.getSpecificationID(), "learn");
        assertTrue(task != null);
    }

    public void testGetTaskWithoutSpecification(){
        YTask task = _engine.getTaskDefinition(new YSpecificationID("badSpecName", 0.1), "irrelevant");
        assertTrue(task == null);
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestRestServiceMethods.class);
        return suite;
    }
}
