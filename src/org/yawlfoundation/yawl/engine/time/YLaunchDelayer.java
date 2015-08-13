/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.time;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.authentication.YSession;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogDataItemList;

import javax.xml.datatype.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

/**
 * A timer to delay the launching of a process instance.
 *
 * Author: Michael Adams
 * Creation Date: 09/02/2012
 */

public class YLaunchDelayer implements YTimedObject {

    private String _ownerID;
    private long _endTime ;

    private YSpecificationID _specID;
    private String _caseParams;
    private URI _completionObserver;
    private String _caseID;
    private YLogDataItemList _logData;
    private String _serviceURI;
    private boolean _persisting ;


    public YLaunchDelayer() {}                                   // for hibernate

    private YLaunchDelayer(YSpecificationID specID, String caseParams,
                           URI completionObserver, String caseID,
                           YLogDataItemList logData, String serviceHandle,
                           boolean persisting) {
        _specID = specID;
        _caseParams = caseParams;
        _completionObserver = completionObserver;
        _caseID = caseID;
        _logData = logData;
        _serviceURI = handleToURI(serviceHandle);
        _persisting = persisting;
        _ownerID = UUID.randomUUID().toString();
    }

    public YLaunchDelayer(YSpecificationID specID, String caseParams,
                          URI completionObserver, String caseID,
                          YLogDataItemList logData, String serviceHandle,
                          long msec, boolean persisting) {
        this(specID, caseParams, completionObserver, caseID, logData, serviceHandle, persisting);
        _endTime = YTimer.getInstance().schedule(this, msec) ;
        persistThis(true);
    }


    public YLaunchDelayer(YSpecificationID specID, String caseParams,
                              URI completionObserver, String caseID,
                              YLogDataItemList logData, String serviceHandle,
                              Date expiryTime, boolean persisting) {
        this(specID, caseParams, completionObserver, caseID, logData, serviceHandle, persisting);
        _endTime = YTimer.getInstance().schedule(this, expiryTime) ;
        persistThis(true);
    }


    public YLaunchDelayer(YSpecificationID specID, String caseParams,
                          URI completionObserver, String caseID,
                          YLogDataItemList logData, String serviceHandle,
                          Duration duration, boolean persisting) {
        this(specID, caseParams, completionObserver, caseID, logData, serviceHandle, persisting);
        _endTime = YTimer.getInstance().schedule(this, duration) ;
        persistThis(true);
    }


    public YLaunchDelayer(YSpecificationID specID, String caseParams,
                          URI completionObserver, String caseID,
                          YLogDataItemList logData, String serviceHandle,
                          long units, YTimer.TimeUnit interval,
                          boolean persisting) {
        this(specID, caseParams, completionObserver, caseID, logData, serviceHandle, persisting);
        _endTime = YTimer.getInstance().schedule(this, units, interval);
        persistThis(true);
    }


    public String getOwnerID() { return _ownerID; }

    public void setOwnerID(String id) { _ownerID = id; }

    public long getEndTime() { return _endTime; }

    public void setEndTime(long time) { _endTime = time; }

    public void setPersisting(boolean persist) { _persisting = persist; }
    

    private void persistThis(boolean insert) {
        if (_persisting) {
            YPersistenceManager pmgr = YEngine.getPersistenceManager();
            if (pmgr != null) {
                try {
                    boolean localTransaction = pmgr.startTransaction();
                    if (insert) pmgr.storeObjectFromExternal(this);
                    else pmgr.deleteObjectFromExternal(this);
                    if (localTransaction) pmgr.commit();
                }
                catch (YPersistenceException ype) {
                    // handle exc.
                }
            }    
        }
    }


    public boolean equals(Object other) {
        return (other instanceof YLaunchDelayer) &&
                ((getOwnerID() != null) ?
                 getOwnerID().equals(((YLaunchDelayer) other).getOwnerID()) :
                 super.equals(other));
    }

    public int hashCode() {
        return (getOwnerID() != null) ? getOwnerID().hashCode() : super.hashCode();
    }

            
    public void handleTimerExpiry() {
        try {
            YEngine.getInstance().launchCase(_specID, _caseParams, _completionObserver,
                _caseID, _logData, _serviceURI, true);
        }
        catch (YAWLException ye) {
            LogManager.getLogger(YLaunchDelayer.class).error(
                    "Unable to launch delayed instance of " + _specID.toString(), ye);
        }
        persistThis(false) ;                                 // unpersist this timer
    }


    // unpersist this timer when the workitem is cancelled
    public void cancel() {
        persistThis(false) ;
    }
    
    
    private String handleToURI(String handle) {
        if (handle != null) {
            YSession session = YEngine.getInstance().getSessionCache().getSession(handle);
            return session != null ? session.getURI() : null;
        }
        return null;
    }
    
    
    //*** hibernate ***//
    
    private String get_completionObserver() {
        return _completionObserver != null ? _completionObserver.toString() : null;
    }
    
    private void set_completionObserver(String uriStr) {
        if (uriStr != null) {
            try {
                _completionObserver = new URI(uriStr);
            }
            catch (URISyntaxException use) {
                _completionObserver = null;
            }
        }
    }
    
    private String get_logData() { 
        return _logData != null ? _logData.toXML() : null; 
    }
    
    private void set_logData(String data) {
        if (data != null) {
            _logData = new YLogDataItemList(data);
        }
    }

}
