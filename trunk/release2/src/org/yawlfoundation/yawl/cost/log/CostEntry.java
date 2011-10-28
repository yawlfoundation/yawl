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

package org.yawlfoundation.yawl.cost.log;

import org.yawlfoundation.yawl.cost.data.CostDriver;
import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * @author Michael Adams
 * @date 25/10/11
 */
public class CostEntry {

    long timestamp;
    YSpecificationID specID;
    String workItemID;
    String driverID;
    String measure;
    String currency;
    double amount;

    public CostEntry() { }

    public CostEntry(YSpecificationID specID, String workItemID, String driverID,
                     String measure, String currency, double amount) {
        this.specID = specID;
        this.workItemID = workItemID;
        this.driverID = driverID;
        this.measure = measure;
        this.currency = currency;
        this.amount = amount;
    }
    
    public CostEntry(YSpecificationID specID, String workItemID, CostDriver driver) {
        this(specID, workItemID, driver.getID(),
                driver.getUnitCost().getUnit(),
                driver.getUnitCost().getCostValue().getCurrency(),
                driver.getUnitCost().getCostValue().getAmount());
    }
    

    public YSpecificationID getSpecID() { return specID; }

    public void setSpecID(YSpecificationID id) { specID = id; }


    public String getWorkItemID() { return workItemID; }

    public void setWorkItemID(String id) { workItemID = id; }


    public String getDriverID() { return driverID; }

    public void setDriverID(String id) { driverID = id; }


    public String getMeasure() { return measure; }

    public void setMeasure(String m) { measure = m; }


    public String getCurrency() { return currency; }

    public void setCurrency(String curr) { currency = curr; }


    public double getAmount() { return amount; }

    public void setAmount(double amt) { amount = amt; }

}
