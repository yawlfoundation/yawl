package org.yawlfoundation.yawl.stateless.listener.event;

import org.jdom2.Document;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;

import java.time.Instant;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public abstract class YEvent {

    private final Instant _timeStamp;
    private final YIdentifier _caseID;
    private final YEventType _eType;
    private YSpecificationID _specID;
    private YWorkItem _item;
    private Document _dataDoc;

    protected YEvent(YEventType eType, YIdentifier caseID) {
        _timeStamp = Instant.now();
        _eType = eType;
        _caseID = caseID;
    }


    public Instant getTimeStamp() { return _timeStamp; }

    public YEventType getEventType() { return _eType; }

    public YIdentifier getCaseID() { return _caseID; }


    public void setSpecID(YSpecificationID specID) { _specID = specID; }

    public YSpecificationID getSpecID() { return _specID; }


    public void setWorkItem(YWorkItem item) { _item = item; }

    public YWorkItem getWorkItem() { return _item; }


    public void setData(Document doc) { _dataDoc = doc; }

    public Document getData() { return _dataDoc; }




}
