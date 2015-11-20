package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;

/**
 * @author Michael Adams
 * @date 19/11/2015
 */
public abstract class CliEngineController implements EngineStatusListener {

    protected EngineStatus _engineStatus;


    protected CliEngineController(EngineStatus initialStatus) {
        _engineStatus = initialStatus;
        new EngineMonitor();
        Publisher.addEngineStatusListener(this);
    }


    public abstract void run();


    @Override
    public void statusChanged(EngineStatus status) { _engineStatus = status; }


    protected void printError(String msg) { System.out.println(msg); }


    protected void pause(int mSecs) {
        try {
            Thread.sleep(mSecs);
        }
        catch (InterruptedException ignore) { }
    }

}
