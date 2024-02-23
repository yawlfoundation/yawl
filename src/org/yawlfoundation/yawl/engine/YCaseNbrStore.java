/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides for the persistence of the last allocated case id, and the generation
 * of new case ids.
 *
 * Author: Michael Adams
 * Date: 1/03/2008
 */

public class YCaseNbrStore {

    private int pkey = 1001 ;                             // primary key for persistence
    private AtomicInteger caseNbr;
    private boolean persisted = false ;                   // has this been persisted yet?
    private boolean persisting = false ;                  // is persistence on?
    private static YCaseNbrStore _instance ;
    private static final Logger log = LogManager.getLogger(YCaseNbrStore.class) ;

    protected YCaseNbrStore() {
        caseNbr = new AtomicInteger();
    }

    /** @return an instance of ths class */
    public static YCaseNbrStore getInstance() {
        if (_instance == null)
            _instance = new YCaseNbrStore();
        return _instance ;
    }


    // Getters & Setters //

    public int getCaseNbr() { return caseNbr.get(); }

    public void setCaseNbr(int nbr) { caseNbr.set(nbr); }


    public int getPkey() { return pkey; }

    public void setPkey(int key) { pkey = key; }


    public boolean isPersisted() { return persisted; }

    public void setPersisted(boolean bool) { persisted = bool; }


    public boolean isPersisting() { return persisting; }

    public void setPersisting(boolean persist) { persisting = persist; }

    public String toString() { return caseNbr.toString(); }


    /** @return the next available case number (as a String) */
    public String getNextCaseNbr(YPersistenceManager pmgr) {
        caseNbr.incrementAndGet();
        if (persisting) persistThis(pmgr) ;
        return caseNbr.toString();
    }


    /** persist the current case number */
    private void persistThis(YPersistenceManager pmgr) {
        try {
            if (persisted)
                updateThis(pmgr);
            else {
                storeThis(pmgr);
                persisted = true ;
            }
        }
        catch (YPersistenceException ype) {
            log.error("Could not persist case number.", ype) ;
        }
    }

    private void updateThis(YPersistenceManager pmgr) throws YPersistenceException {
        if (pmgr != null) {
            pmgr.updateObject(this);
        }
        else YEngine.getInstance().updateObject(this);
    }

    private void storeThis(YPersistenceManager pmgr) throws YPersistenceException {
        if (pmgr != null) {
            pmgr.storeObject(this);
        }
        else YEngine.getInstance().storeObject(this);
    }

}
