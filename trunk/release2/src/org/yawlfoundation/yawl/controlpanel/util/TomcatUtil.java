package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        if (! isRunning()) {
            checkSizeOfLog();
            removePidFile();
            executeCmd(createStartCommandList());
            return true;
        }
        return false;
    }


    public static boolean stop() throws IOException {
        if (isRunning()) {
            executeCmd(createStopCommandList());
            return true;
        }
        return false;
    }


    public static boolean isRunning() {
        return isRunning("localhost");
    }


    public static String getCatalinaHome() {
        return CATALINA_HOME;
    }


    public static int getTomcatServerPort() {
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
        return -1;
    }


    public static boolean isEngineRunning() {
        if (ENGINE_URL == null) {
            try {
                if (SERVER_PORT == -1) SERVER_PORT = getTomcatServerPort();
                ENGINE_URL = new URL("http", "localhost", SERVER_PORT, "/yawl/ib");
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
             httpConnection.setConnectTimeout(2000);
             return httpConnection.getResponseCode() == 200;
         }
         catch (IOException ioe) {
             return false;
         }
     }


    /*************************************************************************/

    private static boolean isRunning(String host) {
        if (SERVER_PORT == -1) SERVER_PORT = getTomcatServerPort();
        try {
            return simplePing(host, SERVER_PORT);
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


    private static XNode loadTomcatConfigFile(String filename) {
        if (!filename.startsWith("conf")) {
            filename = "conf" + File.separator + filename;
        }
        File configFile = new File(filename);
        if (!configFile.isAbsolute()) {
            configFile = new File(getCatalinaHome(), filename);
        }
        return (configFile.exists()) ?
                new XNodeParser().parse(StringUtil.fileToString(configFile)) : null;
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
            cmdList.add(cmd + (isStart ? " start" : " stop"));
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
        pb.start();
    }


    private static String getScriptExtn() {
        String os = System.getProperty("os.name");
        return (os != null && os.toLowerCase().startsWith("win")) ? "bat" : "sh";
    }


    private static void addEnvParameters(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.put("CATALINA_HOME", CATALINA_HOME);
    }


    // rename catalina.out if its too big - tomcat will create a new one on startup
    private static void checkSizeOfLog() {
        File log = new File(FileUtil.buildPath(getCatalinaHome(), "logs", "catalina.out"));
        if (log.exists() && log.length() > (1024 * 1024 * 5)) {              // 5mb
            String suffix = "." + new SimpleDateFormat("yyyyMMdd").format(new Date());
            log.renameTo(new File(log.getAbsolutePath() + suffix));
        }
    }


    private static void removePidFile() {
        File pidTxt = new File(getCatalinaHome(), "catalina_pid.txt");
        if (pidTxt.exists()) pidTxt.delete();
    }


    private static String deriveCatalinaHome() {
        try {
            File thisJar = FileUtil.getJarFile();
            if (thisJar != null && thisJar.getAbsolutePath().endsWith(".jar")) {
                String rootPath = FileUtil.buildPath(thisJar.getParentFile().getParent(),
                        "engine", "apache-tomcat-" + TOMCAT_VERSION);
                System.out.println(rootPath);
                return rootPath;
            }
        }
        catch (URISyntaxException use) {
            //
        }
        return "/Users/adamsmj/Documents/Subversion/installer/YAWL3/engine/apache-tomcat-7.0.55";
 //       return System.getenv("CATALINA_HOME");         // fallback
    }


    private static void captureError(Process p) {
        StringWriter out = new StringWriter(2048);
        InputStream is = p.getErrorStream();
        InputStreamReader isr = new InputStreamReader(is);
        char[] buffer = new char[2048];
        int count;
        try {
            while ((count = isr.read(buffer)) > 0)
                out.write(buffer, 0, count);

            isr.close();
        }
        catch (IOException ioe) {
            //
        }
        System.out.println(out.toString());
    }

}
