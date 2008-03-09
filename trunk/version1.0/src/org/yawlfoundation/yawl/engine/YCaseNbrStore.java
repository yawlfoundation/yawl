/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Provides for the persistence of the last allocated case ids, and the generation
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
    private Logger log = Logger.getLogger(this.getClass()) ;

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
