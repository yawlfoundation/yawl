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
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import org.jdom.Element;
import org.apache.log4j.Logger;

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
    private static List<Interaction> _outStandingInteractions =
            new ArrayList<Interaction>();
    //true if the polling thread is running
    private static boolean _running = false;
    //completed two way sms interactions
    private static List<Interaction> _archivedInteractions =
            new ArrayList<Interaction>();
    //username of sms account
    private String _smsUsername;
    //password for sms account
    private String _smsPassword;

    private static String _sessionHandle = null;

    //param names
    private static final String SMS_MESSAGE_PARAMNAME = "SMSMessage";
    private static final String SMS_PHONENO_PARAMNAME = "SMSPhoneNumber";
    private static final String SMS_REPLYMESSAGE_PARAMNAME = "SMSReplyMessage";

    private Logger _logger = Logger.getLogger(SMSSender.class);


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
                List<WorkItemRecord> executingChildren = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                String resultsFromService = "";
                for (WorkItemRecord itemRecord : executingChildren) {
                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);

                    //next get the parameters for message sending.
                    Element paramsData = itemRecord.getWorkItemData();
                    String message = paramsData.getChildText(SMS_MESSAGE_PARAMNAME);
                    String toPhone = paramsData.getChildText(SMS_PHONENO_PARAMNAME);


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

                _logger.debug("SMSSender " +
                        "\nResults of SMS invocations : " + resultsFromService);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!_running) {
            //this thread polls for a reply while running
            Thread _replyPoller = new Thread(this, "ReplyPoller");
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
    private String performSMSSend(String message, String toPhone) throws IOException {
        System.out.println("performSMSSend::message = " + message);
        System.out.println("performSMSSend::toPhone = " + toPhone);
        System.out.println("performSMSSend::username = " + _smsUsername);
        System.out.println("performSMSSend::password = " + _smsPassword);

        Map<String,String> params = new HashMap<String,String>();
        params.put("u", _smsUsername);
        params.put("p", _smsPassword);
        params.put("d", toPhone);
        params.put("m", message);
        params.put("rr", Integer.toString(1));
        String resultFromSMSService =
                Interface_Client.executePost(
                        "https://www.valuesms.com/msg.php",
                        params);
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
                Interaction inter =
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
            List<Reply> replies = sender.getReplies("941118");
            for (Reply reply : replies) {
                System.out.println("\n");
                System.out.println("reply._fromPhoneNum = " + reply._fromPhoneNum);
                System.out.println("reply._msgTxt   = " + reply._messageTxt);
                System.out.println("reply._replyID = " + reply._replyID);
                System.out.println("reply._jobID = " + reply._jobID);
                System.out.println("reply._time = " + reply._timeStr);
                System.out.println("reply._date = " + reply._dateStr);
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
                for (Interaction inter : _outStandingInteractions) {
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
                    Interaction inter = _outStandingInteractions.get(i);
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

    private List<Reply> getReplies(String smsJobID) throws IOException {
        Map<String, String> params = new HashMap<String,String>();
        params.put("u", _smsUsername);
        params.put("p", _smsPassword);
        params.put("j", smsJobID);
        String resultFromSMSService =
                Interface_Client.executePost(
                        "https://www.valuesms.com/rcv.php",
                        params);
        System.out.println("Returned Message FromSMSService = " + resultFromSMSService);
        return parseReplies(resultFromSMSService);
    }


    private List<Reply> parseReplies(String resultFromSMSService) {
        List<Reply> replies = new ArrayList<Reply>();

        String[] rawreply = resultFromSMSService.split("\n");
        String[][] preprocessedResult = new String[rawreply.length][];
        for (int i = 0; i < preprocessedResult.length; i++) {
            String[] singleReply = rawreply[i].split("\t");
            System.out.println("rawreply = " + rawreply);
            if (singleReply.length > 5 ) {
                Reply reply = new Reply();
                reply._replyID = singleReply[0];
                reply._jobID = singleReply[1];
                reply._dateStr = singleReply[2];
                reply._timeStr = singleReply[3];
                reply._fromPhoneNum = singleReply[4];
                reply._messageTxt = singleReply[5];
            replies.add(reply);
            }
        }
        return replies;
    }

    public void setSMSUsernameAndPassword(String username, String password) {
        _smsUsername = username;
        _smsPassword = password;
    }


    public List<Interaction> getCompletedInteractions() {
        return _archivedInteractions;
    }
}

class Interaction {
    Date _timeOfSend;
    Reply _replyMessage;
    boolean _archivable;
    WorkItemRecord _workItemRecord;
    public Element _caseDataBoundForEngine;
    public String _smsJobID;

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