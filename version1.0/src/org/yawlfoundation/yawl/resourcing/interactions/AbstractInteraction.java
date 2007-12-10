package org.yawlfoundation.yawl.resourcing.interactions;

import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 *  Base class for the Offer, Allocate and Start interaction points.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 02/08/2007
 */

public abstract class AbstractInteraction {

    // possible initiator values
    public static final int USER_INITIATED = 0;
    public static final int SYSTEM_INITIATED = 1;

    protected int _initiator = USER_INITIATED;                         // by default

    protected String _ownerTaskID ;                     // which task owns this int point


    // CONSTRUCTORS //

    public AbstractInteraction() {}                                    // for reflection

    public AbstractInteraction(String ownerTaskID) {
        _ownerTaskID = ownerTaskID ;
    }

    public AbstractInteraction(int initiator) {

        // default to USER if initiator value anything other than SYSTEM
        if (initiator == SYSTEM_INITIATED) _initiator = initiator ;
    }


    // SETTER & GETTERS //

    public String getOwnerTaskID() { return _ownerTaskID; }

    public void setOwnerTaskID(String ownerTaskID) { _ownerTaskID = ownerTaskID; }


    public boolean setInitiator(int i) {
        if ((i == USER_INITIATED) || (i == SYSTEM_INITIATED)) {
            _initiator = i ;
            return true ;
        }
        else return false;                                     // invalid value passed

    }


    public int getInitiator() { return _initiator ; }


    public String getInitiatorString() {
        if (_initiator == SYSTEM_INITIATED) return "system" ;
        else return "user" ;
    }


    public void parseInitiator(Element e, Namespace nsYawl) {
        String init = e.getChildText("initiator", nsYawl) ;
        if (init != null) {
            if (init.equals("system"))
                _initiator = SYSTEM_INITIATED;
        }    
    }

    public Map parseParams(Element e, Namespace nsYawl) {
        HashMap result = new HashMap() ;
        Element eParams = e.getChild("params", nsYawl);
        if (eParams != null) {
            List params = eParams.getChildren("param", nsYawl) ;
            for (Object o : params) {
                Element eParam = (Element) o ;
                result.put(eParam.getChildText("key", nsYawl),
                           eParam.getChildText("value", nsYawl));
             }
        }
        return result ;
    }    
}
