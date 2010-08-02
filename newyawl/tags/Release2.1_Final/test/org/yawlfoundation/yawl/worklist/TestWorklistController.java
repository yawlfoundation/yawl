package org.yawlfoundation.yawl.worklist;

import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
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
                "<response><taskInfo>" +
                "<specification><identifier/><version>0.1</version><uri>makeTrip2.xml</uri></specification>" +
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
                new InterfaceB_EnvironmentBasedClient("").
                parseTaskInformation(thestring);
        assertTrue(taskinfo != null);
    }
}
