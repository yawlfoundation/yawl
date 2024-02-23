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

package org.yawlfoundation.yawl.procletService.persistence;

/**
 * @author Michael Adams
 * @date 2/02/12
 */
public class StoredInteractionArc {

    private long pkey;
    private String emid;
    private String tailClassID;
    private String tailProcletID;
    private String tailBlockID;
    private String headClassID;
    private String headProcletID;
    private String headBlockID;
    private String esid;
    private String arcState;

    public StoredInteractionArc() { }

    public StoredInteractionArc(String emid, String tailClassID, String tailProcletID,
                                String tailBlockID, String headClassID, String headProcletID,
                                String headBlockID, String esid, String arcState) {
        this.emid = emid;
        this.tailClassID = tailClassID;
        this.tailProcletID = tailProcletID;
        this.tailBlockID = tailBlockID;
        this.headClassID = headClassID;
        this.headProcletID = headProcletID;
        this.headBlockID = headBlockID;
        this.esid = esid;
        this.arcState = arcState;
    }


    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }

    public String getEmid() {
        return emid;
    }

    public void setEmid(String emid) {
        this.emid = emid;
    }

    public String getTailClassID() {
        return tailClassID;
    }

    public void setTailClassID(String tailClassID) {
        this.tailClassID = tailClassID;
    }

    public String getTailProcletID() {
        return tailProcletID;
    }

    public void setTailProcletID(String tailProcletID) {
        this.tailProcletID = tailProcletID;
    }

    public String getTailBlockID() {
        return tailBlockID;
    }

    public void setTailBlockID(String tailBlockID) {
        this.tailBlockID = tailBlockID;
    }

    public String getHeadClassID() {
        return headClassID;
    }

    public void setHeadClassID(String headClassID) {
        this.headClassID = headClassID;
    }

    public String getHeadProcletID() {
        return headProcletID;
    }

    public void setHeadProcletID(String headProcletID) {
        this.headProcletID = headProcletID;
    }

    public String getHeadBlockID() {
        return headBlockID;
    }

    public void setHeadBlockID(String headBlockID) {
        this.headBlockID = headBlockID;
    }

    public String getEsid() {
        return esid;
    }

    public void setEsid(String esid) {
        this.esid = esid;
    }

    public String getArcState() {
        return arcState;
    }

    public void setArcState(String arcState) {
        this.arcState = arcState;
    }
}
