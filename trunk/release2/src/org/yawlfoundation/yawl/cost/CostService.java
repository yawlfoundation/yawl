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

package org.yawlfoundation.yawl.cost;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.cost.data.CostModel;
import org.yawlfoundation.yawl.cost.data.CostModelCache;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_Service;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_ServiceSideClient;
import org.yawlfoundation.yawl.unmarshal.XMLValidator;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostService implements InterfaceX_Service {

    private Map<YSpecificationID, CostModelCache> models;
    private InterfaceX_ServiceSideClient _ixClient ;    // interface client to engine
    
    private Logger _log = Logger.getLogger(this.getClass());

    public CostService() {
        models = new ConcurrentHashMap<YSpecificationID, CostModelCache>();
    }


    public void importModel(String costModel) {
        importModel(new XNodeParser(true).parse(costModel));
    }


    public void importModel(XNode costModel) {
        if (isValidModel(costModel)) {
            addToCache(new CostModel(costModel));
        }
    }
    
    public String exportModel(CostModel model) {
        return null;
    }
    
    public boolean removeModel(CostModel model) {
        CostModelCache cache = models.get(model.getSpecID());
        return (cache != null) && cache.remove(model);
    }
    

    private boolean isValidModel(XNode costModel) {
        if (costModel == null) return false;
        URL xsdFile = getClass().getResource("/org/yawlfoundation/yawl/cost/xsd/costmodel.xsd");
        String errors = new XMLValidator().checkSchema(xsdFile, costModel.toString());
        if (errors.length() > 0) {
            _log.error(errors);
        }
        return errors.length() == 0;
    }
    
    
    private CostModelCache addToCache(CostModel model) {
        if (model == null) return null;
        YSpecificationID specID = model.getSpecID();
        CostModelCache cache = models.get(specID);
        if (cache == null) {
            cache = new CostModelCache(specID);
            models.put(specID, cache);
        }
        cache.add(model);
        return cache;
    }


    /********************************************************************************/
    public void handleCheckCaseConstraintEvent(YSpecificationID specID, String caseID,
                                               String data, boolean precheck) {

    }

    public void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                   boolean precheck) {

    }

    public void handleWorkItemAbortException(WorkItemRecord wir) { }

    public void handleTimeoutEvent(WorkItemRecord wir, String taskList) { }

    public void handleResourceUnavailableException(String resourceID, WorkItemRecord wir,
                                                   boolean primary) {
    }

    public void handleConstraintViolationException(WorkItemRecord wir) {

    }

    public void handleCaseCancellationEvent(String caseID) {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

    }
}
