package org.yawlfoundation.yawl.balancer;

import org.apache.logging.log4j.LogManager;
import org.json.JSONException;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Adams
 * @date 20/7/17
 */
public class EngineInstance {

    private String _host;
    private int _port;
    private final LoadReader _loadReader;
    private final Poller _poller;
    private ScheduledExecutorService _executor;

    private int _pollInterval = DEF_POLL_INTERVAL;
    private String _sessionHandle;
    private boolean _active;
    private boolean _restored;
    private boolean _authenticator;
    private ExponentialMovingAverage _expAverage;
    private Forecaster _forecaster;
    private OperatingMode _mode;
    private List<String> _runningCases;

    private static final String YAWL_URL_TEMPLATE = "http://%s:%d/yawl%s";
    private static final int DEF_POLL_INTERVAL = 5;


    public EngineInstance(String host, int port) {
        _host = host;
        _port = port;
        _loadReader = new LoadReader(host, port);
        _poller = new Poller(host, port);
        _active = false;
        _authenticator = false;
        _runningCases = new ArrayList<String>();
        pingUntilActive();
    }

    
    public String getURL(String path) {
        return String.format(YAWL_URL_TEMPLATE, _host, _port, path);
    }


    public String getName() { return _host + ":" + _port; }

    public int getPort() { return _port; }


    public void setLimits(int requests, int procTime, int threads) {
        _loadReader.setLimits(requests, procTime, threads);
    }


    public void setPollInterval(int interval) {
        _pollInterval = interval > 0 ? interval : DEF_POLL_INTERVAL;
    }


    public void setAlpha(double alpha) {
        if (alpha < 0) alpha = 0.0;
        else if (alpha > 1.0) alpha = 1.0;
        _expAverage = new ExponentialMovingAverage(alpha);
    }

    public void setForecastQueueSize(int maxSize) {
        if (maxSize < 1) maxSize = 60;
        _forecaster = new Forecaster(maxSize, _pollInterval);
    }

    public void setMode(OperatingMode mode) { _mode = mode; }


    public void startPolling(boolean verbose) {
        if (_pollInterval > -1) {
            startPolling(_pollInterval, verbose);
            _poller.start(_pollInterval, verbose);
        }
    }


    public void stopPolling() {
        if (_executor != null) _executor.shutdownNow();  
        _poller.stop();
        _loadReader.close();
    }


    private void startPolling(int interval, final boolean verbose) {
        _executor = Executors.newScheduledThreadPool(1);
        _executor.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        if (_mode == OperatingMode.RANDOM || _mode == OperatingMode.SNAPSHOT) {
                            return;
                        }
                        try {
                                double busyness = _loadReader.getBusyness(verbose);
                                if (_mode == OperatingMode.PREDICTIVE_MOVING_AVERAGE &&
                                        _forecaster != null) {
                                    _forecaster.add(busyness);
                                }
                                else if (_mode != OperatingMode.MOVING_AVERAGE &&
                                        _expAverage != null) {
                                    _expAverage.add(busyness);
                                }

                            }
                            catch (Exception e) {
                                LogManager.getLogger(this.getClass()).error(
                                        e.getMessage() + " for engine {}:{}",
                                        _host, _port
                                );
                                // later
                            }
                    }

                }, 0, interval, TimeUnit.SECONDS
        );
    }


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
                            _active = HttpURLValidator.pingUntilAvailable(
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
        double busyness = _loadReader.getBusyness(verbose);
        if (_mode == OperatingMode.PREDICTIVE_MOVING_AVERAGE && _forecaster != null) {
            busyness = _forecaster.forecast().getValue();
        }
        else if (_mode != OperatingMode.SNAPSHOT && _expAverage != null) {
            busyness = _expAverage.getAverage(busyness);
        }
        return busyness;
    }

}
