package org.yawlfoundation.yawl.stateless.listener.event;

import org.jdom2.Document;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YExceptionEvent extends YEvent {
    
    public YExceptionEvent(YEventType eType, YIdentifier id) {
        super(eType, id);
    }

    public YExceptionEvent(YEventType eType, YWorkItem item) {
        super(eType, item.getCaseID());
        setWorkItem(item);
    }

    public YExceptionEvent(YEventType eType, YWorkItem item, Document dataDoc) {
        super(eType, item.getCaseID());
        setWorkItem(item);
        setData(dataDoc);
    }

    public YExceptionEvent(YEventType eType, YSpecificationID specID, YIdentifier id,
                           Document dataDoc) {
        super(eType, id);
        setSpecID(specID);
        setData(dataDoc);
    }
}
