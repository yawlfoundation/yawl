package org.yawlfoundation.yawl.schema;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Michael Adams
 * @date 8/12/14
 */
public class ResourceResolver implements LSResourceResolver {

    private Map<URL, String> cache;

    private static final ResourceResolver INSTANCE = new ResourceResolver();

    private ResourceResolver() { cache = new HashMap<URL, String>(); }


    public static ResourceResolver getInstance() { return INSTANCE; }


    public LSInput resolveResource(String type, String namespaceURI,
                                   String publicId, String systemId, String baseURI) {
        try {
            URL url = new URL(namespaceURI + '/' + systemId);
            String content = cache.get(url);
            if (content == null) {
                content = streamToString(new URL(namespaceURI + '/' + systemId).openStream());
                if (content != null) cache.put(url, content);
            }
            return new Input(publicId, systemId, stringToStream(content));
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
            return null;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }


    private String streamToString(InputStream is) {
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }


    private InputStream stringToStream(String s) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(s.getBytes("UTF-8"));
    }

}
