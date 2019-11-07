package org.yawlfoundation.yawl.demoService;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 6/11/19
 */
public class DemoService extends InterfaceBWebsideController {

    private String _handle = null;
    private int _count = 0;
    private boolean started = false;
    private long startTime;

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
