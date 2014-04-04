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

package org.yawlfoundation.yawl.procletService.persistence;

import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 3/02/12
 */
public class StoredOptions {
    
    private long pkey;
    private String destClassID;
    private String destProcletID;
    private String destBlockID;
    private String sign;
    private String classID;
    private String procletID;
    private String blockID;
    private boolean cr;

    public StoredOptions() {  }

    public StoredOptions(String destClassID, String destBlockID, String sign,
                         String classID, String procletID, String blockID) {
        this.destClassID = destClassID;
        this.destBlockID = destBlockID;
        this.sign = sign;
        this.classID = classID;
        this.procletID = procletID;
        this.blockID = blockID;
        this.cr = true;
    }

    public StoredOptions(String destClassID, String destProcletID, String destBlockID,
                         String sign, String classID, String procletID, String blockID) {
        this.destClassID = destClassID;
        this.destProcletID = destProcletID;
        this.destBlockID = destBlockID;
        this.sign = sign;
        this.classID = classID;
        this.procletID = procletID;
        this.blockID = blockID;
        this.cr = false;
    }
    
    
    public List getInternList() {
        List option = new ArrayList();
   		option.add(destClassID);
        option.add(procletID);
   		option.add(destBlockID);
        option.add(ProcletPort.Signature.valueOf(sign));
        option.add(new InteractionNode(classID, procletID, blockID));
        return option;
    }
    
    
    public List getCrList() {
        List option = new ArrayList();
  		option.add(destClassID);
  		option.add(destBlockID);
  		option.add(ProcletPort.Signature.valueOf(sign));
        option.add(new InteractionNode(classID, procletID, blockID));
        return option;
    }
    
    
    public List getFragmentList() {
        List option = new ArrayList();
   		option.add(destClassID);
   		option.add(destProcletID);
   		option.add(destBlockID);
        option.add(ProcletPort.Signature.valueOf(sign));
        option.add(new InteractionNode(classID, procletID, blockID));
        return option;
    }

    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }

    public String getDestClassID() {
        return destClassID;
    }

    public void setDestClassID(String destClassID) {
        this.destClassID = destClassID;
    }

    public String getDestProcletID() {
        return destProcletID;
    }

    public void setDestProcletID(String destProcletID) {
        this.destProcletID = destProcletID;
    }

    public String getDestBlockID() {
        return destBlockID;
    }

    public void setDestBlockID(String destBlockID) {
        this.destBlockID = destBlockID;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getProcletID() {
        return procletID;
    }

    public void setProcletID(String procletID) {
        this.procletID = procletID;
    }

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
    }

    public boolean isCr() {
        return cr;
    }

    public void setCr(boolean cr) {
        this.cr = cr;
    }
}
