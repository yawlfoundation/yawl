/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.WorklistController;

import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 21/01/2005
 * Time: 17:00:27
 */
public class EngineStateQueryer {

    public EngineStateQueryer() {

        WorklistController wc = new WorklistController();
        wc.setUpInterfaceBClient("http://localhost:8080/yawl/ib");
        String sessionHandle = null;
        try {
            sessionHandle = wc.connect("admin", "YAWL");

            List availableWork = wc.getAvailableWork("admin", sessionHandle);
            /*System.out.println(
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                    "\t\tAvailable Work Items\n" +
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            */
            for (int i = 0; i < availableWork.size(); i++) {
                WorkItemRecord record = (WorkItemRecord) availableWork.get(i);
                //System.out.println("record.toXML() = " + record.toXML());
            }

            List activeWork = wc.getActiveWork("admin", sessionHandle);
            /*System.out.println(
                    "\n\n" +
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                    "\t\tActive Work Items\n" +
                    "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            */
            for (int i = 0; i < activeWork.size(); i++) {
                WorkItemRecord record = (WorkItemRecord) activeWork.get(i);
                //System.out.println("record.toXML() = " + record.toXML());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        EngineStateQueryer esq = new EngineStateQueryer();

    }

}
