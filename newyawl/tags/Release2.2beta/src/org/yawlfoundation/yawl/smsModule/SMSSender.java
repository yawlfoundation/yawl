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

package org.yawlfoundation.yawl.smsModule;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
 * 
 * @author Lachlan Aldred
 * Date: 4/03/2005
 * Time: 11:48:29
 */
public class SMSSender extends InterfaceBWebsideController implements Runnable {

    //sms messages that have as yet received no return messages.
    private static List _outStandingInteractions = new ArrayList();
    //true if the polling thread is running
    private static boolean _running = false;
    //completed two way sms interactions
    private static List _archivedInteractions = new ArrayList();

    //username of sms account
    private String _smsUsername;
    //password for sms account
    private String _smsPassword;

    private static String _sessionHandle = null;

    public static String _sendURI ;
    public static String _receiveURI;

    //param names
    private static final String SMS_MESSAGE_PARAMNAME = "SMSMessage";
    private static final String SMS_PHONENO_PARAMNAME = "SMSPhoneNumber";
    private static final String SMS_REPLYMESSAGE_PARAMNAME = "SMSReplyMessage";

    private static final String _engineUser = "smsService";
    private static final String _enginePassword = "ySMS";

    /**
     * Checks the work item out of the engine, sends an sms message, and
     * starts the thread that checks for a reply.
     * @param enabledWorkItem
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(_engineUser, _enginePassword);
            }
            if (successful(_sessionHandle)) {
                List executingChildren = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                String resultsFromService = "";
                for (int i = 0; i < executingChildren.size(); i++) {
                    WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);

                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);

//                    first of all do a connection with the SMS Service
//                    String smsConnectionID = performSMSConnection(_smsUsername, _smsPassword);

                    //next get the parameters for message sending.
                    Element paramsData = itemRecord.getWorkItemData();
                    String message = paramsData.getChildText(SMS_MESSAGE_PARAMNAME);
                    String toPhone = paramsData.getChildText(SMS_PHONENO_PARAMNAME);
//                    String msgCorrelationID = itemRecord.getID().substring(0, 12);

//                    resultsFromService += performSMSSend(message, toPhone, smsConnectionID, msgCorrelationID);

                    String jobID = performSMSSend(message, toPhone);

                    //an outstanding interaction is an object that records the
                    //details of a reply that needs to be polled for.
                    Interaction inter = new Interaction();
                    inter._smsJobID = jobID;
                    inter._archivable = false;
                    inter._timeOfSend = new Date();
                    inter._workItemRecord = itemRecord;
                    inter._caseDataBoundForEngine = caseDataBoundForEngine;
                    _outStandingInteractions.add(inter);
                }

                System.out.println("\n\nSMSSender " +
                        "\nResults of engine interactions : " + _report +
                        "\nResults of SMS invocations : " + resultsFromService);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!_running) {
            //this thread polls for a reply while running
            Thread replyPoller = new Thread(this, "ReplyPoller");
            replyPoller.start();
        }
    }


    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[3];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, SMS_MESSAGE_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the SMS Message content");
        params[0] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, SMS_PHONENO_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the SMS phone number.");
        params[1] = param;

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE, SMS_REPLYMESSAGE_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the SMS reply mssage.");
        params[2] = param;

        return params;
    }


    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/smsWelcome").forward(req, resp);
    }


    /*
     *performs a sned of an sms message
     */
    private String performSMSSend(String message, String toPhone) throws IOException {
        toPhone = '+' + toPhone;
        System.out.println("performSMSSend::message = " + message);
        System.out.println("performSMSSend::toPhone = " + toPhone);
        System.out.println("performSMSSend::username = " + _smsUsername);
        System.out.println("performSMSSend::password = " + _smsPassword);

        Map<String, String> params = new HashMap<String, String>();
        params.put("u", _smsUsername);
        params.put("p", _smsPassword);
        params.put("d", toPhone);
        params.put("m", message);
        params.put("rr", Integer.toString(1));
        String resultFromSMSService = _interfaceBClient.postToExternalURL(_sendURI, params);
        System.out.println("performSMSSend::resultFromSMSService = " + resultFromSMSService);
        String [] result = resultFromSMSService.split(" ");
        if("ACK".equals(result[0])) {
            return result[1];
        }
        throw new IOException(result[1]);
    }


