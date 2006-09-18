package au.edu.qut.yawl.timeService;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

/*
  This thread sleeps for a specified amount of milliseconds, and checks the
  specified workItem in when it is done.
*/

public class InternalRunner extends Thread {

    long time = 0;
    TimeService t = null;
    String id = null;
    String _sessionHandle = null;
    TaskInformation taskinfo = null;
    WorkItemRecord itemRecord = null;

    boolean stopping = false;

    public long getTime() {
    	return time;
    }
    
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
            //e.printStackTrace();
        }
        if (!stopping)
            t.finish(itemRecord, _sessionHandle);
    }
}
