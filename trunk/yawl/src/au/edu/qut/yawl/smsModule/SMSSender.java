/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.smsModule;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import org.jdom.Element;

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
    //the thread that checks for sms replies
    private static Thread _replyPoller;
    //username of sms account
    private String _smsUsername;
    //password for sms account
    private String _smsPassword;

    private static String _sessionHandle = null;

    //param names
    private static final String SMS_MESSAGE_PARAMNAME = "SMSMessage";
    private static final String SMS_PHONENO_PARAMNAME = "SMSPhoneNumber";
    private static final String SMS_REPLYMESSAGE_PARAMNAME = "SMSReplyMessage";


    /**
     * Checks the work item out of the engine, sends an sms message, and
     * starts the thread that checks for a reply.
     * @param enabledWorkItem
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (successful(_sessionHandle)) {
                List executingChildren = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                String resultsFromService = "";
                for (int i = 0; i < executingChildren.size(); i++) {
                    WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);

                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);

                    //first of all do a connection with the SMS Service
                    String smsConnectionID = performSMSConnection(_smsUsername, _smsPassword);

                    //next get the parameters for message sending.
                    Element paramsData = itemRecord.getWorkItemData();
                    String message = paramsData.getChildText(SMS_MESSAGE_PARAMNAME);
                    String toPhone = paramsData.getChildText(SMS_PHONENO_PARAMNAME);
                    String msgCorrelationID = itemRecord.getID().substring(0, 12);

                    resultsFromService += performSMSSend(message, toPhone, smsConnectionID, msgCorrelationID);

                    //an outstanding interaction is an object that records the
                    //details of a reply that needs to be polled for.
                    OutstandingInteraction inter = new OutstandingInteraction();
                    inter._msgCorrelationID = msgCorrelationID;
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
            _replyPoller = new Thread(this, "ReplyPoller");
            _replyPoller.start();
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
    private String performSMSSend(String message, String toPhone, String smsConnectionID, String msgCorrelationID) throws IOException {
        toPhone = '+' + toPhone;
        System.out.println("performSMSSend::message = " + message);
        System.out.println("performSMSSend::toPhone = " + toPhone);
        System.out.println("performSMSSend::smsConnectionID = " + smsConnectionID);
        System.out.println("performSMSSend::msgCorrelationID = " + msgCorrelationID);

        Map params = new HashMap();
        params.put("connectionid", smsConnectionID);
        params.put("message", message);
        params.put("to", toPhone);
        params.put("messageid", msgCorrelationID);
        String resultFromSMSService =
                Interface_Client.executePost(
                        "http://api.directsms.com.au/s3/http/send_two_way_message",
                        params);
        System.out.println("performSMSSend::resultFromSMSService = " + resultFromSMSService);
        return resultFromSMSService;
    }


    protected static String performSMSConnection(String smsUsername, String smsPassword) throws IOException, YAWLException {
        String finalResult;
        Map params = new HashMap();

        params.put("username", smsUsername);
        params.put("password", smsPassword);

        String resultFromSMSService =
                Interface_Client.executePost(
                        "http://api.directsms.com.au/s3/http/connect",
                        params);
        if (resultFromSMSService.startsWith("id:")) {
            resultFromSMSService = resultFromSMSService.trim();
            finalResult = resultFromSMSService.substring(
                    4,
                    resultFromSMSService.length());
        } else {
            System.out.println("Error in smsUsername = " + smsUsername);
            System.out.println("Error in smsPassword = " + smsPassword);
            throw new YAWLException("Failed to connect with SMS gateway. " +
                    resultFromSMSService);
        }
        return finalResult;
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
        synchronized (_outStandingInteractions) {
            System.out.println("PreCancel::_outStandingInteractions = " + _outStandingInteractions);
            System.out.println("\tCancel:_archivedInteractions = " + _archivedInteractions);
            for (int i = 0; i < _outStandingInteractions.size(); i++) {
                OutstandingInteraction inter = (OutstandingInteraction)
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


    public static void main(String[] args) throws YAWLException, IOException {
        SMSSender sender = new SMSSender();


        String connectionID = performSMSConnection(args[0], args[1]);
//        System.out.println("connectionID = " + connectionID);
//        String messageID = "34543543535";
//        String msgID = sender.performSMSSend(
//                "hello there lachlan",
//                "+61438330056",
//                connectionID,
//                messageID);
//        System.out.println("resultFromSMS Send = " + msgID);
//        System.out.println("user provided messageID = " + messageID);

//        String stuff = "replies: 2id: 34543543535  mobile: +61438330056 message: Hello there back to message two                                                                                                                                  when: -847id: 34543543535  mobile: +61438330056 message: Hello there back to message one                                                                                                                                  when: -782";
//                     = replies: 1id: 3.2:6_Invoke mobile: +61438330056 message: Hi there           when: -702

//        sender.parseReplies(stuff);

        /* clean up standard reply */
        List replies = sender.getReplies(connectionID, "2.2:6_Invoke");
        for (int i = 0; i < replies.size(); i++) {
            Reply reply = (Reply) replies.get(i);
            System.out.println("\nreply._fromMobile = " + reply._fromMobile);
            System.out.println("reply._msg  txt   = " + reply._messageTxt);
            System.out.println("reply._msgCorrelationID = " + reply._msgCorrelationID);
            System.out.println("reply._when = " + reply._when);
        }
