package org.yawlfoundation.yawl.logging;

/**
 * Author: adamsmj
 * Date: Oct 29, 2007
 * Time: 1:29:58 PM
 */
public class YLogManager {

    private static YLogManager _me ;

    public static YLogManager getInstance() {
        if (_me == null) _me = new YLogManager();
        return _me ;
    }


    public String getCaseEventIDsForSpec(String specID) {
        return null ;
    }

    public String getParentWorkItemEventsForCase(String caseEventID) {
        return null ;
    }

    public String getChildWorkItemEventsForParent(String parentEventID) {
        return null ;
    }

    public String getCaseEventsForSpec(String specID) {
        return null ;
    }

    public String getAllSpecIDs() {
        return null;
    }

    public String getAllCaseEventIDs() {
        return null;
    }

    public String getAllCaseEventIDs(String eventType) {
        if (eventType == null) return getAllCaseEventIDs() ;
        return null;
    }

    public String getParentWorkItemsForCaseID(String caseID) {
        return null ;
    }
}
