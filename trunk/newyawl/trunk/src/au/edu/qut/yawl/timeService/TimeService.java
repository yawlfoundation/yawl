/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.timeService;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.interfce.AuthenticationConfig;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import org.jdom.Element;

import java.text.DateFormat;
import java.util.*;

public class TimeService extends InterfaceBWebsideController {


    private String _sessionHandle = null;

    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (!successful(_sessionHandle)) {
                _logger.error("Unsuccessful");
            } else {
                WorkItemRecord child = checkOut(workItemRecord.getID(), _sessionHandle);

                if (child != null) {
                    List children = super.getChildren(workItemRecord.getID(), _sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        if (WorkItemRecord.statusFired.equals(itemRecord.getStatus())) {
                            checkOut(itemRecord.getID(), _sessionHandle);
                        }
                    }
                    children = super.getChildren(workItemRecord.getID(), _sessionHandle);
                    for (int i = 0; i < children.size(); i++) {
                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
                        //System.out.println("WebServiceController::processEnabledAnnouncement() itemRecord = " + itemRecord);
                        super._model.addWorkItem(itemRecord);

                        System.out.println("added: " + itemRecord.getID());

                        Element inputData = itemRecord.getWorkItemData();
                        System.out.println(inputData);
                        Element element = (Element) inputData.getChildren().get(0);
                        String notifytime = element.getText();
                        System.out.println("Notified of: " + notifytime);

                        if (notifytime != null && !notifytime.equals("")) {

                            try {
                                int notify = Integer.parseInt(notifytime);
                                new InternalRunner(notify, itemRecord, this, _sessionHandle).start();

                            } catch (Exception e) {
                                new InternalRunner(notifytime, itemRecord, this, _sessionHandle).start();
                            }
                        }
                    }
                }
            }
            //	return report;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[1];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_STRINGTYPE,"time", XSD_NAMESPACE);
        param.setDocumentation("Amount of Time the TimeService will wait before returning");
        params[0] = param;

        return params;
    }

    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    }

    public String processRequestToCancel(WorkItemRecord workItemRecord) {
        return "Cancelled";
    }


    public void setRemoteAuthenticationDetails(String userName, String password,
                                               String httpProxyHost, String proxyPort) {
        AuthenticationConfig auth = AuthenticationConfig.getInstance();

        auth.setProxyAuthentication(userName, password, httpProxyHost, proxyPort);
    }

    public synchronized void finish(WorkItemRecord itemRecord, String _sessionHandle) {
        try {
            System.out.println("Checking in work Item: " + itemRecord.getID());

            TaskInformation taskinfo = getTaskInformation(itemRecord.getSpecificationID(),
                    itemRecord.getTaskID(),
                    _sessionHandle);

            checkInWorkItem(itemRecord.getID(),
                    itemRecord.getWorkItemData(),
                    new Element(taskinfo.getDecompositionID()),
                    _sessionHandle);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

/*
  This thread sleeps for a specified amount of milliseconds, and checks the
  specified workItem in when it is done.
*/

class InternalRunner extends Thread {

    long time = 0;
    TimeService t = null;
    String id = null;
    String _sessionHandle = null;
    TaskInformation taskinfo = null;
    WorkItemRecord itemRecord = null;

    boolean stopping = false;

    public InternalRunner(String date, WorkItemRecord itemRecord, TimeService t, String _sessionHandle) {
        this.t = t;
        this._sessionHandle = _sessionHandle;
        this.itemRecord = itemRecord;

        /*
          convert the date into a time
          if this is a date
        */


        try {
            DateFormat df = DateFormat.getDateTimeInstance();
            Date todate = df.parse(date);
            GregorianCalendar cal = new GregorianCalendar();
            GregorianCalendar now = new GregorianCalendar();
            cal.setTime(todate);
            long to = cal.getTimeInMillis();
            long from = now.getTimeInMillis();
            time = to - from;

            System.out.println("to: " + to);
            System.out.println("from: " + from);
            System.out.println("time: " + time);
        } catch (Exception e) {

            StringTokenizer st = new StringTokenizer(date);
            try {
                GregorianCalendar cal = new GregorianCalendar();
                GregorianCalendar now = new GregorianCalendar();
                while (st.hasMoreTokens()) {

                    String notifytime = st.nextToken();
                    String measure = st.nextToken();

                    int notify = Integer.parseInt(notifytime);

                    if (measure.equals("s")) {
                        cal.add(Calendar.SECOND, notify);
                    } else if (measure.equals("m")) {
                        cal.add(Calendar.MINUTE, notify);
                    } else if (measure.equals("h")) {
                        cal.add(Calendar.HOUR, notify);
                    } else if (measure.equals("day")) {
                        cal.add(Calendar.DATE, notify);
                    } else if (measure.equals("mth")) {
                        cal.add(Calendar.MONTH, notify);
                    } else if (measure.equals("year")) {
                        cal.add(Calendar.YEAR, notify);
                    }

                }

                long to = cal.getTimeInMillis();
                long from = now.getTimeInMillis();
                time = to - from;

                System.out.println("to: " + to);
                System.out.println("from: " + from);
                System.out.println("time: " + time);

            } catch (Exception e2) {
                System.out.println("Date is in the wrong format");
                time = 0;
            }
        }
    }

    public void stopThread() {
        stopping = true;
    }

    public InternalRunner(long time, WorkItemRecord itemRecord, TimeService t, String _sessionHandle) {
        this.time = time;
        this.t = t;
        this._sessionHandle = _sessionHandle;
        this.itemRecord = itemRecord;
    }

    public void run() {


        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!stopping)
            t.finish(itemRecord, _sessionHandle);
    }
}
