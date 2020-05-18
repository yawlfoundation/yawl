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

package org.yawlfoundation.yawl.demoService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 6/11/19
 */
public class DemoService extends InterfaceBWebsideController {

    private String _handle = null;
    private int _count = 0;
    private boolean started = false;
    private long startTime;

    private static final Logger _log = LogManager.getLogger(DemoService.class);

    private static DateFormat _DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    
    @Override
    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        if (!started) {
            startTime = System.currentTimeMillis();
            started = true;
        }
        try {

             // connect only if not already connected
             if (! connected()) _handle = connect(engineLogonName, engineLogonPassword);

             // checkout ... process ... checkin
             wir = checkOut(wir.getID(), _handle);
             checkInWorkItem(wir.getID(), wir.getDataList(),
                             getOutputData(wir.getTaskID(), "0"), null,  _handle);
             _count++;
             if (_count % 1000 == 0) System.out.println(_count + " items in " +
                     (System.currentTimeMillis() - startTime) + " msecs.");
         }
         catch (Exception ioe) {
             ioe.printStackTrace();
         }

    }

    @Override
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {

    }


    @Override
    public void handleCompleteCaseEvent(String caseID, String casedata) {
        String timeStamp = _DF.format(new Date());
        _log.warn("Announcing case '{}' complete received at {}", caseID, timeStamp);
    }

    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[2];
        params[0] = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        params[0].setDataTypeAndName("int", "maxWaitSeconds", XSD_NAMESPACE);
        params[0].setDocumentation("\"The maximum number of seconds to hold the work item for");
        params[1] = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        params[1].setDataTypeAndName("int", "period", XSD_NAMESPACE);
        params[1].setDocumentation("\"The actual number of seconds the work item was held for");
        return params;
    }


    private boolean connected() throws IOException {
        return _handle != null && checkConnection(_handle);
    }

    private Element getOutputData(String taskName, String data) {
        Element output = new Element(taskName);
        Element result = new Element("period");
        result.setText(data);
        output.addContent(result);
        return output;
    }

}
