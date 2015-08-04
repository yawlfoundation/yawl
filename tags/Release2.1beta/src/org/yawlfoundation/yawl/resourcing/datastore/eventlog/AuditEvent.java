package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 22/10/2009
 */
public class AuditEvent extends BaseEvent {

    private String _userid ;

    public AuditEvent() { }

    public AuditEvent(String userid, EventLogger.audit event) {
        super(event.name());
        _userid = userid;
    }

    public String get_userid() { return _userid; }

    public void set_userid(String userid) { _userid = userid; }

    public String toXML() {
        StringBuilder xml = new StringBuilder(String.format("<event key=\"%d\">", _id));
        xml.append(StringUtil.wrap(_userid, "userid"))
           .append(super.toXML())
           .append("</event>") ;
        return xml.toString();
    }

    
}
