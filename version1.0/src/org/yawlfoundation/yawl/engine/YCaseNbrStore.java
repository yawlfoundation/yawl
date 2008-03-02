package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2008
 */
public class YCaseNbrStore {

    private int pkey = 1001 ;                             // primary key for persistence
    private int caseNbr = 0 ;
    private boolean persisted = false ;
    private static YCaseNbrStore _instance ;

    protected YCaseNbrStore() {}

    public static YCaseNbrStore getInstance() {
        if (_instance == null)
            _instance = new YCaseNbrStore();
        return _instance ;
    }

    public int getCaseNbr() { return caseNbr; }

    public void setCaseNbr(int nbr) { caseNbr = nbr; }


    public int getPkey() { return pkey; }

    public void setPkey(int key) { pkey = key; }


    public boolean isPersisted() { return persisted; }

    public void setPersisted(boolean bool) { persisted = bool; }
    

    public String getNextCaseNbr() {
        String result = String.valueOf(++caseNbr);
        persistThis() ;
        return result;
    }

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
            // do something
        }
    }
}
