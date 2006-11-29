/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.operation;

import com.nexusbpm.operation.WorkflowOperation.NameAndCounter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Nathan Rose
 */
public class TestNameAndCounter extends TestCase {
    private NameAndCounter name1;
    private NameAndCounter name2;
    private NameAndCounter name3;
    private NameAndCounter nameCounter1;
    private NameAndCounter nameCounter2;
    private NameAndCounter nameCounter3;
    private NameAndCounter nameExtension1;
    private NameAndCounter nameExtension2;
    private NameAndCounter nameExtension3;
    private NameAndCounter nameCounterExtension1;
    private NameAndCounter nameCounterExtension2;
    private NameAndCounter nameCounterExtension3;
    
    
    public void setUp(){
        name1 = new NameAndCounter( "" );
        name2 = new NameAndCounter( "blah blah blah_" );
        name3 = new NameAndCounter( "A name_3 and no counter" );
        nameCounter1 = new NameAndCounter( "_5" );
        nameCounter2 = new NameAndCounter( "A name _5 and counter _4" );
        nameCounter3 = new NameAndCounter( "ABC _1_2_3" );
        nameExtension1 = new NameAndCounter( ".xml" );
        nameExtension2 = new NameAndCounter( "blah.txt" );
        nameExtension3 = new NameAndCounter( "asdf asdf a.a sdaf.xml.zip" );
        nameCounterExtension1 = new NameAndCounter( "_3.xml" );
        nameCounterExtension2 = new NameAndCounter( "Simple_1.pdf" );
        nameCounterExtension3 = new NameAndCounter( "asdf_x sd_3_5.html" );
    }
    
    public void testSetOne() {
        checkNameAndCounter( name1, "", "", null, null );
        checkNameAndCounter( name2, "blah blah blah_", "blah blah blah_", null, null );
        checkNameAndCounter( name3, "A name_3 and no counter", "A name_3 and no counter", null, null );
    }
    
    public void testNextNameAndCounterSetOne() {
        NameAndCounter next1 = name1.getNextNameAndCounter();
        NameAndCounter next1b = next1.getNextNameAndCounter();
        NameAndCounter next1c = next1b.getNextNameAndCounter();
        
        checkNameAndCounter( next1, "_2", "", Integer.valueOf( 2 ), null );
        checkNameAndCounter( next1b, "_3", "", Integer.valueOf( 3 ), null );
        checkNameAndCounter( next1c, "_4", "", Integer.valueOf( 4 ), null );
        
        NameAndCounter next2 = name2.getNextNameAndCounter();
        NameAndCounter next2b = new NameAndCounter( next2.toString() );
        
        checkNameAndCounter( next2, "blah blah blah__2", "blah blah blah_", Integer.valueOf( 2 ), null );
        checkNameAndCounter( next2b, "blah blah blah__2", "blah blah blah_", Integer.valueOf( 2 ), null );
        
        NameAndCounter next3 = name3.getNextNameAndCounter();
        
        checkNameAndCounter( next3, "A name_3 and no counter_2",
                "A name_3 and no counter", Integer.valueOf( 2 ), null );
    }
    
    public void testSetTwo() {
        checkNameAndCounter( nameCounter1, "_5", "", Integer.valueOf( 5 ), null );
        checkNameAndCounter( nameCounter2, "A name _5 and counter _4", "A name _5 and counter ",
                Integer.valueOf( 4 ), null );
        checkNameAndCounter( nameCounter3, "ABC _1_2_3", "ABC _1_2", Integer.valueOf( 3 ), null );
    }
    
    public void testNextNameAndCounterSetTwo() {
        NameAndCounter next1 = nameCounter1.getNextNameAndCounter();
        NameAndCounter next2 = nameCounter2.getNextNameAndCounter();
        NameAndCounter next3 = nameCounter3.getNextNameAndCounter();
        
        checkNameAndCounter( next1, "_6", "", Integer.valueOf( 6 ), null );
        checkNameAndCounter( next2, "A name _5 and counter _5", "A name _5 and counter ",
                Integer.valueOf( 5 ), null );
        checkNameAndCounter( next3, "ABC _1_2_4", "ABC _1_2", Integer.valueOf( 4 ), null );
    }
    
    public void testSetThree() {
        checkNameAndCounter( nameExtension1, ".xml", ".xml", null, null );
        checkNameAndCounter( nameExtension2, "blah.txt", "blah", null, ".txt" );
        checkNameAndCounter( nameExtension3, "asdf asdf a.a sdaf.xml.zip",
                "asdf asdf a.a sdaf.xml", null, ".zip" );
    }
    
    public void testNextNameAndCounterSetThree() {
        NameAndCounter next1 = nameExtension1.getNextNameAndCounter();
        NameAndCounter next2 = nameExtension2.getNextNameAndCounter();
        NameAndCounter next3 = nameExtension3.getNextNameAndCounter();
        
        checkNameAndCounter( next1, ".xml_2", ".xml", Integer.valueOf( 2 ), null );
        checkNameAndCounter( next2, "blah_2.txt", "blah", Integer.valueOf( 2 ), ".txt" );
        checkNameAndCounter( next3, "asdf asdf a.a sdaf.xml_2.zip",
                "asdf asdf a.a sdaf.xml", Integer.valueOf( 2 ), ".zip" );
    }
    
    public void testSetFour() {
        checkNameAndCounter( nameCounterExtension1, "_3.xml", "", Integer.valueOf( 3 ), ".xml" );
        checkNameAndCounter( nameCounterExtension2, "Simple_1.pdf",
                "Simple", Integer.valueOf( 1 ), ".pdf" );
        checkNameAndCounter( nameCounterExtension3, "asdf_x sd_3_5.html",
                "asdf_x sd_3", Integer.valueOf( 5 ), ".html" );
    }
    
    public void testNextNameAndCounterSetFour() {
        NameAndCounter next1 = nameCounterExtension1.getNextNameAndCounter();
        NameAndCounter next2 = nameCounterExtension2.getNextNameAndCounter();
        NameAndCounter next3 = nameCounterExtension3.getNextNameAndCounter();
        
        checkNameAndCounter( next1, "_4.xml", "", Integer.valueOf( 4 ), ".xml" );
        checkNameAndCounter( next2, "Simple_2.pdf", "Simple", Integer.valueOf( 2 ), ".pdf" );
        checkNameAndCounter( next3, "asdf_x sd_3_6.html",
                "asdf_x sd_3", Integer.valueOf( 6 ), ".html" );
    }
    
    private void checkNameAndCounter( NameAndCounter nac, String original,
            String name, Integer counter, String extension ) {
        assertNotNull( nac );
        assertNotNull( nac.toString() );
        assertTrue( nac.toString(), nac.toString().equals( original ) );
        assertNotNull( nac.getStrippedName() );
        assertTrue( nac.getStrippedName(), nac.getStrippedName().equals( name ) );
        if( counter != null ) {
            assertNotNull( nac.getCounter() );
            assertTrue( "" + nac.getCounter(), nac.getCounter().intValue() == counter.intValue() );
        }
        else {
            assertNull( nac.getCounter() );
        }
        if( extension != null ) {
            assertNotNull( nac.getExtension() );
            assertTrue( nac.getExtension(), nac.getExtension().equals( extension ) );
        }
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestNameAndCounter.class);
        return suite;
    }
}
