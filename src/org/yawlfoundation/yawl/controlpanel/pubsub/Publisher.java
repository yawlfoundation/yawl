package org.yawlfoundation.yawl.controlpanel.pubsub;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class Publisher {

    private static EngineStatus _currentStatus = EngineStatus.Stopped;
    private static List<EngineStatusListener> _listeners =
            new CopyOnWriteArrayList<EngineStatusListener>();
    private static final Object LOCK = new Object();


    private Publisher() { }


    public static void addEngineStatusListener(EngineStatusListener listener) {
        synchronized (LOCK) {
            _listeners.add(listener);
            listener.statusChanged(_currentStatus);
        }
    }


    public static void removeEngineStatusListener(EngineStatusListener listener) {
        synchronized (LOCK) {
            _listeners.remove(listener);
        }
    }


    // using iterator to avoid concurrent modification exceptions (via add & remove)
    public static void statusChange(EngineStatus status) {
        if (status != _currentStatus) {
            _currentStatus = status;
            synchronized (LOCK) {
                for (EngineStatusListener listener : _listeners) {
                    listener.statusChanged(status);
                }
            }
        }
    }


    public static EngineStatus getCurrentStatus() { return  _currentStatus; }


    public static boolean isTransientStatus() {
        return _currentStatus == EngineStatus.Starting ||
               _currentStatus == EngineStatus.Stopping;
    }


    public static void abortStarting() {
        if (_currentStatus == EngineStatus.Starting) {
            announceStoppedStatus();
        }
    }


    public static void announceStoppedStatus() { statusChange(EngineStatus.Stopped); }

    public static void announceStoppingStatus() { statusChange(EngineStatus.Stopping); }

    public static void announceStartingStatus() { statusChange(EngineStatus.Starting); }

    public static void announceRunningStatus() { statusChange(EngineStatus.Running); }

}
