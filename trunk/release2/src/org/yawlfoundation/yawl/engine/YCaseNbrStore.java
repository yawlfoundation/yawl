/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Provides for the persistence of the last allocated case id, and the generation
 * of new case ids.
 *
 * Author: Michael Adams
 * Date: 1/03/2008
 */

public class YCaseNbrStore {

    private int pkey = 1001 ;                             // primary key for persistence
    private int caseNbr = 0 ;                             // initial default
    private boolean persisted = false ;                   // has this been persisted yet?
    private boolean persisting = false ;                  // is persistence on?
    private static YCaseNbrStore _instance ;
    private static final Logger log = Logger.getLogger(YCaseNbrStore.class) ;

    protected YCaseNbrStore() {}

    /** @return an instance of ths class */
    public static YCaseNbrStore getInstance() {
        if (_instance == null)
            _instance = new YCaseNbrStore();
        return _instance ;
    }


    // Getters & Setters //

    public int getCaseNbr() { return caseNbr; }

    public void setCaseNbr(int nbr) { caseNbr = nbr; }


    public int getPkey() { return pkey; }

    public void setPkey(int key) { pkey = key; }


    public boolean isPersisted() { return persisted; }

    public void setPersisted(boolean bool) { persisted = bool; }


    public boolean isPersisting() { return persisting; }

    public void setPersisting(boolean persist) { persisting = persist; }


    /** @return the next available case number (as a String) */
    public String getNextCaseNbr() {
        String result = String.valueOf(++caseNbr);
        if (persisting) persistThis() ;
        return result;
    }


    /** persist the current case number */
    private void persistThis() {
        try {
            if (persisted)
                YEngine.getInstance().updateObject(this);
            else {
                YEngine.getInstance().storeObject(this);
                persisted = true ;
            }
        }
        catch (YPersistenceException ype) {
            log.error("Could not persist case number.", ype) ;
        }
    }
}
