package org.yawlfoundation.yawl.reporter;

import org.apache.commons.codec.binary.Base64;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Michael Adams
 * @date 1/10/15
 */
public class Sender {

    private Properties _props;

    protected Sender(InputStream isProps) { loadProperties(isProps); }


    protected String send(String subject, String text) throws IOException {
        if (_props == null) {
            throw new IOException("Failed to load sender parameters");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("from",get("f"));
        params.put("to", get("t"));
        params.put("subject", subject);
        params.put("html", text);
        return send(params);
    }



    private void loadProperties(InputStream inputStream) {
        try {
            _props = new Properties();
            _props.load(inputStream);
        }
        catch (Exception e) {
            _props = null;
        }
    }


    private String get(String key) throws IOException {
        String prop = _props.getProperty(key);
        if (prop == null) {
            throw new IOException("Failed to load sender parameter");
        }
        return prop;
    }


    private String send(Map<String, String> params) throws IOException {
        URL url = new URL(get("m"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encodeCredentials());
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        sendData(connection, encodeData(params)) ;
        String result = getReply(connection.getInputStream());
        connection.disconnect();
        return result;
    }


    private String encodeCredentials() throws IOException {
        byte[] bytes = Base64.encodeBase64((get("u") + ":" + get("k")).getBytes());
        return new String(bytes);
    }


    private String encodeData(Map<String, String> params) {
        StringBuilder data = new StringBuilder("");
        for (String param : params.keySet()) {
            String value = params.get(param);
            if (value != null) {
                if (data.length() > 0) data.append("&");
                data.append(param)
                      .append("=")
                      .append(ServletUtils.urlEncode(value));
            }
        }
        return data.toString();
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

}
