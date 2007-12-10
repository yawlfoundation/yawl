/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.state;

import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.jdom.JDOMException;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 26/06/2003
 * Time: 14:14:22
 * 
 */
public class TestYOrJoinUtils extends TestCase{
    private YNet _net;
    private YNet _net2;
    private YNet _net3;
    private YNet _net4;

    public TestYOrJoinUtils(String name){
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        URL fileURL = getClass().getResource("YAWLOrJoinTestSpecification.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                        unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _net  = specification.getRootNet();
        fileURL = getClass().getResource("YAWLOrJoinTestSpecification2.xml");
        yawlXMLFile = new File(fileURL.getFile());
        specification =(YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _net2  = specification.getRootNet();
        fileURL = getClass().getResource("YAWLOrJoinTestSpecificationWithLoop.xml");
        yawlXMLFile = new File(fileURL.getFile());
        specification =(YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _net3  = specification.getRootNet();
        fileURL = getClass().getResource("YAWLOrJoinTestSpecificationLongLoops.xml");
        yawlXMLFile = new File(fileURL.getFile());
            specification =(YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _net4  = specification.getRootNet();
    }


    public void testReduceToEnabled(){
        List markedElements = new Vector();
        markedElements.add(_net.getNetElement("b"));
        markedElements.add(_net.getNetElement("cA"));
        markedElements.add(_net.getNetElement("cC"));
        assertTrue("_parentDecomposition.getNetElements().values().size() " + _net.getNetElements().values().size(),
               _net.getNetElements().values().size() == 12);
        YMarking marking = new YMarking(markedElements);
        Set enabledReduced = YOrJoinUtils.reduceToEnabled(marking, (YTask)_net.getNetElement("d"));
        assertTrue(enabledReduced.size() == 1);
        assertTrue(enabledReduced.contains(_net.getNetElement("b")));
    }


    public void testReduceToEnabled2(){
        List markedElements = new Vector();
        markedElements.add(_net2.getNetElement("b"));
        markedElements.add(_net2.getNetElement("cA"));
        markedElements.add(_net2.getNetElement("cC"));
        assertTrue("_parentDecomposition.getNetElements().values().size() " + _net2.getNetElements().values().size(),
               _net2.getNetElements().values().size() == 12);
        YMarking marking = new YMarking(markedElements);
        Set enabledReduced = YOrJoinUtils.reduceToEnabled(marking, (YTask)_net2.getNetElement("d"));
        assertTrue(enabledReduced.size() == 2);
        assertTrue(enabledReduced.contains(_net2.getNetElement("b")));
        assertTrue(enabledReduced.contains(_net2.getNetElement("a")));
    }


    public void testReduceToEnabled3(){
        List markedElements = new Vector();
        markedElements.add(_net3.getNetElement("b"));
        markedElements.add(_net3.getNetElement("cA"));
        markedElements.add(_net3.getNetElement("cC"));
        assertTrue("_parentDecomposition.getNetElements().values().size() " + _net3.getNetElements().values().size(),
               _net3.getNetElements().values().size() == 13);
        YMarking marking = new YMarking(markedElements);
        Set enabledReduced = YOrJoinUtils.reduceToEnabled(marking, (YTask)_net3.getNetElement("d"));
        assertTrue(enabledReduced.size() == 3);
        assertTrue(enabledReduced.contains(_net3.getNetElement("b")));
        assertTrue(enabledReduced.contains(_net3.getNetElement("a")));
        assertTrue(enabledReduced.contains(_net3.getNetElement("c")));
    }


    public void testPickOptimalEnabledTask(){
        Set enabledTasks = new HashSet();
        YAtomicTask b = (YAtomicTask) _net3.getNetElement("b");
        YAtomicTask q = (YAtomicTask) _net3.getNetElement("q");
        enabledTasks.add(b);
        enabledTasks.add(q);
        YAtomicTask orJoin = (YAtomicTask) _net3.getNetElement("d");
        ArrayList places = new ArrayList();
        places.addAll(b.getPresetElements());
        places.addAll(q.getPresetElements());
        YMarking curr = new YMarking(places);
        YSetOfMarkings set = new YSetOfMarkings();
        Object closest = YOrJoinUtils.pickOptimalEnabledTask(enabledTasks, orJoin, curr, set);
        assertEquals(b, closest);
    }



    public void testPickOptimalEnabledTask2(){
/*
Collection aColl = _net4.getNetElements().values();
for (Iterator iterator = aColl.iterator(); iterator.hasNext();) {
YExternalNetElement element = (YExternalNetElement) iterator.next();
System.out.println("element = " + element);
System.out.println("\telement.getPresetElements() = " + element.getPresetElements());
System.out.println("\telement.getPostsetElements() = " + element.getPostsetElements());
}
*/
        Set enabledTasks2 = new HashSet();
        YAtomicTask b2 = (YAtomicTask) _net4.getNetElement("b");
        YAtomicTask c2 = (YAtomicTask) _net4.getNetElement("c");
        enabledTasks2.add(b2);
        enabledTasks2.add(c2);
        YAtomicTask orJoin2 = (YAtomicTask) _net4.getNetElement("f");
        ArrayList places = new ArrayList();
        places.addAll(b2.getPresetElements());
        places.addAll(c2.getPresetElements());
        YMarking curr = new YMarking(places);
        YSetOfMarkings set = new YSetOfMarkings();
        Object closest = YOrJoinUtils.pickOptimalEnabledTask(enabledTasks2, orJoin2, curr, set);
        assertEquals(c2, closest);
    }



    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYOrJoinUtils.class);
        return suite;
    }
}
