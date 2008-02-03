/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.logging;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Iterator;
import java.util.List;

/**
 * The server side of interface E. An API to retrieve data from the process event logs
 * and pass it back as XML.
 *
 * Create Date: 29/10/2007. Last Date: 12/12/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class YLogManager {

    private static YLogManager _me ;
    private YPersistenceManager _pmgr ;
    private Logger _log ;

    // some error messages
    private final String _exErrStr = "<failure>Unable to retrieve data.</failure>";
    private final String _pmErrStr = "<failure>Error connecting to database.</failure>";
    private final String _noRowsStr = "<failure>No rows returned.</failure>";


    // CONSTRUCTOR - called from getInstance() //

    private YLogManager() {
        _log = Logger.getLogger(this.getClass());
        if (YEngine.isPersisting()) {
            _pmgr = new YPersistenceManager(YEngine.getPMSessionFactory());
            try {
                 _pmgr.startTransactionalSession();
            }
            catch (YPersistenceException ype) {
                 _log.error("Could not initialise connection to log tables.", ype) ;
            }
        }
    }

    public static YLogManager getInstance() {
        if (_me == null) _me = new YLogManager();
        return _me ;
    }


    /*****************************************************************************/

    /**
     * @param specID the specification id to get the case eventids for
     * @return the set of all case ids for the specID passed
     */
    public String getCaseEventIDsForSpec(String specID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YCaseEvent",
                                       String.format("_specID='%s'", specID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<CaseEventIDs specID=\"%s\">", specID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YCaseEvent caseEvent = (YCaseEvent) itr.next() ;
                        xml.append(StringUtil.wrap(caseEvent.get_caseEventID(),
                                                                "caseEventID")) ;
                    }
                    xml.append("</CaseEventIDs>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /*****************************************************************************/

    /**
     *
     * @param caseEventID
     * @return
     */
    public String getParentWorkItemEventsForCase(String caseEventID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YParentWorkItemEvent",
                                       String.format("_caseEventID='%s'", caseEventID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<ParentWorkItemEvents caseEventID=\"%s\">",
                                                                    caseEventID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YParentWorkItemEvent pEvent = (YParentWorkItemEvent) itr.next() ;
                        xml.append(pEvent.toXML()) ;
                    }
                    xml.append("</ParentWorkItemEvents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }


    public String getChildWorkItemEventsForParent(String parentEventID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YChildWorkItemEvent",
                            String.format("_parentWorkItemEventID='%s'", parentEventID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<ChildWorkItemEvents parentWorkItemEventID=\"%s\">",
                                                                    parentEventID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YChildWorkItemEvent cEvent = (YChildWorkItemEvent) itr.next() ;
                        xml.append(cEvent.toXML()) ;
                    }
                    xml.append("</ChildWorkItemEvents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /*******************************************************************************/

    public String getCaseEventsForSpec(String specID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YCaseEvent",
                                       String.format("_specID='%s'", specID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<CaseEvents specID=\"%s\">", specID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YCaseEvent cEvent = (YCaseEvent) itr.next() ;
                        xml.append(cEvent.toXML()) ;
                    }
                    xml.append("</CaseEvents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /*****************************************************************************/

    public String getAllSpecIDs() {
        String result ;
        List rows ;
        String sql = "select distinct yce._specID from YCaseEvent as yce" ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.execQuery(sql);
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append("<SpecIDs>");
                    Iterator uItr = rows.iterator();
                    while (uItr.hasNext()) {
                        String specID = (String) uItr.next();
                        if (specID != null)
                            xml.append(StringUtil.wrap(specID, "specID"));
                    }
                    xml.append("</SpecIDs>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /***************************************************************************/

    public String getAllCaseEventIDs() {
        String result ;
        List rows ;
        String sql = "select yce._caseEventID from YCaseEvent as yce" ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.execQuery(sql);
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append("<CaseEventIDs>");
                    Iterator uItr = rows.iterator();
                    while (uItr.hasNext()) {
                        xml.append(StringUtil.wrap((String) uItr.next(), "caseEventID"));
                    }
                    xml.append("</CaseEventIDs>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

   /********************************************************************************/

    public String getAllCaseEventIDs(String eventName) {
        if (eventName == null) return getAllCaseEventIDs() ;

        String result ;
        List rows ;
        String sql = "select yce._caseEventID from YCaseEvent as yce " +
                     String.format("where yce._eventName='%s'", eventName);
        if (_pmgr != null) {
            try {
                rows = _pmgr.execQuery(sql);
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append("<CaseEventIDs>");
                    Iterator uItr = rows.iterator();
                    while (uItr.hasNext()) {
                        xml.append(StringUtil.wrap((String) uItr.next(), "caseEventID"));
                    }
                    xml.append("</CaseEventIDs>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }


    /******************************************************************************/
    
    public String getParentWorkItemEventsForCaseID(String caseID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YParentWorkItemEvent",
                                       String.format("_caseID='%s'", caseID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<ParentWorkItemEvents caseID=\"%s\">",
                                                                    caseID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YParentWorkItemEvent pEvent = (YParentWorkItemEvent) itr.next() ;
                        xml.append(pEvent.toXML()) ;
                    }
                    xml.append("</ParentWorkItemEvents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    public String getChildWorkItemData(String childEventID) {
        String result ;
        List rows ;
        if (_pmgr != null) {
            try {
                rows = _pmgr.getObjectsForClassWhere("YWorkItemDataEvent",
                            String.format("_childWorkItemEventID='%s'", childEventID)) ;
                if (rows != null) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<WorkItemDataEvents childWorkItemEventID=\"%s\">",
                                                                    childEventID));
                    Iterator itr = rows.iterator();
                    while (itr.hasNext()) {
                        YWorkItemDataEvent dEvent = (YWorkItemDataEvent) itr.next() ;
                        xml.append(dEvent.toXML()) ;
                    }
                    xml.append("</WorkItemDataEvents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }    

}
