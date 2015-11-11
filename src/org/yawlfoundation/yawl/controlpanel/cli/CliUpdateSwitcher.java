package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.UpdateChecker;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;

/**
 * Provides a CLI interface into the control panel update facility
 *
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliUpdateSwitcher {


    public boolean handle(String[] args) {
        String arg = args[0].toLowerCase();
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

        // no matching args
        System.out.println("Command Line Usage:\n\n" +
                FileUtil.getJarName() + " option [argument]\n\n" +
        		"where option is one of \n\n" +
				"-update:           Updates installed components\n" +
				"-versions:         List installed and available components and their versions\n" +
				"-add [component]:    Adds the named component (and performs updates)\n" +
				"-remove [component]: Removes the named component (and performs updates)\n");
        return false;
    }


    private boolean doUpdates() {
        UpdateChecker checker = check();

        if (noErrors(checker)) {
            CliUpdateTableModel model = printTable(checker);

            if (checker.getDiffer().hasUpdates()) {
                String response = System.console().readLine(
                        "Do you wish to update (Y/N)? ==> ");
                if (response.equalsIgnoreCase("y")) {
                    new CliUpdater(model).update();
                    checker.check();
                    System.out.println("Update completed");
                    printTable(checker);
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
        return doAddOrRemove(component, true);
    }


    private boolean doRemove(String component) {
        return doAddOrRemove(component, false);
    }


    private CliUpdateTableModel printTable(UpdateChecker checker) {
        CliUpdateTableModel model = new CliUpdateTableModel(checker.getDiffer());
        model.print();
        return model;
    }


    private UpdateChecker check() {
        System.out.println("Contacting YAWL update server...");
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


    private CliUpdateTableModel setInstallAction(UpdateChecker checker,
                                                 String component, boolean installAction) {
        CliUpdateTableModel model = new CliUpdateTableModel(checker.getDiffer());
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


    private boolean doAddOrRemove(String component, boolean adding) {
        UpdateChecker checker = check();
        if (noErrors(checker)) {
            CliUpdateTableModel model = setInstallAction(checker, component, adding);
            if (model == null) {
                writeAddRemoveError(component, adding);
            }
            else {
                new CliUpdater(model).update();
                checker.check();
                System.out.println(component + " successfully " +
                        (adding ? "added" : "removed"));
                printTable(checker);
            }
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

}
