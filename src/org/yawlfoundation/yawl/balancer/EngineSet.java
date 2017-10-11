package org.yawlfoundation.yawl.balancer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.output.ArffOutputter;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * @author Michael Adams
 * @date 22/9/17
 */
public class EngineSet {

    private final Set<EngineInstance> _set;
    private final Random _random;
    private final Logger _log;
    private final ArffOutputter _arffWriter;

    private EngineInstance _authenticator;
    private int _lastCaseNbr = 0;


    public EngineSet() {
        _set = new HashSet<EngineInstance>();
        _random = new Random();
        _log = LogManager.getFormatterLogger(this.getClass());
        _arffWriter = new ArffOutputter();
    }


    public void initialize() {
        boolean activateAll = Config.getBusynessLimit() <= 0;
        List<String> locations = Config.getLocations();
        for (String location : locations) {
            String[] parts = location.split(":");
            String host = parts[0];
            int port = StringUtil.strToInt(parts[1], -1);
            if (port > 0) {
                EngineInstance instance = new EngineInstance(host, port);
                instance.setArffWriter(_arffWriter);
                if (activateAll) instance.setActive(true);
                _set.add(instance);
            }
        }
    }


    public Set<EngineInstance> getAll() { return _set; }


    public Set<EngineInstance> getAllActive() {
        Set<EngineInstance> activeSet = new HashSet<EngineInstance>();
        for (EngineInstance instance : _set) {
            if (instance.isActive()) {
                activeSet.add(instance);
            }
        }
        return activeSet;
    }


    public EngineInstance promoteAuthenticator() {
        _authenticator = getRandomInstance();
        if (_authenticator != null) {
            _authenticator.setAuthenticator(true);
            _authenticator.setActive(true);
        }
        return _authenticator;
    }


    public EngineInstance getAuthenticator() {
        return _authenticator;
    }

    
    public void waitUntilAllInitialized() {
        boolean allInitialized;
        int countdown = Config.getEngineInitWaitMSecs();
        do {
            allInitialized = areAllInitialized();
            if (!allInitialized) {
                try {
                    Thread.sleep(5000);
                    countdown -= 5000;
                }
                catch (InterruptedException ie) {
                    //
                }
            }
        } while (!allInitialized && countdown > 0);

        removeUninitialized();
        promoteAuthenticator();
    }


    private boolean areAllInitialized() {
        for (EngineInstance instance : _set) {
            if (!instance.isInitialized()) {
                return false;
            }
        }
        return true;
    }


    public String getNextCaseNbr() {
        if (_lastCaseNbr == 0) {
            for (EngineInstance instance : _set) {
                 _lastCaseNbr = Math.max(_lastCaseNbr, instance.getLastCaseNbr());
            }
        }
        return String.valueOf(++_lastCaseNbr);
    }


    public EngineInstance getEngineForCase(String caseID) {
        for (EngineInstance instance : _set) {
            if (instance.hasCase(caseID)) {
                return instance;
            }
        }
        return null;
    }


    public EngineInstance getIdlestEngine() {
        if (Config.getOperatingMode() == OperatingMode.RANDOM) {
            return getRandomActiveInstance();
        }
        EngineInstance idlest = null;
        double lowestScore = Double.MAX_VALUE;
        for (EngineInstance instance : getAllActive()) {
            try {
                double busyness = instance.getBusyness(false);
                _log.info("Busyness: %s %.3f",
                        instance.getName(), busyness);
                if (idlest == null || busyness < lowestScore) {
                    idlest = instance;
                    lowestScore = busyness;
                }
            }
            catch (Exception e) {
                _log.warn("Unable to gather statistics from engine %s - %s",
                        instance.getName(), e.getMessage());
            }
        }
        return checkBusynessLimit(idlest, lowestScore);
    }


    public EngineInstance getRandomInstance(Set<EngineInstance> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        int count = _random.nextInt(set.size());
        Iterator<EngineInstance> iter = set.iterator();
        for (int i = 0; i < count; i++) {
            iter.next();
        }
        return iter.next();
    }


    public EngineInstance getRandomInstance() {
        return getRandomInstance(_set);
    }


    public EngineInstance getRandomActiveInstance() {
        return getRandomInstance(getAllActive());
    }

    
    public void closeAll() {
        for (EngineInstance instance : _set) {
            instance.close();
        }
    }


    private void removeUninitialized() {
       Set<EngineInstance> uninitialized = new HashSet<EngineInstance>();
        for (EngineInstance instance : _set) {
            if (!instance.isInitialized()) {
                uninitialized.add(instance);
                _log.warn("Engine at port %d has not initialised, " +
                        "and so has been removed from the pool.", instance.getPort());
            }
        }
        _set.removeAll(uninitialized);
    }


    private EngineInstance checkBusynessLimit(EngineInstance idlest, double score) {
        double limit = Config.getBusynessLimit();
        if (limit <= 0 || score <= limit) {
            return idlest;
        }

        // score exceeds positive limit
        EngineInstance instance = getInactiveInstance();
        if (instance != null) {
            instance.setActive(true);
            _log.info("Activated idle engine at %s", instance.getName());
            return instance;
        }
        _log.warn("Busyness limit exceeded but no more idle engines available.");
        return idlest;
    }


    private EngineInstance getInactiveInstance() {
        for (EngineInstance instance : _set) {
            if (! instance.isActive()) {
                return instance;
            }
        }
        return null;
    }

}
