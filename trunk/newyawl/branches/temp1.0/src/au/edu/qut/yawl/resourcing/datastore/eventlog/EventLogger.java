package au.edu.qut.yawl.resourcing.datastore.eventlog;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.resourcing.datastore.persistence.Persister;
import au.edu.qut.yawl.resourcing.WorkQueue;

/**
 * Created by IntelliJ IDEA. User: Default Date: 27/09/2007 Time: 11:35:22 To change this
 * template use File | Settings | File Templates.
 */
public class EventLogger {

    private static boolean _logging ;
    private static boolean _logOffers ;

    public EventLogger() {}

    public static enum event { offer, allocate, start, suspend, deallocate, delegate,
                               reallocate_stateless, reallocate_stateful, skip, pile,
                               cancel, complete }

    public static void setLogging(boolean flag) {
        _logging = flag;
        System.out.println("EventLogging flag is: " + flag);
    }

    public static void setOfferLogging(boolean flag) { _logOffers = flag ; }



    public static void log(WorkItemRecord wir, String pid, event eType) {
        if (_logging) {
            ResourceEvent resEvent = new ResourceEvent(wir, pid, eType);
            Persister persister = Persister.getInstance() ;
            if (persister != null) persister.insert(resEvent);
        }
    }


    public static void log(WorkItemRecord wir, String pid, int eType) {
        switch (eType) {
            case WorkQueue.OFFERED   : if (_logOffers) log(wir, pid, event.offer); break;
            case WorkQueue.ALLOCATED : log(wir, pid, event.allocate); break;
            case WorkQueue.STARTED   : log(wir, pid, event.start); break;
            case WorkQueue.SUSPENDED : log(wir, pid, event.suspend);
        }
    }

}
