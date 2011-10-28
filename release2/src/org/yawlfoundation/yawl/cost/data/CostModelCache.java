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

package org.yawlfoundation.yawl.cost.data;

import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 20/10/11
 */
public class CostModelCache {

    private Set<CostModel> models;
    private YSpecificationID specID;

    private CostModelCache() {
        models = new HashSet<CostModel>();
    }
    
    public CostModelCache(YSpecificationID specID) {
        this();
        this.specID = specID; 
    }

    public YSpecificationID getSpecID() {
        return specID;
    }

    public void setSpecID(YSpecificationID specID) {
        this.specID = specID;
    }


    public Set<CostModel> getModels() {
        return models;
    }

    public void setModels(Set<CostModel> models) {
        this.models = models;
    }

    public boolean add(CostModel model) {
        return (model != null) && models.add(model);
    }


    public boolean remove(CostModel model) {
        return models.remove(model);
    }
    
    
    public CostModel getModel(String modelID) {
        for (CostModel model : models) {
            if (model.getId().equals(modelID)) return model;
        }
        return null;
    }
    
    
    public Set<CostDriver> getTaskInvocationDrivers() {
        Set<CostDriver> drivers = new HashSet<CostDriver>();
        for (CostModel model : models) {
            for (CostDriver driver : model.getDrivers()) {
                if (driver.getUnitCost().getUnit().equals("invocation")) {
                    drivers.add(driver);
                }
            }
        }
        return drivers;
    }

}
