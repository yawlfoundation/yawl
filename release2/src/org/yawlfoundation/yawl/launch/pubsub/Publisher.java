package org.yawlfoundation.yawl.launch.pubsub;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class Publisher {

    private static EngineStatus _currentStatus = EngineStatus.Stopped;
    private static Set<EngineStatusListener> _listeners =
            new HashSet<EngineStatusListener>();

    private Publisher() { }


    public static void addEngineStatusListener(EngineStatusListener listener) {
        _listeners.add(listener);
    }


    public static void removeEngineStatusListener(EngineStatusListener listener) {
        _listeners.remove(listener);
    }


    public static void statusChange(EngineStatus status) {
        for (EngineStatusListener listener : _listeners) {
            listener.statusChanged(status);
        }
        _currentStatus = status;
    }


    public static EngineStatus getCurrentStatus() { return  _currentStatus; }


    public static void announceStoppedStatus() { statusChange(EngineStatus.Stopped); }

    public static void announceStoppingStatus() { statusChange(EngineStatus.Stopping); }

    public static void announceStartingStatus() { statusChange(EngineStatus.Starting); }

    public static void announceRunningStatus() { statusChange(EngineStatus.Running); }

}
