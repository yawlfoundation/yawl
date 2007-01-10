/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
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
 * Date: 24/06/2003
 * Time: 18:24:55
 * 
 */
public class TestYMarking extends TestCase{
    private YMarking _marking1;
    private YMarking _marking2;
    private YMarking _marking3;
    private YMarking _marking4;
    private YMarking _marking5;
    private YMarking _marking6;
    private YAtomicTask _xorJoinAndSplit;
    private YAtomicTask _xorJoinXorSplit;
    private YAtomicTask _xorJoinOrSplit;
    private YAtomicTask _andJoinOrSplit;
    private YTask _orJoin;
    private YMarking _marking7;
    private YCondition[] _conditionArr;
    private YNet _loopedNet;
    private YMarking _marking8;
    private YMarking _marking9;
    private YMarking _marking10;
    private YMarking _marking11;


    public TestYMarking(String name){
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        

        YSpecification spec = new YSpecification("");
        spec.setBetaVersion(YSpecification._Beta2);
        YNet _net = new YNet("aNetName", spec);
        
    	_conditionArr = new YCondition[6];
        for (int i = 0; i < _conditionArr.length; i++) {
            _conditionArr[i] = new YCondition("ct"+i, "YConditionInterface " + i, null);
            _net.addNetElement(_conditionArr[i]);
        }
        YIdentifier id1, id2, id3, id4, id5, id6;
        id1 = new YIdentifier();
        id2 = new YIdentifier();
        id3 = new YIdentifier();
        id4 = new YIdentifier();
        id5 = new YIdentifier();
        id6 = new YIdentifier();

        YNetRunner netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id1);
        netRunner.setId(new Long(1));
        YNetRunner.saveNetRunner(netRunner);

        netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id2);
        netRunner.setId(new Long(2));
        YNetRunner.saveNetRunner(netRunner);

        netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id3);
        netRunner.setId(new Long(3));
        YNetRunner.saveNetRunner(netRunner);

        netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id4);
        netRunner.setId(new Long(4));
        YNetRunner.saveNetRunner(netRunner);

        netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id5);
        netRunner.setId(new Long(5));
        YNetRunner.saveNetRunner(netRunner);

        netRunner = new YNetRunner();
        netRunner.setNet(_net);
        netRunner.setCaseID(id6);
        netRunner.setId(new Long(6));
        YNetRunner.saveNetRunner(netRunner);

       
        _conditionArr[0].add(id1);
        _conditionArr[1].add(id1);
        _conditionArr[2].add(id1);
        _conditionArr[3].add(id1);
        _conditionArr[4].add(id1);
        _marking1 = new YMarking(id1, _net);

        _conditionArr[0].add(id2);
        _conditionArr[1].add(id2);
        _conditionArr[2].add(id2);
        _conditionArr[3].add(id2);
        _conditionArr[4].add(id2);
        _marking2 = new YMarking(id2, _net);

        _conditionArr[0].add(id3);
        _conditionArr[1].add(id3);
        _conditionArr[2].add(id3);
        _conditionArr[3].add(id3);
        _marking3 = new YMarking(id3, _net);

        _conditionArr[0].add(id4);
        _conditionArr[1].add(id4);
        _conditionArr[2].add(id4);
        _conditionArr[3].add(id4);
        _conditionArr[4].add(id4);
        _conditionArr[4].add(id4);
        _marking4 = new YMarking(id4, _net);

        _conditionArr[4].add(id5);
        _conditionArr[5].add(id5);
        _marking5 = new YMarking(id5, _net);

        _conditionArr[0].add(id6);
        _conditionArr[1].add(id6);
        _conditionArr[2].add(id6);
        _conditionArr[2].add(id6);
        _conditionArr[3].add(id6);
        _conditionArr[4].add(id6);
        _marking6 = new YMarking(id6, _net);

        int xor = YTask._XOR;
        int and = YTask._AND;
        int or = YTask._OR;
        _xorJoinAndSplit = new YAtomicTask("xorAnd", xor, and, null);
        _xorJoinOrSplit = new YAtomicTask("xorOr", xor, or, null);
        _xorJoinXorSplit = new YAtomicTask("xorXor", xor, xor, null);
        _andJoinOrSplit = new YAtomicTask("andOr", and, or, null);

        YIdentifier id = new YIdentifier();
        _orJoin = new YAtomicTask("orJ", or, and, null);
        for(int i = 0; i < 3; i++){
            _conditionArr[i].setPostset(new YFlow(_conditionArr[i], _xorJoinAndSplit));
            _conditionArr[i + 3].setPostset(new YFlow(_conditionArr[i + 3], _orJoin));
            _conditionArr[i].setPostset(new YFlow(_conditionArr[i], _xorJoinOrSplit));
            _conditionArr[i].setPostset(new YFlow(_conditionArr[i], _xorJoinXorSplit));
            _conditionArr[i].setPostset(new YFlow(_conditionArr[i], _andJoinOrSplit));
            _xorJoinAndSplit.setPostset(new YFlow(_xorJoinAndSplit, _conditionArr[i + 3]));
            _xorJoinOrSplit .setPostset(new YFlow(_xorJoinOrSplit, _conditionArr[i + 3]));
            _xorJoinXorSplit.setPostset(new YFlow(_xorJoinXorSplit, _conditionArr[i + 3]));
            _andJoinOrSplit .setPostset(new YFlow(_andJoinOrSplit, _conditionArr[i + 3]));
            _conditionArr[i] .add(id);
        }
        netRunner = new YNetRunner();
        netRunner.setNet(_net);netRunner.setCaseID(id);netRunner.setId(new Long(7));
        YNetRunner.saveNetRunner(netRunner);
        _marking7 = new YMarking(id, _net);


        URL fileURL = getClass().getResource("YAWLOrJoinTestSpecificationLongLoops.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _loopedNet  = specification.getRootNet();


        
        id = new YIdentifier();
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);netRunner.setCaseID(id);netRunner.setId(new Long(8));
        YNetRunner.saveNetRunner(netRunner);
        ((YCondition)_loopedNet.getNetElement("c(w_d)")).add(id);
        _marking8 = new YMarking(id, _loopedNet);
        id = new YIdentifier();
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);netRunner.setCaseID(id);netRunner.setId(new Long(9));
        YNetRunner.saveNetRunner(netRunner);
        _marking9 = new YMarking(id, _loopedNet);
        ((YCondition)_loopedNet.getNetElement("cA")).add(id);
        _marking10 = new YMarking(id, _loopedNet);
        id = new YIdentifier();
        netRunner = new YNetRunner();
        netRunner.setNet(_loopedNet);netRunner.setCaseID(id);netRunner.setId(new Long(10));
        YNetRunner.saveNetRunner(netRunner);
        ((YCondition)_loopedNet.getNetElement("i-top")).add(id);
        _marking11 = new YMarking(id, _loopedNet);
    }


    public void testEquals(){
        assertTrue(_marking1.equals(_marking2));
        assertTrue(_marking2.equals(_marking1));
        assertFalse(_marking3.equals(_marking1));
        assertFalse(_marking1.equals(_marking3));
    }


    public void testGreaterThanOrEquals(){
        //XPathSaxonUser equal markings
        assertTrue(_marking1.strictlyGreaterThanOrEqualWithSupports(_marking2));
        assertTrue(_marking2.strictlyGreaterThanOrEqualWithSupports(_marking1));
        //XPathSaxonUser m4[0 1 2 3 4 4] m2[0 1 2 3 4]

        assertTrue(_marking4.strictlyGreaterThanOrEqualWithSupports(_marking2));
        //XPathSaxonUser strictly lesser marking with supports

        assertFalse(_marking2.strictlyGreaterThanOrEqualWithSupports(_marking4));
        //XPathSaxonUser m2[0 1 2 3 4] m3[0 1 2 3]

        assertFalse(_marking2.strictlyGreaterThanOrEqualWithSupports(_marking3));

        //XPathSaxonUser m4[0 1 2 3 4 4] m6[0 1 2 2 3 4]

        assertFalse(_marking4.strictlyGreaterThanOrEqualWithSupports(_marking6));
        //XPathSaxonUser m6[0 1 2 2 3 4] m4[0 1 2 3 4 4]

        assertFalse(_marking6.strictlyGreaterThanOrEqualWithSupports(_marking4));
        //XPathSaxonUser m5[4 5] m3[0 1 2 3]

        assertFalse(_marking5.strictlyGreaterThanOrEqualWithSupports(_marking3));

        assertFalse(_marking3.strictlyGreaterThanOrEqualWithSupports(_marking5));

    }


    public void testLessThan(){
        //XPathSaxonUser m2[0 1 2 3 4] m4[0 1 2 3 4 4]
        assertTrue(_marking2.strictlyLessThanWithSupports(_marking4));
        assertFalse(_marking4.strictlyLessThanWithSupports(_marking2));
        //XPathSaxonUser m3[0 1 2 3] m2[0 1 2 3 4]
        assertFalse(_marking3.strictlyLessThanWithSupports(_marking2));
        assertFalse(_marking2.strictlyLessThanWithSupports(_marking3));
        //XPathSaxonUser m4[0 1 2 3 4 4] m6[0 1 2 2 3 4]
        assertFalse(_marking4.strictlyLessThanWithSupports(_marking6));
        assertFalse(_marking6.strictlyLessThanWithSupports(_marking4));
        //XPathSaxonUser equal markings -  should be false
        assertFalse(_marking1.strictlyLessThanWithSupports(_marking2));
    }


    public void testHashcode(){
        assertTrue(_marking1.hashCode() == _marking2.hashCode());
        assertFalse(_marking1.hashCode() == _marking4.hashCode());
        assertFalse(_marking1.hashCode() == _marking3.hashCode());
        assertFalse(_marking4.hashCode() == _marking6.hashCode());
    }


    public void testDoPowerSetRecursion(){
        Set aSet = new HashSet();
        aSet.add("1");
        aSet.add("2");
        aSet.add("3");
        aSet.add("4");
        aSet.add("5");
        Set powerSet = _marking1.doPowerSetRecursion(aSet);
        assertTrue(powerSet.size() == Math.pow(2, aSet.size()) - 1);
//        System.out.println("powerSet: " + powerSet);
    }


    public void testXorJoinAndSplit(){
//System.out.println("_xorJoinAndSplit preset " + _xorJoinAndSplit.getPresetElements());
//System.out.println("_xorJoinAndSplit postset " + _xorJoinAndSplit.getPostsetElements());
//System.out.println("marking locations " + _marking7.getLocations());
        YSetOfMarkings markingSet = _marking7.reachableInOneStep(_xorJoinAndSplit, _orJoin);
        for (Iterator iterator = markingSet.getMarkings().iterator(); iterator.hasNext();) {
            YMarking marking = (YMarking) iterator.next();
            List list = marking.getLocations();
//System.out.println("list is " + list);
            assertTrue(list.size() == 5);
            List conditionsList = new Vector();
            for (int i = 0; i < _conditionArr.length; i++) {
                  conditionsList.add(_conditionArr[i]);
            }
            List visited = new Vector();
            for (Iterator listIter = list.iterator(); listIter.hasNext();) {
                YCondition  condition = (YCondition) listIter.next();
                assertTrue(conditionsList.contains(condition));
                assertFalse(visited.contains(condition));
                visited.add(condition);
            }
        }
    }


    public void testAndJoinOrSplit(){
        _marking7.getLocations().add(new YCondition("ct10", "CT 10", null));
        YSetOfMarkings markingSet = _marking7.reachableInOneStep(_andJoinOrSplit, _orJoin);
//        for (Iterator iterator = markingSet.getMarkings().iterator(); iterator.hasNext();) {
//            YMarking marking = (YMarking) iterator.next();
//            List list = marking.getLocations();
//            System.out.println("" + list);
//        }
        assertEquals(markingSet.getMarkings().size(), 1);
    }


    public void testDeadlock(){
        //deadlocked marking with one token in and-join
        assertTrue(_marking8.deadLock(null));
        //XPathSaxonUser empty marking
        assertTrue(_marking9.deadLock(null));
        //XPathSaxonUser non deadlocked marking
        assertFalse(_marking10.deadLock(null));
        //XPathSaxonUser another non deadlocked marking
        assertFalse(_marking11.deadLock(null));
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYMarking.class);
        return suite;
    }
}
