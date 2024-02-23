/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.balancer.instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.balancer.OperatingMode;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.monitor.Monitor;
import org.yawlfoundation.yawl.balancer.output.ArffOutputter;
import org.yawlfoundation.yawl.balancer.output.CombinedBusynessOutputter;
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
    private final Monitor _monitor;
    
    private EngineInstance _authenticator;
    private int _lastCaseNbr = 0;
    private int _activeCount = 0;


    public EngineSet(Monitor monitor) {
        _set = new HashSet<EngineInstance>();
        _random = new Random();
        _log = LogManager.getFormatterLogger(this.getClass());
        _arffWriter = new ArffOutputter();
        _monitor = monitor;
    }


    public void initialize() {
        CombinedBusynessOutputter combinedOutputter = new CombinedBusynessOutputter();
        boolean activateAll = Config.getBusynessLimit() <= 0;
        List<String> locations = Config.getLocations();
        for (String location : locations) {
            String[] parts = location.split(":");
            String host = parts[0];
            int port = StringUtil.strToInt(parts[1], -1);
            if (port > 0) {
                EngineInstance instance = new EngineInstance(host, port);
         //       instance.setArffWriter(_arffWriter);
                instance.setCombinedOutputter(combinedOutputter);
                instance.addBusynessListener(_monitor);
                if (activateAll) {
                    activate(instance);
                }
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


    public int getActiveCount() { return _activeCount; }


    public EngineInstance promoteAuthenticator() {
        _authenticator = getRandomInstance();
        if (_authenticator != null) {
            _authenticator.setAuthenticator(true);
            activate(_authenticator);
        }
        return _authenticator;
    }


    public EngineInstance getAuthenticator() {
        if (_authenticator == null) {
            promoteAuthenticator();
        }
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


    private boolean activate(EngineInstance instance) {
        return setActive(instance, true);
    }

    
    public boolean deactivate(EngineInstance instance) {
        return setActive(instance, false);
    }


    private boolean setActive(EngineInstance instance, boolean activate) {
         if (activate != instance.isActive()) {
             instance.setActive(activate);
             _activeCount += activate ? 1 : -1;
             return true;
         }
         return false;
     }

}
