package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

/**
 * Stops tomcat from the command line
 *
 * @author Michael Adams
 * @date 19/11/2015
 */
public class CliStopper extends CliEngineController {

    public CliStopper() {
        super(EngineStatus.Running);
    }


    public void run() {
        if (TomcatUtil.isTomcatRunning()) {
            TomcatUtil.stop();

            // user feedback while waiting for shutdown completion
            while (_engineStatus != EngineStatus.Stopped) {
                pause(500);
                System.out.print(".");
            }
            System.out.println("\nYAWL Engine shutdown successfully.\n");

        }
        else {
            printError("Engine is already shutdown.");
        }
    }


    protected void printError(String msg) {
        super.printError("ERROR stopping Engine: " + msg);
    }

}
