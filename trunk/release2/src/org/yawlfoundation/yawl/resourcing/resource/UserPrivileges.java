/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

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
//        setCanViewAllOffered(canViewAllOffered) ;
//        setCanViewAllAllocated(canViewAllAllocated) ;
//        setCanViewAllExecuting(canViewAllExecuting) ;
        setCanViewTeamItems(canViewTeamItems) ;
        setCanViewOrgGroupItems(canViewOrgGroupItems);
        setCanChainExecution(canChainExecution) ;
        setCanManageCases(canManageCases) ;
    }

    public UserPrivileges clone() {
        return new UserPrivileges(_participantID,
                                  canChooseItemToStart, canStartConcurrent,
                                  canReorder, canViewAllOffered,
                                  canViewAllAllocated, canViewAllExecuting,
                                  canViewTeamItems, canViewOrgGroupItems,
                                  canChainExecution, canManageCases);
    }

    // copies values from up to this (does NOT change id)   
    public void merge(UserPrivileges up) {
        setCanChooseItemToStart(up.canChooseItemToStart()) ;
        setCanStartConcurrent(up.canStartConcurrent()) ;
        setCanReorder(up.canReorder()) ;
//        setCanViewAllOffered(up.canViewAllOffered()) ;
//        setCanViewAllAllocated(up.canViewAllAllocated()) ;
//        setCanViewAllExecuting(up.canViewAllExecuting()) ;
        setCanViewTeamItems(up.canViewTeamItems()) ;
        setCanViewOrgGroupItems(up.canViewOrgGroupItems());
        setCanChainExecution(up.canChainExecution()) ;
        setCanManageCases(up.canManageCases()) ;
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

//    public boolean canViewAllOffered() {
//        return carteblanche || canViewAllOffered;
//    }
//
//    public boolean canViewAllAllocated() {
//        return carteblanche || canViewAllAllocated;
//    }
//
//    public boolean canViewAllExecuting() {
//        return carteblanche || canViewAllExecuting;
//    }

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

//    public void setCanViewAllOffered(boolean canViewAllOffered) {
//        this.canViewAllOffered = canViewAllOffered;
//    }
//
//    public void setCanViewAllAllocated(boolean canViewAllAllocated) {
//        this.canViewAllAllocated = canViewAllAllocated;
//    }
//
//    public void setCanViewAllExecuting(boolean canViewAllExecuting) {
//        this.canViewAllExecuting = canViewAllExecuting;
//    }

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


    public String toXML() {
        StringBuilder xml = new StringBuilder("<UserPrivileges") ;
        xml.append(" participantid=\"").append(_participantID).append("\">")
           .append(wrapPrivilege(canChooseItemToStart(), "canChooseItemToStart"))
           .append(wrapPrivilege(canStartConcurrent(), "canStartConcurrent"))
           .append(wrapPrivilege(canReorder(), "canReorder"))
//           .append(wrapPrivilege(canViewAllOffered(), "canViewAllOffered"))
//           .append(wrapPrivilege(canViewAllAllocated(), "canViewAllAllocated"))
//           .append(wrapPrivilege(canViewAllExecuting(), "canViewAllExecuting"))
           .append(wrapPrivilege(canViewTeamItems(), "canViewTeamItems"))
           .append(wrapPrivilege(canViewOrgGroupItems(), "canViewOrgGroupItems"))
           .append(wrapPrivilege(canChainExecution(), "canChainExecution"))
           .append(wrapPrivilege(canManageCases(), "canManageCases"))
           .append("</UserPrivileges>");

        return xml.toString();
    }

    public void fromXML(String xml) {
        Element e = JDOMUtil.stringToElement(xml);
        if (e != null) {
            setID(e.getAttributeValue("participantid"));
            setCanChooseItemToStart(e.getChildText("canChooseItemToStart").equals("true"));
            setCanStartConcurrent(e.getChildText("canStartConcurrent").equals("true"));
            setCanReorder(e.getChildText("canReorder").equals("true"));
//            setCanViewAllOffered(e.getChildText("canViewAllOffered").equals("true"));
//            setCanViewAllAllocated(e.getChildText("canViewAllAllocated").equals("true"));
//            setCanViewAllExecuting(e.getChildText("canViewAllExecuting").equals("true"));
            setCanViewTeamItems(e.getChildText("canViewTeamItems").equals("true"));
            setCanViewOrgGroupItems(e.getChildText("canViewOrgGroupItems").equals("true"));
            setCanChainExecution(e.getChildText("canChainExecution").equals("true"));
            setCanManageCases(e.getChildText("canManageCases").equals("true"));
        }
    }

    private String wrapPrivilege(boolean value, String name) {
        return StringUtil.wrap(String.valueOf(value), name);
    }
}