//        List replies = sender.parseReplies("replies: 1id: 3.2:6_Invoke mobile: +61438330056 message: Hi there           when: -702");
//        for (int i = 0; i < replies.size(); i++) {
//            Reply reply = (Reply) replies.get(i);
//            System.out.println("\nreply._fromMobile = " + reply._fromMobile);
//            System.out.println("reply._msg  txt   = " + reply._messageTxt);
//            System.out.println("reply._msgCorrelationID = " + reply._msgCorrelationID);
//            System.out.println("reply._when = " + reply._when);
//        }
//
//        replies = sender.parseReplies("replies: 2id: 34543:mobile  mobile: +61438330056 message: Hello there back to message two                                                                                                                                  when: -847id: 34543:fred  mobile: +61438330056 message: Hello there back to message one                                                                                                                                  when: -782");
//        for (int i = 0; i < replies.size(); i++) {
//            Reply reply = (Reply) replies.get(i);
//            System.out.println("\nreply._fromMobile = " + reply._fromMobile);
//            System.out.println("reply._msg  txt   = " + reply._messageTxt);
//            System.out.println("reply._msgCorrelationID = " + reply._msgCorrelationID);
//            System.out.println("reply._when = " + reply._when);
//        }

    }

    public void run() {
        _running = true;
        while (_outStandingInteractions.size() > 0) {
            System.out.println("Run::going round again");
            String smsConnectionID = null;
            try {
                smsConnectionID = SMSSender.performSMSConnection(_smsUsername, _smsPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (_outStandingInteractions) {
                for (int i = 0; i < _outStandingInteractions.size(); i++) {
                    OutstandingInteraction inter = (OutstandingInteraction)
                            _outStandingInteractions.get(i);
                    try {
                        List replies = getReplies(smsConnectionID, inter._msgCorrelationID);
                        if (replies.size() > 0) {
                            inter._replyMessage = (Reply) replies.iterator().next();
                            System.out.println("Run::reply text = " + inter._replyMessage._messageTxt);
                            System.out.println("Run::reply from = " + inter._replyMessage._fromMobile);
                            System.out.println("Run::reply corr = " + inter._replyMessage._msgCorrelationID);
                            System.out.println("Run::reply when = " + inter._replyMessage._when);
                            inter._archivable = true;
                            Element smsReplyMessage = new Element(SMS_REPLYMESSAGE_PARAMNAME);
                            smsReplyMessage.setText(inter._replyMessage._messageTxt);
                            inter._caseDataBoundForEngine.addContent(smsReplyMessage);
                            String result = checkInWorkItem(inter._workItemRecord.getID(),
                                    inter._workItemRecord.getWorkItemData(),
                                    inter._caseDataBoundForEngine,
                                    _sessionHandle);
                            System.out.println("\tRun::result of work item checkin = " + result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("\tRun::_outStandingInteractions = " + _outStandingInteractions);
                System.out.println("\tRun::_archivedInteractions = " + _archivedInteractions);
                for (int i = 0; i < _outStandingInteractions.size(); i++) {
                    OutstandingInteraction inter = (OutstandingInteraction)
                            _outStandingInteractions.get(i);
                    if (inter._archivable) {
                        _outStandingInteractions.remove(i);
                        i--;
                        _archivedInteractions.add(inter);
                    }
                }
            }
            System.out.println("\tRun::_outStandingInteractions = " + _outStandingInteractions);
            System.out.println("\tRun::_archivedInteractions = " + _archivedInteractions);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _running = false;
    }

    private List getReplies(String smsConnectionID, String msgCorrelationID) throws IOException {
        Map params = new HashMap();
        params.put("connectionid", smsConnectionID);
        params.put("messageid", msgCorrelationID);
        params.put("mark_as_read", "mark_as_read");
        params.put("when", "when");
        String resultFromSMSService =
                Interface_Client.executePost(
                        "http://api.directsms.com.au/s3/http/get_replies",
                        params);
        System.out.println("Returned Message FromSMSService = " + resultFromSMSService);
        List replies = parseReplies(resultFromSMSService);
        return replies;
    }

    private List parseReplies(String resultFromSMSService) {
        List replies = new ArrayList();

        //first get rid of word replies
        if (resultFromSMSService.length() < 11) {
            return replies;
        }

        String[] rawReplies = getRawReplies(resultFromSMSService);

        for (int i = 0; i < rawReplies.length; i++) {
            String toks = rawReplies[i];
            String msgCorrelationID = toks.substring(
                    0,
                    toks.indexOf(" mobile: ")).trim();

            String phoneNumber = toks.substring(
                    toks.indexOf(" mobile:") + 9,
                    toks.indexOf(" message:")).trim();

            String message = toks.substring(
                    toks.indexOf(" message: ") + 10,
                    toks.indexOf("when: ")).trim();

            String when = toks.substring(
                    toks.indexOf(" when: ") + 7,
                    toks.length()).trim();

            Reply reply = new Reply();
            reply._msgCorrelationID = msgCorrelationID;
            reply._fromMobile = phoneNumber;
            reply._messageTxt = message;
            reply._when = when;
            replies.add(reply);
        }
        return replies;
    }

    private String[] getRawReplies(String resultFromSMSService) {
        String numRepliesStr = resultFromSMSService.substring(
                9,
                resultFromSMSService.indexOf("id"));

        int numRepliesInt = Integer.parseInt(numRepliesStr);

        int begCurrChunk = resultFromSMSService.indexOf("id: ");
        int begNextChunk = resultFromSMSService.indexOf("id: ", 4 + begCurrChunk);

        String[] rawReplies = new String[numRepliesInt];

        for (int i = 0; i < rawReplies.length; i++) {
            String rawReply = resultFromSMSService.substring(
                    begCurrChunk,
                    begNextChunk != -1 ? begNextChunk : resultFromSMSService.length()
            );
            begCurrChunk = begNextChunk;
            begNextChunk = resultFromSMSService.indexOf("id: ", 4 + begCurrChunk);
            rawReplies[i] = rawReply;
        }

        return rawReplies;
    }

    public void setSMSUsernameAndPassword(String username, String password) {
        _smsUsername = username;
        _smsPassword = password;
    }


}

class OutstandingInteraction {
    Date _timeOfSend;
    String _msgCorrelationID;
    Reply _replyMessage;
    boolean _archivable;
    WorkItemRecord _workItemRecord;
    public Element _caseDataBoundForEngine;

    public String toString() {
        return _workItemRecord.getID() + " archivable <" + _archivable + ">";
    }
}


class Reply {
    String _msgCorrelationID;
    String _fromMobile;
    String _messageTxt;
    String _when;
}