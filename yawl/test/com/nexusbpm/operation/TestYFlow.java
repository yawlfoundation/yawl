/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.operation;

import java.util.HashSet;
import java.util.Set;

import com.nexusbpm.operation.WorkflowOperation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;

/**
 * @author Nathan Rose
 */
public class TestYFlow extends TestCase{
    private YSpecification spec;
    private YNet root;
    private YInputCondition ic;
    private YOutputCondition oc;
    private YTask[] t = new YTask[ 7 ];
    
    private static final int JOIN = YTask._AND;
    private static final int SPLIT = YTask._AND;
    
    public void setUp(){
        spec = new YSpecification( "TestYFlow_Spec" );
        root = new YNet( "TestYFlow_Net", spec );
        spec.setRootNet( root );
        ic = new YInputCondition( "TestYFlow_input", "input", root );
        oc = new YOutputCondition( "TestYFlow_output", "output", root );
        for( int index = 0; index < t.length; index++ )
            t[index] = new YAtomicTask( "TestYFlow_t" + index, JOIN, SPLIT, root );
        YFlow flow = WorkflowOperation.createFlow();
        WorkflowOperation.attachFlowToElements( flow, ic, t[0] );
        for( int index = 1; index < t.length - 1; index++ ) {
            flow = WorkflowOperation.createFlow();
            WorkflowOperation.attachFlowToElements( flow, t[0], t[index] );
        }
        for( int index = 1; index < t.length - 1; index++ ) {
            flow = WorkflowOperation.createFlow();
            WorkflowOperation.attachFlowToElements( flow, t[index], t[t.length - 1] );
        }
        flow = WorkflowOperation.createFlow();
        WorkflowOperation.attachFlowToElements( flow, t[t.length - 1], oc );
    }
    
    public void testMultiplePostsetFlows() {
        assertTrue( "" + t[0].getPostsetFlows().size(), t[0].getPostsetFlows().size() == ( t.length - 2 ) );
        
        Set<YExternalNetElement> postsetElements = new HashSet<YExternalNetElement>();
        
        for( YFlow flow : t[0].getPostsetFlows() ) {
            assertNotNull( "" + flow, flow.getNextElement() );
            postsetElements.add( flow.getNextElement() );
        }
        
        for( int index = 1; index < t.length - 1; index++ ) {
            assertTrue( index + "\t" + postsetElements, postsetElements.contains( t[index] ) );
        }
    }
    
    public void testMultiplePresetFlows() {
        assertTrue( "" + t[t.length - 1].getPresetFlows().size(), t[t.length - 1].getPresetFlows().size() == ( t.length - 2 ) );
        
        Set<YExternalNetElement> presetElements = new HashSet<YExternalNetElement>();
        
        for( YFlow flow : t[t.length - 1].getPresetFlows() ) {
            assertNotNull( "" + flow, flow.getPriorElement() );
            presetElements.add( flow.getPriorElement() );
        }
        
        for( int index = 1; index < t.length - 1; index++ ) {
            assertTrue( index + "\t" + presetElements, presetElements.contains( t[index] ) );
        } 
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYFlow.class);
        return suite;
    }
}
