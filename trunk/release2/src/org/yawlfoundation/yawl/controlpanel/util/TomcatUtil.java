package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class TomcatUtil {

    private static int SERVER_PORT = -1;
    private static URL ENGINE_URL;

    public static boolean start() throws IOException {
        if (! isRunning()) {
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
        return System.getenv("CATALINA_HOME");
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
             httpConnection.setConnectTimeout(500);
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
        String extn = getScriptExtn();
        String cmd = getCatalinaHome() + "/bin/catalina." + extn;
        if (extn.endsWith("sh")) {
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
   //     Map<String, String> env = pb.environment();
   //     env.put(key, value);
    }

}
