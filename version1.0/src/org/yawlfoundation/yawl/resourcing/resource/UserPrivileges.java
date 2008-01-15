/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import java.io.Serializable;

/**
 * Simple repository for participant privileges
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class UserPrivileges implements Serializable {

    private String _participantID ;

    private boolean canChooseItemToStart ;
    private boolean canStartConcurrent ;
    private boolean canReorder ;
    private boolean canViewAllOffered ;
    private boolean canViewAllAllocated ;
    private boolean canViewAllExecuting ;
    private boolean canViewTeamItems ;
    private boolean canViewOrgGroupItems ;
    private boolean canChainExecution ;
    private boolean canManageCases ;

    private boolean carteblanche ;                // if true, overrides all privileges


    public UserPrivileges() {}                             // for Hibernate persistence

    public UserPrivileges(String pid) { _participantID = pid ; }

    public UserPrivileges(String pid,
                          boolean canChooseItemToStart, boolean canStartConcurrent,
                          boolean canReorder, boolean canViewAllOffered,
                          boolean canViewAllAllocated, boolean canViewAllExecuting,
                          boolean canViewTeamItems, boolean canViewOrgGroupItems,
                          boolean canChainExecution, boolean canManageCases) {

        setID(pid);
        setCanChooseItemToStart(canChooseItemToStart) ;
        setCanStartConcurrent(canStartConcurrent) ;
        setCanReorder(canReorder) ;
        setCanViewAllOffered(canViewAllOffered) ;
        setCanViewAllAllocated(canViewAllAllocated) ;
        setCanViewAllExecuting(canViewAllExecuting) ;
        setCanViewTeamItems(canViewTeamItems) ;
        setCanViewOrgGroupItems(canViewOrgGroupItems);
        setCanChainExecution(canChainExecution) ;
        setCanManageCases(canManageCases) ;
    }

    public void allowAll() { carteblanche = true ; }

    public void disallowAll() { carteblanche = false ; }


    public String getID() {
        return _participantID;
    }

    public void setID(String pid) {
        _participantID = pid;
    }

    public boolean canChooseItemToStart() {
        return carteblanche || canChooseItemToStart;
    }

    public boolean canStartConcurrent() {
        return carteblanche || canStartConcurrent;
    }

    public boolean canReorder() {
        return carteblanche || canReorder;
    }

    public boolean canViewAllOffered() {
        return carteblanche || canViewAllOffered;
    }

    public boolean canViewAllAllocated() {
        return carteblanche || canViewAllAllocated;
    }

    public boolean canViewAllExecuting() {
        return carteblanche || canViewAllExecuting;
    }

    public boolean canChainExecution() {
        return carteblanche || canChainExecution;
    }

    public boolean canViewOrgGroupItems() {
        return carteblanche || canViewOrgGroupItems;
    }

    public boolean canViewTeamItems() {
        return carteblanche || canViewTeamItems;
    }

    public boolean canManageCases() {
        return carteblanche || canManageCases;
    }

    public void setCanChooseItemToStart(boolean canChooseItemToStart) {
        this.canChooseItemToStart = canChooseItemToStart;
    }

    public void setCanStartConcurrent(boolean canStartConcurrent) {
        this.canStartConcurrent = canStartConcurrent;
    }

    public void setCanReorder(boolean canReorder) {
        this.canReorder = canReorder;
    }

    public void setCanViewAllOffered(boolean canViewAllOffered) {
        this.canViewAllOffered = canViewAllOffered;
    }

    public void setCanViewAllAllocated(boolean canViewAllAllocated) {
        this.canViewAllAllocated = canViewAllAllocated;
    }

    public void setCanViewAllExecuting(boolean canViewAllExecuting) {
        this.canViewAllExecuting = canViewAllExecuting;
    }

    public void setCanViewTeamItems(boolean canViewTeamItems) {
        this.canViewTeamItems = canViewTeamItems;
    }

    public void setCanViewOrgGroupItems(boolean canViewOrgGroupItems) {
        this.canViewOrgGroupItems = canViewOrgGroupItems;
    }

    public void setCanChainExecution(boolean canChainExecution) {
        this.canChainExecution = canChainExecution;
    }

    public void setCanManageCases(boolean canManageCases) {
         this.canManageCases = canManageCases;
     }

}
