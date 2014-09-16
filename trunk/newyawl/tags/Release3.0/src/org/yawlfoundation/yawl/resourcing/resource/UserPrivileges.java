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

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom2.Element;
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

    private static final int PRIV_COUNT = 8;

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

    public UserPrivileges(Element e) {
        reconstitute(e);
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
    public void setValues(UserPrivileges up) {
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
        reconstitute(JDOMUtil.stringToElement(xml));
    }

    public void reconstitute(Element e) {
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


    public String getPrivilegesAsBits() {
        StringBuilder result = new StringBuilder();
        result.append(canChooseItemToStart ? 1 : 0);
        result.append(canStartConcurrent ? 1 : 0);
        result.append(canReorder ? 1 : 0);
        result.append(canViewTeamItems ? 1 : 0);
        result.append(canViewOrgGroupItems ? 1 : 0);
        result.append(canChainExecution ? 1 : 0);
        result.append(canManageCases ? 1 : 0);
        result.append(carteblanche ? 1 : 0);
        return result.toString();
    }


    public void setPrivilegesFromBits(String bits) {
        if (bits.length() >= PRIV_COUNT) {
            char[] bitArray = bits.toCharArray();
            canChooseItemToStart = (bitArray[0] == '1');
            canStartConcurrent = (bitArray[1] == '1');
            canReorder = (bitArray[2] == '1');
            canViewTeamItems = (bitArray[3] == '1');
            canViewOrgGroupItems = (bitArray[4] == '1');
            canChainExecution = (bitArray[5] == '1');
            canManageCases = (bitArray[6] == '1');
            carteblanche = (bitArray[7] == '1');
        }
    }
}

