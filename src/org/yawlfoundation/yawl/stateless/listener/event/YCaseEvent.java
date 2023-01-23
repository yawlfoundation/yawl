package org.yawlfoundation.yawl.stateless.listener.event;

import org.jdom2.Document;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YCaseEvent extends YEvent {

    private Set<YTask> _deadlockedTasks;
    private YNetRunner _runner;


    public YCaseEvent(YEventType eType, YIdentifier id) {
        super(eType, id);
    }

    public YCaseEvent(YEventType eType, YNetRunner runner) {
        this(eType, runner.getCaseID());
        setRunner(runner);
    }

    public YCaseEvent(YEventType eType, YNetRunner runner, YSpecification spec) {
        this(eType, runner, spec.getSpecificationID());
    }

    public YCaseEvent(YEventType eType, YNetRunner runner, YSpecificationID specID) {
        this(eType, runner);
        setSpecID(specID);
    }

    public YCaseEvent(YEventType eType, YIdentifier id, Document dataDoc) {
        super(eType, id);
        setData(dataDoc);
     }

    public YCaseEvent(YEventType eType, YIdentifier id, Set<YTask> deadlockedTasks) {
        super(eType, id);
        setDeadlockedTasks(deadlockedTasks);
     }


    private void setRunner(YNetRunner runner) { _runner = runner; }

    public YNetRunner getRunner() { return _runner; }


    private void setDeadlockedTasks(Set<YTask> tasks) { _deadlockedTasks = tasks; }

    public Set<YTask> getDeadlockedTasks() { return _deadlockedTasks; }

}
