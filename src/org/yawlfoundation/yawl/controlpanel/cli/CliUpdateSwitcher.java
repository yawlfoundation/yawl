package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.Differ;
import org.yawlfoundation.yawl.controlpanel.update.UpdateChecker;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import java.io.Console;


/**
 * Provides a CLI interface into the control panel update facility
 *
 * @author Michael Adams
 * @author Joerg Evermann
 * @date 3/11/2015
 */
public class CliUpdateSwitcher {


    public boolean handle(String[] args) {
        String arg = args[0].toLowerCase();
        if (arg.equals("-start")) {
            return doStart();
        }
        if (arg.equals("-stop")) {
            return doStop();
        }
        if (arg.equals("-update") || arg.equals("-u")) {
            return doUpdates();
        }
        if (arg.equals("-versions") || arg.equals("-v")) {
            return doVersions();
        }
        if ((arg.equals("-add") || arg.equals("-a")) && (args.length > 1)) {
            return doAdd(args[1]);
        }
        if ((arg.equals("-remove") || arg.equals("-r")) && (args.length > 1)) {
            return doRemove(args[1]);
        }
        if (arg.equals("-status")) {
            return getStatus();
        }


        // no matching args
        Console console = System.console();
        if (console != null) {                                   // likely a CLI start
            System.out.println("Command Line Usage:\n\n" +
                    FileUtil.getJarName() + " option [argument]\n\n" +
                    "where option is one of \n\n" +
                    "-start               // Start the YAWL engine\n" +
                    "-stop                // Stop the YAWL engine\n" +
                    "-status              // Check whether the YAWL engine is running or stopped\n" +
                    "-update              // Update installed components\n" +
                    "-versions            // List installed and available components and their versions\n" +
                    "-add [component]     // Add the named component (and perform updates)\n" +
                    "-remove [component]  // Remove the named component\n");
            return true;
        }

        return false;
    }


    private boolean getStatus() {
        System.out.println("The YAWL Engine is " +
                (TomcatUtil.isEngineRunning() ? "Running" : "Stopped"));
        return true;
    }


    private boolean doStart() {
        new CliStarter().run();
        return true;
    }


    private boolean doStop() {
        new CliStopper().run();
        return true;
    }


    private boolean doUpdates() {
        UpdateChecker checker = check();

        if (noErrors(checker)) {
            CliUpdateTableModel model = printTable(checker);

            if (checker.getDiffer().hasUpdates()) {
                String response = System.console().readLine(
                        "Do you wish to update (Y/N)? ==> ");
                if (response.equalsIgnoreCase("y")) {
                    update(model, "Update Completed");
                }

            } else {
                System.out.println("\nNo updates available");
            }
        }
        return true;
    }


    private boolean doVersions() {
        UpdateChecker checker = check();
        if (noErrors(checker)) {
            printTable(checker);
        }
        return true;
    }


    private boolean doAdd(String component) {
        UpdateChecker checker = check();
        if (noErrors(checker)) {
            doAddOrRemove(component, checker.getDiffer(), true);
        }
        return true;
    }


    private boolean doRemove(String component) {

        // version comparison not required for a removal
        Differ differ = new Differ(null, FileUtil.getLocalCheckSumFile());
        return doAddOrRemove(component, differ, false);
    }


    private CliUpdateTableModel printTable(UpdateChecker checker) {
        CliUpdateTableModel model = new CliUpdateTableModel(checker.getDiffer());
        model.print();
        return model;
    }


    private UpdateChecker check() {
        System.out.println("Please wait, checking installed versions...");
        UpdateChecker checker = new UpdateChecker();
        checker.check();
        return checker;
    }


    private boolean noErrors(UpdateChecker checker) {
        if (checker.hasError()) {
            System.out.println(checker.getErrorMessage());
        }
        return ! checker.hasError();
    }


    private CliUpdateTableModel setInstallAction(Differ differ, String component,
                                                 boolean installAction) {
        CliUpdateTableModel model = new CliUpdateTableModel(differ);
        for (UpdateRow r : model.getRows()) {
            if (r.getName().equals(component)) {

                // if remove request and removing not allowed for component
                if (! (installAction || r.isInstallable())) {
                    break;
                }
                r.setInstallAction(installAction);
                return model;
            }
        }
        return null;    // no component of that name found
    }


    private boolean doAddOrRemove(String component, Differ differ, boolean adding) {
        CliUpdateTableModel model = setInstallAction(differ, component, adding);
        if (model == null) {
            writeAddRemoveError(component, adding);
        }
        else {
            update(model, component + " successfully " + (adding ? "added" : "removed"));
        }
        return true;
    }


    private void writeAddRemoveError(String component, boolean adding) {
        StringBuilder s = new StringBuilder();
        s.append("Unknown component name");
        if (! adding) s.append(", or component not removable");
        s.append(": ").append(component);
        System.out.println(s.toString());
    }


    protected void update(CliUpdateTableModel model, String msg) {
        CliUpdater updater = new CliUpdater(model);
        updater.start();

        // wait for update to complete
        while (! updater.isDone()) {
            pause(1000);
        }

        UpdateChecker checker = check();
        printTable(checker);
        if (!updater.hasErrors()) {
            System.out.println(msg);
        }
    }


    private void pause(int mSecs) {
        try {
            Thread.sleep(mSecs);
        }
        catch (InterruptedException ignore) { }
    }

}
