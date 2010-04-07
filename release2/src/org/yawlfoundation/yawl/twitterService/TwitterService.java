package org.yawlfoundation.yawl.twitterService;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * A simple service that provides for status updates to a Twitter account
 *
 * Author: Michael Adams
 * Creation Date: 25/07/2009
 */

public class TwitterService extends InterfaceBWebsideController {

    // holds a session handle to the engine
    private String _handle = null;

    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        try {

            // connect only if not already connected
            if (! connected()) _handle = connect("admin", "YAWL");

            // checkout ... process ... checkin
            wir = checkOut(wir.getID(), _handle);
            String result = updateStatus(wir);
            checkInWorkItem(wir.getID(), wir.getDataList(),
                            getOutputData(wir.getTaskName(), result), null,  _handle);
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    // have to implement abstract method, but have no need for this event
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {  }


    // these parameters are automatically inserted (in the Editor) into a task
    // decomposition when this service is selected from the list
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[4];
        params[0] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[0].setDataTypeAndName("string", "status", XSD_NAMESPACE);
        params[0].setDocumentation("The status message to post to Twitter");

        params[1] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[1].setDataTypeAndName("string", "userid", XSD_NAMESPACE);
        params[1].setDocumentation("Your Twitter ID");

        params[2] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[2].setDataTypeAndName("string", "password", XSD_NAMESPACE);
        params[2].setDocumentation("Your Twitter password");

        params[3] = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        params[3].setDataTypeAndName("string", "result", XSD_NAMESPACE);
        params[3].setDocumentation("The status result or error mesage returned from Twitter");
        return params;
    }

    
    //********************* PRIVATE METHODS *************************************//

    private String updateStatus(WorkItemRecord wir) {
        String result ;
        String msg = getStatusMsg(wir);
        if (msg != null) {
            Element data = wir.getDataList();
            result = updateStatus(getUserID(data), getPassword(data), msg);
        }
        else result = "Null status passed - Twitter update cancelled";
        return result;
    }


    private String updateStatus(String userid, String password, String msg) {
        String error = checkParams(userid, password) ;
        if (error != null) return error;

        String result;
        Twitter twitter = new Twitter(userid, password);
        try {
            twitter.updateStatus(msg) ;
            result = "Status successfully posted to Twitter";
        }
        catch (TwitterException te) {
            result = te.getMessage();
        }
        return result;
    }


    private String getUserID(Element data) {
        return getDataValue(data, "userid");
    }


    private String getPassword(Element data) {
        return getDataValue(data, "password");
    }


    private String getStatusMsg(WorkItemRecord wir) {
        String result = null;
        String status = getDataValue(wir.getDataList(), "status");
        if (status != null) {
            StringBuilder msg = new StringBuilder(wir.getID());
            msg.append(":: ").append(status);
            result = msg.toString();
        }
        return result;
    }


    private String getDataValue(Element data, String name) {
        return (data != null) ? data.getChildText(name) : null;
    }


    private String checkParams(String userid, String password) {
        String result = checkParam("userid", userid);
        if (result == null) result = checkParam("password", password);
        return result;
    }


    private String checkParam(String name, String value) {
        String result = null;
        if ((value == null) || (value.length() == 0)) {
           result = "No value supplied for " + name;
        }
        return result;
    }


    private Element getOutputData(String taskName, String data) {
        Element output = new Element(taskName);
        Element result = new Element("result");
        result.setText(data);
        output.addContent(result);
        return output;
    }

    
    private boolean connected() {
        return _handle != null;
    }


}
