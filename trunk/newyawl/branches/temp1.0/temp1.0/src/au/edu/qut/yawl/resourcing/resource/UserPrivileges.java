/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.resource;

/**
 * Simple repository for participant privileges
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class UserPrivileges {

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


    public UserPrivileges() {}                             // for Hibernate persistence

    public UserPrivileges(String pid) { _participantID = pid ; }

    public UserPrivileges(String pid,
                          boolean canChooseItemToStart, boolean canStartConcurrent,
                          boolean canReorder, boolean canViewAllOffered,
                          boolean canViewAllAllocated, boolean canViewAllExecuting,
                          boolean canViewTeamItems, boolean canViewOrgGroupItems,
                          boolean canChainExecution) {

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
    }


    public String getID() {
        return _participantID;
    }

    public void setID(String pid) {
        _participantID = pid;
    }

    public boolean canChooseItemToStart() {
        return canChooseItemToStart;
    }

    public boolean canStartConcurrent() {
        return canStartConcurrent;
    }

    public boolean canReorder() {
        return canReorder;
    }

    public boolean canViewAllOffered() {
        return canViewAllOffered;
    }

    public boolean canViewAllAllocated() {
        return canViewAllAllocated;
    }

    public boolean canViewAllExecuting() {
        return canViewAllExecuting;
    }

    public boolean canChainExecution() {
        return canChainExecution;
    }

    public boolean canViewOrgGroupItems() {
        return canViewOrgGroupItems;
    }

    public boolean canViewTeamItems() {
        return canViewTeamItems;
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

}
