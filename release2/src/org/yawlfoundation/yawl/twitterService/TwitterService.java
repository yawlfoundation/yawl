package org.yawlfoundation.yawl.twitterService;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Author: Michael Adams
 * Creation Date: 25/07/2009
 */
public class TwitterService extends InterfaceBWebsideController {

    private static final String DEFAULT_ID = "YAWLProc";
    private static final String DEFAULT_PW = "YAWLYAWL8";
    private String _handle = null;

    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        try {
            if (! connected()) _handle = connect("admin", "YAWL");
            wir = checkOut(wir.getID(), _handle);
            String result = updateStatus(wir);
            checkInWorkItem(wir.getID(), wir.getDataList(),
                            getOutputData(wir.getTaskName(), result), _handle);
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {  }


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
        String specifiedUserID = getDataValue(data, "userid");
        return (specifiedUserID != null) ? specifiedUserID : DEFAULT_ID;
    }


    private String getPassword(Element data) {
        String specifiedPassword = getDataValue(data, "password");
        return (specifiedPassword != null) ? specifiedPassword : DEFAULT_PW;
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


    private Element getOutputData(String taskName, String data) {
        Element output = new Element(taskName);
        Element result = new Element("result");
        result.setText(data);
        output.addContent(result);
        return output;
    }

    
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[2];
        params[0] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[0].setDataTypeAndName("string", "status", XSD_NAMESPACE);
        params[0].setDocumentation("The status message to post to Twitter");

        params[1] = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        params[1].setDataTypeAndName("string", "result", XSD_NAMESPACE);
        params[1].setDocumentation("The status result or error mesage returned from Twitter");
        return params;
    }

    private boolean connected() {
        return _handle != null;
    }


}
