package org.yawlfoundation.yawl.balancer;

import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.config.ConfigChangeListener;
import org.yawlfoundation.yawl.balancer.output.ArffOutputter;
import org.yawlfoundation.yawl.balancer.polling.Pollable;
import org.yawlfoundation.yawl.balancer.polling.PollingService;
import org.yawlfoundation.yawl.balancer.rule.BusynessRule;
import org.yawlfoundation.yawl.balancer.rule.ExponentialMovingAverage;
import org.yawlfoundation.yawl.balancer.rule.HawkularForecaster;
import org.yawlfoundation.yawl.balancer.rule.OpenForecaster;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 20/7/17
 */
public class EngineInstance implements ConfigChangeListener, Pollable {

    private String _host;
    private int _port;
    private final LoadReader _loadReader;
    private BusynessRule _busyRule;
    private String _sessionHandle;
    private boolean _active;
    private boolean _initialized;
    private boolean _restored;
    private boolean _authenticator;
    private List<String> _runningCases;

    private static final String YAWL_URL_TEMPLATE = "http://%s:%d/yawl%s";


    public EngineInstance(String host, int port) {
        _host = host;
        _port = port;
        _loadReader = new LoadReader(host, port);
        _active = false;
        _initialized = false;
        _authenticator = false;
        _runningCases = new ArrayList<String>();
        _busyRule = getBusyRule();
        PollingService.add(new RequestCollator(host, port));
        PollingService.add(this);
        Config.addChangeListener(this);
        pingUntilActive();
    }


    @Override
    public void configChanged(Map<String, String> changedValues) {
        if (changedValues.containsKey("mode")) {
            _busyRule = getBusyRule();
        }
    }


    @Override
    public void scheduledEvent() {
        try {

            // this will write values to log for all options (if configured)
            double busyness = _loadReader.getBusyness(Config.isWriteLog());
            if (_busyRule != null) {
                _busyRule.add(busyness);
            }
        }
        catch (Exception e) {
            LogManager.getLogger(this.getClass()).error(
                    e.getMessage() + " for engine {}:{}", _host, _port);
            // later
        }
    }


    public void setArffWriter(ArffOutputter writer) {
        if (_loadReader != null) _loadReader.setArffWriter(writer);
    }


    public String getURL(String path) {
        return String.format(YAWL_URL_TEMPLATE, _host, _port, path);
    }


    public String getName() { return _host + ":" + _port; }

    public int getPort() { return _port; }



    public void close() {
        _loadReader.close();
    }


    public boolean isInitialized() { return _initialized; }


    public boolean isActive() { return _active; }

    public void setActive(boolean active) { _active = active; }


    public boolean isRestored() { return _restored; }

    public void setRestored(boolean restored) { _restored = restored; }


    public boolean isAuthenticator() { return _authenticator; }

    public void setAuthenticator(boolean authenticator) { _authenticator = authenticator; }


    public String getSessionHandle() { return _sessionHandle; }

    public void setSessionHandle(String handle) { _sessionHandle = handle; }


    public void addCase(String caseid) { _runningCases.add(caseid); }

    public void addCases(List<String> caseids) { _runningCases.addAll(caseids); }

    public boolean removeCase(String caseid) { return _runningCases.remove(caseid); }

    public boolean hasCase(String caseid) { return _runningCases.contains(caseid); }

    
    public int getLastCaseNbr() {
        int caseNbr = -1;
        for (String caseID : _runningCases) {
            caseNbr = Math.max(caseNbr, StringUtil.strToInt(caseID, -1));
        }
        return caseNbr;
    }

    public void pingUntilActive() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            _initialized = HttpURLValidator.pingUntilAvailable(
                                    getURL("/ib"), 60);
                        }
                        catch (MalformedURLException mue) {
                            // remains unavailable
                        }

                    }
                }
        ).start();
    }


    public double getBusyness(boolean verbose) throws IOException, JSONException {
        return _busyRule != null ? _busyRule.get() : _loadReader.getBusyness(verbose);
    }


    private BusynessRule getBusyRule() {
        switch (Config.getOperatingMode()) {
            case MOVING_AVERAGE:
                return new ExponentialMovingAverage(Config.getForgetFactor());
            case PREDICTIVE_MOVING_AVERAGE:
                if (Config.getPreferredForecastModeller() == 1) {
                    return new HawkularForecaster(Config.getForecastQueueSize(),
                            Config.getPollInterval());
                }
                else return new OpenForecaster();
            default:
                return null;
        }
    }

}
