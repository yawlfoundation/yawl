/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


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
