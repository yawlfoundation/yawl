package org.yawlfoundation.yawl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class HttpUtil {


    public static boolean isResponsive(URL url) {
        try {
            return resolveURL(url) != null;
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public static URL resolveURL(String urlString) throws IOException {
        return resolveURL(new URL(urlString));
    }


    // follows redirects and returns the final url
    public static URL resolveURL(URL url) throws IOException {
        while (url != null) {
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("HEAD");
            httpConnection.setConnectTimeout(1000);
            httpConnection.setReadTimeout(1000);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode < 300) {                        // some non-error response
                break;
            }
            if (responseCode < 400) {                        // some redirect response
                String location = httpConnection.getHeaderField("Location");
                url = new URL(location);
            }
            else {                                           // error response
                throw new IOException(httpConnection.getResponseMessage());
            }
        }
        return url;
    }


    public static boolean isPortActive(String host, int port) {
        try {
            return simplePing(host, port);
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public static void download(String fromURL, File toFile) throws IOException {
        download(new URL(fromURL), toFile);
    }


    public static void download(URL fromURL, File toFile) throws IOException {
        URL webFile = HttpUtil.resolveURL(fromURL);
        ReadableByteChannel rbc = Channels.newChannel(webFile.openStream());
        FileOutputStream fos = new FileOutputStream(toFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
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


}
