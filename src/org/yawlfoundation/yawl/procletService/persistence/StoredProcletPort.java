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

import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;

/**
 * @author Michael Adams
 * @date 2/02/12
 */
public class StoredProcletPort {

    private long pkey;
    private String classID;
    private String blockID;
    private String portID;
    private String direction;
    private String cardinality;
    private String multiplicity;

    public StoredProcletPort() { }

    public StoredProcletPort(String classID, String blockID, String portID,
                             String direction, String cardinality, String multiplicity) {
        this.classID = classID;
        this.blockID = blockID;
        this.portID = portID;
        this.direction = direction;
        this.cardinality = cardinality;
        this.multiplicity = multiplicity;
    }

    public ProcletPort newProcletPort() {
        return new ProcletPort(portID, ProcletPort.getDirectionFromString(direction),
                ProcletPort.getSignatureFromString(cardinality),
                ProcletPort.getSignatureFromString(multiplicity));
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
    }

    public String getPortID() {
        return portID;
    }

    public void setPortID(String portID) {
        this.portID = portID;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }

    public long getPkey() {
        return pkey;
    }

    public void setPkey(long pkey) {
        this.pkey = pkey;
    }
}
