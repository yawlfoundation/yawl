package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import java.io.IOException;

/**
 * Starts tomcat from the command line
 *
 * @author Michael Adams
 * @date 19/11/2015
 */
public class CliStarter extends CliEngineController {

    public CliStarter() {
        super(EngineStatus.Stopped);
    }


    public void run() {
        try {
            if (TomcatUtil.start()) {
                System.out.print("Starting the YAWL Engine.");

                // user feedback while waiting for startup completion
                while (_engineStatus != EngineStatus.Running) {
                    pause(1000);
                }
                System.out.println("\nYAWL Engine started successfully.\n");
            }
            else {
                printError("Engine is already running.");
            }
        }
        catch (IOException ioe) {
            printError(ioe.getMessage());
        }

    }


    protected void printError(String msg) {
        super.printError("ERROR starting Engine: " + msg);
    }

}
