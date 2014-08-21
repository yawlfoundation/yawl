package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.update.table.AppEnum;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/08/2014
 */
public class Registrar {

    private static final String URL_STR = "http://localhost:8080/yawl/ia";
    private String _handle;


    public Registrar() { }


    public boolean add(String name, String password, String url) throws IOException {
        return isYAWLService(name) ? addService(name, password, url) :
                addApp(name, password);
    }


    public boolean remove(String name) throws IOException {
        return isYAWLService(name) ? removeService(name) : removeApp(name);
    }


    public void disconnect() {
        try {
            if (_handle != null) send(prepareParamMap("disconnect", _handle));
        }
        catch (IOException ignore) {
            //
        }
    }


    private boolean addService(String name, String password, String url) throws IOException {
        _handle = connect();
        Map<String, String> params = prepareParamMap("newYAWLService", _handle);
        params.put("service", getServiceTransport(name, password, url));
        return successful(send(params));
    }

    private boolean addApp(String name, String password) throws IOException {
        _handle = connect();
        Map<String, String> params = prepareParamMap("createAccount", _handle);
        params.put("userID", name);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        params.put("doco", AppEnum.fromString(name).getDescription());
        return successful(send(params));
    }


    private boolean removeService(String uri) throws IOException {
        _handle = connect();
        Map<String, String> params = prepareParamMap("removeYAWLService", _handle);
        params.put("serviceURI", uri);
        return successful(send(params));
    }


    private boolean removeApp(String name) throws IOException {
        _handle = connect();
        Map<String, String> params = prepareParamMap("deleteAccount", _handle);
        params.put("userID", name);
        return successful(send(params));
    }



    private String connect() throws IOException {
        if (_handle == null) {
            Map<String, String> paramMap = prepareParamMap("connect", null);
            paramMap.put("userID", "admin");
            paramMap.put("password", PasswordEncryptor.encrypt("YAWL", "YAWL"));
            String response = send(paramMap);
            if (! successful(response)) {
                throw new IOException("Failed to connect to engine: " +
                        StringUtil.unwrap(response));
            }
            _handle = response;
        }
        return _handle;
    }


    private boolean successful(String message) {
        return (message != null)  &&
               (message.length() > 0) &&
               (! message.contains("<failure>")) ;
    }


    private Map<String, String> prepareParamMap(String action, String handle) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", action) ;
        if (handle != null) paramMap.put("sessionHandle", handle) ;
        return paramMap;
    }


    private String send(Map<String, String> paramsMap) throws IOException {
        HttpURLConnection connection = initPostConnection();
        sendData(connection, encodeData(paramsMap)) ;
        String result = getReply(connection.getInputStream());
        connection.disconnect();
        return StringUtil.unwrap(result);
    }


    private HttpURLConnection initPostConnection() throws IOException {
        URL url = new URL(URL_STR);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setReadTimeout(0);
        connection.setRequestProperty("Connection", "close");
        return connection ;
    }


    private String encodeData(Map<String, String> params) {
        StringBuilder result = new StringBuilder("");
        for (String param : params.keySet()) {
            String value = params.get(param);
            if (value != null) {
                if (result.length() > 0) result.append("&");
                result.append(param)
                      .append("=")
                      .append(urlEncode(value));
            }
        }
        return result.toString();
    }


    private String urlEncode(String s) {
        if (s == null) return s;
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }


    private void sendData(HttpURLConnection connection, String data)
            throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write(data);
        out.close();
    }


    private String getReply(InputStream is) throws IOException {
        final int BUF_SIZE = 16384;

        // read reply into a buffered byte stream - to preserve UTF-8
        BufferedInputStream inStream = new BufferedInputStream(is);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }

        outStream.close();
        inStream.close();

        // convert the bytes to a UTF-8 string
        return outStream.toString("UTF-8");
    }


    private boolean isYAWLService(String name) {
        return ! (name.equals("costService") || name.equals("documentStore") ||
                  name.equals("monitorService"));
    }


    private String getServiceTransport(String name, String password, String url) {
        XNode root = new XNode("yawlService");
        root.addAttribute("id", url);
        root.addChild("documentation", AppEnum.fromString(name).getDescription());
        root.addChild("servicename", name);
        root.addChild("servicepassword", PasswordEncryptor.encrypt(password, password));
        root.addChild("assignable", true);
        return root.toString();
    }

}
