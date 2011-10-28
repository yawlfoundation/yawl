/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.twitterService;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.IOException;

/**
 * A simple service that provides for status updates to the YAWL Twitter account
 *
 * @author Michael Adams
 * @date 25/07/2009
 */

public class TwitterService extends InterfaceBWebsideController {

    // holds a session handle to the engine
    private String _handle = null;

    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        try {

            // connect only if not already connected
            if (! connected()) _handle = connect(engineLogonName, engineLogonPassword);

            // checkout ... process ... checkin
            wir = checkOut(wir.getID(), _handle);
            String result = updateStatus(wir);
            checkInWorkItem(wir.getID(), wir.getDataList(),
                            getOutputData(wir.getTaskID(), result), null,  _handle);
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
        YParameter[] params = new YParameter[2];
        params[0] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[0].setDataTypeAndName("string", "status", XSD_NAMESPACE);
        params[0].setDocumentation("The status message to post to Twitter");

        params[1] = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        params[1].setDataTypeAndName("string", "result", XSD_NAMESPACE);
        params[1].setDocumentation("The status result or error message returned from Twitter");
        return params;
    }

    
    //********************* PRIVATE METHODS *************************************//

    private String updateStatus(WorkItemRecord wir) {
        String result ;
        String msg = getStatusMsg(wir);
        if (msg != null) {
            result = updateStatus(msg);
        }
        else result = "Null status passed - Twitter update cancelled";
        return result;
    }


    private String updateStatus(String msg) {
        String result;
        Twitter twitter = new TwitterFactory().getInstance();
        try {
            twitter.updateStatus(msg) ;
            result = "Status successfully posted to Twitter";
        }
        catch (TwitterException te) {
            result = te.getMessage();
        }
        return result;
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

    
    private boolean connected() throws IOException {
        return _handle != null && checkConnection(_handle);
    }


}
