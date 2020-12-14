package org.yawlfoundation.yawl.stateless.listener.event;

import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YTimerEvent extends YEvent {

    public YTimerEvent(YEventType eType, YIdentifier caseID) {
        super(eType, caseID);
    }

}
