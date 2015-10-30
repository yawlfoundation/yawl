package org.yawlfoundation.yawl.controlpanel.util;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 21/10/2015
 */
public class TomcatProcess {

    private ProcessRunner _tomcatRunner;
    private String _catalinaHome;
    private String _winPid;


    public TomcatProcess(String catalinaHome) {
        _catalinaHome = catalinaHome;
    }


    public void start() throws IOException {
        _tomcatRunner = new ProcessRunner();
        Map<String, String> envParams = getEnvParams();
        printEnvParameters(envParams);

        // for windows, this hack is needed to get the PID of the tomcat process.
        // the pre and post task lists are diffed
        boolean isWin = FileUtil.isWindows();
        Set<String> preTasks = null;
        if (isWin) preTasks = getJavaTasks();

        _tomcatRunner.run(getStartCommand(), envParams);
        monitorStartup(isWin, preTasks);
    }


    public boolean stop(PropertyChangeListener listener) throws IOException {
        if (isAlive()) {
            System.out.println("INFO: Shutting down the server, please wait... ");
            new ProcessRunner().run(getStopCommand(), getEnvParams());
            monitorShutdown(listener);
        }
        else {
            System.out.println("WARN: Server is already shutdown.");
        }
        return true;
    }


    public boolean isAlive() { return _tomcatRunner != null && _tomcatRunner.isAlive(); }


    public void kill() throws IOException {
        List<String> cmdList;
        if (FileUtil.isWindows()) {
            if (_winPid == null) return;
            cmdList = Arrays.asList("TASKKILL", "/PID", _winPid, "/F");
        }
        else {
            cmdList = Arrays.asList("pkill", "-f", "catalina");
        }
        new ProcessBuilder(cmdList).start();
    }


    public void destroy() { _tomcatRunner.stop(); }


    private Map<String, String> getEnvParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("CATALINA_HOME", _catalinaHome);
        params.put("JAVA_HOME", System.getProperty("java.home"));
        return params;
    }


    private void printEnvParameters(Map<String, String> params) {
        for (String key : params.keySet()) {
            System.out.println("Using " + key + ": " + params.get(key));
        }
    }


    private List<String> getStartCommand() {  return getCommand("run"); }


    private List<String> getStopCommand() {  return getCommand("stop"); }


    private List<String> getCommand(String arg) {
        List<String> cmdList = new ArrayList<String>();
        String baseCmd = buildBaseCmd();
        if (baseCmd.endsWith("sh")) {
            cmdList.add("bash");
            cmdList.add("-c");
            cmdList.add(baseCmd + " " + arg);
        }
        else {
            cmdList.add("cmd");
            cmdList.add("/c");
            cmdList.add(baseCmd);
            cmdList.add(arg);
        }
        return cmdList;
    }


    private String buildBaseCmd() {
        StringBuilder s = new StringBuilder();
        s.append(_catalinaHome)
         .append(FileUtil.SEP)
         .append("bin")
         .append(FileUtil.SEP)
         .append("catalina.")
         .append(FileUtil.isWindows() ? "bat" : "sh");
        return s.toString();
    }


    private void monitorStartup(final boolean isWindows, final Set<String> preTasks) {
        final StartMonitor startMonitor = new StartMonitor(10);
        startMonitor.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("state") &&
                        event.getNewValue() == SwingWorker.StateValue.DONE) {
                    if (isWindows) {
                        try {
                            Set<String> postTasks = getJavaTasks();
                            _winPid = extractPID(preTasks, postTasks);
                        }
                        catch (IOException ioe) {
                            // ignore
                        }
                    }
                }
            }

        });
        startMonitor.execute();
    }


    private void monitorShutdown(PropertyChangeListener listener) throws IOException {
        StopMonitor stopMonitor = new StopMonitor(this, 10);
        if (listener != null) {
            stopMonitor.addPropertyChangeListener(listener);
        }
        stopMonitor.execute();
    }


    private Set<String> getJavaTasks() throws IOException {
        final Set<String> tasks = new HashSet<String>();
        ProcessRunner winProc = new ProcessRunner() {
            protected void handleOutput(String line) {
                tasks.add(line);
            }
        };

        // get all tasks running java, return details as csv with no header
        List<String> cmd = Arrays.asList("tasklist", "/M", "java*", "/FO", "CSV", "/NH");
        winProc.run(cmd, null);
        winProc.waitFor();
        return tasks;
    }


    private String extractPID(Set<String> preTasks, Set<String> postTasks) {
        for (String task : postTasks) {
            if (! preTasks.contains(task)) {
                return task.split(",")[1];
            }
        }
        return null;
    }

}
