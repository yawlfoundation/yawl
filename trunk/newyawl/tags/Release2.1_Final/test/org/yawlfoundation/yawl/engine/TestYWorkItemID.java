package org.yawlfoundation.yawl.engine;

import junit.framework.TestCase;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/07/2005
 * Time: 08:25:30
 * 
 */
public class TestYWorkItemID extends TestCase{

    public TestYWorkItemID(String name) {
        super(name);
    }

    public void testUniqueIDGenerator() {
        char[] alphas = UniqueIDGenerator.newAlphas();
        for(int i = 0; i < 14776336; i++){
            alphas = UniqueIDGenerator.nextInOrder(alphas);
        }
        assertEquals("0000000000000000000010000", new String(alphas));
    }



}
