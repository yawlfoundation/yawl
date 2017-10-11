package org.yawlfoundation.yawl.balancer.config;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.balancer.OperatingMode;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 8/9/17
 */
public class Config {

    private static String _fullPath;

    private static final DefaultFileMonitor FM =
            new DefaultFileMonitor(new FileChangeListener());

    private static final Set<ConfigChangeListener> CHANGE_LISTENERS =
            new HashSet<ConfigChangeListener>();

    private static final Properties PROPS = new Properties();
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static final String FILE_PATH = "/WEB-INF/config/balancer.properties";
    private static final int DEFAULT_MODE = 0;
    private static final int DEFAULT_INIT_WAIT = 60;
    private static final int DEFAULT_POLL_INTERVAL = 10;
    private static final int DEFAULT_REQUEST_LIMIT = 100;
    private static final int DEFAULT_PROC_TIME_LIMIT = 70;
    private static final int DEFAULT_THREADS_LIMIT = 16;
    private static final int DEFAULT_REQUEST_WEIGHT = 1;
    private static final int DEFAULT_PROC_TIME_WEIGHT = 1;
    private static final int DEFAULT_THREADS_WEIGHT = 1;
    private static final double DEFAULT_FORGET_FACTOR = 0.5;
    private static final int DEFAULT_FORECAST_QUEUE_SIZE = 60;
    private static final int DEFAULT_FORECAST_LOOKAHEAD = 1;
    private static final double DEFAULT_MAX_BUSYNESS = 0.0;
    private static final int DEFAULT_FORECAST_MODELLER = 2;
    
    public static boolean load(ServletContext context) {
        try {
            PROPS.load(context.getResourceAsStream(FILE_PATH));
            _fullPath = context.getRealPath("") + FILE_PATH;
            startConfigMonitoring(_fullPath);
            LOG.debug("Balancer config successfully loaded.");
            return true;
        }
        catch (IOException ioe) {
            LOG.error("Failed to load Balancer config");
        }
        return false;
    }


    public static void reload() {
        if (_fullPath != null) {
            try {
                Map<String, String> oldValues = enumerateProperties();
                PROPS.load(new FileReader(_fullPath));
                announceChange(getChangedProperties(oldValues));
                LOG.info("Balancer config successfully reloaded.");
            }
            catch (IOException ioe) {
                LOG.error("Failed to reload Balancer config");
            }
        }
    }


    public static void addChangeListener(ConfigChangeListener listener) {
        CHANGE_LISTENERS.add(listener);
    }


    public static void removeChangeListener(ConfigChangeListener listener) {
         CHANGE_LISTENERS.remove(listener);
     }


    public static List<String> getLocations() {
        String locations = get("locations");
        return locations != null ? Arrays.asList(locations.split(",")) :
                Collections.<String>emptyList();
    }


    public static OperatingMode getOperatingMode() {
        int modeValue = getInt("mode", DEFAULT_MODE);
        if (modeValue == -1 || modeValue > OperatingMode.values().length - 1) {
            return OperatingMode.RANDOM;            // default
        }
        return OperatingMode.values()[modeValue];
    }


    public static int getEngineInitWait() {
        return getInt("max_init_wait_secs", DEFAULT_INIT_WAIT);
    }


    public static int getEngineInitWaitMSecs() {
        return getEngineInitWait() * 1000;
    }


    public static int getPollInterval() {
        return getInt("poll_interval", DEFAULT_POLL_INTERVAL);
    }


    public static int getRequestLimit() {
        return getInt("request_limit", DEFAULT_REQUEST_LIMIT);
    }


    public static int getRequestLimitPerPollInterval() {
        return getRequestLimit() * getPollInterval();
    }


    public static int getProcessTimeLimit() {
        return getInt("proc_time_limit", DEFAULT_PROC_TIME_LIMIT);
    }


    public static int getThreadsLimit() {
        return getInt("threads_limit", DEFAULT_THREADS_LIMIT);
    }


    public static double getRequestWeight() {
        return getDouble("request_weight", DEFAULT_REQUEST_WEIGHT);
    }


    public static double getProcessTimeWeight() {
        return getDouble("proc_time_weight", DEFAULT_PROC_TIME_WEIGHT);
    }


    public static double getThreadsWeight() {
        return getDouble("threads_weight", DEFAULT_THREADS_WEIGHT);
    }


    public static double getWeightedRequestLimitPerPollInterval() {
        return getRequestLimitPerPollInterval() / getRequestWeight();
    }


    public static double getWeightedProcessTimeLimit() {
        return getProcessTimeLimit() / getProcessTimeWeight();
    }


    public static double getWeightedThreadsLimit() {
        return getThreadsLimit() / getThreadsWeight();
    }


    public static boolean isWriteLog() {
        String value = get("write_log");
        return value != null && ( value.equals("1") ||
                value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("t") ||
                value.equalsIgnoreCase("y") ||
                value.equalsIgnoreCase("yes"));
    }


    public static double getForgetFactor() {
        double alpha = getDouble("forget_factor", DEFAULT_FORGET_FACTOR);
        if (alpha < 0) alpha = 0.0;
        else if (alpha > 1.0) alpha = 1.0;
        return alpha;
    }


    public static int getForecastQueueSize() {
        return getInt("forecast_queue_size", DEFAULT_FORECAST_QUEUE_SIZE);
    }


    public static int getForecastLookahead() {
        return getInt("forecast_lookahead", DEFAULT_FORECAST_LOOKAHEAD);
    }


    public static double getBusynessLimit() {
        return getDouble("max_busyness", DEFAULT_MAX_BUSYNESS);
    }


    public static int getPreferredForecastModeller() {
        return getInt("preferred_forecaster", DEFAULT_FORECAST_MODELLER);
    }


    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String def) {
        String value = PROPS.getProperty(key);
        return value != null ? value : def;
    }



    public static int getInt(String key) {
        return getInt(key, -1);
    }

    public static int getInt(String key, int def) {
        return StringUtil.strToInt(get(key), def);
    }


    public static double getDouble(String key) {
         return getDouble(key, -1);
    }

    public static double getDouble(String key, double def) {
        return StringUtil.strToDouble(get(key), def);
    }


    public static void stopConfigMonitoring() { FM.stop(); }

    private static void startConfigMonitoring(String filePath) throws FileSystemException {
        FM.addFile(VFS.getManager().resolveFile(filePath));
        FM.setDelay(10000);
        FM.start();
    }


    private static void announceChange(Map<String, String> changedValues) {
        for (ConfigChangeListener listener : CHANGE_LISTENERS) {
            listener.configChanged(changedValues);
        }
    }


    private static Map<String, String> enumerateProperties() {
        Map<String, String> map = new HashMap<String, String>();
        for (Object key : PROPS.keySet()) {
            map.put((String) key, (String) PROPS.get(key));
        }
        return map;
    }


    private static Map<String, String> getChangedProperties(Map<String, String> old) {
        Map<String, String> map = new HashMap<String, String>();
        for (String key : old.keySet()) {
            String oldValue = old.get(key);
            String newValue = PROPS.getProperty(key);
            if (!oldValue.equals(newValue)) {
                map.put(key, newValue);
            }
        }
        return map;
    }
}
