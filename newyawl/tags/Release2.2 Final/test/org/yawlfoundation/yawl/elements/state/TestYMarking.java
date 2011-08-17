package org.yawlfoundation.yawl.elements.state;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
        _conditionArr = new YCondition[6];
        for (int i = 0; i < _conditionArr.length; i++) {
            _conditionArr[i] = new YCondition("ct"+i, "YConditionInterface " + i, null);
        }
        YIdentifier id1, id2, id3, id4, id5, id6;
        id1 = new YIdentifier(null);
        id2 = new YIdentifier(null);
        id3 = new YIdentifier(null);
        id4 = new YIdentifier(null);
        id5 = new YIdentifier(null);
        id6 = new YIdentifier(null);

        id1.addLocation(null, _conditionArr[0]);
        id1.addLocation(null, _conditionArr[1]);
        id1.addLocation(null, _conditionArr[2]);
        id1.addLocation(null, _conditionArr[3]);
        id1.addLocation(null, _conditionArr[4]);
        _marking1 = new YMarking(id1);

        id2.addLocation(null, _conditionArr[0]);
        id2.addLocation(null, _conditionArr[1]);
        id2.addLocation(null, _conditionArr[2]);
        id2.addLocation(null, _conditionArr[3]);
        id2.addLocation(null, _conditionArr[4]);
        _marking2 = new YMarking(id2);

        id3.addLocation(null, _conditionArr[0]);
        id3.addLocation(null, _conditionArr[1]);
        id3.addLocation(null, _conditionArr[2]);
        id3.addLocation(null, _conditionArr[3]);
        _marking3 = new YMarking(id3);

        id4.addLocation(null, _conditionArr[0]);
        id4.addLocation(null, _conditionArr[1]);
        id4.addLocation(null, _conditionArr[2]);
        id4.addLocation(null, _conditionArr[3]);
        id4.addLocation(null, _conditionArr[4]);
        id4.addLocation(null, _conditionArr[4]);
        _marking4 = new YMarking(id4);

        id5.addLocation(null, _conditionArr[4]);
        id5.addLocation(null, _conditionArr[5]);
        _marking5 = new YMarking(id5);

        id6.addLocation(null, _conditionArr[0]);
        id6.addLocation(null, _conditionArr[1]);
        id6.addLocation(null, _conditionArr[2]);
        id6.addLocation(null, _conditionArr[2]);
        id6.addLocation(null, _conditionArr[3]);
        id6.addLocation(null, _conditionArr[4]);
        _marking6 = new YMarking(id6);

        int xor = YTask._XOR;
        int and = YTask._AND;
        int or = YTask._OR;
        _xorJoinAndSplit = new YAtomicTask("xorAnd", xor, and, null);
        _xorJoinOrSplit = new YAtomicTask("xorOr", xor, or, null);
        _xorJoinXorSplit = new YAtomicTask("xorXor", xor, xor, null);
        _andJoinOrSplit = new YAtomicTask("andOr", and, or, null);

        YIdentifier id = new YIdentifier(null);
        _orJoin = new YAtomicTask("orJ", or, and, null);
        for(int i = 0; i < 3; i++){
            _conditionArr[i].addPostset(new YFlow(_conditionArr[i], _xorJoinAndSplit));
            _conditionArr[i + 3].addPostset(new YFlow(_conditionArr[i + 3], _orJoin));
            _conditionArr[i].addPostset(new YFlow(_conditionArr[i], _xorJoinOrSplit));
            _conditionArr[i].addPostset(new YFlow(_conditionArr[i], _xorJoinXorSplit));
            _conditionArr[i].addPostset(new YFlow(_conditionArr[i], _andJoinOrSplit));
            _xorJoinAndSplit.addPostset(new YFlow(_xorJoinAndSplit, _conditionArr[i + 3]));
            _xorJoinOrSplit .addPostset(new YFlow(_xorJoinOrSplit, _conditionArr[i + 3]));
            _xorJoinXorSplit.addPostset(new YFlow(_xorJoinXorSplit, _conditionArr[i + 3]));
            _andJoinOrSplit .addPostset(new YFlow(_andJoinOrSplit, _conditionArr[i + 3]));
            _conditionArr[i] .add(null, id);
        }
        _marking7 = new YMarking(id);

        URL fileURL = getClass().getResource("YAWLOrJoinTestSpecificationLongLoops.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        _loopedNet  = specification.getRootNet();
        id = new YIdentifier(null);
        id.addLocation(null, (YCondition)_loopedNet.getNetElement("c{w_d}"));
        _marking8 = new YMarking(id);
        id = new YIdentifier(null);
        _marking9 = new YMarking(id);
        id.addLocation(null, (YCondition)_loopedNet.getNetElement("cA"));
        _marking10 = new YMarking(id);
        id = new YIdentifier(null);
        id.addLocation(null, (YCondition)_loopedNet.getNetElement("i-top"));
        _marking11 = new YMarking(id);
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
            assertTrue(list.size() == 3);
            List conditionsList = new Vector();
            for (int i = 0; i < _conditionArr.length; i++) {
                  conditionsList.add(_conditionArr[i]);
            }
            List visited = new Vector();
  // System.out.println("conditionsList is " + conditionsList);
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
