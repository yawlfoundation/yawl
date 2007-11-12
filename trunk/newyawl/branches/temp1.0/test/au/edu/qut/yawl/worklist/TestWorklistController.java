/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist;

import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import junit.framework.TestCase;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/02/2004
 * Time: 14:22:31
 * 
 */
public class TestWorklistController extends TestCase{


    public void testGetTaskInformation(){
        String thestring =
                "<response><taskInfo><specificationID>makeTrip2.xml</specificationID>" +
                "<taskID>register</taskID><taskName>register</taskName>" +
                "<params>" +
                "<inputParam name=\"customer\"><type>xs:string</type><ordering>0</ordering></inputParam>" +
                "<outputParam name=\"payment_account_number\"><type>xs:string</type>" +
                "<ordering>0</ordering><mandatory/></outputParam>" +
                "<outputParam name=\"legs\"><type>mm:LegType</type><ordering>2</ordering><mandatory/>" +
                "</outputParam>" +
                "<outputParam name=\"customer\"><type>xs:string</type><ordering>1</ordering><mandatory/>" +
                "</outputParam>" +
                "</params></taskInfo></response>";
        TaskInformation taskinfo =
                InterfaceB_EnvironmentBasedClient.
                parseTaskInformation(thestring);
        assertTrue(taskinfo != null);
    }
}
