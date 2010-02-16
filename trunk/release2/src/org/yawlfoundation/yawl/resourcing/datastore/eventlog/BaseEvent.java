package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Michael Adams
 * Creation Date: 4/02/2010
 */
public abstract class BaseEvent {

    protected long _id ;                                           // hibernate PK
    protected String _event ;
    protected long _timeStamp ;

    public BaseEvent() {}

    public BaseEvent(String event)  {
        _event = event;
        _timeStamp = System.currentTimeMillis();
    }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_event() { return _event; }

    public void set_event(String event) {_event = event; }

    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) {_timeStamp = timeStamp; }

    public String getTimeStampString() {
        return new SimpleDateFormat("yyyy-MM-dd H:mm:ss").format(new Date(_timeStamp));
    }

    
    public String toXML() {
        return StringUtil.wrap(_event, "eventtype") +
               StringUtil.wrap(String.valueOf(_timeStamp), "timestamp");        
    }
    
}