    /**
     * By implementing this method and deploying a web app containing the implementation
     * the YAWL engine will send events to this method notifying your custom
     * YAWL service that an active work item has been cancelled by the
     * engine.
     * @param workItemRecord a "snapshot" of the work item cancelled in
     * the engine.
     */
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
        synchronized (this) {
            System.out.println("PreCancel::_outStandingInteractions = " + _outStandingInteractions);
            System.out.println("\tCancel:_archivedInteractions = " + _archivedInteractions);
            for (int i = 0; i < _outStandingInteractions.size(); i++) {
                Interaction inter = (Interaction)
                        _outStandingInteractions.get(i);
                if (inter._workItemRecord.getID().equals(workItemRecord.getID())) {
                    _outStandingInteractions.remove(i);
                    i--;
                    _archivedInteractions.add(inter);
                }
            }
        }
        System.out.println("\tPostCancel::_outStandingInteractions = " + _outStandingInteractions);
        System.out.println("\tPostCancel::_archivedInteractions = " + _archivedInteractions);
    }


    public static void main(String[] args) throws InterruptedException {
        SMSSender sender = new SMSSender();
        if(args.length != 8) {
            System.out.println("Usage: -m <message> -d <phonenumber>" +
            "-u <username> -p <password>");
        }
        sender.setSMSUsernameAndPassword(args[5], args[7]);

        String smsJobID = null;
        try {
            smsJobID = sender.performSMSSend(args[1], args[3]);
        } catch (IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("result = " + smsJobID);
        Thread.sleep(240000);
        try {
            /* clean up standard reply */
            List replies = sender.getReplies(smsJobID);
            for (int i = 0; i < replies.size(); i++) {
                Reply reply = (Reply) replies.get(i);
                Logger _logger = Logger.getLogger(SMSSender.class);
                _logger.info("\n");
                _logger.info("reply._fromPhoneNum = " + reply._fromPhoneNum);
                _logger.info("reply._msgTxt   = " + reply._messageTxt);
                _logger.info("reply._replyID = " + reply._replyID);
                _logger.info("reply._jobID = " + reply._jobID);
                _logger.info("reply._time = " + reply._timeStr);
                _logger.info("reply._date = " + reply._dateStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //e.g.
        //7684  940635  2006-07-28  09:44:18    61438330056 "JjjjC"
        //e.g.
        //7685	941118	2006-07-28	14:34:32	61438330056	"Kjg.Dm"
        //7686	941118	2006-07-28	14:34:49	61438330056	"Jmwtpgjmwtp"
    }


    public void run() {
        _running = true;
        while (_outStandingInteractions.size() > 0) {
            _logger.info("---> Outstandaing interactions loop");

            synchronized (this) {
                for (int i = 0; i < _outStandingInteractions.size(); i++) {
                    Interaction inter = (Interaction) _outStandingInteractions.get(i);
                    try {
                        List replies = getReplies(inter._smsJobID);
                        if (replies.size() > 0) {
                            inter._replyMessage = (Reply) replies.iterator().next();
                            _logger.info("reply text = " + inter._replyMessage._messageTxt);
                            _logger.info("reply from = " + inter._replyMessage._fromPhoneNum);
                            _logger.info("reply corr = " + inter._replyMessage._jobID);
                            _logger.info("reply when = " + inter._replyMessage._timeStr);
                            inter._archivable = true;
                            Element smsReplyMessage = new Element(SMS_REPLYMESSAGE_PARAMNAME);
                            smsReplyMessage.setText(inter._replyMessage._messageTxt);
                            inter._caseDataBoundForEngine.addContent(smsReplyMessage);
                            String result = checkInWorkItem(inter._workItemRecord.getID(),
                                    inter._workItemRecord.getWorkItemData(),
                                    inter._caseDataBoundForEngine,
                                    _sessionHandle);
                            _logger.info("result of work item checkin = " + result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < _outStandingInteractions.size(); i++) {
                    Interaction inter = (Interaction) _outStandingInteractions.get(i);

                    if (inter._archivable) {
                        _outStandingInteractions.remove(i);
                        i--;
                        _archivedInteractions.add(inter);
                    }
                }
            }
            _logger.info("outStandingInteractions = " + _outStandingInteractions);
            _logger.info("_archivedInteractions = " + _archivedInteractions);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _running = false;
    }

    private List getReplies(String smsJobID) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("u", _smsUsername);
        params.put("p", _smsPassword);
        params.put("j", smsJobID);

        String resultFromSMSService =
                _interfaceBClient.postToExternalURL(_receiveURI, params);
        _logger.info("Returned Message FromSMSService = " + resultFromSMSService);

        return parseReplies(resultFromSMSService);
    }

    private List parseReplies(String resultFromSMSService) {
        List replies = new ArrayList();

        String[] rawreply = resultFromSMSService.split("\n");
        String[][] preprocessedResult = new String[rawreply.length][];
        for (int i = 0; i < preprocessedResult.length; i++) {
            String[] singleReply = rawreply[i].split("\t");

            if (singleReply.length > 5 ) {
                String replyStr = parseReplyFromService(singleReply[5]);
                Reply reply = new Reply();
                reply._replyID = singleReply[0];
                reply._jobID = singleReply[1];
                reply._dateStr = singleReply[2];
                reply._timeStr = singleReply[3];
                reply._fromPhoneNum = singleReply[4];
                reply._messageTxt = replyStr;
            replies.add(reply);
            }
        }
        return replies;
    }

    private static String parseReplyFromService(String replyFromPhone) {
        String [] parsed = replyFromPhone.split("\"");
        String uberparsed = "";
        for (int i = 0; i < parsed.length; i++) {
            uberparsed += parsed[i];
        }
        return uberparsed;
    }




    public void setSMSUsernameAndPassword(String username, String password) {
        _smsUsername = username;
        _smsPassword = password;
    }

    public List getCompletedInteractions() {
        return _archivedInteractions;
    }


}


class Interaction {
    Date _timeOfSend;
    String _smsJobID;
    Reply _replyMessage;
    boolean _archivable;
    WorkItemRecord _workItemRecord;
    public Element _caseDataBoundForEngine;

    public String toString() {
        return _workItemRecord.getID() + " archivable <" + _archivable + ">";
    }
}


class Reply {
    String _jobID;
    String _replyID;
    String _fromPhoneNum;
    String _messageTxt;
    String _dateStr;
    String _timeStr;
}