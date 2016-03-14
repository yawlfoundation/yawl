package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.yawlfoundation.yawl.util.HttpUtil.resolveURL;

/**
 * @author Michael Adams
 * @date 17/02/2016
 */
public class UpdateConstants {

//    private static final String BASE_1 = "http://sourceforge.net";
//    private static final String PATH_1 = "/projects/yawl/files/updatecache4/engine/";
//    private static final String SUFFIX_1 = "/download";

    private static final String BASE_1 = "https://raw.githubusercontent.com";
    private static final String PATH_1 = "/yawlfoundation/yawl/master/";
    private static final String CHECK_PATH_1 = "checksums/";
    private static final String SUFFIX_1 = "";

    private static final String BASE_2 = "http://yawlfoundation.org";
    private static final String PATH_2 = "/yawl/updates/engine/";
    private static final String CHECK_PATH_2 = "checksums/";
    private static final String SUFFIX_2 = "";


    public static String URL_BASE;
    public static String URL_PATH;
    public static String CHECK_PATH;
    public static String URL_SUFFIX;

    public static final String CHECK_FILE = "checksums.xml";


    // Checks the url values for a server response. If success, sets the values.
    // If no set of values is responsive, leaves the values as null.
    public static void init() throws IOException {
        if (URL_BASE == null) {
            boolean ignore = initFromChecksums() ||
                resolve(BASE_1, PATH_1, CHECK_PATH_1, SUFFIX_1) ||
                resolve(BASE_2, PATH_2, CHECK_PATH_2, SUFFIX_2);
        }
        checkInitSuccess();
    }


    public static URL getCheckUrl() throws IOException {
        checkInitSuccess();
        return new URL(URL_BASE + URL_PATH + CHECK_PATH + CHECK_FILE + URL_SUFFIX);
    }


    public static String getBasePath() {
        return URL_BASE != null ? URL_BASE + URL_PATH : null;
    }


    private static boolean resolve(String base, String path, String checkPath, String suffix) {
        try {
            URL url = resolveURL(base + path + checkPath + CHECK_FILE + suffix);
            if (url != null) {
                URL_BASE = url.getProtocol() + "://" + url.getAuthority();
                String fullPath = url.getPath();
                URL_PATH = fullPath.substring(0, fullPath.indexOf(checkPath));
                CHECK_PATH = checkPath;
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
        if (anyAreNull(URL_BASE, URL_PATH, CHECK_PATH, URL_SUFFIX)) {
            throw new IOException("Update servers are currently offline or unavailable");
        }
    }


    private static boolean initFromChecksums() {
        File current = FileUtil.getLocalCheckSumFile();
        if (current.exists()) {
            ChecksumsReader reader = new ChecksumsReader(current);
            XNode pathsNode = reader.getNode("paths");
            if (pathsNode != null) {
                PathResolver resolver = new PathResolver(pathsNode);
                String base = resolver.get("host");
                String path = resolver.get("base");
                String check = resolver.get("check");
                String suffix = resolver.get("suffix");
                if (! anyAreNull(base, path, check)) {
                    if (suffix == null) suffix = "";
                    return resolve(base, path, check, suffix);
                }
            }
        }
        return false;
    }


    private static boolean anyAreNull(String... strings) {
        for (String s : strings) {
            if (s == null) return true;
        }
        return false;
    }

}
