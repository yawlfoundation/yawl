package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:14:05
 * 
 */
public class TestYNetElement extends TestCase{
    public TestYNetElement(String name){
        super(name);
    }

    public void testNothingMuch(){
        YNetElement netEl = new YCondition("netElement1", null, null);
        assertTrue(netEl.getID().equals("netElement1"));
    }
}
