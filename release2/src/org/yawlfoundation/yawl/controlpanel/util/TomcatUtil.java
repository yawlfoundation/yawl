package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class TomcatUtil {

    private static int SERVER_PORT = -1;
    private static URL ENGINE_URL;
    private static final String TOMCAT_VERSION = "7.0.55";
    private static final String CATALINA_HOME = deriveCatalinaHome();

    public static boolean start() throws IOException {
        if (! isEngineRunning()) {                    // yawl isn't running
            if (isPortActive()) {                        // but localhost:port is responsive
                throw new IOException("Tomcat port is already in use by another service.\n" +
                     "Please check/change the port in Preferences and try again.");
            }
            checkSizeOfLog();
            removePidFile();                            // if not already removed
            executeCmd(createStartCommandList());
            return true;
        }
        return false;                                   // already started
    }


    public static boolean stop() throws IOException {
        if (isPortActive()) {
            executeCmd(createStopCommandList());
            return true;
        }
        return false;
    }


    public static boolean isPortActive() {
        return isPortActive("localhost", getTomcatServerPort());
    }


    public static boolean isPortActive(int port) {
        return isPortActive("localhost", port);
    }


    public static boolean isTomcatRunning() {
        return pidExists();
    }


    public static String getCatalinaHome() {
        return CATALINA_HOME;
    }


    public static int getTomcatServerPort() {
        if (SERVER_PORT < 0) SERVER_PORT = loadTomcatServerPort();
        return SERVER_PORT;
    }


    public static boolean setTomcatServerPort(int port) {
        XNode root = loadTomcatConfigFile("server.xml");
        if (root != null) {
            XNode service = root.getChild("Service");
            if (service != null) {
                XNode connector = service.getChild("Connector");
                if (connector != null) {
                    connector.addAttribute("port", port);
                    if (writeTomcatConfigFile("server.xml", root.toPrettyString(true))) {
                        updateServiceConfigs(getTomcatServerPort(), port);
                        SERVER_PORT = port;
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static boolean isEngineRunning() {
        if (ENGINE_URL == null) {
            try {
                ENGINE_URL = new URL("http", "localhost", getTomcatServerPort(),
                        "/yawl/ib");
            }
            catch (MalformedURLException mue) {
                return false;
            }
        }
        return isResponsive(ENGINE_URL);
    }


    public static boolean isResponsive(URL url) {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("HEAD");
            httpConnection.setConnectTimeout(1000);
            httpConnection.setReadTimeout(1000);
            return httpConnection.getResponseCode() == 200;
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public static void killTomcatProcess() throws IOException {
        if (FileUtil.isWindows()) {
            executeCmd(Arrays.asList("TASKKILL", "/F", "/FI",
                    "\"WINDOWTITLE eq Tomcat\"", "/IM", "java.exe"));
        }
    }


    /*************************************************************************/

    private static boolean isPortActive(String host, int port) {
        try {
            return simplePing(host, port);
        }
        catch (IOException ioe) {
            return false;
        }
    }


    private static boolean simplePing(String host, int port) throws IOException {
        if ((host == null) || (port < 0)) {
            throw new IOException("Error: bad parameters");
        }
        InetAddress address = InetAddress.getByName(host);
        Socket socket = new Socket(address, port);
        socket.close();
        return true;
    }


    private static int loadTomcatServerPort() {
        XNode root = loadTomcatConfigFile("server.xml");
        if (root != null) {
            XNode service = root.getChild("Service");
            if (service != null) {
                XNode connector = service.getChild("Connector");
                if (connector != null) {
                    return StringUtil.strToInt(connector.getAttributeValue("port"), -1);
                }
            }
        }
        return -1;       // default
    }


    private static XNode loadTomcatConfigFile(String filename) {
        File configFile = getTomcatConfigFile(filename);
        return (configFile.exists()) ?
                new XNodeParser().parse(StringUtil.fileToString(configFile)) : null;
    }


    private static boolean writeTomcatConfigFile(String filename, String content) {
        File configFile = getTomcatConfigFile(filename);
        if (configFile.exists()) {
            configFile = StringUtil.stringToFile(configFile, content);
        }
        return configFile != null;
    }


    private static File getTomcatConfigFile(String filename) {
        if (!filename.startsWith("conf")) {
            filename = "conf" + File.separator + filename;
        }
        File configFile = new File(filename);
        if (!configFile.isAbsolute()) {
            configFile = new File(getCatalinaHome(), filename);
        }
        return configFile;
    }


    private static List<String> createStartCommandList() {
        return createCommandList(true);
    }


    private static List<String> createStopCommandList() {
        return createCommandList(false);
    }


    private static List<String> createCommandList(boolean isStart) {
        List<String> cmdList = new ArrayList<String>();
        String cmd = buildCmd();
        if (cmd.endsWith("sh")) {
            cmdList.add("bash");
            cmdList.add("-c");
            cmdList.add(cmd + (isStart ? " start" : " stop -force"));
        }
        else {
            cmdList.add("cmd");
            cmdList.add("/c");
            cmdList.add(cmd);
            cmdList.add(isStart ? "start" : "stop");
        }
        return cmdList;
    }


    private static String buildCmd() {
        StringBuilder s = new StringBuilder();
        s.append(getCatalinaHome())
         .append(FileUtil.SEP)
         .append("bin")
         .append(FileUtil.SEP)
         .append("catalina.")
                .append(getScriptExtn());
        return s.toString();
    }


    private static void executeCmd(List<String> cmdList) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        addEnvParameters(pb);
        final Process process = pb.start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    InputStreamReader isr = new InputStreamReader(process.getErrorStream());
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                catch (IOException ioe) {
                    //
                }
            }
        }).start();

    }


    private static String getScriptExtn() {
        return FileUtil.isWindows() ? "bat" : "sh";
    }


    private static void addEnvParameters(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("CATALINA_HOME", CATALINA_HOME);
        String javaHome = deriveJavaHome();
        env.put("JAVA_HOME", javaHome);

        // output env (cmd line starts only)
        System.out.println("Using CATALINA_HOME: " + CATALINA_HOME);
        System.out.println("Using JAVA_HOME: " + javaHome);
    }


    // rename catalina.out if its too big - tomcat will create a new one on startup
    private static void checkSizeOfLog() {
        File log = new File(FileUtil.buildPath(getCatalinaHome(), "logs", "catalina.out"));
        if (log.exists() && log.length() > (1024 * 1024 * 5)) {              // 5mb
            String suffix = "." + new SimpleDateFormat("yyyyMMdd").format(new Date());
            log.renameTo(new File(log.getAbsolutePath() + suffix));
        }
    }


    public static void removePidFile() {
        File pidTxt = new File(getCatalinaHome(), "catalina_pid.txt");
        if (pidTxt.exists()) pidTxt.delete();
    }


    private static boolean pidExists() {
        File pidFile = new File(getCatalinaHome(), "catalina_pid.txt");
        return pidFile != null && pidFile.exists();
    }


    private static String deriveCatalinaHome() {
        try {
            File thisJar = FileUtil.getJarFile();
            if (thisJar != null && thisJar.getAbsolutePath().endsWith(".jar")) {
                String rootPath = FileUtil.buildPath(thisJar.getParentFile().getParent(),
                        "engine", "apache-tomcat-" + TOMCAT_VERSION);
                return rootPath;
            }
        }
        catch (URISyntaxException use) {
            //
        }
        return System.getenv("CATALINA_HOME");         // fallback
    }


    private static String deriveJavaHome() {
        String javaHome = System.getenv("JAVA_HOME");
        return javaHome != null ? javaHome : System.getProperty("java.home");
    }


    private static boolean updateServiceConfigs(int oldPort, int newPort) {
        if (oldPort == newPort) return true;
        String oldChars = ":" + oldPort;
        String newChars = ":" + newPort;
        File appsBase = new File(getCatalinaHome(), "webapps");
        for (File appDir : FileUtil.getDirList(appsBase)) {
            File webxml = new File(appDir, "WEB-INF" + File.separator + "web.xml");
            if (webxml.exists()) {
                StringUtil.replaceInFile(webxml, oldChars, newChars);
            }
        }
        return true;
    }

}
