/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.datastore;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A workitem record hashmap with added persistence.
 *
 *  @author Michael Adams
 *  v0.1, 15/09/2007
 */

public class WorkItemCache extends ConcurrentHashMap<String, WorkItemRecord> {

    private Persister _persister;
    private static WorkItemCache _me ;
    private boolean _persistOn = false ;

    private WorkItemCache() {
        super();
        _me = this ;
    }


    public static WorkItemCache getInstance() {
        if (_me == null) _me = new WorkItemCache() ;
        return _me ;
    }

    public static WorkItemCache getInstance(boolean persist) {
        if (_me == null) _me = new WorkItemCache();
        _me.setPersist(persist);
        return _me ;
    }

    public void setPersist(boolean persist) {
        _persistOn = persist ;
        _persister = _persistOn ? Persister.getInstance() : null ;
    }

    public boolean isPersistOn() { return _persistOn ; }


    public WorkItemRecord add(WorkItemRecord wir) {
        return (wir != null) ? this.put(wir.getID(), wir) : null;
    }

    public WorkItemRecord remove(WorkItemRecord wir) {
        return this.remove(wir.getID());
    }

    public WorkItemRecord replace(WorkItemRecord oldWir, WorkItemRecord newWir) {
        if (oldWir != null) {
            this.remove(oldWir.getID());
            copyDocumentation(oldWir, newWir);
        }
        return add(newWir);
    }
    

    public void removeCase(String caseID) {
        Set<WorkItemRecord> clonedValues = new HashSet<WorkItemRecord>(this.values());
        for (WorkItemRecord wir : clonedValues) {
            if (wir.getRootCaseID().equals(caseID)) {
                this.remove(wir);
            }
        }
    }

    public WorkItemRecord update(WorkItemRecord wir) {
        return this.put(wir.getID(), wir) ;
    }


    public WorkItemRecord updateResourceStatus(WorkItemRecord wir, String status) {
        wir.setResourceStatus(status);
        return this.put(wir.getID(), wir);
    }

    public WorkItemRecord updateStatus(WorkItemRecord wir, String status) {
        wir.setStatus(status);
        return this.put(wir.getID(), wir);
    }

    public void restore() {
        if (_persistOn) {
            List wirList = _persister.select("WorkItemRecord") ;

            if (wirList != null) {
                for (Object o : wirList) {
                    WorkItemRecord wir = (WorkItemRecord) o ;
                    wir.restoreDataList();
                    wir.restoreAttributeTable();                    
                    super.put(wir.getID(), wir);
                }
            }
            _persister.commit();
        }
    }


    private void copyDocumentation(WorkItemRecord oldWir, WorkItemRecord newWir) {
        if ((newWir != null) && oldWir.isDocumentationChanged()) {
            newWir.setDocumentation(oldWir.getDocumentation());
            newWir.setDocumentationChanged(true);
        }
    }



    // OVERRIDES //

    public synchronized WorkItemRecord put(String id, WorkItemRecord wir) {
        if (_persistOn) {
            if (containsKey(id))
                _persister.update(wir) ;
            else
                _persister.insert(wir) ;
        }
        return super.put(id, wir);
    }

    public synchronized WorkItemRecord remove(String id) {
        if (containsKey(id)) {
           if (_persistOn) _persister.delete(get(id)) ;
           return super.remove(id);
        }
        else return null ;
    }

}
