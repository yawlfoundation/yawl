/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 4/05/2004
 * Time: 12:43:03
 * 
 */
public class TestYSetOfMarkings extends TestCase{
    private YSetOfMarkings _markingSet1;
    private YSetOfMarkings _markingSet2;

    public TestYSetOfMarkings(String name){
        super(name);
    }


    public void setUp(){
        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);
        Integer four = new Integer(4);
        Integer five = new Integer(5);
        Integer six = new Integer(6);
        _markingSet1 = new YSetOfMarkings();
        List locs1 = new ArrayList();
        locs1.add(one);
        locs1.add(two);
        _markingSet1.addMarking(new YMarking(locs1));//1,2
        locs1.add(three);
        locs1.add(four);
        _markingSet1.addMarking(new YMarking(locs1));//1,2,3,4

        _markingSet2 = new YSetOfMarkings();
        List locs2 = new ArrayList();
        locs2.add(one);
        locs2.add(two);
        _markingSet2.addMarking(new YMarking(locs2)); //1,2,
        locs2.add(five);
        locs2.add(six);
        _markingSet2.addMarking(new YMarking(locs2));//1,2,5,6
    }


    public void testContainsEquivalentTo(){
        assertTrue(_markingSet1.containsEquivalentMarkingTo(_markingSet2));

    }
}
