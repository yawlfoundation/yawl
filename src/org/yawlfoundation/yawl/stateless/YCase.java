package org.yawlfoundation.yawl.stateless;

import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Michael Adams
 * @date 21/8/20
 */
public class YCase {

    private final long _startTime;
    private long _lastTime;
    private final YSpecification _specification;
    private final Set<YNetRunner> _runners;



    public YCase(YSpecification specification, YNetRunner runner) {
        _startTime = System.nanoTime();
        _runners = new HashSet<>();
        addRunner(runner);
        _specification = specification;
    }


    public long getStartTime() { return _startTime; }

    public long getLastChangeTime() { return _lastTime; }

    public YSpecification getSpecification() { return _specification; }
    

    public boolean addRunner(YNetRunner runner) {
        return _runners.add(runner);
    }


    public YNetRunner getRunner(YIdentifier caseID) throws YStateException {
        for (YNetRunner runner : _runners) {
            if (runner.getCaseID().equals(caseID)) {
                return runner;
            }
        }
        throw new YStateException("No runner found for case " + caseID);
    }


    public boolean removeRunner(YIdentifier caseID) throws YStateException {
        YNetRunner runner = getRunner(caseID);
        return runner != null && _runners.remove(runner);
    }
}
