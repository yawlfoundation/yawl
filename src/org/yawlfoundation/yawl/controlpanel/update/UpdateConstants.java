package org.yawlfoundation.yawl.controlpanel.update;

import java.io.IOException;
import java.net.URL;

import static org.yawlfoundation.yawl.util.HttpUtil.resolveURL;

/**
 * @author Michael Adams
 * @date 17/02/2016
 */
public class UpdateConstants {

    private static final String BASE_1 = "http://sourceforge.net";
    private static final String PATH_1 = "/projects/yawl/files/updatecache4/engine/";
    private static final String SUFFIX_1 = "/download";

    private static final String BASE_2 = "http://yawlfoundation.org";
    private static final String PATH_2 = "/yawl/updates/engine/";
    private static final String SUFFIX_2 = "";


    public static String URL_BASE;
    public static String URL_PATH;
    public static String URL_SUFFIX;

    public static final String CHECK_FILE = "checksums.xml";


    // Checks the url values for a server response. If success, sets the values.
    // If neither set of values is responsive, leaves the values as null.
    public static void init() throws IOException {
        if (URL_BASE == null) {
            if (!resolve(BASE_1, PATH_1, SUFFIX_1)) {
                resolve(BASE_2, PATH_2, SUFFIX_2);
            }
        }
        checkInitSuccess();
    }


    public static URL getCheckUrl() throws IOException {
        checkInitSuccess();
        return new URL(URL_BASE + URL_PATH + "lib/" + CHECK_FILE + URL_SUFFIX);
    }


    public static String getBasePath() {
        return URL_BASE != null ? URL_BASE + URL_PATH : null;
    }


    private static boolean resolve(String base, String path, String suffix) {
        try {
            URL url = resolveURL(base + path + "lib/" + CHECK_FILE + suffix);
            if (url != null) {
                URL_BASE = url.getProtocol() + "://" + url.getAuthority();
                String fullPath = url.getPath();
                URL_PATH = fullPath.substring(0, fullPath.indexOf("lib/"));
                URL_SUFFIX = fullPath.substring(fullPath.indexOf(CHECK_FILE) +
                        CHECK_FILE.length());
            }
            return url != null;
        }
        catch (IOException ioe) {
            return false;
        }
    }


    private static void checkInitSuccess() throws IOException {
        if (URL_BASE == null || URL_PATH == null || URL_SUFFIX == null) {
            throw new IOException("Update servers are offline or unavailable");
        }
    }

}
