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

package org.yawlfoundation.yawl.elements.data.external;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 13/08/2009
 */
public class SimpleExternalDBGatewayImpl extends AbstractExternalDBGateway {

    public SimpleExternalDBGatewayImpl() {}

    private boolean configured = false;


    private boolean configure() {
        if (! configured) {
            configureSession("org.hibernate.dialect.PostgreSQLDialect",
                            "org.postgresql.Driver", "jdbc:postgresql:testDB",
                            "postgres", "yawl", null);
            configured = true;
        }
        return configured;
    }


    public String getDescription() {
        return "A simple example of an external database gateway implementation";
    }


    public Element populateTaskParameter(YTask task, YParameter param, Element caseData) {
        Element result = new Element(param.getName());
//        if (configure()) {
//            List resultSet = _dbEngine.execQuery("SELECT * FROM privilege");
//            for (Object row : resultSet) {
//                System.out.println(row) ;
//            }
            result.setText("the data");
 //       }
        result.setAttribute("trusty", "true");
        result.setAttribute("other", "smith");
        return result;
    }


    public void updateFromTaskCompletion(String paramName, Element outputData, Element caseData) {}

    
    public Element populateCaseData(YSpecificationID specID, String caseID,
                                    List<YParameter> inputParams,
                                    List<YVariable> localVars, Element caseDataTemplate) {
        return null; 
    }

    public void updateFromCaseData(YSpecificationID specID, String caseID,
                                   List<YParameter> outputParams,
                                   Element updatingData) {
        System.out.println(caseID);        
    }


}
