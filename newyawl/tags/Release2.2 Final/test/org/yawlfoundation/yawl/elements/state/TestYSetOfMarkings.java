package org.yawlfoundation.yawl.elements.state;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YNetElement;

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
//        Integer one = new Integer(1);
//        Integer two = new Integer(2);
//        Integer three = new Integer(3);
//        Integer four = new Integer(4);
//        Integer five = new Integer(5);
//        Integer six = new Integer(6);
        YCondition a = new YCondition("a", null);
        YCondition b = new YCondition("b", null);
        YCondition c = new YCondition("c", null);
        YCondition d = new YCondition("d", null);
        YCondition e = new YCondition("e", null);
        YCondition f = new YCondition("f", null);
        List<YNetElement> locs1 = new ArrayList<YNetElement>();
        List<YNetElement> locs2 = new ArrayList<YNetElement>();
        List<YNetElement> locs3 = new ArrayList<YNetElement>();
        List<YNetElement> locs4 = new ArrayList<YNetElement>();
        _markingSet1 = new YSetOfMarkings();
        locs1.add(a);
        locs1.add(b);
        _markingSet1.addMarking(new YMarking(locs1));//1,2
        locs2.addAll(locs1);
        locs2.add(c);
        locs2.add(d);
        _markingSet1.addMarking(new YMarking(locs2));//1,2,3,4

        _markingSet2 = new YSetOfMarkings();
        locs3.add(a);
        locs3.add(b);
        _markingSet2.addMarking(new YMarking(locs3)); //1,2,
        locs4.addAll(locs3);
        locs4.add(e);
        locs4.add(f);
        _markingSet2.addMarking(new YMarking(locs4));//1,2,5,6
    }


    public void testContainsEquivalentTo() {
        assertTrue(_markingSet1.containsEquivalentMarkingTo(_markingSet2));

    }
}
