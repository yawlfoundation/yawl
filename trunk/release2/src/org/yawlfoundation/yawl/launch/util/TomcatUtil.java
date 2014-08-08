package org.yawlfoundation.yawl.launch.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.*;
import java.net.*;
import java.util.*;

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
        Element root = getRootConfigElement();
        if (root != null) {
            Element service = root.getChild("Service");
            if (service != null) {
                Element connector = service.getChild("Connector");
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
        return isEngineRunning(ENGINE_URL);
    }


    /*************************************************************************/

    private static boolean isEngineRunning(URL url) {
         try {
             HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
             httpConnection.setRequestMethod("HEAD");
             httpConnection.setConnectTimeout(200);
             return httpConnection.getResponseCode() == 200;
         }
         catch (IOException ioe) {
             return false;
         }
     }


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


    private static Element getRootConfigElement() {
        Document serverConfigDoc = loadTomcatConfigFile("server.xml");
        return (serverConfigDoc != null) ? serverConfigDoc.getRootElement() : null;
    }


    private static Document loadTomcatConfigFile(String filename) {
        if (!filename.startsWith("conf")) {
            filename = "conf" + File.separator + filename;
        }
        File configFile = new File(filename);
        if (!configFile.isAbsolute()) {
            configFile = new File(getCatalinaHome(), filename);
        }
        return (configFile.exists()) ? JDOMUtil.fileToDocument(configFile) : null;
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
